package org.sterl.jmsui.bl.connectors.api;

import java.io.Closeable;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;

import org.sterl.jmsui.api.JmsHeaderRequestValues;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource.Type;

/**
 * Represents an active instance of an JMS connector.
 */
public interface JmsConnectorInstance extends Closeable {
    void connect() throws JMSException;

    List<JmsResource> listResources() throws JMSException;

    void sendMessage(String destination, Type jmsType, String message, JmsHeaderRequestValues header) throws JMSException;
    
    int getQueueDepth(String queueName) throws JMSException;
    
    /**
     * Receive a JMS message for a given destination.
     * 
     * @param destination the end point to listen too
     * @param timeout the optional timeout // maybe <code>null</code>
     * @return the message received // maybe <code>null</code>
     */
    Message receive(String destination, Type jmsType, Long timeout) throws JMSException;
    
    boolean isClosed();
}
