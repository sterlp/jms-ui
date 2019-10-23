package org.sterl.jmsui.bl.connectors.api;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.sterl.jmsui.api.ConfigMetaData;
import org.sterl.jmsui.bl.common.spring.JsonRestController;
import org.sterl.jmsui.bl.connectors.ibm.IbmMqConnectorFactory;

@JsonRestController("/api/connectors")
public class ConnectorBF {

    private final IbmMqConnectorFactory ibmFactory = new IbmMqConnectorFactory();

    @GetMapping
    public List<SupportedConnector> get() {
        SupportedConnector t = SupportedConnector.builder().id("test").name("Test")
            .configMeta(
                    new ConfigMetaData[]
                        {ConfigMetaData.<String>builder().property("userId").label("user").mandatory(false).build()})
            .build();
        return Arrays.asList(
                //t,
                SupportedConnector.builder().id(IbmMqConnectorFactory.class.getName()).name("IBM MQ").configMeta(ibmFactory.getConfigMetaData()).build());
    }
}
