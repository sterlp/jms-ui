package org.sterl.jmsui.bl.connectors.activemq;

import javax.annotation.PreDestroy;

import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.plugin.StatisticsBrokerPlugin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddedActiveMqServerConfig {

    @Value("${jms-ui.embedded.activemq.enabled:false}")
    private boolean enabled;
    @Value("${jms-ui.embedded.activemq.port:61616}")
    private int activeMqPort;

    private BrokerService broker;
    private String connectionUri;

    @Bean
    public BrokerService start() throws Exception {
        broker = new BrokerService();
        broker.setBrokerName("JMS-UI-Embedded-Broker");
        broker.setUseJmx(true);
        broker.setPersistent(false);
        broker.addConnector("tcp://localhost:" + activeMqPort);
        broker.setPlugins(new BrokerPlugin[] {new StatisticsBrokerPlugin()});
        broker.start();
        broker.waitUntilStarted();

        connectionUri = broker.getTransportConnectors().get(0).getPublishableConnectString();
        return broker;
    }
    
    @PreDestroy
    public void stop() throws Exception {
        broker.stop();
    }
}
