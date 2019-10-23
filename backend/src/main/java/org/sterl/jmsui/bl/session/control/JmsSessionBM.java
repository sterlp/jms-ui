package org.sterl.jmsui.bl.session.control;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import javax.jms.JMSException;
import javax.jms.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.sterl.jmsui.api.JmsHeaderRequestValues;
import org.sterl.jmsui.bl.common.spring.BusinessManager;
import org.sterl.jmsui.bl.connection.control.JmsConnectionBM;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource;
import org.sterl.jmsui.bl.connectors.ibm.IbmMqConnector;

@BusinessManager
public class JmsSessionBM {

    @Autowired JmsConnectionBM connectionBM;
    @Autowired SessionBA sessionBA;

    public Collection<Long> openSessions() {
        return sessionBA.openSessions();
    }
    public void sendMessage(long connectorId, String destination, Object message, JmsHeaderRequestValues header) {
        final JmsTemplate template = getOrConnect(connectorId).getJmsTemplate();
        
        if (header.getJMSPriority() != null) template.setPriority(header.getJMSPriority());
        else template.setPriority(4);
        
        template.convertAndSend(destination, message, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws JMSException {
                setMeassageHeader(header, message);
                return message;
            }
        });
    }
    public Message receive(long connectorId, String destination, Long timeout) {
        JmsTemplate jmsTemplate = getOrConnect(connectorId).getJmsTemplate();
        if (timeout != null) jmsTemplate.setReceiveTimeout(timeout);
        return jmsTemplate.receive(destination);
    }
    public List<JmsResource> listQueues(long connectorId) {
        IbmMqConnector connector = getOrConnect(connectorId);
        return connector.listQueues();
    }
    public long connect(long connectorId) {
        Optional<Entry<Long, IbmMqConnector>> storedSession = sessionBA.getStoredSession(connectorId);
        long result;
        if (storedSession.isPresent()) {
            result = connectorId;
        } else {
            sessionBA.connect(connectionBM.getWithConfig(connectorId));
            result = connectorId;
        }
        return result;
    }
    public void disconnect(long connectorId) {
        sessionBA.disconnect(connectorId);
    }
    
    private IbmMqConnector getOrConnect(long connectorId) {
        Optional<Entry<Long, IbmMqConnector>> storedSession = sessionBA.getStoredSession(connectorId);
        IbmMqConnector result;
        if (storedSession.isEmpty()) {
            result = sessionBA.connect(connectionBM.getWithConfig(connectorId));
        } else {
            result = storedSession.get().getValue();
        }
        return result;
    }
    
    
    private void setMeassageHeader(JmsHeaderRequestValues header, Message message) throws JMSException {
        if (header != null) {
            if (header.getJMSExpiration() != null) message.setJMSExpiration(header.getJMSExpiration());
            if (header.getJMSCorrelationID() != null) message.setJMSCorrelationID(header.getJMSCorrelationID());
            if (header.getJMSDeliveryMode() != null) message.setJMSDeliveryMode(header.getJMSDeliveryMode());
            if (header.getJMSMessageID() != null) message.setJMSMessageID(header.getJMSMessageID());
            if (header.getJMSPriority() != null) message.setJMSPriority(header.getJMSPriority());
            if (header.getJMSTimestamp() != null) message.setJMSTimestamp(header.getJMSTimestamp());
            if (header.getJMSType() != null) message.setJMSType(header.getJMSType());
            if (header.getProperties() != null && !header.getProperties().isEmpty()) {
                for (Entry<String, Object> headerValue : header.getProperties().entrySet()) {
                    if (headerValue.getValue() == null) {
                        message.setObjectProperty(headerValue.getKey(), null);
                    } else if (headerValue.getValue() instanceof Boolean) {
                        message.setBooleanProperty(headerValue.getKey(), (Boolean)headerValue.getValue());
                    } if (headerValue.getValue() instanceof Double) {
                        message.setDoubleProperty(headerValue.getKey(), (Double)headerValue.getValue());
                    } if (headerValue.getValue() instanceof String) {
                        message.setStringProperty(headerValue.getKey(), (String)headerValue.getValue());
                    } else {
                        message.setObjectProperty(headerValue.getKey(), null);
                    }
                }
            }
        }
    }
}
