/*
 * Copyright © 2018 Gradiant and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.gradiant.arqueopterix.openflowqos;

import org.gradiant.arqueopterix.flowwriter.FlowWriter;
import org.gradiant.arqueopterix.flowwriter.InitialConfigurator;
import org.gradiant.arqueopterix.flowwriter.MeterWriter;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.yang.gen.v1.urn.org.gradiant.arqueopterix.openflowqos.rev180103.OpenFlowQosService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;

/**
 * Provider for the OpenFlowQoS service. This service is automatically 
 * instantiated and started by Karaf using the information included in
 * the blueprint.xml file.
 * 
 * @author pcounhago
 *
 */
public class OpenFlowQosProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OpenFlowQosProvider.class);

    private final DataBroker dataBroker;
    private final SalFlowService salFlowService;
    private final SalMeterService salMeterService;
    private final RpcProviderRegistry rpcProviderRegistry;
    private RpcRegistration<OpenFlowQosService> serviceRegistration;
    private ListenerRegistration<DataChangeListener> listenerRegistration;

    public OpenFlowQosProvider(final DataBroker dataBroker,
    		final SalFlowService salFlowService,
    		final SalMeterService salMeterService, 
    		final RpcProviderRegistry rpcProviderRegistry) {
        this.dataBroker = dataBroker;
        this.salFlowService = salFlowService;
        this.rpcProviderRegistry = rpcProviderRegistry;
        this.salMeterService = salMeterService;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("FlowWriterProvider Session Initiated");

        // Create a FlowWriter instance responsible for the creation of flows in the configuration
        // database
        FlowWriter flowWriter = new FlowWriter(this.salFlowService);
        
        MeterWriter meterWriter = new MeterWriter(this.salMeterService);
        
        // Create an instance of the OpenFlowQosService and register it as an RPC provider 
        OpenFlowQosServiceImpl rpcApi = new OpenFlowQosServiceImpl(flowWriter);
        serviceRegistration = rpcProviderRegistry.addRpcImplementation(OpenFlowQosService.class, rpcApi);
        
        // Create a MeterConfigurator instance responsible for the creation of meters
        // when new nodes are connected to the controller.
        InitialConfigurator meterConfigurator = new InitialConfigurator(meterWriter, flowWriter);
        listenerRegistration = meterConfigurator.registerAsDataChangeListener(dataBroker);
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("FlowWriterProvider Closed");

        serviceRegistration.close();
        listenerRegistration.close();
    }
}
