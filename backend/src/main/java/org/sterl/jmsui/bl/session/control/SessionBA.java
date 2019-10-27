package org.sterl.jmsui.bl.session.control;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Component;
import org.sterl.jmsui.bl.connection.model.JmsConnectionBE;
import org.sterl.jmsui.bl.connectors.api.JmsConnectorInstance;
import org.sterl.jmsui.bl.connectors.api.JmsConnectorInstanceFactory;
import org.sterl.jmsui.bl.connectors.api.SupportedConnector;
import org.sterl.jmsui.bl.connectors.ibm.IbmMqConnectorFactory;
import org.sterl.jmsui.bl.connectors.memory.MemoryQueueConnectorFactory;

@Component
class SessionBA implements Closeable {
    private static final Map<String, JmsConnectorInstanceFactory> FACTORIES = new HashMap<>();
    private static final Map<Long, JmsConnectorInstance> SESSIONS = Collections.synchronizedMap(new Hashtable<>());
    
    static {
        FACTORIES.put(IbmMqConnectorFactory.class.getName(), new IbmMqConnectorFactory());
        FACTORIES.put(MemoryQueueConnectorFactory.class.getName(), new MemoryQueueConnectorFactory());
    }

    @PreDestroy
    public void close() {
        Collection<JmsConnectorInstance> sessions = SESSIONS.values();
        SESSIONS.clear();
        sessions.forEach(t -> {
            try {
                t.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
    }
    Set<Long> openSessions() {
        return SESSIONS.keySet();
    }
    JmsConnectorInstance connect(JmsConnectionBE connection) {
        Optional<Entry<Long, JmsConnectorInstance>> storedSession = getStoredSession(connection.getId());
        JmsConnectorInstance connector;

        try {
            if (storedSession.isEmpty()) {
                connector = FACTORIES.get(connection.getType()).create(connection);
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
        JmsConnectorInstance remove = SESSIONS.remove(connectorId);
        if (remove != null) {
            try {
                remove.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
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
    public Collection<SupportedConnector> getJmsFactories() {
        return FACTORIES.entrySet().stream().map(e -> {
            return SupportedConnector.builder().id(e.getKey())
                .name(e.getValue().getName())
                .configMeta(e.getValue().getConfigMetaData())
                .build();
        }).collect(Collectors.toList());
    }
}
