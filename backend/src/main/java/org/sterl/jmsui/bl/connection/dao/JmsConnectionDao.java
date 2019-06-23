package org.sterl.jmsui.bl.connection.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.sterl.jmsui.bl.connection.model.JmsConnection;

@RepositoryRestResource(collectionResourceRel = "jmsConnections", path = "jms-connections")
public interface JmsConnectionDao extends JpaRepository<JmsConnection, Long> {

    public List<JmsConnection> findByType(String type);
}
