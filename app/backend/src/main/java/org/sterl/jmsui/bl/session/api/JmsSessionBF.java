package org.sterl.jmsui.bl.session.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.JMSException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.sterl.jmsui.bl.common.api.SimplePage;
import org.sterl.jmsui.bl.common.spring.JsonRestController;
import org.sterl.jmsui.bl.connection.api.JmsConnectionBF;
import org.sterl.jmsui.bl.connection.api.model.JmsConnectionView;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource;
import org.sterl.jmsui.bl.session.control.JmsSessionBM;

@JsonRestController(JmsSessionBF.BASE_URL)
public class JmsSessionBF {


    public static final String BASE_URL = "/api/sessions";

    @Autowired private JmsSessionBM jmsSessionBM;
    @Autowired private JmsConnectionBF connectionBF;

    /**
     * Opens a session and returns the currently open sessions back.
     * 
     * @param connectorId the connection to open
     * @return the open sessions
     * @throws JMSException 
     */
    @PostMapping("/{connectorId}")
    public SimplePage<JmsConnectionView> open(@PathVariable long connectorId) throws JMSException {
        jmsSessionBM.connect(connectorId);
        return connectionBF.list(jmsSessionBM.openSessions(), null);
    }
    
    @GetMapping("/{connectorId}/queues")
    public List<JmsResource> listQueues(@PathVariable long connectorId) throws JMSException {
        return jmsSessionBM.listQueues(connectorId);
    }
    @GetMapping("/{connectorId}/topics")
    public List<JmsResource> listTopics(@PathVariable long connectorId) throws JMSException {
        return jmsSessionBM.listTopics(connectorId);
    }
    
    @PostMapping("/{connectorId}/queue/depths")
    public Map<String, Integer> queuesDepths(@PathVariable long connectorId, @RequestBody List<String> queues) throws JMSException {
        return jmsSessionBM.queueDepths(connectorId, queues);
    }
    @GetMapping("/{connectorId}/queues/{destination}/info")
    public Map<String, Object> queueInfo(@PathVariable long connectorId, 
            @PathVariable String destination) throws JMSException {

        return jmsSessionBM.getQueueInfo(connectorId, destination);
    }
    @GetMapping("/{connectorId}/topics/{destination}/info")
    public Map<String, Object> topicInfo(@PathVariable long connectorId, 
            @PathVariable String destination) throws JMSException {

        return jmsSessionBM.getTopicInfo(connectorId, destination);
    }

    /**
     * Closes the given session and returns all still open ones.
     * @param connectorId the session id to close
     * @return the still open sessions
     */
    @DeleteMapping("/{connectorId}")
    public SimplePage<JmsConnectionView> close(@PathVariable long connectorId) {
        Set<Long> sessions = jmsSessionBM.disconnect(connectorId);
        SimplePage<JmsConnectionView> result;
        if (sessions == null || sessions.isEmpty()) {
            result = SimplePage.of(new ArrayList<>());
        } else {
            result = connectionBF.list(sessions, null);
        }
        return result;
    }
}
