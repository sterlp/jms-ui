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

    public JmsConnection get(Long id) {
        return connectionDao.getOne(id);
    }
    public JmsConnection save(JmsConnection connection) {
        return connectionDao.save(connection);
    }
}
