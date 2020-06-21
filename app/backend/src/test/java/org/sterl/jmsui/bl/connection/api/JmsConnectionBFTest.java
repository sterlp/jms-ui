package org.sterl.jmsui.bl.connection.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.sterl.jmsui.bl.connection.api.model.JmsConnectionDetails;
import org.sterl.jmsui.bl.connection.api.model.JmsConnectionView;
import org.sterl.jmsui.bl.connection.dao.JmsConnectionDao;
import org.sterl.jmsui.bl.connection.model.JmsConnectionBE;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class JmsConnectionBFTest {

    @Autowired private JmsConnectionDao conectionDao;
    
    @LocalServerPort private int port;
    @Autowired private TestRestTemplate restTemplate;
    
    private String baseUrl;

    @BeforeEach
    public void init() {
        this.conectionDao.deleteAll();
        this.baseUrl = "http://127.0.0.1:" + this.port;
        this.conectionDao.save(new JmsConnectionBE("Test1", "Bar1").addOrSetConfig("user", "pass"));
    }
    
    @Test
    public void testSave() {
        JmsConnectionDetails toSave = new JmsConnectionDetails();
        toSave.setType("Foo Id");
        toSave.setName("foo 1");
        toSave.addConfig("key1", "value1").addConfig("key2", "value2");

        ResponseEntity<JmsConnectionDetails> exchange = this.restTemplate.exchange(
                this.baseUrl + JmsConnectionBF.URL,
                HttpMethod.POST, new HttpEntity<>(toSave), JmsConnectionDetails.class);

        assertThat(exchange.getStatusCode().is2xxSuccessful()).isTrue();
        assertNotNull(exchange.getBody().getId());
        assertEquals("foo 1", exchange.getBody().getName());
        assertEquals("value1", exchange.getBody().getConfigValues().get("key1"));
        assertEquals("value2", exchange.getBody().getConfigValues().get("key2"));
    }
    
    @Test
    public void testUpdate() {
        JmsConnectionDetails toSave = new JmsConnectionDetails();
        toSave.setType("Foo Id");
        toSave.setName("foo 1");
        toSave.addConfig("key1", "value1")
              .addConfig("key2", "value2")
              .addConfig("key3", "value3");

        ResponseEntity<JmsConnectionDetails> exchange = this.restTemplate.exchange(
                this.baseUrl + JmsConnectionBF.URL,
                HttpMethod.POST, new HttpEntity<>(toSave), JmsConnectionDetails.class);

        assertThat(exchange.getStatusCode().is2xxSuccessful()).isTrue();
        toSave = exchange.getBody();
        
        toSave.setName("ahs");
        toSave.setType("arrr");
        toSave.getConfigValues().clear();
        toSave.addConfig("key1", "value1as") // edit
              .addConfig("key2", "value2") // no change
              .addConfig("key7", "value7"); // new, delete
        
        // save using PUT
        exchange = this.restTemplate.exchange(
                this.baseUrl + JmsConnectionBF.URL + "/" + toSave.getId(),
                HttpMethod.PUT, new HttpEntity<>(toSave), JmsConnectionDetails.class);

        assertThat(exchange.getStatusCode().is2xxSuccessful()).isTrue();
        assertEquals("ahs", exchange.getBody().getName());
        assertEquals("arrr", exchange.getBody().getType());
        assertEquals("value1as", exchange.getBody().getConfigValues().get("key1"));
        assertEquals("value2", exchange.getBody().getConfigValues().get("key2"));
        assertEquals("value7", exchange.getBody().getConfigValues().get("key7"));
        assertEquals(3, exchange.getBody().getConfigValues().size()); // only 3
        
        // save using the POST end point
        toSave = exchange.getBody();
        exchange = this.restTemplate.exchange(
                this.baseUrl + JmsConnectionBF.URL,
                HttpMethod.POST, new HttpEntity<>(toSave), JmsConnectionDetails.class);
        
        assertThat(exchange.getStatusCode().is2xxSuccessful()).isTrue();
        assertEquals(toSave.getId(), exchange.getBody().getId());
        assertEquals(toSave.getConfigValues(), exchange.getBody().getConfigValues());
    }
    
    @Test
    public void testRead() {
        for (int i = 2; i < 5; i++) {
            this.conectionDao.save(new JmsConnectionBE("Test" + i, "Foo").addOrSetConfig("user", "pass"));
        }
        ResponseEntity<String> result = this.restTemplate.exchange(
                this.baseUrl + JmsConnectionBF.URL +
                    "?page=0&size=5&sort=name,desc",
                HttpMethod.GET, null, 
                String.class);
        System.out.println(result);
        assertTrue(result.getStatusCode().is2xxSuccessful());
        
        for (int i = 1; i < 5; i++) {
            assertTrue(result.getBody().contains("Test" + i));
        }
        
        result = this.restTemplate.exchange(
                this.baseUrl + JmsConnectionBF.URL +
                    "?page=30&size=5&sort=name,desc",
                HttpMethod.GET, null, 
                String.class);
        assertTrue(result.getStatusCode().is2xxSuccessful());
        assertFalse(result.getBody().contains("Test1"));
    }

    Page<JmsConnectionView> getRestPage(String url) {
        ResponseEntity<Page<JmsConnectionView>> exchange = this.restTemplate.exchange(url,
                HttpMethod.GET, null, new ParameterizedTypeReference<Page<JmsConnectionView>>() {});
        
        return null;
    }

}
