/*
 * Copyright © 2018 Gradiant and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.gradiant.arqueopterix.northbound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gradiant.arqueopterix.northbound.Policy;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.org.gradiant.arqueopterix.openflowqos.rev180103.OpenFlowQosService;
import org.opendaylight.yang.gen.v1.urn.org.gradiant.arqueopterix.openflowqos.rev180103.SetQosLevelInputBuilder;

import org.osgi.framework.FrameworkUtil;

/**
 * PolicyResource implements the REST API itself. It includes a list of Policy
 * objects that can be managed through CRUD requests.
 * 
 * @author gradiant
 */
@Path("/policies")
public class PolicyResource {
	private static final Logger LOG = LoggerFactory.getLogger(PolicyResource.class);
	/* Policy list */
	private ArrayList<Policy> policyStore;
	private BundleContext context;
	/* Reference of OpenFlowQosService to make MD-SAL RPC calls */
	private OpenFlowQosService service;

	public PolicyResource() {
		policyStore = new ArrayList<>();

		/*
		 * Bundle context is used to obtain a reference to the OpenFlowQosService so
		 * that it can reconfigure the network according to the stored policies
		 */
		context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

		ServiceReference<BindingAwareBroker> brokerRef = context.getServiceReference(BindingAwareBroker.class);
		BindingAwareBroker broker = context.getService(brokerRef);

		broker.registerConsumer(new BindingAwareConsumer() {

			@Override
			public void onSessionInitialized(ConsumerContext session) {
				service = session.getRpcService(OpenFlowQosService.class);
			}
		});
	}

	/**
	 * POST policy request handler
	 * 
	 * @param policy received policy object
	 * @return response status
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON, "text/json" })
	@Produces({ MediaType.APPLICATION_JSON, "text/json" })
	public Response add(Policy policy) {
		LOG.info("POST policy");

		// Validate Policy object correctness
		List<String> errorMessages = policy.validate();
		if (!errorMessages.isEmpty()) {
			LOG.error(String.format("Could not validate provided Policy: %s", errorMessages.toString()));
			return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN)
					.entity("Validation exception. Malformed Policy object.").build();
		}

		if (policyExists(policy)) {
			return Response.status(Status.CONFLICT).type(MediaType.TEXT_PLAIN)
					.entity("There is already a Policy that matches the provided parameters.").build();
		}
		
		configureNetwork(policy);

		// TODO: Convert id to UUID.randomUUID();
		// Add policy to the list
		policy.setId(policyStore.size() + 1);
		this.policyStore.add(policy);
		return Response.ok(policy, MediaType.APPLICATION_JSON_TYPE).build();
	}

	/**
	 * PUT policy request handler
	 * 
	 * @param policy received policy object
	 * @return response status
	 */
	@PUT
	@Consumes({ MediaType.APPLICATION_JSON, "text/json" })
	@Produces({ MediaType.APPLICATION_JSON, "text/json" })
	public Response update(Policy policy) {
		LOG.info("UPDATE policy");

		// Validate Policy object correctness
		List<String> errorMessages = policy.validate();
		if (!errorMessages.isEmpty()) {
			LOG.error(String.format("Could not validate provided Policy: %s", errorMessages.toString()));
			return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN)
					.entity("Validation exception. Malformed Policy object.").build();
		}

		// Get policy with specified ID
		Policy storedPolicy = findId(policy.getId());

		// Verify that it matches the provided policy object
		if (!storedPolicy.isEqual(policy)) {
			LOG.error("Provided Policy does not match previously stored one.");

			return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN)
					.entity("Validation exception. Malformed Policy object.").build();
		}

		configureNetwork(policy);
		
		// Update policy in the list
		this.policyStore.remove(storedPolicy);
		this.policyStore.add(policy);
		return Response.ok(policy, MediaType.APPLICATION_JSON_TYPE).build();
	}

	/**
	 * DELETE policy request handler
	 * 
	 * @param id provided policy ID 
	 * @return response status
	 */
	@DELETE
	@Path("/{id}")
	@Consumes({ MediaType.APPLICATION_JSON, "text/json" })
	@Produces({ MediaType.APPLICATION_JSON, "text/json" })
	public Response deleteById(@PathParam("id") int id){
		LOG.info("REQUEST DELETE policy with ID: " + id);
		Policy policy = findId(id);
		if (policy != null) {
			policyStore.remove(policy);
			return Response.ok().type(MediaType.TEXT_PLAIN).entity("Policy successfully deleted.").build();
		}

		return Response.status(Status.NOT_FOUND).type(MediaType.TEXT_PLAIN)
				.entity(String.format("No Policy found with id %d", id)).build();
	}

	/**
	 * DELETE policy request handler
	 * 
	 * @param id provided policy ID
	 * @return response status
	 */
	@GET
	@Path("/{id}")
	@Consumes({ MediaType.APPLICATION_JSON, "text/json" })
	@Produces({ MediaType.APPLICATION_JSON, "text/json" })
	public Response findById(@PathParam("id") int id) {
		LOG.info("REQUEST GET policy with ID: " + id);
		Policy policy = findId(id);
		if (policy != null) {
			return Response.ok(policy, MediaType.APPLICATION_JSON_TYPE).build();
		}

		return Response.status(Status.NOT_FOUND).type(MediaType.TEXT_PLAIN)
				.entity(String.format("No Policy found with id %d", id)).build();
	}

	/**
	 * GET policies request handler
	 * 
	 * @return response status
	 */
	@GET
	@Consumes({ MediaType.APPLICATION_JSON, "text/json" })
	@Produces({ MediaType.APPLICATION_JSON, "text/json" })
	public Response findAll(){
		Map<String, ArrayList<Policy>> res = new HashMap<>();
		res.put("policies", policyStore);
		return Response.ok(res, MediaType.APPLICATION_JSON_TYPE).build();
	}

	/**
	 * Verify policy existence
	 * 
	 * @param policy Policy object that will be compared to the list
	 * @return true if policy exists, false otherwise.
	 */
	private boolean policyExists(Policy policy) {
		Iterator<Policy> itr = policyStore.iterator();
		while (itr.hasNext()) {
			Policy storedPolicy = itr.next();

			if (storedPolicy.isEqual(policy)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Find policy by ID
	 * 
	 * @param id ID of the policy
	 * @return Policy object matching the provided ID
	 */
	private Policy findId(int id) {
		Iterator<Policy> itr = policyStore.iterator();
		while (itr.hasNext()) {
			Policy policy = itr.next();
			if (policy.getId() == id) {
				return policy;
			}
		}

		return null;
	}
	
	/**
	 * Configure flows on the network through OpenFlowQosService
	 * 
	 * @param policy Policy object describing the flows.
	 */
	private void configureNetwork(Policy policy) {
		SetQosLevelInputBuilder inputBuilder = new SetQosLevelInputBuilder();

		Ipv4Prefix srcAddr = new Ipv4Prefix(policy.getSrcAddr());
		Ipv4Prefix dstAddr = new Ipv4Prefix(policy.getDstAddr());

		inputBuilder.setSrcAddr(srcAddr);
		inputBuilder.setDstAddr(dstAddr);
		inputBuilder.setSrcPort(policy.getSrcPort());
		inputBuilder.setDstPort(policy.getDstPort());
		inputBuilder.setAdditionalInfo("");
		inputBuilder.setServiceLevel(policy.getServiceLevel());
		inputBuilder.setProtocol(policy.getProtocol());

		service.setQosLevel(inputBuilder.build());
	}
}
