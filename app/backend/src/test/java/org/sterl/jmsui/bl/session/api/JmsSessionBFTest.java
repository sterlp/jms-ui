package org.sterl.jmsui.bl.session.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.sterl.jmsui.bl.connection.dao.JmsConnectionDao;
import org.sterl.jmsui.bl.connection.model.JmsConnectionBE;
import org.sterl.jmsui.bl.connectors.memory.MemoryQueueConnectorFactory;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class JmsSessionBFTest {

    @Autowired private JmsConnectionDao conectionDao;
    
    @LocalServerPort private int port;
    @Autowired private TestRestTemplate restTemplate;
    
    private String baseUrl;
    private JmsConnectionBE connector;

    @BeforeEach
    public void init() {
        this.conectionDao.deleteAll();
        this.baseUrl = "http://127.0.0.1:" + this.port;
        
        this.connector = this.conectionDao.save(new JmsConnectionBE()
                .setType(MemoryQueueConnectorFactory.class.getName())
                .setName("Test1"));
    }
    
    @Test
    public void testSimpleMessage() {
        SendJmsMessageCommand msg = new SendJmsMessageCommand();
        msg.setBody("Some message");

        ResponseEntity<Void> exchange = this.restTemplate.exchange(
                this.baseUrl + JmsSessionBF.BASE_URL + "/" + this.connector.getId() + "/message/MEMORY",
                HttpMethod.POST, new HttpEntity<>(msg), Void.class);

        assertThat(exchange.getStatusCode().is2xxSuccessful()).isTrue();
        
        ResponseEntity<JmsResultMessage> result = this.restTemplate.getForEntity(
                this.baseUrl + JmsSessionBF.BASE_URL + "/" + this.connector.getId() + "/message/MEMORY", 
                JmsResultMessage.class);
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();

        assertThat(result.getBody().getBody()).isEqualTo(msg.getBody());
        assertThat(result.getBody().getHeader()).isNotNull();
    }
}
