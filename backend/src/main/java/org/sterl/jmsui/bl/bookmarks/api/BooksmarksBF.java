package org.sterl.jmsui.bl.bookmarks.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.sterl.jmsui.bl.bookmarks.dao.BookmarksDao;
import org.sterl.jmsui.bl.bookmarks.model.BookmarkBE;
import org.sterl.jmsui.bl.common.api.SimplePage;
import org.sterl.jmsui.bl.common.spring.JsonRestController;
import org.sterl.jmsui.bl.connection.control.JmsConnectionBM;

@JsonRestController(BooksmarksBF.URL)
public class BooksmarksBF {
    public static final String URL = "/api/jms/bookmarks";
 
    @Autowired private BookmarksDao bookmarksDao;
    @Autowired private JmsConnectionBM jmsConnectionBM;
    
    @GetMapping("/{connectorId}")
    public SimplePage<BookmarkBE> get(@PathVariable long connectorId, Pageable page) {
        Page<BookmarkBE> result = bookmarksDao.findByConnectionId(connectorId, page);
        return new SimplePage<>(result);
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

    @DeleteMapping("/{connectorId}/{id}")
    @Transactional
    public void delete(@PathVariable long connectorId, @PathVariable Long id) {
        bookmarksDao.deleteById(id);
    }
}
