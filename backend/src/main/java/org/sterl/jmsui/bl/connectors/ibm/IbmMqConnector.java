package org.sterl.jmsui.bl.connectors.ibm;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.UncategorizedJmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.sterl.jmsui.api.JmsHeaderRequestValues;
import org.sterl.jmsui.api.exception.JmsAuthorizationException;
import org.sterl.jmsui.bl.connectors.api.JmsConnectorInstance;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource;
import org.sterl.jmsui.bl.connectors.ibm.converter.IbmConverter.ToJmsResourceType;
import org.sterl.jmsui.bl.connectors.ibm.model.QTypes;
import org.sterl.jmsui.bl.connectors.util.JmsHeaderUtil;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.CMQCFC;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFMessageAgent;
import com.ibm.msg.client.jms.DetailedJMSSecurityException;

import lombok.AccessLevel;
import lombok.Getter;

public class IbmMqConnector implements JmsConnectorInstance {
    private static final Logger LOG = LoggerFactory.getLogger(IbmMqConnector.class);
    
    private static final String[] SYSTEM_PREFIXES = {"LOOPBACK", "SYSTEM."};
    
    private final String queueManagerName;
    @Getter
    private final JmsTemplate jmsTemplate;
    
    private final Object LOCK = new Object();
    private final Long defaultTimeoutInMs;

    @Getter(value = AccessLevel.PACKAGE)
    private final Hashtable<String, Object> config;

    private volatile MQQueueManager ibmMqManager;
    private volatile PCFMessageAgent agent;
    
    public IbmMqConnector(String queueManagerName, 
            Long defaultTimeoutInMs,
            JmsTemplate jmsTemplate, Hashtable<String, Object> config) {
        this.queueManagerName = queueManagerName;
        this.jmsTemplate = jmsTemplate;
        this.config = config;
        this.defaultTimeoutInMs = defaultTimeoutInMs;
    }

    @Override
    public void sendMessage(String destination, String message, JmsHeaderRequestValues header) {
        if (header.getJMSPriority() != null) jmsTemplate.setPriority(header.getJMSPriority());
        else jmsTemplate.setPriority(4);
        
        jmsTemplate.convertAndSend(destination, message, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws JMSException {
                JmsHeaderUtil.setMeassageHeader(header, message);
                return message;
            }
        });
        
    }
    
    public int getQueueDepth(String queueName) throws JMSException {
        MQQueue destQueue = null;
        int depth = 0;
        try {
           destQueue = getMQQueueManager().accessQueue(queueName, CMQC.MQOO_INQUIRE);
           depth = destQueue.getCurrentDepth();
        } catch (MQException e) {
            throw new UncategorizedJmsException("Failed to read queue depth of " + queueName + ". " + e.getMessage(), e);
        } finally {
           if (destQueue != null) {
              try {
                 destQueue.close();
              } catch (MQException e) {}
           }
        }

        return depth;
     }

    @Override
    public Message receive(String destination, Long timeout) {
        if (timeout != null) jmsTemplate.setReceiveTimeout(timeout);
        else if (defaultTimeoutInMs != null) jmsTemplate.setReceiveTimeout(defaultTimeoutInMs);

        return jmsTemplate.receive(destination);
    }
    
    /**
     * Connects and disconnects again.
     */
    public void testConnection() throws JMSException {
        try {
            jmsTemplate.getConnectionFactory().createConnection().close();
        } catch (JMSException e) {
            if (e instanceof DetailedJMSSecurityException) {
                throw new JmsAuthorizationException(e.getMessage(), e);
            } else if (e.getCause() instanceof DetailedJMSSecurityException) {
                throw new JmsAuthorizationException(e.getMessage(), e);
            }
            throw e;
        }
    }

    public List<JmsResource> listResources() {
        List<JmsResource> result = new ArrayList<>();
        try {
            final PCFMessage request = new PCFMessage(CMQCFC.MQCMD_INQUIRE_Q_NAMES);
            request.addParameter(CMQC.MQCA_Q_NAME, "*");
            //request.addParameter(CMQC.MQIA_Q_TYPE, CMQC.MQQT_ALL);
            synchronized (this.LOCK) {
                PCFMessageAgent messageAgent = getAgent();
                PCFMessage[] response = messageAgent.send(request);
                
                String[] names = response[0].getStringListParameterValue(MQConstants.MQCACF_Q_NAMES);
                int[] types = response[0].getIntListParameterValue(MQConstants.MQIACF_Q_TYPES);
                
                for (int i = 0; i < names.length; i++) {
                    final String qName = trim(names[i]);

                    if (qName != null && !isSystemQueue(qName)) {                        
                        final QTypes type = QTypes.from(types[i]);
                        result.add(new JmsResource(
                                trim(names[i]), ToJmsResourceType.INSTANCE.convert(type), type.name()));
                    }
                }
            }
            return result;
        } catch (MQException e) {
            throw parseException(e, "Failed to read resources.");
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) throw (IllegalArgumentException)e;
            if (e instanceof RuntimeException) throw (RuntimeException)e;
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Checks our connection and if needed connects us again.
     */
    private MQQueueManager getMQQueueManager() throws MQException {
        if (isClosed()) {
            synchronized (LOCK) {
                // clean and reconnect
                this.close(); 
                ibmMqManager = new MQQueueManager(queueManagerName, config);
            }
        }
        return ibmMqManager;
    }
    
    private PCFMessageAgent getAgent() throws MQException, MQDataException {
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
    
    private RuntimeException parseException(MQException e, String message) {
        if (e.getCompCode() == 2 && e.getReason() == 2035) {
            return new IllegalArgumentException("User has no permissions to list queues.", e);
        }
        return new RuntimeException(message, e);
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
    
    private static boolean isSystemQueue(String name) {
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
