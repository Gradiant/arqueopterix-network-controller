/*
 * Copyright © 2018 Gradiant and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.gradiant.arqueopterix.flowwriter;

import java.util.ArrayList;
import java.util.concurrent.Future;

import org.gradiant.arqueopterix.flowwriter.utils.IidUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.BandId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DropBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeadersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeaderBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.meter.band.header.MeterBandTypesBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeterWriter {
    private final SalMeterService salMeterService;
    
    private static final Logger LOG = LoggerFactory.getLogger(MeterWriter.class);
    
    public MeterWriter(SalMeterService salMeterService) {
    	this.salMeterService = salMeterService;
    }
    
    private Meter getMeter(long dropRate, long dropBurstSize, long meterId) {
        //LOG.info("nodeConnectorRef is" + nodeConnectorRef.toString());
        DropBuilder dropBuilder = new DropBuilder();
        dropBuilder
                .setDropBurstSize(dropBurstSize)
                .setDropRate(dropRate);

        MeterBandHeaderBuilder mbhBuilder = new MeterBandHeaderBuilder()
                .setBandType(dropBuilder.build())
                .setBandId(new BandId(0L))
                .setMeterBandTypes(new MeterBandTypesBuilder()
                        .setFlags(new MeterBandType(true, false, false)).build())
                .setBandRate(dropRate)
                .setBandBurstSize(dropBurstSize);

        LOG.info("In createDropMeter, MeterBandHeaderBuilder is" + mbhBuilder.toString());

        ArrayList<MeterBandHeader> mbhList = new ArrayList<>();
        mbhList.add(mbhBuilder.build());

        MeterBandHeadersBuilder mbhsBuilder = new MeterBandHeadersBuilder()
                .setMeterBandHeader(mbhList);

        LOG.info("In createDropMeter, MeterBandHeader is " + mbhBuilder.build().toString());
        MeterBuilder meterBuilder = new MeterBuilder()
                .setFlags(new MeterFlags(false, true, false, false))
                .setMeterBandHeaders(mbhsBuilder.build())
                .setMeterId(new MeterId(meterId))
                .setMeterName("arqueopterix meter")
                .setContainerName("arqueopterix container");
        
        return meterBuilder.build();
    }
    
    private void writeMeter(String nodeId, Meter meter) {
    	InstanceIdentifier<Node> nodeInstanceId = IidUtils.getNodeIid(nodeId);
    	InstanceIdentifier<Meter> meterInstanceId = IidUtils.getMeterIid();
    	
        final AddMeterInputBuilder builder = new AddMeterInputBuilder(meter);
		builder.setNode(new NodeRef(nodeInstanceId));
        builder.setMeterRef(new MeterRef(meterInstanceId));
        builder.setTransactionUri(new Uri(meter.getMeterId().getValue().toString()));
    	
    	salMeterService.addMeter(builder.build());
    }
    
    public void createMeter(String nodeId, long dropRate, long dropBurstSize, long meterId) {
    	Meter meter = getMeter(dropRate, dropBurstSize, meterId);
    	writeMeter(nodeId, meter);
    }
}
