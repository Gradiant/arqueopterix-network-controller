/*
 * Copyright © 2018 Gradiant and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.gradiant.arqueopterix.tcmanager;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.JsonAdapter;
import com.sun.jersey.api.client.ClientResponse;

public class TcPolicy {
	private Match match;
	private Action action;
	private String policyId;
	
	@JsonAdapter(IntIgnoreZeroAdapter.class)
	private int pref;
	
	public class Match {
		public String src_ip;
		public String dst_ip;
		
		@JsonAdapter(IntIgnoreZeroAdapter.class)
		public int src_port;
		@JsonAdapter(IntIgnoreZeroAdapter.class)
		public int dst_port;
		
		@Override
		public boolean equals(Object object) {
			if (object.getClass() != Match.class) return false;
			Match match = (Match)object;
			if (!match.src_ip.equals(this.src_ip)) return false;
			if (!match.dst_ip.equals(this.dst_ip)) return false;
			if (!(match.src_port == this.src_port)) return false;
			if (!(match.dst_port == this.dst_port)) return false;
			
			return true;
		}
	}
	
	public class Action {
		public String rate;
	}
	
	public void setMatch(String srcAddr, String dstAddr, int srcPort, int dstPort) {
		this.match = new Match();
		this.match.src_ip = srcAddr;
		this.match.dst_ip = dstAddr;
		this.match.src_port = srcPort;
		this.match.dst_port = dstPort;
	}
	
	public void setMatch(String srcAddr, String dstAddr) {
		this.match = new Match();
		this.match.src_ip = srcAddr;
		this.match.dst_ip = dstAddr;
	}
	
	public void setAction(int rate) {
		this.action = new Action();
		this.action.rate = Integer.toString(rate)+"Mbit";
	}
	
	public void setPolicyId(String id) {
		this.policyId = id;
	}
	
	public void setPref(int pref) {
		this.pref = pref;
	}
	
	public Action getAction() {
		return this.action;
	}
	
	public Match getMatch() {
		return this.match;
	}
	
	public String getPolicyId() {
		return this.policyId;
	}
	
	public int getPref() {
		return this.pref;
	}
	
	@Override
	public String toString() {
		Gson gson = new GsonBuilder()
				.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
				.create();
		
		return gson.toJson(this);
	}
	
	@Override
	public boolean equals(Object object) {
		if (object.getClass() != TcPolicy.class) return false;
		
		TcPolicy policy = (TcPolicy)object;
		if (!policy.getPolicyId().equals(this.policyId)) return false;
		if (!policy.getMatch().equals(this.match)) return false;
		
		return true;
	}
}
