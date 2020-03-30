package org.sterl.jmsui.bl.connectors.ibm;

import static org.sterl.jmsui.bl.connectors.util.JmsHeaderUtil.getOrDefault;
import static org.sterl.jmsui.bl.connectors.util.JmsHeaderUtil.setMeassageHeader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sterl.jmsui.api.JmsHeaderRequestValues;
import org.sterl.jmsui.bl.common.helper.StopWatch;
import org.sterl.jmsui.bl.connectors.api.JmsConnectorInstance;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource.Type;
import org.sterl.jmsui.bl.connectors.api.model.JmsResourceComperator;
import org.sterl.jmsui.bl.connectors.exception.UnknownJmsException;
import org.sterl.jmsui.bl.connectors.ibm.common.IbmResourceHelper;
import org.sterl.jmsui.bl.connectors.ibm.converter.IbmConverter.ToJmsResourceType;
import org.sterl.jmsui.bl.connectors.ibm.model.QTypes;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.CMQCFC;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.pcf.PCFException;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFMessageAgent;
import com.ibm.msg.client.jms.JmsConnectionFactory;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * 
 * Links:
 * https://github.com/spring-cloud/spring-cloud-stream-binder-ibm-mq/blob/master/src/main/java/org/springframework/cloud/stream/binder/jms/ibmmq/IBMMQRequests.java
 * https://www.ibm.com/support/knowledgecenter/SSFKSJ_7.5.0/com.ibm.mq.dev.doc/q030730_.htm
 */
public class IbmMqConnector implements JmsConnectorInstance {
    private static final Logger LOG = LoggerFactory.getLogger(IbmMqConnector.class);
    
    private final Integer DEFAULT_PRIO = Integer.valueOf(4);
    private static final String[] SYSTEM_PREFIXES = {"LOOPBACK", "SYSTEM."};
    private static final JmsResourceComperator RESOURCE_CMP = new JmsResourceComperator();
    
    private final String queueManagerName;
    @Getter(AccessLevel.PACKAGE)
    private final JmsConnectionFactory connectionFactory;
    
    private final long defaultTimeoutInMs;

    @Getter(value = AccessLevel.PACKAGE)
    private final Hashtable<String, Object> config;

    private final StopWatch LOCK = new StopWatch();
    // MQQueueManager is NOT THREAD SAVE!
    protected volatile MQQueueManager ibmMqManager;
    protected volatile PCFMessageAgent agent;
    
    public IbmMqConnector(String queueManagerName, 
            Long defaultTimeoutInMs,
            JmsConnectionFactory connectionFactory, Hashtable<String, Object> config) {
        this.queueManagerName = queueManagerName;
        this.connectionFactory = connectionFactory;
        this.config = config;
        this.defaultTimeoutInMs = defaultTimeoutInMs == null ? 3000 : defaultTimeoutInMs;
    }

    @Override
    public void sendMessage(String destination, Type jmsType, String message, JmsHeaderRequestValues header) {
        try (JMSContext c = connectionFactory.createContext()) {
            final Destination d = jmsType == Type.TOPIC ? c.createTopic(destination) : c.createQueue(destination);
            final TextMessage m = message == null ? c.createTextMessage() : c.createTextMessage(message);
            setMeassageHeader(header, m);
            c.createProducer()
             .setPriority(getOrDefault(header.getJMSPriority(), DEFAULT_PRIO))
             .send(d, m);
            
        }
    }
    @Override
    public Message receive(String destination, Type jmsType, Long timeout) {
        Message result;
        try (JMSContext c = connectionFactory.createContext()) {
            final Destination d = jmsType == Type.TOPIC ? c.createTopic(destination) : c.createQueue(destination);
            try (JMSConsumer consumer = c.createConsumer(d)) {
                result = consumer.receive(getOrDefault(timeout, defaultTimeoutInMs));
                c.acknowledge();
            }
        }
        return result;
    }
    
    public int getQueueDepth(String queueName) throws JMSException {
        MQQueue destQueue = null;
        int depth = 0;
        try {
            MQQueueManager qm = getMQQueueManager();
            synchronized (LOCK) {
                destQueue = qm.accessQueue(queueName, CMQC.MQOO_INQUIRE);
            }
            depth = destQueue.getCurrentDepth();
        } catch (MQException e) {
            throw parseException(e, "");
        } finally {
            IbmResourceHelper.close(destQueue);
        }
        return depth;
    }
    
    /**
     * Connects and disconnects again.
     */
    public void connect() throws JMSException {
        if (isClosed()) {
            try {
                LOCK.start();
                boolean connected = getMQQueueManager().isConnected();
                LOCK.stop();
                LOG.info("Connected to '{}' status '{}' in {}ms.", queueManagerName, connected ? "open" : "closed", LOCK.getTimeInMs());
            } catch (MQException e) {
                throw parseException(e, "Failed to connect IBM MQ.");
            }
        }        
    }
    public List<JmsResource> listResources() throws JMSException {
        final List<JmsResource> result = listQueues();
        final List<JmsResource> topics = listTopics();
        Collections.sort(result, RESOURCE_CMP);
        Collections.sort(topics, RESOURCE_CMP);
        result.addAll(topics);
        return result;
    }
    @Override
    public List<JmsResource> listQueues() throws JMSException {
        List<JmsResource> result = new ArrayList<>();
        try {
            final PCFMessage request = new PCFMessage(CMQCFC.MQCMD_INQUIRE_Q_NAMES);
            request.addParameter(CMQC.MQCA_Q_NAME, "*");
            request.addParameter(CMQC.MQIA_Q_TYPE, CMQC.MQQT_ALL);

            final PCFMessage[] response = sendPfcMessage(request);
            final String[] names = response[0].getStringListParameterValue(MQConstants.MQCACF_Q_NAMES);
            final int[] types = response[0].getIntListParameterValue(MQConstants.MQIACF_Q_TYPES);
            
            for (int i = 0; i < names.length; i++) {
                final String qName = trim(names[i]);
                
                if (qName != null && !isSystemResource(qName)) {                        
                    final QTypes type = QTypes.from(types[i]);
                    result.add(new JmsResource(
                            qName, ToJmsResourceType.INSTANCE.convert(type), type.name()));
                }
            }
            return result;
        } catch (MQException e) {
            throw parseException(e, "Failed to read IBM MQ Queues.");
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) throw (IllegalArgumentException)e;
            if (e instanceof RuntimeException) throw (RuntimeException)e;
            throw new RuntimeException(e);
        }
    }
    @Override
    public List<JmsResource> listTopics() throws JMSException {
        List<JmsResource> result = new ArrayList<>();
        try {
            final PCFMessage request = new PCFMessage(CMQCFC.MQCMD_INQUIRE_TOPIC_NAMES);
            request.addParameter(CMQC.MQCA_TOPIC_NAME, "*");

            final PCFMessage[] response = sendPfcMessage(request);
            final String[] names = response[0].getStringListParameterValue(MQConstants.MQCACF_TOPIC_NAMES);
            
            for (int i = 0; i < names.length; i++) {
                final String tName = trim(names[i]);
                
                if (tName != null && !isSystemResource(tName)) {                        
                    result.add(new JmsResource(tName, Type.TOPIC, "TOPIC"));
                }
            }
            return result;
        } catch (MQException e) {
            throw parseException(e, "Failed to read IBM MQ Topics.");
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) throw (IllegalArgumentException)e;
            if (e instanceof RuntimeException) throw (RuntimeException)e;
            throw new RuntimeException(e);
        }
    }

    private PCFMessage[] sendPfcMessage(final PCFMessage request)
            throws MQException, MQDataException, PCFException, IOException {
        PCFMessage[] response;
        synchronized (this.LOCK) {
            final PCFMessageAgent messageAgent = getAgent();
            response = messageAgent.send(request);
        }
        return response;
    }
    
    /**
     * Checks our connection and if needed connects us again.
     */
    protected MQQueueManager getMQQueueManager() throws MQException {
        if (isClosed()) {
            synchronized (LOCK) {
                // clean and reconnect
                this.close(); 
                ibmMqManager = new MQQueueManager(queueManagerName, config);
            }
        }
        return ibmMqManager;
    }
    
    protected PCFMessageAgent getAgent() throws MQException, MQDataException {
        final MQQueueManager qManager = getMQQueueManager(); // ensure connection
        if (agent == null) {
            synchronized (LOCK) {
                if (agent == null) {                
                    agent = new PCFMessageAgent(qManager);
                }
            }
        }
        return agent;
    }
    
    private JMSException parseException(Exception e, String message) {
        if (e instanceof MQException) {
            final MQException mqEx = (MQException)e;
            final String errorCode = "Code: '" + mqEx.getCompCode() + "', Reason: '" + mqEx.getReason() + "'.";

            if (mqEx.getCompCode() == 2 && mqEx.getReason() == 2035) {
                return new JMSSecurityException(message + " User has not permissions." , errorCode);
            } else {
                return new UnknownJmsException(message + " " + errorCode, errorCode, e);
            }
        } else if (e instanceof MQDataException) {
            final MQDataException dataEx = (MQDataException)e;
            final String errorCode = "Code: '" + dataEx.getCompCode() + "', Reason: '" + dataEx.getReason() + "'.";
            return new UnknownJmsException(message + " " + errorCode, errorCode, e);
        }
        return new UnknownJmsException(message, e);
    }

    @Override
    public void close() {
        if (agent != null || ibmMqManager != null) {
            synchronized (LOCK) {
                if (agent != null) {
                    try {
                        agent.disconnect();
                    } catch (MQDataException e) {
                        LOG.info("Error during closing PCFMessageAgent. {}", e.getMessage());
                    } finally {
                        agent = null;
                    }
                }
                if (ibmMqManager != null) {
                    try {
                        ibmMqManager.close();
                    } catch (MQException e) {
                        LOG.info("Error during closing MQQueueManager. {}", e.getMessage());
                    } finally {
                        ibmMqManager = null;
                    }
                }
            }
        }
    }
    
    private static boolean isSystemResource(String name) {
        if (name != null && name.length() > 0) {
            for (String prefix : SYSTEM_PREFIXES) {
                if (name.startsWith(prefix)) return true;
            }
        }
        return false;
    }
    
    private static String trim(String value) {
        if (value == null) return value;
        else return value.trim();
    }

    @Override
    public boolean isClosed() {
        return ibmMqManager == null || !ibmMqManager.isConnected() || !ibmMqManager.isOpen();
    }
}
