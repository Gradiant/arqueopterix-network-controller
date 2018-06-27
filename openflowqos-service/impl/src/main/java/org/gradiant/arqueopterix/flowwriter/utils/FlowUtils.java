/*
 * Copyright © 2018 Gradiant and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.gradiant.arqueopterix.flowwriter.utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.gradiant.arqueopterix.flowwriter.FlowWriter;
import org.gradiant.arqueopterix.flowwriter.utils.IidUtils;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Dscp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopMplsActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushMplsActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetQueueActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.mpls.action._case.PopMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.vlan.action._case.PopVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.mpls.action._case.PushMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.vlan.action._case.PushVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.queue.action._case.SetQueueActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowTableRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.MeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.go.to.table._case.GoToTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.go.to.table._case.GoToTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.GoToTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.meter._case.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFieldsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanIdBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;

public class FlowUtils {
	private static final Logger LOG = LoggerFactory.getLogger(FlowUtils.class);
	
	private static final int FLOW_IDLE_TIMEOUT = 0;
	private static final int FLOW_HARD_TIMEOUT = 0;
	
	private static AtomicLong flowCookieInc = new AtomicLong(0x2a00000000000000L);
	
	/**
	 * Match by IP source and destination
	 * 
	 * @param srcAddr Flow source IP address.
	 * @param dstAddr Flow destination IP address.
	 * @return Match object
	 */
	public static Match getFlowMatch(Ipv4Prefix srcAddr, Ipv4Prefix dstAddr) {
		// Create an Ethernet match specifying IP type payloads
		EthernetType ethernetType = new EthernetTypeBuilder().setType(new EtherType((long) 2048)).build();
		EthernetMatchBuilder ethernetMatchBuilder = new EthernetMatchBuilder().setEthernetType(ethernetType);
		EthernetMatch ethernetMatch = ethernetMatchBuilder.build();
		
		// Create matchbuilder
		MatchBuilder matchBuilder = new MatchBuilder();

		// Add Ethernet match to builder
		matchBuilder.setEthernetMatch(ethernetMatch);

		// Source and destination IP addresses can be null to indicate a wildcard address
		if (srcAddr != null || dstAddr != null) {
			Ipv4MatchBuilder ipv4MatchBuilder = new Ipv4MatchBuilder();
			if (srcAddr != null) {
				// Add source IP if not null
				ipv4MatchBuilder.setIpv4Source(srcAddr);
			}
			
			if (dstAddr != null) {
				// Add destination IP if not null
				ipv4MatchBuilder.setIpv4Destination(dstAddr);
			}

			// Create layer 3 (IP) match
			matchBuilder.setLayer3Match(ipv4MatchBuilder.build());			
		}

		return matchBuilder.build();
	}
	
	
	/**
	 * Match by IP source, destination and transport protocol
	 * 
	 * @param srcAddr Flow source IP address.
	 * @param dstAddr Flow destination IP address.
	 * @param protocol Flow transport level protocol ID.
	 * @return Match object.
	 */
	public static Match getFlowMatch(Ipv4Prefix srcAddr, Ipv4Prefix dstAddr, long protocol) {
		// Create an Ethernet match specifying IP type payloads
		EthernetType ethernetType = new EthernetTypeBuilder()
				.setType(new EtherType((long) 2048))
				.build();
		EthernetMatchBuilder ethernetMatchBuilder = new EthernetMatchBuilder()
				.setEthernetType(ethernetType);
		EthernetMatch ethernetMatch = ethernetMatchBuilder.build();
		
		// Create matchbuilder
		MatchBuilder matchBuilder = new MatchBuilder();

		// Add Ethernet match to builder
		matchBuilder.setEthernetMatch(ethernetMatch);

		// Source and destination IP addresses can be null to indicate a wildcard address
		if (srcAddr != null || dstAddr != null) {
			Ipv4MatchBuilder ipv4MatchBuilder = new Ipv4MatchBuilder();
			if (srcAddr != null) {
				// Add source IP if not null
				ipv4MatchBuilder.setIpv4Source(srcAddr);
			}
			
			if (dstAddr != null) {
				// Add destination IP if not null
				ipv4MatchBuilder.setIpv4Destination(dstAddr);
			}

			// Create layer 3 (IP) match
			matchBuilder.setLayer3Match(ipv4MatchBuilder.build());			
		}

		return matchBuilder.build();
	}
	
	/**
	 * Match by destination MAC address
	 * 
	 * @param dstMac Destination MAC address
	 * @return Match object
	 */
	public static Match getFlowMatch(MacAddress dstMac) {
        // Create a match specifying destination MAC address
        EthernetMatchBuilder ethernetMatchBuilder = new EthernetMatchBuilder() //
                .setEthernetDestination(new EthernetDestinationBuilder() //
                        .setAddress(dstMac) //
                        .build());
        
		// Create matchbuilder
		MatchBuilder matchBuilder = new MatchBuilder();        

		// Add Ethernet match to builder
        matchBuilder.setEthernetMatch(ethernetMatchBuilder.build());
		
		return matchBuilder.build();
	}
	
	/**
	 * Match by VLAN ID
	 * 
	 * @param vlanId VLAN identifier
	 * @return Match object
	 */
	public static Match getFlowMatch(int vlanId) {
		VlanPcp pcp = new VlanPcp((short)3);
		VlanId id = new VlanId(vlanId);
		
        // Create a match specifying destination MAC address
		VlanMatchBuilder vlanIdMatchBuilder = new VlanMatchBuilder()
				.setVlanId(new VlanIdBuilder()
						.setVlanIdPresent(true)
						.setVlanId(id)
						.build())
				.setVlanPcp(pcp);
        
		// Create matchbuilder
		MatchBuilder matchBuilder = new MatchBuilder();        

		// Add ethernet match to builder
        matchBuilder.setVlanMatch(vlanIdMatchBuilder.build());
		
		return matchBuilder.build();		
	}
	
	/**
	 * Match by MPLS label
	 * 
	 * @param mplsLabel MPLS label
	 * @return Match object
	 */
	public static Match getFlowMatch(long mplsLabel) {
		// Create a MPLS traffic match
		EthernetType ethernetType = new EthernetTypeBuilder()
				.setType(new EtherType((long)0x8847)).build();
		EthernetMatchBuilder ethernetMatchBuilder = new EthernetMatchBuilder()
				.setEthernetType(ethernetType);
		
		// Create an MPLS label match
		ProtocolMatchFieldsBuilder mplsMatchBuilder = new ProtocolMatchFieldsBuilder()
				.setMplsLabel(mplsLabel);
		
		// Create matchbuilder
		MatchBuilder matchBuilder = new MatchBuilder();        

		// Add MPLS traffic match to builder
		matchBuilder.setEthernetMatch(ethernetMatchBuilder.build());
		// Add MPLS label match to builder
        matchBuilder.setProtocolMatchFields(mplsMatchBuilder.build());
		
		return matchBuilder.build();
	}
	
	/**
	 * Match MPLS tagged traffic. It matches any MPLS label.
	 * 
	 * @return Match object
	 */
	public static Match getMplsMatch() {
		// Create a MPLS traffic match
		EthernetType ethernetType = new EthernetTypeBuilder()
				.setType(new EtherType((long)0x8847)).build();
		EthernetMatchBuilder ethernetMatchBuilder = new EthernetMatchBuilder()
				.setEthernetType(ethernetType);

		
		// Create matchbuilder
		MatchBuilder matchBuilder = new MatchBuilder();        

		// Add MPLS traffic match to builder
		matchBuilder.setEthernetMatch(ethernetMatchBuilder.build());
		
		return matchBuilder.build();		
	}
	
	/**
	 * Match by input port. Port identifier is built as nodeID:inport
	 * 
	 * @param nodeId Node identifier
	 * @param inPort Port identifier
	 * @return Match object
	 */
	public static Match getFlowMatch(String nodeId, long inPort) {	
		// Create matchbuilder
		MatchBuilder matchBuilder = new MatchBuilder();
		
		String portId = String.format("%s:%d", nodeId, inPort);
		
		// Create in port match and add it to builder
		matchBuilder.setInPort(new NodeConnectorId(portId));

		return matchBuilder.build();
	}
	
//	/**
//	 * Returns an Instructions instance to assign a specific flow to
//	 * the corresponding output queue and output port.
//	 * 
//	 * @param queueId Output queue ID number.
//	 * @param outputPort Output port ID number.
//	 * @param nodeId Node ID String.
//	 * @return
//	 */
//	public static ArrayList<Instruction> getQueueInstructions(long queueId, long outputPort, String nodeId) {
//		Action setQueueAction = new ActionBuilder()
//				.setOrder(0)
//				.setAction(new SetQueueActionCaseBuilder()
//						.setSetQueueAction(new SetQueueActionBuilder()
//								.setQueueId(queueId)
//								.build())
//						.build())
//				.build();
//
//		InstanceIdentifier<NodeConnector> nodeConnectorInstanceId = IidUtils.getNodeConnectorIid(nodeId,(short)outputPort);
//		NodeConnectorRef nodeConnectorRef = new NodeConnectorRef(nodeConnectorInstanceId);
//		Uri outputPortUri = nodeConnectorRef.getValue().firstKeyOf(NodeConnector.class, NodeConnectorKey.class).getId();
//
//		Action outputToControllerAction = new ActionBuilder()
//				.setOrder(1).setAction(new OutputActionCaseBuilder()
//						.setOutputAction(new OutputActionBuilder()
//								.setMaxLength(0xffff)
//								.setOutputNodeConnector(outputPortUri)
//								.build())
//						.build())
//				.build();
//		
//        Dscp dscp = new Dscp((short)5);
//        
//        SetFieldBuilder setFieldBuilder = new SetFieldBuilder()
//                .setIpMatch(new IpMatchBuilder().setIpDscp(dscp).build());
//		
//		Action setField = new ActionBuilder()
//				.setOrder(2).setAction(new SetFieldCaseBuilder()
//						.setSetField(setFieldBuilder.build())
//						.build())
//				.build();
//
//		ArrayList<Action> actionsList = new ArrayList<Action>();
//		//actionsList.add(outputToControllerAction);
//		actionsList.add(setQueueAction);
//		actionsList.add(setField);
//
//		// Create an Apply Action
//		ApplyActions applyActions = new ApplyActionsBuilder().setAction(actionsList).build();
//
//		// Wrap our Apply Action in an Instruction
//		Instruction applyActionsInstruction = new InstructionBuilder() //
//				.setOrder(0).setInstruction(new ApplyActionsCaseBuilder()//
//						.setApplyActions(applyActions) //
//						.build()) //
//				.build();
//		
//		GoToTable gotoTable = new GoToTableBuilder().setTableId((short)1).build();
//		
//		Instruction goToTableInstruction = new InstructionBuilder()
//				.setOrder(2).setInstruction(new GoToTableCaseBuilder()
//					.setGoToTable(gotoTable)
//					.build())
//				.build();
//				
//		ArrayList<Instruction> instructionsList = new ArrayList<Instruction>();
//		instructionsList.add(applyActionsInstruction);
//		instructionsList.add(goToTableInstruction);
//		
//		return instructionsList;
//	}
	

	/**
	 * Send packets to specific output port. Port ID is built as nodeId:outPort
	 * 
	 * @param order Action order in the list
	 * @param nodeId Node identifier
	 * @param outPort Output port number
	 * @return Action object
	 */
	public static Action getOutputAction(int order, String nodeId, long outPort) {
		InstanceIdentifier<NodeConnector> nodeConnectorInstanceId = IidUtils.getNodeConnectorIid(nodeId,(short)outPort);
		NodeConnectorRef nodeConnectorRef = new NodeConnectorRef(nodeConnectorInstanceId);
		Uri outPortUri = nodeConnectorRef.getValue().firstKeyOf(NodeConnector.class, NodeConnectorKey.class).getId();
		
		Action outputAction = new ActionBuilder() //
				.setOrder(order).setAction(new OutputActionCaseBuilder() //
						.setOutputAction(new OutputActionBuilder() //
								.setMaxLength(0xffff) //
								.setOutputNodeConnector(outPortUri) //
								.build()) //
						.build()) //
				.build();
		
		return outputAction;
	}
	
	/**
	 * Send packets to specific output queue
	 * 
	 * @param order Action order in the list
	 * @param queueId Queue identifier
	 * @return Action object
	 */
	public static Action getQueueAction(int order, long queueId) {
		Action setQueueAction = new ActionBuilder().setOrder(0).setAction(new SetQueueActionCaseBuilder()
				.setSetQueueAction(new SetQueueActionBuilder().setQueueId(queueId).build()).build()).build();
		
		return setQueueAction;
	}
	
	/**
	 * Overwrite DSCP code
	 * 
	 * @param order Action order in the list
	 * @param dscp DSCP code value
	 * @return Action object
	 */
	public static Action getRewriteDscpAction(int order, short dscp) {
		Dscp dscpObj = new Dscp(dscp);
		
		SetFieldBuilder setFieldBuilder = new SetFieldBuilder()
				.setIpMatch(new IpMatchBuilder().setIpDscp(dscpObj).build());
		
		Action setField = new ActionBuilder()
				.setOrder(order).setAction(new SetFieldCaseBuilder()
						.setSetField(setFieldBuilder.build())
						.build())
				.build();
		
		return setField;
	}
	
	/**
	 * Add VLAN header to packet
	 * 
	 * @param order Action order in the list
	 * @return Action object
	 */
	public static Action getPushVlanAction(int order) {
		Action pushVlanAction = new ActionBuilder().setOrder(order).setAction(new PushVlanActionCaseBuilder()
				.setPushVlanAction(new PushVlanActionBuilder().setEthernetType(0x8100).build()).build()).build();
		
		return pushVlanAction;
	}
	
	/**
	 * Set VLAN ID value
	 * 
	 * @param order Action order in the list
	 * @param vlanId VLAN identifier
	 * @return Action object
	 */
	public static Action getSetVlanIdAction(int order, int vlanId) {
		LOG.info("Vlan ID: %d", vlanId);
		SetFieldBuilder setFieldBuilder = new SetFieldBuilder()
				.setVlanMatch(new VlanMatchBuilder()
//						.setVlanId(new VlanIdBuilder()
//								.setVlanId(new VlanId(vlanId))
//								.build())
						.setVlanPcp(new VlanPcp((short)3))
						.build());
		
		Action setField = new ActionBuilder()
				.setOrder(order).setAction(new SetFieldCaseBuilder()
						.setSetField(setFieldBuilder.build())
						.build())
				.build();
		
		return setField;
	}
	
	/**
	 * Remove VLAN header from packet
	 * 
	 * @param order Action order in the list
	 * @return Action object
	 */
	public static Action getPopVlanAction(int order) {
		Action popVlanAction = new ActionBuilder()
				.setOrder(order)
				.setAction(new PopVlanActionCaseBuilder()
						.setPopVlanAction(new PopVlanActionBuilder()
								.build())
						.build())
				.build();
		
		return popVlanAction;
	}
	
	/**
	 * Add MPLS header to the packet
	 * 
	 * @param order Action order in the list
	 * @return Action object
	 */
	public static Action getPushMplsAction(int order) {
		Action pushMplsAction = new ActionBuilder()
				.setOrder(order)
				.setAction(new PushMplsActionCaseBuilder()
						.setPushMplsAction(new PushMplsActionBuilder()
								.setEthernetType(0x8847)
								.build())
						.build())
				.build();
		
		return pushMplsAction;
	}
	
	/**
	 * Set MPLS label value
	 * 
	 * @param order Action order in the list
	 * @param mplsLabel MPLS label value
	 * @return Action object
	 */
	public static Action getSetMplsLabelAction(int order, long mplsLabel) {
		LOG.info("MPLS label: %d", mplsLabel);
		SetFieldBuilder setFieldBuilder = new SetFieldBuilder()
				.setProtocolMatchFields(new ProtocolMatchFieldsBuilder()
						.setMplsLabel(mplsLabel)
						.build());
		
		Action setField = new ActionBuilder()
				.setOrder(order).setAction(new SetFieldCaseBuilder()
						.setSetField(setFieldBuilder.build())
						.build())
				.build();
		
		return setField;
	}
	
	/**
	 * Remove MPLS header from packet
	 * 
	 * @param order Action order in the list
	 * @return Action object
	 */
	public static Action getPopMplsAction(int order) {
		Action popMplsAction = new ActionBuilder()
				.setOrder(order)
				.setAction(new PopMplsActionCaseBuilder()
						.setPopMplsAction(new PopMplsActionBuilder()
								.setEthernetType(0x0800)
								.build())
						.build())
				.build();
		
		return popMplsAction;
	}
	
	/**
	 * Apply action list to the flow
	 * 
	 * @param order Instruction order in the list
	 * @param list List of actions to apply
	 * @return Instruction object
	 */
	public static Instruction getApplyActionsInstruction(int order, ArrayList<Action> list) {
		ApplyActions applyActions = new ApplyActionsBuilder().setAction(list).build();
		
		// Wrap our Apply Action in an Instruction
		Instruction applyActionsInstruction = new InstructionBuilder() //
				.setOrder(order).setInstruction(new ApplyActionsCaseBuilder()//
						.setApplyActions(applyActions)
						.build())
				.build();
		
		return applyActionsInstruction;
	}
	
	/**
	 * Jump to the specified OpenFlow table
	 * 
	 * @param order Instruction order in the list
	 * @param tableId Next table identifier
	 * @return Instruction object
	 */
	public static Instruction getGoToTableInstruction(int order, short tableId) {
		GoToTable gotoTable = new GoToTableBuilder().setTableId(tableId).build();
		
		Instruction goToTableInstruction = new InstructionBuilder()
				.setOrder(order).setInstruction(new GoToTableCaseBuilder()
					.setGoToTable(gotoTable)
					.build())
				.build();
		
		return goToTableInstruction;
	}
	
	/**
	 * Assign flow to specific meter
	 * 
	 * @param order Instruction order in the list
	 * @param meterId Meter identifier
	 * @return Instruction object
	 */
	public static Instruction getMeterInstruction(int order, long meterId) {
		Instruction meterInstruction = new InstructionBuilder().setOrder(order).setInstruction(
				new MeterCaseBuilder().setMeter(new MeterBuilder().setMeterId(new MeterId(meterId)).build()).build())
				.build();
		
		return meterInstruction;
	}
	
	/**
	 * Create a Flow instance with the specified information
	 * 
	 * @param tableId Table ID number where the Flow is stored.
	 * @param match Match instance that defines the corresponding flow.
	 * @param instructions Instructions instance that carries the actions
	 * to perform in case of a successful match.
	 * @param priority Flow priority value.
	 * @return Flow instance generated.
	 */
	public static Flow createFlow(short tableId, Match match, ArrayList<Instruction> instructions, int priority) {
		FlowBuilder flowBuilder = new FlowBuilder();
		
		InstructionsBuilder instructionsBuilder = new InstructionsBuilder();
		instructionsBuilder.setInstruction(instructions);
		
		flowBuilder.setTableId(tableId);
		flowBuilder.setFlowName("arqueopterix");
		flowBuilder.setId(new FlowId(Long.toString(flowBuilder.hashCode())));
		flowBuilder.setMatch(match);
		flowBuilder.setInstructions(instructionsBuilder.build());
		flowBuilder.setPriority(priority);
		flowBuilder.setBufferId(OFConstants.OFP_NO_BUFFER);
		flowBuilder.setHardTimeout(FLOW_HARD_TIMEOUT);
		flowBuilder.setIdleTimeout(FLOW_IDLE_TIMEOUT);
		flowBuilder.setCookie(new FlowCookie(BigInteger.valueOf(flowCookieInc.getAndIncrement())));
		flowBuilder.setFlags(new FlowModFlags(false, false, false, false, false));

		return flowBuilder.build();
	}
	
	/**
	 * Write Flow to OpenDaylight configuration database.
	 * 
	 * @param nodeId ID of the node where the flow will be written.
	 * @param tableId ID of the table where the flow will be written.
	 * @param flow Flow instance that defines the flow to write to the switch.
	 */
	public static void writeFlow(SalFlowService salFlowService, String nodeId, short tableId, Flow flow) {
		InstanceIdentifier<Node> nodeInstanceId = IidUtils.getNodeIid(nodeId);
		InstanceIdentifier<Table> tableInstanceId = IidUtils.getTableIid(nodeId, tableId);
		InstanceIdentifier<Flow> flowInstanceId = IidUtils.getFlowIid(nodeId, tableId, "arqueopterix-");
		
		final AddFlowInputBuilder addFlowBuilder = new AddFlowInputBuilder(flow);
		addFlowBuilder.setNode(new NodeRef(nodeInstanceId));
		addFlowBuilder.setFlowRef(new FlowRef(flowInstanceId));
		addFlowBuilder.setFlowTable(new FlowTableRef(tableInstanceId));
		addFlowBuilder.setTransactionUri(new Uri(flow.getId().getValue()));

		salFlowService.addFlow(addFlowBuilder.build());		
	}
}
