/*
 * Copyright © 2018 Gradiant and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.gradiant.arqueopterix.northbound;

import javax.ws.rs.core.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.emptySet;

import com.google.common.collect.ImmutableSet;
import java.util.Set;


/**
 * NorthboundApiApplication provides a REST northbound API for QoS management in ODL
 * 
 * @author gradiant
 *
 */
public class NorthboundApiApplication extends Application {
	private static final Logger LOG = LoggerFactory.getLogger(NorthboundApiApplication.class);

    @Override
    public Set<Class<?>> getClasses() {
        return emptySet();
    }

    @Override
    public Set<Object> getSingletons() {
    	LOG.info("returning immutable set");
    	
    	//PolicyResource class implements REST API methods
        return ImmutableSet.builder()
        		.add(new PolicyResource())
        		.build();
    }
}
