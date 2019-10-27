package org.sterl.jmsui.bl.bookmarks.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.sterl.jmsui.bl.bookmarks.dao.BookmarksDao;
import org.sterl.jmsui.bl.bookmarks.model.BookmarkBE;
import org.sterl.jmsui.bl.common.api.SimplePage;
import org.sterl.jmsui.bl.connection.dao.JmsConnectionDao;
import org.sterl.jmsui.bl.connection.model.JmsConnectionBE;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class BooksmarksBFTest {
    
    @LocalServerPort private int port;
    @Autowired private TestRestTemplate restTemplate;
    
    @Autowired JmsConnectionDao connectionDao;
    @Autowired BookmarksDao bookmarksDao;
    String baseUrl;

    JmsConnectionBE con1;
    JmsConnectionBE con2;
    @BeforeEach
    void before() {
        this.baseUrl = "http://127.0.0.1:" + this.port;
        //bookmarksDao.deleteAll();
        connectionDao.deleteAll();
        con1 = connectionDao.save(new JmsConnectionBE("Con1", "Foo"));
        con2 = connectionDao.save(new JmsConnectionBE("Con2", "Bar"));
    }
    
    @Test
    void testCreate() {
        BookmarkBE c = bookmarksDao.save(new BookmarkBE("FOO1", "QUEUE", con1));
        assertNotNull(c.getId());
    }

    @Test
    void testSimpleRead() {
        bookmarksDao.save(new BookmarkBE("FOO1", "QUEUE", con1));
        bookmarksDao.save(new BookmarkBE("FOO2", "QUEUE", con1));
        bookmarksDao.save(new BookmarkBE("FOO3", "QUEUE", con2));
        
        
        ResponseEntity<SimplePage<BookmarkBE>> exchange = restTemplate.exchange(
                baseUrl + BooksmarksBF.URL + "/" + con2.getId(), HttpMethod.GET, null, 
                new ParameterizedTypeReference<SimplePage<BookmarkBE>>() {});
        
        assertTrue(exchange.getStatusCode().is2xxSuccessful());
        assertEquals(1L, exchange.getBody().getTotalElements());
    }
    
    @Test
    public void testCRUD() {
        BookmarkBE b = new BookmarkBE("FOO3", "TOPIC", null);
        ResponseEntity<BookmarkBE> exchange = restTemplate.exchange(
                baseUrl + BooksmarksBF.URL + "/" + con2.getId(), HttpMethod.POST, 
                new HttpEntity<>(b), 
                BookmarkBE.class);
        
        assertTrue(exchange.getStatusCode().is2xxSuccessful());
        b = exchange.getBody();
        assertEquals("FOO3", b.getName());
        assertNotNull(b.getId());
        assertEquals(1, restTemplate.exchange(
                baseUrl + BooksmarksBF.URL + "/" + con2.getId(), HttpMethod.GET, null, 
                new ParameterizedTypeReference<SimplePage<BookmarkBE>>() {})
            .getBody().getContent().size());
        
        //-- update using PUT
        b.setName("foo.bar.tar");
        exchange = restTemplate.exchange(
                baseUrl + BooksmarksBF.URL + "/" + con2.getId() + "/" + b.getId(), HttpMethod.PUT, 
                new HttpEntity<>(b), 
                BookmarkBE.class);
        assertEquals("foo.bar.tar", b.getName());
        assertEquals(1, restTemplate.exchange(
                baseUrl + BooksmarksBF.URL + "/" + con2.getId(), HttpMethod.GET, null, 
                new ParameterizedTypeReference<SimplePage<BookmarkBE>>() {})
            .getBody().getContent().size());
        
        //-- update using POST
        b.setName("foo.bar");
        exchange = restTemplate.exchange(
                baseUrl + BooksmarksBF.URL + "/" + con2.getId(), HttpMethod.POST, 
                new HttpEntity<>(b), 
                BookmarkBE.class);
        assertEquals("foo.bar", b.getName());
        assertEquals(1, restTemplate.exchange(
                baseUrl + BooksmarksBF.URL + "/" + con2.getId(), HttpMethod.GET, null, 
                new ParameterizedTypeReference<SimplePage<BookmarkBE>>() {})
            .getBody().getContent().size());
        
        final long connections = connectionDao.count();
        //-- delete again the resource
        restTemplate.delete(baseUrl + BooksmarksBF.URL + "/" + con2.getId() + "/" + b.getId());
        
        
        assertEquals(0, restTemplate.exchange(
                baseUrl + BooksmarksBF.URL + "/" + con2.getId(), HttpMethod.GET, null, 
                new ParameterizedTypeReference<SimplePage<BookmarkBE>>() {})
            .getBody().getContent().size());
        
        assertEquals(connections, connectionDao.count());
    }

}
