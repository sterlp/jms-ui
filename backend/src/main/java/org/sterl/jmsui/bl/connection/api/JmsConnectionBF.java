package org.sterl.jmsui.bl.connection.api;

import java.util.Collection;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.sterl.jmsui.bl.common.api.SimplePage;
import org.sterl.jmsui.bl.common.spring.JsonRestController;
import org.sterl.jmsui.bl.connection.api.ConnectionConverter.ToJmsConnection;
import org.sterl.jmsui.bl.connection.api.ConnectionConverter.ToJmsConnectionDetails;
import org.sterl.jmsui.bl.connection.api.model.JmsConnectionDetails;
import org.sterl.jmsui.bl.connection.api.model.JmsConnectionView;
import org.sterl.jmsui.bl.connection.control.JmsConnectionBM;
import org.sterl.jmsui.bl.connection.dao.JmsConnectionDao;
import org.sterl.jmsui.bl.connection.model.JmsConnectionBE;
import org.sterl.jmsui.bl.session.control.JmsSessionBM;

@JsonRestController(JmsConnectionBF.URL)
public class JmsConnectionBF {
    public static final String URL = "/api/jms/connections";
    
    @Autowired JmsConnectionDao jmsConnectionDao;
    @Autowired JmsConnectionBM jmsConnectionBM;
    @Autowired JmsSessionBM jmsSessionsBM;
    
    @GetMapping
    public SimplePage<JmsConnectionView> list(
            @RequestParam(name = "ids", required = false) Collection<Long> ids, Pageable page) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (ids == null || ids.isEmpty()) {            
            return new SimplePage<>(jmsConnectionDao.findViewBy(page));
        } else {
            List<JmsConnectionView> found = jmsConnectionDao.findByIdIn(ids);
            return SimplePage.of(found);
        }
    }
    
    @GetMapping("/{id}")
    public JmsConnectionDetails get(@PathVariable Long id) {
        return ToJmsConnectionDetails.INSTANCE.convert(jmsConnectionBM.getWithConfig(id));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        jmsConnectionBM.delete(id);
        jmsSessionsBM.disconnect(id);
    }
    
    @PostMapping
    public JmsConnectionDetails save(@RequestBody @Valid JmsConnectionDetails request) {
        return saveRequest(request);
    }
    @PutMapping("/{id}")
    public JmsConnectionDetails update(@PathVariable long id, 
            @RequestBody @Valid JmsConnectionDetails request) {
        request.setId(id);
        return saveRequest(request);
    }

    JmsConnectionDetails saveRequest(JmsConnectionDetails request) {
        JmsConnectionBE save;
        if (request.getId() == null) {
            save = new JmsConnectionBE();
        } else {
            save = jmsConnectionBM.getWithConfig(request.getId());
        }
        ToJmsConnection.setValues(request, save);
        save = jmsConnectionBM.save(save);
        request.setVersion(save.getVersion());
        request.setId(save.getId());
        return request;
    }
}
