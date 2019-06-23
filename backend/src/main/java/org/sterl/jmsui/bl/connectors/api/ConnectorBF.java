package org.sterl.jmsui.bl.connectors.api;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.sterl.jmsui.bl.common.spring.JsonRestController;
import org.sterl.jmsui.bl.connectors.ibm.IbmMqConnectorFactory;

@JsonRestController("/api/connectors")
public class ConnectorBF {

    private final IbmMqConnectorFactory ibmFactory = new IbmMqConnectorFactory();

    @GetMapping
    public List<SupportedConnector> get() {
        return Arrays.asList(SupportedConnector.builder().id(IbmMqConnectorFactory.class.getName()).name("IBM MQ").configMeta(ibmFactory.getConfigMetaData()).build());
    }
}
