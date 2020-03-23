package org.sterl.jmsui.bl.session.control;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.sterl.jmsui.api.JmsHeaderRequestValues;
import org.sterl.jmsui.bl.common.spring.BusinessManager;
import org.sterl.jmsui.bl.connection.control.JmsConnectionBM;
import org.sterl.jmsui.bl.connectors.api.JmsConnectorInstance;
import org.sterl.jmsui.bl.connectors.api.SupportedConnector;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource;

@BusinessManager
public class JmsSessionBM {
    private static final Logger LOG = LoggerFactory.getLogger(JmsSessionBM.class);
    @Autowired JmsConnectionBM connectionBM;
    @Autowired SessionBA sessionBA;

    public Collection<Long> openSessions() {
        return sessionBA.openSessions();
    }
    public void sendMessage(long connectorId, String destination, String message, JmsHeaderRequestValues header) throws JMSException {
        JmsConnectorInstance c = getOrConnect(connectorId);
        c.sendMessage(destination, message, header);
    }
    public Message receive(long connectorId, String destination, Long timeout) throws JMSException {
        JmsConnectorInstance connector = getOrConnect(connectorId);
        return connector.receive(destination, timeout);
    }
    @Cacheable("jms-resources")
    public List<JmsResource> listResources(long connectorId) throws JMSException {
        final JmsConnectorInstance connector = getOrConnect(connectorId);
        return connector.listResources();
    }
    public Map<String, Integer> queueDepths(long connectorId, List<String> queues) throws JMSException {
        final JmsConnectorInstance connector = getOrConnect(connectorId);
        final Map<String, Integer> result = new LinkedHashMap<>();
        for (String queueName : queues) {
            // do the request only once ...
            if (!result.containsKey(queueName)) {                
                result.put(queueName, connector.getQueueDepth(queueName));
            }
        }
        return result;
    }

    /**
     * Creates a connections to the given connector and returns all open sessions id's.
     * @param connectorId the connector to connect to
     * @return the {@link Set} of open sessions
     * @throws JMSException if the connection failed.
     */
    public JmsConnectorInstance connect(long connectorId) throws JMSException {
        return sessionBA.connect(connectionBM.getWithConfig(connectorId));
    }
    @CacheEvict("jms-resources")
    public Set<Long> disconnect(long connectorId) {
        sessionBA.disconnect(connectorId);
        return sessionBA.openSessions();
    }
    
    private JmsConnectorInstance getOrConnect(long connectorId) throws JMSException {
        Optional<Entry<Long, JmsConnectorInstance>> storedSession = sessionBA.getStoredSession(connectorId);
        JmsConnectorInstance result;
        if (storedSession.isEmpty()) {
            result = sessionBA.connect(connectionBM.getWithConfig(connectorId));
        } else {
            result = storedSession.get().getValue();
        }
        return result;
    }
    public Collection<SupportedConnector> getSupportedJmsFactories() {
        return sessionBA.getJmsFactories();
    }
}
