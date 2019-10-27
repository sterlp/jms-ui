package org.sterl.jmsui.bl.session.control;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.jms.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.sterl.jmsui.api.JmsHeaderRequestValues;
import org.sterl.jmsui.bl.common.spring.BusinessManager;
import org.sterl.jmsui.bl.connection.control.JmsConnectionBM;
import org.sterl.jmsui.bl.connectors.api.JmsConnectorInstance;
import org.sterl.jmsui.bl.connectors.api.SupportedConnector;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource;

@BusinessManager
public class JmsSessionBM {

    @Autowired JmsConnectionBM connectionBM;
    @Autowired SessionBA sessionBA;

    public Collection<Long> openSessions() {
        return sessionBA.openSessions();
    }
    public void sendMessage(long connectorId, String destination, String message, JmsHeaderRequestValues header) {
        JmsConnectorInstance c = getOrConnect(connectorId);
        c.sendMessage(destination, message, header);
    }
    public Message receive(long connectorId, String destination, Long timeout) {
        JmsConnectorInstance connector = getOrConnect(connectorId);
        return connector.receive(destination, timeout);
    }
    public List<JmsResource> listQueues(long connectorId) {
        JmsConnectorInstance connector = getOrConnect(connectorId);
        return connector.listResources();
    }
    /**
     * Creates a connections to the given connector and returns all open sessions id's.
     * @param connectorId the connector to connect to
     * @return the {@link Set} of open sessions
     */
    public Set<Long> connect(long connectorId) {
        Optional<Entry<Long, JmsConnectorInstance>> storedSession = sessionBA.getStoredSession(connectorId);
        if (!storedSession.isPresent()) {
            sessionBA.connect(connectionBM.getWithConfig(connectorId));
        }
        return sessionBA.openSessions();
    }
    public Set<Long> disconnect(long connectorId) {
        sessionBA.disconnect(connectorId);
        return sessionBA.openSessions();
    }
    
    private JmsConnectorInstance getOrConnect(long connectorId) {
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
