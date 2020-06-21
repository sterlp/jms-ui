package org.sterl.jmsui.bl.session.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueBrowser;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jms.core.BrowserCallback;
import org.springframework.jms.core.JmsTemplate;
import org.sterl.jmsui.api.JmsHeaderRequestValues;
import org.sterl.jmsui.bl.common.spring.BusinessManager;
import org.sterl.jmsui.bl.connection.control.JmsConnectionBM;
import org.sterl.jmsui.bl.connectors.api.JmsConnectorInstance;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource.Type;

@BusinessManager
public class JmsSessionBM {
    private static final Logger LOG = LoggerFactory.getLogger(JmsSessionBM.class);
    @Autowired JmsConnectionBM connectionBM;
    @Autowired SessionBA sessionBA;

    public Collection<Long> openSessions() {
        return sessionBA.openSessions();
    }
    public void sendMessage(long connectorId, String destination, Type jmsType, String message, JmsHeaderRequestValues header) throws JMSException {
        JmsConnectorInstance c = getOrConnect(connectorId);
        c.sendMessage(destination, jmsType, message, header);
    }
    public Message receive(long connectorId, String destination, Type jmsType, Long timeout) throws JMSException {
        JmsConnectorInstance connector = getOrConnect(connectorId);
        return connector.receive(destination, jmsType, timeout);
    }
    public List<JmsResource> listQueues(long connectorId) throws JMSException {
        final JmsConnectorInstance connector = getOrConnect(connectorId);
        return connector.listQueues();
    }
    public List<JmsResource> listTopics(long connectorId) throws JMSException {
        final JmsConnectorInstance connector = getOrConnect(connectorId);
        return connector.listTopics();
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
    public Map<String, Object> getQueueInfo(long connectorId, String destination) throws JMSException {
        JmsConnectorInstance connector = getOrConnect(connectorId);
        return connector.getQueueInformation(destination);
    }
    public Map<String, Object> getTopicInfo(long connectorId, String destination) throws JMSException {
        JmsConnectorInstance connector = getOrConnect(connectorId);
        return connector.getTopicInformation(destination);
    }
    
    public Page<Message> browseQueue(long connectorId, String destination, Pageable page) throws JMSException {
        final JmsConnectorInstance c = getOrConnect(connectorId);
        final ConnectionFactory cf = c.getConnectionFactory();
        if (page == null) page = PageRequest.of(0, 100);
        final List<Message> content = new ArrayList<>();
        
        final long skip = page.getPageSize() * page.getPageNumber();
        final long toReturnCount = page.getPageSize();
        final JmsTemplate t = new JmsTemplate(cf);
        long total = t.browse(destination, new BrowserCallback<Long>() {
            @Override
            public Long doInJms(Session session, QueueBrowser browser) throws JMSException {
                long total = 0;
                final Enumeration<Message> e = browser.getEnumeration();
                while (e.hasMoreElements()) {
                    Message m = e.nextElement();
                    if (total >= skip && toReturnCount > content.size()) {
                        content.add(m);
                    }
                    total++;
                }
                return total;
            }
        });
        return new PageImpl<>(content, page, total);
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
}
