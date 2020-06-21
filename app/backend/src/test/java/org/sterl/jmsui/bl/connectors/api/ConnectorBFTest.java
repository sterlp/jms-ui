package org.sterl.jmsui.bl.connectors.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ConnectorBFTest {

    @LocalServerPort private int port;
    @Autowired private TestRestTemplate restTemplate;
    
    private String baseUrl;

    @BeforeEach
    public void init() {
        this.baseUrl = "http://127.0.0.1:" + this.port;
    }

    @Test
    public void testGetConnectors() {
        ResponseEntity<List<SupportedConnector>> exchange = this.restTemplate.exchange(
                this.baseUrl + ConnectorBF.BASE_URL,
                HttpMethod.GET, null, new ParameterizedTypeReference<List<SupportedConnector>>() {});
        
        assertThat(exchange.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(exchange.getBody().size()).isGreaterThanOrEqualTo(2);
        
        for(SupportedConnector c : exchange.getBody()) {
            assertThat(c.getId()).isNotEmpty();
            assertThat(c.getName()).isNotEmpty();
        }
    }
}
