package org.sterl.jmsui.bl.connection.api;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.sterl.jmsui.bl.common.spring.JsonRestController;
import org.sterl.jmsui.bl.connection.api.ConnectionConverter.ToJmsConnection;
import org.sterl.jmsui.bl.connection.api.model.JmsConnectionDetails;
import org.sterl.jmsui.bl.connection.control.JmsConnectionBM;
import org.sterl.jmsui.bl.connection.model.JmsConnection;

@JsonRestController("api/jms/connections")
public class JmsConnectionBF {
    @Autowired JmsConnectionBM jmsConnectionBM;
    
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
        JmsConnection jmsConnection;
        if (request.getId() == null) {
            jmsConnection = new JmsConnection();
        } else {
            jmsConnection = jmsConnectionBM.get(request.getId());
        }
        ToJmsConnection.setValues(request, jmsConnection);
        JmsConnection save = jmsConnectionBM.save(jmsConnection);
        request.setVersion(save.getVersion());
        request.setId(save.getId());
        return request;
    }
}
