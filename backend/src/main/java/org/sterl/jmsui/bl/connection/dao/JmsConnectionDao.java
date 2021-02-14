package org.sterl.jmsui.bl.connection.dao;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.sterl.jmsui.bl.connection.api.model.JmsConnectionView;
import org.sterl.jmsui.bl.connection.model.JmsConnectionBE;

public interface JmsConnectionDao extends JpaRepository<JmsConnectionBE, Long> {

    public List<JmsConnectionBE> findByType(String type);
    public Page<JmsConnectionView> findViewBy(Pageable page);
    
    public List<JmsConnectionView> findByIdIn(Collection<Long> ids);
}
