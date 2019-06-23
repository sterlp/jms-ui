package org.sterl.jmsui.bl.connection.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.sterl.jmsui.bl.connection.model.ConfigValue;

@RepositoryRestResource(collectionResourceRel = "configValues", path = "jms-config-values")
public interface ConfigValueDao extends JpaRepository<ConfigValue, Long> {
}
