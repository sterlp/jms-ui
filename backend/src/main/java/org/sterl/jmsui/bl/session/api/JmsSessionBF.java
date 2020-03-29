package org.sterl.jmsui.bl.session.api;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.sterl.jmsui.api.JmsMessageConverter.ToJmsHeaderResultValues;
import org.sterl.jmsui.bl.common.api.SimplePage;
import org.sterl.jmsui.bl.common.spring.JsonRestController;
import org.sterl.jmsui.bl.connection.api.JmsConnectionBF;
import org.sterl.jmsui.bl.connection.api.model.JmsConnectionView;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource.Type;
import org.sterl.jmsui.bl.session.control.JmsSessionBM;

@Transactional
@JsonRestController(JmsSessionBF.BASE_URL)
public class JmsSessionBF {

    public static final String BASE_URL = "/api/sessions";

    @Autowired JmsSessionBM jmsSessionBM;
    @Autowired JmsConnectionBF connectionBF;

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
    
    @GetMapping("/{connectorId}/resources")
    public List<JmsResource> listResources(@PathVariable long connectorId) throws JMSException {
        return jmsSessionBM.listResources(connectorId);
    }
    
    @PostMapping("/{connectorId}/queue/depths")
    public Map<String, Integer> queuesDepths(@PathVariable long connectorId, @RequestBody List<String> queues) throws JMSException {
        return jmsSessionBM.queueDepths(connectorId, queues);
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
    
    @PostMapping("/{connectorId}/message")
    public void sendMessage(@PathVariable long connectorId, 
            @RequestBody @Valid SendJmsMessageCommand message) throws JMSException {
        jmsSessionBM.sendMessage(connectorId, message.getDestination(), message.getDestinationType(), message.getBody(), message.getHeader());
    }
    
    @PostMapping("/{connectorId}/message/{destination}")
    public void sendMessage(@PathVariable long connectorId, @PathVariable String destination, 
            @RequestBody @Valid SendJmsMessageCommand message) throws JMSException {
        message.setDestination(destination);
        this.sendMessage(connectorId, message);
    }
    
    @GetMapping("/{connectorId}/message/{destination}")
    public JmsResultMessage receiveMessage(@PathVariable long connectorId, 
            @PathVariable String destination,
            @RequestParam(required = false, defaultValue = "QUEUE") Type type,
            @RequestParam(required = false) Long timeout) throws JMSException {
        final Message msg = jmsSessionBM.receive(connectorId, destination, type, timeout);
        return new JmsResultMessage(getJmsBody(msg), ToJmsHeaderResultValues.INSTANCE.convert(msg));
    }
    
    private static String getJmsBody(Message msg) throws JMSException {
        if (msg == null) return null;
        if (msg instanceof TextMessage) {
            return ((TextMessage)msg).getText();
        } else if (msg instanceof BytesMessage) {
            BytesMessage message = (BytesMessage)msg;
            byte[] bytes = new byte[(int) message.getBodyLength()];
            message.readBytes(bytes);
            return new String(bytes, Charset.forName("UTF-8"));
        } else {
            throw new IllegalStateException("Result message of type " + msg.getClass() + " isn't supported.");
        }
    }
}
