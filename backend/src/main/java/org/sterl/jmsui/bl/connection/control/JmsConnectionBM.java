package org.sterl.jmsui.bl.connection.control;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.jmsui.bl.connection.dao.JmsConnectionDao;
import org.sterl.jmsui.bl.connection.model.JmsConnection;

@Service
@Transactional
public class JmsConnectionBM {
    @Autowired JmsConnectionDao connectionDao;

    public JmsConnection getWithConfig(Long id) {
        JmsConnection result = connectionDao.getOne(id);
        if (result != null) result.getConfigValues().size(); // fetch them
        return result;
    }
    public JmsConnection save(JmsConnection connection) {
        JmsConnection result = connectionDao.save(connection);
        connectionDao.flush();
        return result;
    }
}
