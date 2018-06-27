/*
 * Copyright © 2018 Gradiant and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.gradiant.arqueopterix.flowwriter.utils;

import java.util.concurrent.atomic.AtomicLong;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * This class contains helper functions that simplify the creation of Instance Identifier objects for
 * multiple elements in Opendaylight Inventory.
 * 
 * @author pcounhago
 *
 */
public class IidUtils {
	private static AtomicLong flowIdInc = new AtomicLong();
	
	/**
	 * Generate Node instance identifier
	 * 
	 * @param nodeId Node identifier for the corresponding node.
	 * @return Node instance identifier
	 */
	public static InstanceIdentifier<Node> getNodeIid(String nodeId) {
		
		return InstanceIdentifier.builder(Nodes.class) //
				.child(Node.class, new NodeKey(new NodeId(nodeId))).build();
	}
	
	/**
	 * Generate Table instance identifier
	 * 
	 * @param nodeId Node identifier for the corresponding node.
	 * @param tableId Table identifier
	 * @return Table instance identifier
	 */
	public static InstanceIdentifier<Table> getTableIid(String nodeId, short tableId) {
		InstanceIdentifier<Node> nodeInstanceIdentifier = getNodeIid(nodeId);
		
		return nodeInstanceIdentifier.builder().augmentation(FlowCapableNode.class)
				.child(Table.class, new TableKey(tableId)).build();
	}
	
	/**
	 * Generate Flow instance identifier.
	 * 
	 * @param nodeId Node identifier for the corresponding node.
	 * @param tableId Identifier of the table where returned Flow is created.
	 * @param flowIdPrefix String prepended to the generated flowId
	 * @return Flow instance identifier
	 */
	public static InstanceIdentifier<Flow> getFlowIid(String nodeId, short tableId, String flowIdPrefix){
		FlowId flowId = new FlowId(flowIdPrefix + String.valueOf(flowIdInc.getAndIncrement()));
		
		InstanceIdentifier<Table> tableInstanceIdentifier = getTableIid(nodeId, tableId);
		
		return tableInstanceIdentifier.child(Flow.class, new FlowKey(flowId));
	}
	
	/**
	 * Generate NodeConnector instance identifier
	 * 
	 * @param nodeId Node identifier for the corresponding node.
	 * @param port Port number that NodeConnector refers to.
	 * @return NodeConnector instance identifier
	 */
	public static InstanceIdentifier<NodeConnector> getNodeConnectorIid(String nodeId, short port){
		InstanceIdentifier<Node> nodeInstanceIdentifier = getNodeIid(nodeId);
		
		return nodeInstanceIdentifier.builder()
				.child(NodeConnector.class, new NodeConnectorKey(new NodeConnectorId(nodeId+":"+Short.toString(port))))
				.build();
	}
	
	public static InstanceIdentifier<Meter> getMeterIid() {
        InstanceIdentifier<Meter> meterInstanceIdentifier = InstanceIdentifier.create(Nodes.class)
        		.child(Node.class)
        		.augmentation(FlowCapableNode.class)
        		.child(Meter.class);
        return meterInstanceIdentifier;
	}
}
