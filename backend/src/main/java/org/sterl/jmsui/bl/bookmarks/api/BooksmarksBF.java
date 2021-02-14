package org.sterl.jmsui.bl.bookmarks.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.sterl.jmsui.bl.bookmarks.dao.BookmarksDao;
import org.sterl.jmsui.bl.bookmarks.model.BookmarkBE;
import org.sterl.jmsui.bl.common.api.SimplePage;
import org.sterl.jmsui.bl.common.spring.JsonRestController;
import org.sterl.jmsui.bl.connection.control.JmsConnectionBM;

@JsonRestController(BooksmarksBF.URL)
public class BooksmarksBF {
    public static final String URL = "/api/bookmarks";
 
    @Autowired private BookmarksDao bookmarksDao;
    @Autowired private JmsConnectionBM jmsConnectionBM;
    
    @GetMapping
    public SimplePage<BookmarkBE> getByConnectorId(@RequestParam(required = true) long connectorId, Pageable page) {
        return new SimplePage<>(bookmarksDao.findByConnectionId(connectorId, page));
    }
    
    @GetMapping("/{id}")
    public BookmarkBE get(@PathVariable long id) {
        return bookmarksDao.findById(id).orElseGet(null);
    }
    
    @PostMapping("/{connectorId}")
    @Transactional
    public BookmarkBE create(@PathVariable long connectorId, @RequestBody BookmarkBE toSave) {
        return update(connectorId, toSave.getId(), toSave);
    }
    
    @PostMapping("/{connectorId}/{id}")
    @Transactional
    public BookmarkBE update(
            @PathVariable long connectorId, @PathVariable Long id, 
            @RequestBody BookmarkBE toSave) {
        toSave.setId(id);
        toSave.setConnection(jmsConnectionBM.getWithConfig(connectorId));
        return bookmarksDao.save(toSave);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable Long id) {
        bookmarksDao.deleteById(id);
    }
}
