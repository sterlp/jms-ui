package org.sterl.jmsui.bl.connectors.activemq.control;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.DestinationSource;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sterl.jmsui.api.JmsHeaderRequestValues;
import org.sterl.jmsui.bl.common.helper.JmsUtil;
import org.sterl.jmsui.bl.connectors.activemq.model.ActiveMqConnectorConfigBE;
import org.sterl.jmsui.bl.connectors.api.JmsConnectorInstance;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource.Type;
import org.sterl.jmsui.bl.connectors.api.model.JmsResourceComperator;
import org.sterl.jmsui.bl.connectors.common.JmsHeaderUtil;

import lombok.RequiredArgsConstructor;

/**
 * https://github.com/apache/activemq/tree/master/activemq-unit-tests/src/test/java/org/apache/activemq 
 */
@RequiredArgsConstructor
public class ActiveMqConnector implements JmsConnectorInstance {
    private static final Logger LOG = LoggerFactory.getLogger(ActiveMqConnector.class);
    
    private static final String SYSTEM_PREFIX = "ActiveMQ.";

    private final ActiveMQConnectionFactory cf;
    private final ActiveMqConnectorConfigBE config;
    private ActiveMQConnection connection;

    @Override
    public void close() {
        JmsUtil.close(connection);
        connection = null;
    }

    @Override
    public synchronized void connect() throws JMSException {
        close();
        connection = (ActiveMQConnection)cf.createConnection();
        connection.start();
        connection.setExceptionListener((e) -> {
            LOG.warn("Lost connection with error {}", e.getMessage());
            this.close();
        });
    }

    @Override
    public List<JmsResource> listQueues() throws JMSException {
        final DestinationSource destinationSource = connection.getDestinationSource();
        final Set<ActiveMQQueue> queues = destinationSource.getQueues();
        final List<JmsResource> result = new ArrayList<>(queues.size());
        for (ActiveMQQueue queue : queues) {
            if (filterDestinationNames(queue.getQueueName())) {
                result.add(JmsResource.builder()
                        .name(queue.getQueueName())
                        .type(Type.QUEUE)
                        .vendorType(queue.getQualifiedName())
                        .build());
            }
        }
        JmsResourceComperator.sort(result);
        return result;
    }

    @Override
    public List<JmsResource> listTopics() throws JMSException {
        final DestinationSource destinationSource = connection.getDestinationSource();
        final Set<ActiveMQTopic> topics = destinationSource.getTopics();
        final List<JmsResource> result = new ArrayList<>(topics.size());
        for (ActiveMQTopic t : topics) {
            if (filterDestinationNames(t.getTopicName())) {
                result.add(JmsResource.builder()
                        .name(t.getTopicName())
                        .type(Type.TOPIC)
                        .vendorType(t.getQualifiedName())
                        .build());
            }
        }
        JmsResourceComperator.sort(result);
        return result;
    }

    @Override
    public Map<String, Object> getQueueInformation(String queueName) throws JMSException {
        return readStatistic("ActiveMQ.Statistics.Destination." + queueName);
    }

    @Override
    public Map<String, Object> getTopicInformation(String destination) {
        return new HashMap<>();
    }

    @Override
    public void sendMessage(String destination, Type jmsType, String message, JmsHeaderRequestValues header)
            throws JMSException {
        
        try (Session s = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
            final Destination d = jmsType == Type.TOPIC ? s.createTopic(destination) : s.createQueue(destination);
            final TextMessage jmsMessage = message == null ? s.createTextMessage() : s.createTextMessage(message);
            
            final MessageProducer producer = s.createProducer(d);
            if (header != null && header.getJMSPriority() != null) producer.setPriority(header.getJMSPriority());
            JmsHeaderUtil.setMeassageHeader(header, jmsMessage);
            producer.send(jmsMessage);
        }
    }

    @Override
    public Message receive(String destination, Type jmsType, Long timeout) throws JMSException {
        try (Session s = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
            final Destination d = jmsType == Type.TOPIC ? s.createTopic(destination) : s.createQueue(destination);
            return s.createConsumer(d).receive(timeout == null ? config.getDefaultTimeout() : timeout);
        }
    }

    @Override
    public Integer getQueueDepth(String queueName) throws JMSException {
        Number result = (Number)readStatistic("ActiveMQ.Statistics.Destination." + queueName).get("size");
        return result == null ? null : result.intValue();
    }
    
    private Map<String, Object> readStatistic(String type) throws JMSException {
        final Map<String, Object> result = new TreeMap<>();
        // http://www.rajdavies.today/2009/10/query-statistics-for-apache-activemq.html
        // https://activemq.apache.org/statisticsplugin
        if (config.hasStatisticsPlugin()) {
            try (Session s = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
                TemporaryQueue replyTo = s.createTemporaryQueue();
                MessageConsumer consumer = s.createConsumer(replyTo);
                Queue query = s.createQueue(type);
                MessageProducer producer = s.createProducer(query);
                Message msg = s.createMessage();
                msg.setJMSReplyTo(replyTo);
                producer.send(msg);
                
                MapMessage reply = (MapMessage) consumer.receive(1500);
                if (reply != null) {
                    for (Enumeration<?> e = reply.getMapNames(); e.hasMoreElements();) {
                        final String name = e.nextElement().toString();
                        result.put(name, reply.getObject(name));
                    }
                }
            }
        }
        return result;
    }
    
    private boolean filterDestinationNames(String name) {
        if (name == null || name.startsWith(SYSTEM_PREFIX)) return false;
        else return true;
    }

    @Override
    public synchronized boolean isClosed() {
        return connection == null || connection.isClosing() || connection.isClosed();
    }

    @Override
    public ConnectionFactory getConnectionFactory() {
        return cf;
    }
}
