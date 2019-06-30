package org.sterl.jmsui.bl.session.control;

import java.io.Closeable;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;

import javax.annotation.PreDestroy;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Component;
import org.sterl.jmsui.bl.connection.model.JmsConnection;
import org.sterl.jmsui.bl.connectors.ibm.IbmMqConnector;
import org.sterl.jmsui.bl.connectors.ibm.IbmMqConnectorFactory;

@Component
class SessionBA implements Closeable {
    private static final IbmMqConnectorFactory SESSION_FACTORY = new IbmMqConnectorFactory();
    private static final Map<Long, IbmMqConnector> SESSIONS = Collections.synchronizedMap(new Hashtable<>());
    
    @PreDestroy
    public void close() {
        Collection<IbmMqConnector> sessions = SESSIONS.values();
        SESSIONS.clear();
        sessions.forEach(IbmMqConnector::close);
    }
    Set<Long> openSessions() {
        return SESSIONS.keySet();
    }
    IbmMqConnector connect(JmsConnection connection) {
        Optional<Entry<Long, IbmMqConnector>> storedSession = getStoredSession(connection.getId());
        IbmMqConnector connector;

        try {
            if (storedSession.isEmpty()) {
                connector = SESSION_FACTORY.create(connection);
                SESSIONS.put(connection.getId(), connector);
            } else {
                connector = storedSession.get().getValue();
            }
            connector.testConnection();
        } catch (Exception e) {
            SESSIONS.remove(connection.getId());
            if (e instanceof RuntimeException) throw (RuntimeException)e;
            else throw new RuntimeException("Failed to connect to " + connection, e);
        }
        return connector;
    }
    void disconnect(long connectorId) {
        IbmMqConnector remove = SESSIONS.remove(connectorId);
        if (remove != null) remove.close();
    }
    Optional<Entry<Long, IbmMqConnector>> getStoredSession(
            @NotNull long connectorId) {
        return SESSIONS.entrySet().stream()
                .filter(e -> connectorId == e.getKey().longValue())
                .findFirst();
    }
}
