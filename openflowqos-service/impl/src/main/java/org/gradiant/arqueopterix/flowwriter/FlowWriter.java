/*
 * Copyright © 2018 Gradiant and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.gradiant.arqueopterix.flowwriter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.gradiant.arqueopterix.flowwriter.utils.FlowUtils;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanIdActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.mpls.action._case.PopMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.vlan.action._case.PopVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.mpls.action._case.PushMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.vlan.action._case.PushVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.queue.action._case.SetQueueActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.vlan.id.action._case.SetVlanIdActionBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanIdBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopMplsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFieldsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;

/**
 * This class simplifies the task of writing new flows to the OpenDaylight
 * configuration database.
 *
 * @author pcounhago
 */
public class FlowWriter {
	private final int FLOW_BASE_PRIORITY = 10;
	private final int FLOW_QOS_PRIORITY = 20;
	private final int FLOW_IDLE_TIMEOUT = 0;
	private final int FLOW_HARD_TIMEOUT = 0;
	
	private SalFlowService salFlowService;

	private static final Logger LOG = LoggerFactory.getLogger(FlowWriter.class);

	public FlowWriter(SalFlowService salFlowService) {
		this.salFlowService = salFlowService;
	}
	
	/**
	 * Creates a new flow in the specified node that matches the provided flow configuration,
	 * assigns any packet of said flow to the provided queue ID and redirects the flow to the
	 * specified table in the OpenFlow pipeline.
	 * 
	 * @param nodeId Node where the rule is installed
	 * @param tableId Table id for the table where the rule will be installed
	 * @param srcAddr IP source address
	 * @param dstAddr IP destination address
	 * @param queueId Output queue identifier
	 * @param outTableId Table id for the next table to go to
	 * @param priority Rule priority
	 */
	public void assignFlowToQueue(String nodeId, short tableId, Ipv4Prefix srcAddr, Ipv4Prefix dstAddr, long queueId, short outTableId, int priority) {
		Match match;
		Action action;
		Instruction instruction;
		ArrayList<Action> actionList = new ArrayList<>();
		ArrayList<Instruction> instructionList = new ArrayList<>();

		// Create the corresponding match
		match = new MatchBuilder().build();
		
		if (dstAddr != null || srcAddr != null) {
			// Create the corresponding match
			match = FlowUtils.getFlowMatch(srcAddr, dstAddr);
		}
		
		// Create a Queue action and add it to the list
		action = FlowUtils.getQueueAction(0, queueId);
		actionList.add(action);
		
		// Create an ApplyActions instruction and add it to the list
		instruction = FlowUtils.getApplyActionsInstruction(0, actionList);
		instructionList.add(instruction);
		
		// Create GoToTable instruction and add it to the list
		instruction = FlowUtils.getGoToTableInstruction(1, outTableId);
		instructionList.add(instruction);

		// Generate the desired flow
		Flow flow = FlowUtils.createFlow(tableId, match, instructionList, priority);
		
		// Write flow to the configuration database
		FlowUtils.writeFlow(salFlowService, nodeId, tableId, flow);
	}
	
	/**
	 * Creates a new flow in the specified node that matches the provided flow configuration,
	 * assigns any packet of said flow to the provided queue ID and redirects the flow to the
	 * specified table in the OpenFlow pipeline.
	 * 
	 * @param nodeId Node where the rule is installed
	 * @param tableId Table id for the table where the rule will be installed
	 * @param inPort Input port
	 * @param outPort Output port
	 * @param priority Rule priority
	 */
	public void outToPort(String nodeId, short tableId, String inPort, String outPort, int priority) {
		Match match;
		Action action;
		Instruction instruction;
		ArrayList<Action> actionList = new ArrayList<>();
		ArrayList<Instruction> instructionList = new ArrayList<>();

		// Create the corresponding match
		match = FlowUtils.getFlowMatch(nodeId, inPort);
		
		// Create a Queue action and add it to the list
		action = FlowUtils.getOutputAction(0, nodeId, outPort);
		actionList.add(action);
		
		// Create an ApplyActions instruction and add it to the list
		instruction = FlowUtils.getApplyActionsInstruction(0, actionList);
		instructionList.add(instruction);

		// Generate the desired flow
		Flow flow = FlowUtils.createFlow(tableId, match, instructionList, priority);
		
		// Write flow to the configuration database
		FlowUtils.writeFlow(salFlowService, nodeId, tableId, flow);
	}
	
	/**
	 * Write table miss rule to the specified node. Table miss rule matches every packet
	 * and executes a go to table action.
	 * 
	 * @param nodeId Node where the rule is installed
	 * @param tableId Table id for the table where the rule will be installed
	 * @param outTableId Table id for the next table to go to
	 * @param priority Rule priority
	 */
	public void tableMiss(String nodeId, short tableId, short outTableId, int priority) {
		Match match;
		Instruction instruction;
		ArrayList<Instruction> instructionList = new ArrayList<>();
		
		match = new MatchBuilder().build();

		// Create GoToTable instruction and add it to the list
		instruction = FlowUtils.getGoToTableInstruction(0, outTableId);
		instructionList.add(instruction);

		// Generate the desired flow
		Flow flow = FlowUtils.createFlow(tableId, match, instructionList, priority);

		// Write flow to the configuration database
		FlowUtils.writeFlow(salFlowService, nodeId, tableId, flow);		
	}
	
	/**
	 * Write rule to assign a flow to a meter and jump to other OpenFlow table.
	 * 
	 * @param nodeId Node where the rule is installed
	 * @param tableId Table id for the table where the rule will be installed
	 * @param srcAddr IP source address
	 * @param dstAddr IP destination address
	 * @param meterId Meter identifier
	 * @param outTableId Table id for the next table to go to
	 * @param priority Rule priority
	 * @param outPort Output port for the flow
	 */
	public void setMeter(String nodeId, short tableId, Ipv4Prefix srcAddr, Ipv4Prefix dstAddr, long meterId, short outTableId, int priority, long outPort) {
		LOG.error("Install default meter flow");
		
		Match match;
		Action action;
		Instruction instruction;
		ArrayList<Action> actionList = new ArrayList<>();
		ArrayList<Instruction> instructionList = new ArrayList<>();
		
		match = new MatchBuilder().build();
		
		if (dstAddr != null || srcAddr != null) {
			// Create the corresponding match
			match = FlowUtils.getFlowMatch(srcAddr, dstAddr);
		}
		
		// Create a Meter instruction and add it to the list
		instruction = FlowUtils.getMeterInstruction(0, meterId);
		instructionList.add(instruction);
		
		
		action = FlowUtils.getOutputAction(0, nodeId, outPort);
		actionList.add(action);
		
		instruction = FlowUtils.getApplyActionsInstruction(1, actionList);
		instructionList.add(instruction);

		// Create GoToTable instruction and add it to the list
		//instruction = FlowUtils.getGoToTableInstruction(1, outTableId);

		// Generate the desired flow
		Flow flow = FlowUtils.createFlow(tableId, match, instructionList, priority);

		// Write flow to the configuration database
		FlowUtils.writeFlow(salFlowService, nodeId, tableId, flow);
	}

}
