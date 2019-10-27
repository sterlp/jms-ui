package org.sterl.jmsui.bl.bookmarks.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.sterl.jmsui.bl.bookmarks.model.BookmarkBE;

public interface BookmarksDao extends JpaRepository<BookmarkBE, Long> {

    Page<BookmarkBE> findByConnectionId(long connectorId, Pageable page);

}
