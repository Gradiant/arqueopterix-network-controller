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
import org.gradiant.arqueopterix.tcmanager.TcManager;
import org.gradiant.arqueopterix.tcmanager.TcPolicy;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.org.gradiant.arqueopterix.openflowqos.rev180103.OpenFlowQosService;
import org.opendaylight.yang.gen.v1.urn.org.gradiant.arqueopterix.openflowqos.rev180103.SetQosLevelInput;
import org.opendaylight.yang.gen.v1.urn.org.gradiant.arqueopterix.openflowqos.rev180103.SetQosLevelOutput;
import org.opendaylight.yang.gen.v1.urn.org.gradiant.arqueopterix.openflowqos.rev180103.SetQosLevelOutputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the OpenFlowQosService defined in Yang model.
 *
 * @author pcounhago
 */
public class OpenFlowQosServiceImpl implements OpenFlowQosService {
    private FlowWriter flowWriter;
    private TcManager tcManager;
    
    private static final Logger LOG = LoggerFactory.getLogger(OpenFlowQosServiceImpl.class);
    
    private final long PORT1_LO_UP_METER_ID = 1;
    private final long PORT1_HI_UP_METER_ID = 2;
    private final long PORT1_LO_DOWN_METER_ID = 3;
    private final long PORT1_HI_DOWN_METER_ID = 4;
    private final long PORT2_LO_UP_METER_ID = 5;
    private final long PORT2_HI_UP_METER_ID = 5;
    private final long PORT2_LO_DOWN_METER_ID = 7;
    private final long PORT2_HI_DOWN_METER_ID = 8;
    
    private String ifaceA;
    private String ifaceB;

    public OpenFlowQosServiceImpl(FlowWriter flowWriter) {
        this.flowWriter = flowWriter;
        
    	this.tcManager = new TcManager("http://127.0.0.1:5000");
    	
    	if (System.getenv("ARQ_IFACE_A") != null) {
    		this.ifaceA = System.getenv("ARQ_IFACE_A");
    	} else {
    		this.ifaceA = "enp0s3";
    	}
    	
    	if (System.getenv("ARQ_IFACE_B") != null) {
    		this.ifaceB = System.getenv("ARQ_IFACE_B");
    	} else {
    		this.ifaceB = "enp0s8";
    	}
    	
    	tcManager.setDefaultRate(this.ifaceA, 5);
    	tcManager.setDefaultRate(this.ifaceB, 5);
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
        
        LOG.error("Received new QoS request");

        if (input.getServiceLevel() == 0) {
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
    	int bw = 5;    	
    	String nodeId;
    	
    	// Decide what network devices are implied based on origin and destination IP addresses
    	if (input.getSrcAddr().getValue().startsWith("192.168.2.")) {
    		nodeId = this.ifaceB;
    	} else if (input.getSrcAddr().getValue().startsWith("192.168.1.")) {
    		nodeId = this.ifaceA; 
    	} else {
    		return;
    	}
    	
    	if (value == 1 || value == 2) {
    		bw = 10;
    	} else {
    		bw = 5;
    	}
    	
    	tcManager.setPolicy(nodeId, input.getSrcAddr(), input.getDstAddr(), bw);
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
    	//strNodeId = "openflow:8796763005145";
    	strNodeId = "openflow:8796751961110";
    	
    	
    	// Commit rule to the configuration datastore
		flowWriter.assignFlowToQueue(
				strNodeId,
				(short)0,
				input.getSrcAddr(),
				input.getDstAddr(),
				queueId,
				(short)1,
				20);
		
    	// Priority is only applied in core switch so it is directly hardcoded
    	//strNodeId = "openflow:8796751298470";
		strNodeId = "openflow:8796749200715";
    	
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
