package org.sterl.jmsui.bl.connectors.api;

import java.io.Closeable;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;

import org.springframework.jms.core.JmsTemplate;
import org.sterl.jmsui.api.JmsHeaderRequestValues;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource;

/**
 * Represents an active instance of an JMS connector.
 */
public interface JmsConnectorInstance extends Closeable {
    /**
     * Only if supported ...
     * 
     * @return spring {@link JmsTemplate} for more use cases later on
     */
    JmsTemplate getJmsTemplate();
    void testConnection() throws JMSException;

    List<JmsResource> listResources();

    void sendMessage(String destination, String message, JmsHeaderRequestValues header);
    /**
     * Receive a JMS message for a given destination.
     * 
     * @param destination the end point to listen too
     * @param timeout the optional timeout // maybe <code>null</code>
     * @return the message received // maybe <code>null</code>
     */
    Message receive(String destination, Long timeout);
}
