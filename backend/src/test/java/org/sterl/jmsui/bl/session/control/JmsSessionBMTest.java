package org.sterl.jmsui.bl.session.control;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.UUID;

import javax.jms.Message;
import javax.jms.TextMessage;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.sterl.jmsui.bl.connection.control.JmsConnectionBM;
import org.sterl.jmsui.bl.connection.model.JmsConnectionBE;
import org.sterl.jmsui.bl.connectors.activemq.ActiveMqConnectorFactory;
import org.sterl.jmsui.bl.connectors.activemq.TestActiveMQBroker;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource.Type;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class JmsSessionBMTest {

    @Autowired  JmsSessionBM subject;
    @Autowired JmsConnectionBM connectionBM;
    JmsConnectionBE jmsConnection;
    static TestActiveMQBroker testBroker;
    
    @BeforeAll
    static void init() throws Exception {
        testBroker = new TestActiveMQBroker();
    }
    @AfterAll
    static void clean() throws IOException {
        testBroker.close();
    }
    @BeforeEach
    void beforeEach() {
        jmsConnection = new JmsConnectionBE();
        jmsConnection.setName("Test Active MQ");
        jmsConnection.setTimeout(1000L);
        jmsConnection.setType(ActiveMqConnectorFactory.class.getName());
        jmsConnection.addOrSetConfig("brokerURL", testBroker.getConnectionUri());
        jmsConnection.addOrSetConfig("hasStatisticsPlugin", "true");
        jmsConnection = connectionBM.save(jmsConnection);
    }
    @AfterEach
    void afterEach() {
        if (jmsConnection != null) subject.disconnect(jmsConnection.getId());
    }

    @Test
    void test() throws Exception {
        final String queueName = UUID.randomUUID().toString();
        for (int i = 0; i < 50; i++) {
            subject.sendMessage(jmsConnection.getId(), queueName, Type.QUEUE, "Message " + (i + 1), null);
        }
        
        Page<Message> result = subject.browseQueue(jmsConnection.getId(), queueName, null);
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(50);
        assertThat(result.getContent().size()).isEqualTo(50);
        
        result = subject.browseQueue(jmsConnection.getId(), queueName, PageRequest.of(0, 5));
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(50);
        assertThat(result.getContent().size()).isEqualTo(5);
        
        result = subject.browseQueue(jmsConnection.getId(), queueName, PageRequest.of(2, 5));
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(50);
        assertThat(result.getContent().size()).isEqualTo(5);
        
        assertThat(((TextMessage)result.getContent().get(0)).getText()).isEqualTo("Message 11");
        assertThat(((TextMessage)result.getContent().get(4)).getText()).isEqualTo("Message 15");
        
        subject.disconnect(jmsConnection.getId());
    }

}
