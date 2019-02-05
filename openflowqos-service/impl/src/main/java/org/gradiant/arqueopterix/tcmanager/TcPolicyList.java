/*
 * Copyright © 2018 Gradiant and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.gradiant.arqueopterix.tcmanager;

import java.util.Iterator;
import java.util.ArrayList;

public class TcPolicyList {
	private ArrayList<TcPolicy> list = new ArrayList<>();
	
	public void addPolicy(TcPolicy policy) {
		//First remove any policy that has the same ID, just in case
		removePolicy(policy.getPolicyId());
		list.add(policy);
	}
	
	public void removePolicy(String id) {
		TcPolicy policy = getPolicy(id); 
		if (policy != null) {
			list.remove(policy);
		}
	}
	
	public TcPolicy getPolicy(String id) {
		Iterator<TcPolicy> listIterator = list.iterator();
		
		while (listIterator.hasNext()) {
			TcPolicy policy = listIterator.next();
			
			if (policy.getPolicyId().equals(id)) {
				return policy;
			}
		}
		
		return null;		
	}
	
	public TcPolicy findByAddr(String srcIP, String dstIP) {
		Iterator<TcPolicy> listIterator = list.iterator();
		
		while (listIterator.hasNext()) {
			TcPolicy policy = listIterator.next();
			
			if (policy.getMatch().src_ip.equals(srcIP) &&
				policy.getMatch().dst_ip.equals(dstIP)) {
				return policy;
			}
		}
		
		return null;
	}
}
