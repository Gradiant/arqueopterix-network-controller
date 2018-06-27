/*
 * Copyright © 2018 Gradiant and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.gradiant.arqueopterix.northbound;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlRootElement;

import org.gradiant.arqueopterix.northbound.Policy;

/**
 * Policy object. This is the base resource for the REST API and includes information to describe
 * flows affected by specific QoS actions.
 * 
 * @author gradiant
 *
 */
@XmlRootElement
public class Policy {
	/* Define valid ranges for different parameters */
	private static int PORT_MIN = 0;
	private static int PORT_MAX = 65535;
	private static int SERVICE_LEVEL_MIN = 0;
	private static int SERVICE_LEVEL_MAX = 10;
	private static int PROTOCOL_MIN = 0;
	private static int PROTOCOL_MAX = 255;	
    private static Pattern ipPattern = Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})/32$");

    /* Parameters included in Policy objects */
	private int id;
	private int srcPort;
	private int dstPort;
	private String srcAddr;
	private String dstAddr;
	private int serviceLevel;
	private int protocol;

	/**
	 * Get policy ID
	 * @return policy ID
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Set policy ID
	 * @param id policy ID
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Get flow source port
	 * @return flow source port
	 */
	public int getSrcPort() {
		return srcPort;
	}
	
	/**
	 * Get flow destination port
	 * @return flow destination port
	 */
	public int getDstPort() {
		return dstPort;
	}
	
	/**
	 * Get flow source IP address
	 * @return flow source IP address
	 */
	public String getSrcAddr() {
		return srcAddr;
	}
	
	/**
	 * Get flow destination IP address
	 * @return flow destination IP address
	 */
	public String getDstAddr() {
		return dstAddr;
	}
	
	/**
	 * Get flow QoS level
	 * @return flow QoS level
	 */
	public int getServiceLevel() {
		return serviceLevel;
	}
	
	/**
	 * Get flow transport level protocol
	 * @return flow transport level protocol
	 */
	public int getProtocol() {
		return protocol;
	}
	
	/**
	 * Validate policy object to ensure that its values are set within the specified ranges
	 * and that all required values are present.
	 * @return true if valid, false otherwise
	 */
    public List<String> validate() {
        List<String> errorMessages = new ArrayList<String>();
        Matcher matcher;
        
        if (this.srcPort < PORT_MIN) {
        	errorMessages.add("Source port minimum allowed value is 0");
        } else if (srcPort > PORT_MAX) {
        	errorMessages.add("Source port maximum allowed value is 65535");
        }

        if (this.dstPort < PORT_MIN) {
        	errorMessages.add("Destination port minimum allowed value is 0");
        } else if (dstPort > PORT_MAX) {
        	errorMessages.add("Destination port maximum allowed value is 65535");
        }
        
        if (this.serviceLevel < SERVICE_LEVEL_MIN) {
        	errorMessages.add("Service level minimum allowed value is 0");
        } else if (this.serviceLevel > SERVICE_LEVEL_MAX) {
        	errorMessages.add("Service level maximum allowed value is 10");
        }
        
        matcher = ipPattern.matcher(this.srcAddr);
        if (!matcher.find()) {
        	errorMessages.add("Invalid source IP address");
        }
        
        matcher = ipPattern.matcher(this.dstAddr);
        if (!matcher.find()) {
        	errorMessages.add("Invalid destination IP address");
        }

        return errorMessages;
    }

    /**
     * Check if the provided policy object is equal to this. Two policies are considered as equal
     * if they apply to the same network flow, that is, their source and destination addresses and ports as well 
     * as their transport level protocols match.
     * 
     * @param policy Policy object to compare
     * @return true if policies match, false otherwise.
     */
	public boolean isEqual(Policy policy) {
		if (this.srcPort != policy.getSrcPort()) return false;
		if (this.dstPort != policy.getDstPort()) return false;
		if (!this.srcAddr.equals(policy.getSrcAddr())) return false;
		if (!this.dstAddr.equals(policy.getDstAddr())) return false;
		if (this.protocol != policy.getProtocol()) return false;

		return true;
	}

	/**
	 * Get String representation of the policy object.
	 */
	public String toString() {
		return String.format("id: %d, srcPort: %d, dstPort: %d, srcAddr: %s, dstAddr: %s, serviceLevel: %d, protocol: %d",
				id,	srcPort, dstPort, srcAddr, dstAddr, serviceLevel, protocol);
	}
}
