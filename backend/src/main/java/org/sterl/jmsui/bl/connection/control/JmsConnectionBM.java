package org.sterl.jmsui.bl.connection.control;

import org.springframework.beans.factory.annotation.Autowired;
import org.sterl.jmsui.bl.common.spring.BusinessManager;
import org.sterl.jmsui.bl.connection.dao.JmsConnectionDao;
import org.sterl.jmsui.bl.connection.model.JmsConnectionBE;

@BusinessManager
public class JmsConnectionBM {
    @Autowired JmsConnectionDao connectionDao;

    public JmsConnectionBE getWithConfig(Long id) {
        JmsConnectionBE result = connectionDao.getOne(id);
        if (result != null) result.getConfigValues().size(); // fetch them to be sure 
        return result;
    }
    public JmsConnectionBE save(JmsConnectionBE connection) {
        JmsConnectionBE result = connectionDao.save(connection);
        connectionDao.flush();
        return result;
    }
    public void delete(Long id) {
        connectionDao.deleteById(id);
    }
}
