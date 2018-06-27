/*
 * Copyright © 2018 Gradiant and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.gradiant.arqueopterix.openflowqos;

import java.util.concurrent.Future;

import org.gradiant.arqueopterix.flowwriter.FlowWriter;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.org.gradiant.arqueopterix.openflowqos.rev180103.OpenFlowQosService;
import org.opendaylight.yang.gen.v1.urn.org.gradiant.arqueopterix.openflowqos.rev180103.SetQosLevelInput;
import org.opendaylight.yang.gen.v1.urn.org.gradiant.arqueopterix.openflowqos.rev180103.SetQosLevelOutput;
import org.opendaylight.yang.gen.v1.urn.org.gradiant.arqueopterix.openflowqos.rev180103.SetQosLevelOutputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Implementation of the OpenFlowQosService defined in Yang model.
 *
 * @author pcounhago
 */
public class OpenFlowQosServiceImpl implements OpenFlowQosService {
    private FlowWriter flowWriter;
    
    private final long PORT1_LO_UP_METER_ID = 1;
    private final long PORT1_HI_UP_METER_ID = 2;
    private final long PORT1_LO_DOWN_METER_ID = 3;
    private final long PORT1_HI_DOWN_METER_ID = 4;
    private final long PORT2_LO_UP_METER_ID = 5;
    private final long PORT2_HI_UP_METER_ID = 5;
    private final long PORT2_LO_DOWN_METER_ID = 7;
    private final long PORT2_HI_DOWN_METER_ID = 8;

    public OpenFlowQosServiceImpl(FlowWriter flowWriter) {
        this.flowWriter = flowWriter;
    }

    /**
     * RPC API call to set current QoS level for the specified flow
     * 
     * @param input Includes flow definition data (IPs, ports and protocol) as well
     * as the corresponding QoS level to apply.
     * @return RPC call result 
     */
    @Override
    public Future<RpcResult<SetQosLevelOutput>> setQosLevel(SetQosLevelInput input) {
        SetQosLevelOutputBuilder setQosOutputBuilder = new SetQosLevelOutputBuilder();
        setQosOutputBuilder.setCode((short)10);
        setQosOutputBuilder.setMessage("Fine");

        if (input.getServiceLevel() == 0) {
        	System.out.println(input.toString());
        	setLoBandwidth(input);
        	setLoPriority(input);
        } else if (input.getServiceLevel() == 1) {
        	setHiBandwidth(input);
        	setLoPriority(input);
        } else if (input.getServiceLevel() == 2) {
        	setHiBandwidth(input);
        	setHiPriority(input);
        } else {
        	System.out.println("Unknown quality level");
        }

        return RpcResultBuilder.success(setQosOutputBuilder.build()).buildFuture();
    }
    
    /**
     * Configure network to provide high bandwidth to the specified flow
     * 
     * @param input Input containing policy information
     */
    private void setHiBandwidth(SetQosLevelInput input) {
    	setBandwidth(1, input);
    }
    
    /**
     * Configure network to provide low bandwidth to the specified flow
     * 
     * @param input Input containing policy information
     */
    private void setLoBandwidth(SetQosLevelInput input) {
    	setBandwidth(0, input);
    }
    
    /**
     * Configure network to provide on demand bandwidth to a specific flow
     * 
     * @param value Bandwidth indicator. 0 means low bw and 1 means high bw.
     * @param input Input containing policy information
     */
    private void setBandwidth(int value, SetQosLevelInput input) {
    	long meter = 0;
    	long outPort = 0;    	
    	String nodeId;
    	
    	// Dedide what network devices are implied based on origin and destination IP addresses
    	if (input.getSrcAddr().getValue().equals("192.168.0.61/32") || 
    			input.getDstAddr().getValue().equals("192.168.0.61/32") ||
    			input.getSrcAddr().getValue().equals("192.168.0.62/32") || 
    			input.getDstAddr().getValue().equals("192.168.0.62/32")) {
    		nodeId = "openflow:123917682138460";
    	} else if (input.getSrcAddr().getValue().equals("192.168.0.63/32") || 
    			input.getDstAddr().getValue().equals("192.168.0.63/32") ||
    			input.getSrcAddr().getValue().equals("192.168.0.64/32") || 
    			input.getDstAddr().getValue().equals("192.168.0.64/32")) {
    		nodeId = "openflow:123917682138471"; 
    	} else {
    		return;
    	}
    	
    	// Decide output port and meter based on origin and desination IP addresses and demanded bw
    	if (input.getSrcAddr().getValue().equals("192.168.0.61/32") ||
    			input.getSrcAddr().getValue().equals("192.168.0.63/32")) {
    		if (value == 0) {
    			meter = PORT1_LO_UP_METER_ID;
    		} else {
    			meter = PORT1_HI_UP_METER_ID;
    		}
    		
    		outPort = 3;
    	}
    	
    	if (input.getSrcAddr().getValue().equals("192.168.0.62/32") ||
    			input.getSrcAddr().getValue().equals("192.168.0.64/32")) {
    		if (value == 0) {
    			meter = PORT2_LO_UP_METER_ID;
    		} else {
    			meter = PORT2_HI_UP_METER_ID;
    		}
    		
    		outPort = 3;
    	}
    	
    	if (input.getDstAddr().getValue().equals("192.168.0.61/32") ||
    			input.getDstAddr().getValue().equals("192.168.0.63/32")) {
    		if (value == 0) {
    			meter = PORT1_LO_DOWN_METER_ID;
    		} else {
    			meter = PORT1_HI_DOWN_METER_ID;
    		}
    		
    		outPort = 1;
    	}
    	
    	if (input.getDstAddr().getValue().equals("192.168.0.62/32") ||
    			input.getDstAddr().getValue().equals("192.168.0.64/32")) {
    		if (value == 0) {
    			meter = PORT2_LO_DOWN_METER_ID;
    		} else {
    			meter = PORT2_HI_DOWN_METER_ID;
    		}
    		
    		outPort = 2;
    	}

    		
        // Commit rule to the Configuration datastore
        flowWriter.setMeter(
        		nodeId,
        		(short)0,
        		input.getSrcAddr(),
        		input.getDstAddr(),
        		meter,
        		(short)1,
        		20,
        		outPort
        );
    }
    
    /**
     * Configure network to provide low priority to the specified flow
     * 
     * @param input Input containing policy information
     */
    private void setLoPriority(SetQosLevelInput input) {
    	setPriority(0, input);
    }

    /**
     * Configure network to provide high priority to the specified flow
     * 
     * @param input Input containing policy information
     */
    private void setHiPriority(SetQosLevelInput input) {
    	setPriority(1, input);
    }
    
    /**
     * Configure network to provide on demand priority to a specific flow
     * 
     * @param value
     * @param input
     */
    private void setPriority(int value, SetQosLevelInput input) {
    	int queueId;
    	String strNodeId;
    	
    	// Decide output queue based on demanded priority
    	if (value == 0) {
    		queueId = 0;
    	} else {
    		queueId = 1;
    	}
    	
    	// Priority is only applied in core switch so it is directly hardcoded
    	strNodeId = "openflow:272792296473262";
    	
    	// Commit rule to the configuration datastore
		flowWriter.assignFlowToQueue(
				strNodeId,
				(short)0,
				input.getSrcAddr(),
				input.getDstAddr(),
				queueId,
				(short)1,
				20);
    }
}
