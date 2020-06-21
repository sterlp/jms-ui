package org.sterl.jmsui.bl.session.control;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.jms.JMSException;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sterl.jmsui.bl.connection.model.JmsConnectionBE;
import org.sterl.jmsui.bl.connectors.api.JmsConnectorInstance;
import org.sterl.jmsui.bl.connectors.control.JmsConnectorBM;

@Component
class SessionBA implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(SessionBA.class);
    private static final Map<Long, JmsConnectorInstance> SESSIONS = Collections.synchronizedMap(new Hashtable<>());
    
    @Autowired JmsConnectorBM connecrtorBM;

    @PreDestroy
    public void close() {
        Collection<JmsConnectorInstance> sessions = SESSIONS.values();
        SESSIONS.clear();
        LOG.info("Closing {} JMS sessions", sessions.size());
        sessions.forEach(t -> {
            try {
                t.close();
            } catch (IOException e) {
                // ignored
            }
        });
    }
    Set<Long> openSessions() {
        return SESSIONS.keySet();
    }
    JmsConnectorInstance connect(JmsConnectionBE connection) throws JMSException {
        Optional<Entry<Long, JmsConnectorInstance>> storedSession = getStoredSession(connection.getId());
        JmsConnectorInstance connector;

        try {
            if (storedSession.isEmpty()) {
                LOG.info("Creating new connection to {}", connection);
                // TODO check if null
                connector = connecrtorBM.getFactory(connection.getType()).get().create(connection);
                SESSIONS.put(connection.getId(), connector);
            } else {
                connector = storedSession.get().getValue();
            }
            connector.connect();
        } catch (Exception e) {
            SESSIONS.remove(connection.getId());
            if (e instanceof RuntimeException) throw (RuntimeException)e;
            else if (e instanceof JMSException) throw (JMSException)e;
            else throw new RuntimeException("Failed to connect to " + connection, e);
        }
        return connector;
    }
    void disconnect(long connectorId) {
        JmsConnectorInstance remove = SESSIONS.remove(connectorId);
        if (remove != null) {
            try {
                remove.close();
            } catch (IOException e) {
                LOG.warn("Failled to close {}.", connectorId, e);
            }
        }
    }
    Optional<Entry<Long, JmsConnectorInstance>> getStoredSession(
            @NotNull long connectorId) {
        Optional<Entry<Long, JmsConnectorInstance>> session = SESSIONS.entrySet().stream()
                .filter(e -> connectorId == e.getKey().longValue())
                .findFirst();
        if (session.isPresent() && session.get().getValue().isClosed()) {
            disconnect(session.get().getKey());
            session = Optional.empty();
        }
        return session;
    }
}
