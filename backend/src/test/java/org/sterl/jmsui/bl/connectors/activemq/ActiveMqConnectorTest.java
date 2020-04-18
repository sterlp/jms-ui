package org.sterl.jmsui.bl.connectors.activemq;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;

import javax.jms.Message;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.plugin.StatisticsBrokerPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sterl.jmsui.AwaitUtil;
import org.sterl.jmsui.api.JmsMessageConverter;
import org.sterl.jmsui.bl.connectors.activemq.control.ActiveMqConnector;
import org.sterl.jmsui.bl.connectors.activemq.model.ActiveMqConnectorConfigBE;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource.Type;

/**
 * As we start a in memory test we can do the whole functionality check with activemq
 */
class ActiveMqConnectorTest {

    private BrokerService broker;
    private String connectionUri;
    
    private ActiveMqConnector subject;
    
    @BeforeEach
    void before() throws Exception {
        createBroker();
        final ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(connectionUri);
        cf.setCloseTimeout(1000);
        cf.setSendTimeout(1000);
        subject = new ActiveMqConnector(cf, ActiveMqConnectorConfigBE.builder().defaultTimeout(1_000).hasStatisticsPlugin(true).build());
    }
    @AfterEach
    void after() throws Exception {
        subject.close();
        broker.stop();
    }
    
    @Test
    void testSimpleSend() throws Exception {
        subject.connect();

        subject.sendMessage("FOO", null, "hallo", null);
        Message receivedMessage = subject.receive("FOO", null, null);
        assertThat(JmsMessageConverter.getMessageBody(receivedMessage)).isEqualTo("hallo");
    }
    
    @Test
    void testListQueues() throws Exception {
        subject.connect();
        
        subject.sendMessage("FOO_1", null, "hallo", null);
        subject.sendMessage("ZZZZ_2", null, "hallo", null);
        
        AwaitUtil.waitFor(() -> subject.listQueues().size(), 2, Duration.ofSeconds(1));
        List<JmsResource> result = subject.listQueues();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getName()).isEqualTo("FOO_1");
        assertThat(result.get(0).getType()).isEqualTo(Type.QUEUE);
        
        assertThat(subject.listTopics().size()).isEqualTo(0);
    }
    
    @Test
    void testListTopics() throws Exception {
        subject.connect();
        
        subject.sendMessage("FOO.1", Type.TOPIC, "hallo", null);
        subject.sendMessage("FOO.2", Type.TOPIC, "hallo", null);
        
        List<JmsResource> result = subject.listTopics();
        AwaitUtil.waitFor(() -> subject.listTopics().size(), 2, Duration.ofSeconds(1));
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getName()).isEqualTo("FOO.1");
        assertThat(result.get(0).getType()).isEqualTo(Type.TOPIC);
        
        assertThat(subject.listQueues().size()).isEqualTo(0);
    }
    
    @Test
    void testListDepth() throws Exception {
        subject.connect();
        subject.sendMessage("FOO1", Type.QUEUE, "hallo", null);
        assertThat(subject.getQueueDepth("FOO1")).isEqualTo(1);
    }

    private void createBroker() throws Exception {
        broker = new BrokerService();
        broker.setUseJmx(false);
        broker.setPersistent(false);
        broker.addConnector("tcp://localhost:0");
        broker.setPlugins(new BrokerPlugin[] {new StatisticsBrokerPlugin()});
        broker.start();
        broker.waitUntilStarted();

        connectionUri = broker.getTransportConnectors().get(0).getPublishableConnectString();
    }
}
