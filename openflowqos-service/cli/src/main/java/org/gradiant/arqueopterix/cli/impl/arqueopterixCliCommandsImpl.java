/*
 * Copyright © 2017 Gradiant and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.gradiant.arqueopterix.cli.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gradiant.arqueopterix.cli.api.arqueopterixCliCommands;

public class arqueopterixCliCommandsImpl implements arqueopterixCliCommands {

    private static final Logger LOG = LoggerFactory.getLogger(arqueopterixCliCommandsImpl.class);
    private final DataBroker dataBroker;

    public arqueopterixCliCommandsImpl(final DataBroker db) {
        this.dataBroker = db;
        LOG.info("arqueopterixCliCommandImpl initialized");
    }

    @Override
    public Object testCommand(Object testArgument) {
        return "This is a test implementation of test-command";
    }
}