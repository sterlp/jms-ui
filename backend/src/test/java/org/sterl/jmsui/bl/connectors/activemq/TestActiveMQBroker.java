package org.sterl.jmsui.bl.connectors.activemq;

import java.io.Closeable;
import java.io.IOException;

import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.plugin.StatisticsBrokerPlugin;

import lombok.Data;

@Data
public class TestActiveMQBroker implements Closeable {

    private final BrokerService broker;
    private final String connectionUri;

    public TestActiveMQBroker() throws Exception {
        broker = new BrokerService();
        broker.setUseJmx(false);
        broker.setPersistent(false);
        broker.addConnector("tcp://localhost:0");
        broker.setPlugins(new BrokerPlugin[] {new StatisticsBrokerPlugin()});
        broker.start();
        broker.waitUntilStarted();

        connectionUri = broker.getTransportConnectors().get(0).getPublishableConnectString();
    }
    @Override
    public void close() throws IOException {
        try {
            broker.stop();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
