package org.sterl.jmsui.bl.connection.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.sterl.jmsui.bl.connection.model.ConfigValueBE;

public interface ConfigValueDao extends JpaRepository<ConfigValueBE, Long> {
    
    public List<ConfigValueBE> getByConnectionId(long id);
}
