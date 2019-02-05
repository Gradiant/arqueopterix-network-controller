/*
 * Copyright © 2018 Gradiant and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.gradiant.arqueopterix.flowwriter;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates meters with the specified configurations in every switch that
 * connects to the controller
 */
public class InitialConfigurator implements DataChangeListener {
    private static final Logger LOG = LoggerFactory.getLogger(InitialConfigurator.class);

    private final ExecutorService initialConfigurationExecutor = Executors.newCachedThreadPool();
    private final FlowWriter flowWriter;
    private final MeterWriter meterWriter;
    private final String FLOW_ID_PREFIX = "Arqueopterix-";
    private final int LOW_PRIO_MPLS = 300;
    private final int HIGH_PRIO_MPLS = 350;
    
    private final int LOW_PRIO_QUEUE = 0;
    private final int HIGH_PRIO_QUEUE = 1;
    
    private final long PORT1_LO_UP_METER_ID = 1;
    private final long PORT1_HI_UP_METER_ID = 2;
    private final long PORT1_LO_DOWN_METER_ID = 3;
    private final long PORT1_HI_DOWN_METER_ID = 4;
    private final long PORT2_LO_UP_METER_ID = 5;
    private final long PORT2_HI_UP_METER_ID = 5;
    private final long PORT2_LO_DOWN_METER_ID = 7;
    private final long PORT2_HI_DOWN_METER_ID = 8;

    public InitialConfigurator(MeterWriter meterWriter, FlowWriter flowWriter) {
        this.meterWriter = meterWriter;
        this.flowWriter = flowWriter;
    }

    /**
     * Register itself as an ODL DataChangeListener.
     * 
     * @param dataBroker
     * @return ListenerRegistration instance used to perform de-registration.
     */
    public ListenerRegistration<DataChangeListener> registerAsDataChangeListener(DataBroker dataBroker) {
        InstanceIdentifier<Node> nodeInstanceIdentifier = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class).build();

        return dataBroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, nodeInstanceIdentifier, this, AsyncDataBroker.DataChangeScope.BASE);
    }


    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> instanceIdentifierDataObjectAsyncDataChangeEvent) {
        Map<InstanceIdentifier<?>, DataObject> createdData = instanceIdentifierDataObjectAsyncDataChangeEvent.getCreatedData();
        if(createdData !=null && !createdData.isEmpty()) {
            Set<InstanceIdentifier<?>> nodeIds = createdData.keySet();
            if(nodeIds != null && !nodeIds.isEmpty()) {
            	LOG.error("New node connected to the controller");
            	
                initialConfigurationExecutor.submit(new InitialConfigurationProcessor(nodeIds));
            }
        }
    }

    /**
     * A private class to process the node updated event in separate thread. Allows to release the
     * thread that invoked the data node updated event. Avoids any thread lock it may cause.
     */
    private class InitialConfigurationProcessor implements Runnable {
        Set<InstanceIdentifier<?>> nodeIds = null;

        public InitialConfigurationProcessor(Set<InstanceIdentifier<?>> nodeIds) {
            this.nodeIds = nodeIds;
        }

        @Override
        public void run() {

            if(nodeIds == null) {
                return;
            }
            
            try {
				Thread.sleep(1000);

	            for(InstanceIdentifier<?> nodeId : nodeIds) {
	                if(Node.class.isAssignableFrom(nodeId.getTargetType())) {
	
	                    InstanceIdentifier<Node> invNodeId = (InstanceIdentifier<Node>)nodeId;
	                    String strNodeId = invNodeId.firstKeyOf(Node.class,NodeKey.class).getId().getValue();
	                    
						short queueId = 0;
						short tableId = 0;
						short l2SwitchTable = 1;
	                    
	                	switch (strNodeId) {
	                		case "openflow:8796749200715":
	                		case "openflow:8796751961110":
	                		case "openflow:8796763005145":
	                		case "openflow:8796751298470":
	                			LOG.error("Handling OVS nodes. test");
	                			flowWriter.assignFlowToQueue(
	                					strNodeId,
	                					tableId,
	                					null,
	                					null,
	                					LOW_PRIO_QUEUE,
	                					(short)(tableId+1),
	                					10
	                			);
	                			
	                			flowWriter.outToPort(
	                					strNodeId,
	                					(short)(tableId+1), 
	                					"1", 
	                					"LOCAL",  
	                					10);
	                			
	                			flowWriter.outToPort(
	                					strNodeId,
	                					(short)(tableId+1), 
	                					"LOCAL", 
	                					"1",  
	                					10);
	                			
	                			break;
	                		//Switch S1 Zodiac FX
							case "openflow:123917682138460":
							case "openflow:1":
								//Uplink meters
			                    meterWriter.createMeter(strNodeId, 10000, 0, PORT1_LO_UP_METER_ID);
			                    meterWriter.createMeter(strNodeId, 25000, 0, PORT1_HI_UP_METER_ID);
			                    meterWriter.createMeter(strNodeId, 10000, 0, PORT2_LO_UP_METER_ID);
			                    meterWriter.createMeter(strNodeId, 25000, 0, PORT2_HI_UP_METER_ID);
			                    
			                    //Downlink meters
			                    meterWriter.createMeter(strNodeId, 10000, 0, PORT1_LO_DOWN_METER_ID);
			                    meterWriter.createMeter(strNodeId, 25000, 0, PORT1_HI_DOWN_METER_ID);
			                    meterWriter.createMeter(strNodeId, 10000, 0, PORT2_LO_DOWN_METER_ID);
			                    meterWriter.createMeter(strNodeId, 25000, 0, PORT2_HI_DOWN_METER_ID);
			                    
			                    //----------------------------------------------------------------------------------------------------
			                    //	TABLE 0 
			                    //----------------------------------------------------------------------------------------------------
			                    tableId = 0;
	
			                    
			                    //Uplink meter for H1
			                    flowWriter.setMeter(
			                    		strNodeId,
			                    		tableId,
			                    		new Ipv4Prefix("192.168.0.61/32"),
			                    		null,
			                    		PORT1_LO_UP_METER_ID,
			                    		(short)(tableId+1),
			                    		10,
			                    		(long)3);			                    
			                    Thread.sleep(1000);
			                    
			                    //Downlink meter for H1
			                    flowWriter.setMeter(
			                    		strNodeId,
			                    		tableId,
			                    		null,
			                    		new Ipv4Prefix("192.168.0.61/32"),
			                    		PORT1_LO_DOWN_METER_ID,
			                    		(short)(tableId+1),
			                    		10,
			                    		(long)1);
			                    
			                    Thread.sleep(1000);
			                    
			                    //Uplink meter for H2
			                    flowWriter.setMeter(
			                    		strNodeId,
			                    		tableId,
			                    		new Ipv4Prefix("192.168.0.62/32"),
			                    		null,
			                    		PORT2_LO_UP_METER_ID,
			                    		(short)(tableId+1),
			                    		10,
			                    		(long)3);
			                    
			                    Thread.sleep(1000);
			                    
			                    //Downlink meter for H2
			                    flowWriter.setMeter(
			                    		strNodeId,
			                    		tableId,
			                    		null,
			                    		new Ipv4Prefix("192.168.0.62/32"),
			                    		PORT2_LO_DOWN_METER_ID,
			                    		(short)(tableId+1),
			                    		10,
			                    		(long)2);
			                    
			                    Thread.sleep(1000);
			                    
			                    //Any other traffic is sent to the next table
			                    flowWriter.tableMiss(
			                    		strNodeId,
			                    		tableId,
			                    		(short)(tableId+1),
			                    		5);

								break;
								
		                		//Switch S2 Zodiac FX
								case "openflow:123917682138471":
								case "openflow:2":
									//Uplink meters
				                    meterWriter.createMeter(strNodeId, 10000, 0, PORT1_LO_UP_METER_ID);
				                    meterWriter.createMeter(strNodeId, 20000, 0, PORT1_HI_UP_METER_ID);
				                    meterWriter.createMeter(strNodeId, 10000, 0, PORT2_LO_UP_METER_ID);
				                    meterWriter.createMeter(strNodeId, 20000, 0, PORT2_HI_UP_METER_ID);
				                    
				                    //Downlink meters
				                    meterWriter.createMeter(strNodeId, 10000, 0, PORT1_LO_DOWN_METER_ID);
				                    meterWriter.createMeter(strNodeId, 20000, 0, PORT1_HI_DOWN_METER_ID);
				                    meterWriter.createMeter(strNodeId, 10000, 0, PORT2_LO_DOWN_METER_ID);
				                    meterWriter.createMeter(strNodeId, 20000, 0, PORT2_HI_DOWN_METER_ID);
				                    
				                    //----------------------------------------------------------------------------------------------------
				                    //	TABLE 0 
				                    //----------------------------------------------------------------------------------------------------
				                    tableId = 0;
		
				                    
				                    //Uplink meter for H3
				                    flowWriter.setMeter(
				                    		strNodeId,
				                    		tableId,
				                    		new Ipv4Prefix("192.168.0.63/32"),
				                    		null,
				                    		PORT1_LO_UP_METER_ID,
				                    		(short)(tableId+1),
				                    		10,
				                    		(long)3);			                    
				                    Thread.sleep(1000);
				                    
				                    //Downlink meter for H3
				                    flowWriter.setMeter(
				                    		strNodeId,
				                    		tableId,
				                    		null,
				                    		new Ipv4Prefix("192.168.0.63/32"),
				                    		PORT1_LO_DOWN_METER_ID,
				                    		(short)(tableId+1),
				                    		10,
				                    		(long)1);
				                    
				                    Thread.sleep(1000);
				                    
				                    //Uplink meter for H4
				                    flowWriter.setMeter(
				                    		strNodeId,
				                    		tableId,
				                    		new Ipv4Prefix("192.168.0.64/32"),
				                    		null,
				                    		PORT2_LO_UP_METER_ID,
				                    		(short)(tableId+1),
				                    		10,
				                    		(long)3);
				                    
				                    Thread.sleep(1000);
				                    
				                    //Downlink meter for H4
				                    flowWriter.setMeter(
				                    		strNodeId,
				                    		tableId,
				                    		null,
				                    		new Ipv4Prefix("192.168.0.64/32"),
				                    		PORT2_LO_DOWN_METER_ID,
				                    		(short)(tableId+1),
				                    		10,
				                    		(long)1);
				                    
				                    Thread.sleep(1000);
				                    
				                    //Any other traffic is sent to L2Switch
				                    flowWriter.tableMiss(
				                    		strNodeId,
				                    		tableId,
				                    		(short)(tableId+1),
				                    		5);

									break;
							//Switch S3 TP-LINK WDR3600
							case "openflow:272792296473262":
							case "openflow:3":
								//Configure TP-LINK WDR3600
								
			                    //----------------------------------------------------------------------------------------------------
			                    //	TABLE 0 
			                    //----------------------------------------------------------------------------------------------------
								tableId = 0;
								
								//Default rule to send all traffic through the low priority queue
								flowWriter.assignFlowToQueue(
										strNodeId,
										tableId,
										null,
										null,
										LOW_PRIO_QUEUE,
										(short)(tableId+1),
										10);
								
								Thread.sleep(1000);
								
			                    //Any other traffic is sent to the next table
			                    flowWriter.tableMiss(
			                    		strNodeId,
			                    		tableId,
			                    		(short)(tableId+1),
			                    		5);
								break;
	                		//Switch S4 Zodiac FX
							case "openflow:4":
							case "openflow:123917682138470":
			                    //----------------------------------------------------------------------------------------------------
			                    //	TABLE 0 
			                    //----------------------------------------------------------------------------------------------------
								tableId = 0;
								
			                    //Send traffic to L2Switch table
			                    flowWriter.tableMiss(
			                    		strNodeId,
			                    		tableId,
			                    		(short)(tableId+1),
			                    		5);
								break;
							default:
								break;
						}
	                }
	            }
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
}
