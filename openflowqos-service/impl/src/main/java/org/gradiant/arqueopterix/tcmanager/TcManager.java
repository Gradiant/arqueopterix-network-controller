/*
 * Copyright © 2018 Gradiant and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.gradiant.arqueopterix.tcmanager;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.WebResource;

public class TcManager {
	private String managerApiAddr;
	private ClientConfig config;
	private Client client;
	private TcPolicyList policyList;
	
	public TcManager(String managerApiAddr) {
		this.managerApiAddr = managerApiAddr;
		this.config = new DefaultClientConfig();
		this.client = Client.create(config);
		this.policyList = new TcPolicyList();
	}
	
	public void setDefaultRate(String iface, int rate) {	
		WebResource webResource = client.resource(UriBuilder.fromUri(this.managerApiAddr+"/api/interfaces/"+iface+"/default_rate").build());
		
		ClientResponse response = webResource.accept("application/json")
				.type("application/json")
				.put(ClientResponse.class, String.format("\"%dMbit\"", rate));
	}
	
	public void setPolicy(String iface, Ipv4Prefix srcIp, Ipv4Prefix dstIp, int rate) {
		String srcIpStr = srcIp.getValue().substring(0, srcIp.getValue().length()-3);
		String dstIpStr = dstIp.getValue().substring(0, dstIp.getValue().length()-3);
		
		TcPolicy inList = this.policyList.findByAddr(srcIpStr, dstIpStr);
		if (inList != null) {
			deletePolicy(iface, inList.getPolicyId());
		}
		
		addPolicy(iface, srcIpStr, dstIpStr, rate);

	}
	
	private void addPolicy(String iface, String srcIp, String dstIp, int rate) {
		TcPolicy policy = new TcPolicy();
		policy.setMatch(srcIp, dstIp);
		policy.setAction(rate);
		
		WebResource webResource = client.resource(UriBuilder.fromUri(this.managerApiAddr+"/api/interfaces/"+iface+"/policies").build());
		
		ClientResponse response = webResource.accept("application/json")
				.type("application/json")
				.post(ClientResponse.class, policy.toString());
		
		String responseString = response.getEntity(String.class);
		
		Gson gson = new GsonBuilder()
				.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
				.create();
		
		TcPolicy responsePolicy = gson.fromJson(responseString, TcPolicy.class);
		
		this.policyList.addPolicy(responsePolicy);
	}
	
	public void deletePolicy(String iface, String id) {
		WebResource webResource = client.resource(UriBuilder.fromUri(this.managerApiAddr+"/api/interfaces/"+iface+"/policies/"+id).build());
		
		ClientResponse response = webResource.accept("application/json").type("application/json").delete(ClientResponse.class);
		
		this.policyList.removePolicy(id);
	}
}
