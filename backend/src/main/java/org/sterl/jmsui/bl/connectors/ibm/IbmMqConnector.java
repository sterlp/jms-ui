package org.sterl.jmsui.bl.connectors.ibm;

import static org.sterl.jmsui.bl.connectors.util.JmsHeaderUtil.getOrDefault;
import static org.sterl.jmsui.bl.connectors.util.JmsHeaderUtil.setMeassageHeader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
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
    
    private final JmsPoolConnectionFactory poolConnectionFactory;
    
    public IbmMqConnector(String queueManagerName, 
            Long defaultTimeoutInMs,
            JmsConnectionFactory connectionFactory, Hashtable<String, Object> config) {
        this.queueManagerName = queueManagerName;
        this.connectionFactory = connectionFactory;
        this.config = config;
        this.defaultTimeoutInMs = defaultTimeoutInMs == null ? 3000 : defaultTimeoutInMs;
        
        this.poolConnectionFactory = new JmsPoolConnectionFactory();
        this.poolConnectionFactory.setMaxConnections(10);
        this.poolConnectionFactory.setConnectionFactory(connectionFactory);
        this.poolConnectionFactory.setConnectionIdleTimeout(5 * 60 * 1_000);
    }

    @Override
    public void sendMessage(String destination, Type jmsType, String message, JmsHeaderRequestValues header) {
        try (JMSContext c = poolConnectionFactory.createContext()) {
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
        try (JMSContext c = poolConnectionFactory.createContext()) {
            final Destination d = jmsType == Type.TOPIC ? c.createTopic(destination) : c.createQueue(destination);
            try (JMSConsumer consumer = c.createConsumer(d)) {
                result = consumer.receive(getOrDefault(timeout, defaultTimeoutInMs));
                c.acknowledge();
            }
        }
        return result;
    }
    
    public void listen(String destination, Type jmsType, MessageListener listener) {
        try (JMSContext c = poolConnectionFactory.createContext()) {
            final Destination d = jmsType == Type.TOPIC ? c.createTopic(destination) : c.createQueue(destination);
            JMSConsumer consumer = poolConnectionFactory.createContext().createConsumer(d);
            consumer.setMessageListener(listener);
        }
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
                this.poolConnectionFactory.start();
                this.poolConnectionFactory.initConnectionsPool();
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
    
    /**
     *  copy from https://github.com/jmstoolbox/jmstoolbox/blob/dev/org.titou10.jtb.qm.ibmmq/src/org/titou10/jtb/qm/ibmmq/MQQManager.java
     */
    public Map<String, Object> getQueueInformation(String queueName) {
        final SortedMap<String, Object> properties = new TreeMap<>();
        MQQueue destQueue = null;
        try {
           try {
              destQueue = getMQQueueManager().accessQueue(queueName, CMQC.MQOO_INQUIRE);

              properties.put("Real name", destQueue.getName());

              try {
                 properties.put("AlternateUserId", destQueue.getAlternateUserId());
              } catch (MQException e) {
                 LOG.warn("Exception when reading AlternateUserId. Ignoring. " + e.getMessage());
              }

              try {
                 switch (destQueue.getCloseOptions()) {
                    case CMQC.MQCO_NONE:
                       properties.put("CloseOptions", "NONE");
                       break;
                    case CMQC.MQCO_DELETE:
                       properties.put("CloseOptions", "DELETE");
                       break;
                    case CMQC.MQCO_DELETE_PURGE:
                       properties.put("CloseOptions", "DELETE PURGE");
                       break;
                    case CMQC.MQCO_KEEP_SUB:
                       properties.put("CloseOptions", "KEEP SUB");
                       break;
                    case CMQC.MQCO_REMOVE_SUB:
                       properties.put("CloseOptions", "REMOVE SUB");
                       break;
                    default:
                       properties.put("CloseOptions", destQueue.getCloseOptions());
                       break;
                 }

              } catch (MQException e) {
                 LOG.warn("Exception when reading CloseOptions. Ignoring. " + e.getMessage());
              }

              try {
                 properties.put("CreationDateTime", destQueue.getCreationDateTime().getTime());
              } catch (MQException e) {
                 LOG.warn("Exception when reading CreationDateTime. Ignoring. " + e.getMessage());
              }

              try {
                 properties.put("CurrentDepth", destQueue.getCurrentDepth());
              } catch (MQException e) {
                 LOG.warn("Exception when reading CurrentDepth. Ignoring. " + e.getMessage());
              }

              try {
                 switch (destQueue.getDefinitionType()) {
                    case CMQC.MQQDT_PREDEFINED:
                       properties.put("DefinitionType", "PREDEFINED");
                       break;
                    case CMQC.MQQDT_PERMANENT_DYNAMIC:
                       properties.put("DefinitionType", "PERMANENT_DYNAMIC");
                       break;
                    case CMQC.MQQDT_TEMPORARY_DYNAMIC:
                       properties.put("DefinitionType", "TEMPORARY_DYNAMIC");
                       break;
                    default:
                       properties.put("DefinitionType", destQueue.getDefinitionType());
                       break;
                 }
              } catch (MQException e) {
                 LOG.warn("Exception when reading DefinitionType. Ignoring" + e.getMessage());
              }

              try {
                 properties.put("Description", destQueue.getDescription());
              } catch (MQException e) {
                 LOG.warn("Exception when reading Description. Ignoring. " + e.getMessage());
              }

              switch (destQueue.getDestinationType()) {
                 case CMQC.MQOT_Q:
                    properties.put("DestinationType", "Queue");
                    break;
                 case CMQC.MQOT_TOPIC:
                    properties.put("DestinationType", "Topic");
                    break;
                 default:
                    properties.put("DestinationType", destQueue.getDestinationType());
                    break;
              }

              try {
                 switch (destQueue.getInhibitGet()) {
                    case CMQC.MQQA_PUT_INHIBITED:
                       properties.put("InhibitGet", "INHIBITED");
                       break;
                    case CMQC.MQQA_PUT_ALLOWED:
                       properties.put("InhibitGet", "ALLOWED");
                       break;
                    default:
                       properties.put("InhibitGet", destQueue.getInhibitGet());
                       break;
                 }
              } catch (MQException e) {
                 LOG.warn("Exception when reading InhibitGet. Ignoring" + e.getMessage());
              }

              try {
                 switch (destQueue.getInhibitPut()) {
                    case CMQC.MQQA_PUT_INHIBITED:
                       properties.put("InhibitPut", "INHIBITED");
                       break;
                    case CMQC.MQQA_PUT_ALLOWED:
                       properties.put("InhibitPut", "ALLOWED");
                       break;
                    default:
                       properties.put("InhibitPut", destQueue.getInhibitPut());
                       break;
                 }
              } catch (MQException e) {
                 LOG.warn("Exception when reading InhibitPut. Ignoring" + e.getMessage());
              }

              try {
                 properties.put("MaximumDepth", destQueue.getMaximumDepth());
              } catch (MQException e) {
                 LOG.warn("Exception when reading MaximumDepth. Ignoring" + e.getMessage());
              }

              try {
                 properties.put("MaximumMessageLength", destQueue.getMaximumMessageLength());
              } catch (MQException e) {
                 LOG.warn("Exception when reading MaximumMessageLength. Ignoring" + e.getMessage());
              }

              try {
                 properties.put("OpenOptions", destQueue.getOpenOptions());
                 switch (destQueue.getQueueType()) {
                    case CMQC.MQOO_ALTERNATE_USER_AUTHORITY:
                       properties.put("OpenOptions", "ALTERNATE_USER_AUTHORITY");
                       break;
                    case CMQC.MQOO_BIND_AS_Q_DEF:
                       properties.put("OpenOptions", "BIND_AS_QDEF");
                       break;
                    case CMQC.MQOO_BIND_NOT_FIXED:
                       properties.put("OpenOptions", "BIND_NOT_FIXED");
                       break;
                    case CMQC.MQOO_BIND_ON_OPEN:
                       properties.put("OpenOptions", "BIND_ON_OPEN");
                       break;
                    case CMQC.MQOO_BROWSE:
                       properties.put("OpenOptions", "BROWSE");
                       break;
                    case CMQC.MQOO_FAIL_IF_QUIESCING:
                       properties.put("OpenOptions", "FAIL_IF_QUIESCING");
                       break;
                    case CMQC.MQOO_INPUT_AS_Q_DEF:
                       properties.put("OpenOptions", "INPUT_AS_Q_DEF");
                       break;
                    case CMQC.MQOO_INPUT_SHARED:
                       properties.put("OpenOptions", "INPUT_SHARED");
                       break;
                    case CMQC.MQOO_INPUT_EXCLUSIVE:
                       properties.put("OpenOptions", "INPUT_EXCLUSIVE");
                       break;
                    case CMQC.MQOO_INQUIRE:
                       properties.put("OpenOptions", "INQUIRE");
                       break;
                    case CMQC.MQOO_OUTPUT:
                       properties.put("OpenOptions", "OUTPUT");
                       break;
                    case CMQC.MQOO_PASS_ALL_CONTEXT:
                       properties.put("OpenOptions", "PASS_ALL_CONTEXT");
                       break;
                    case CMQC.MQOO_PASS_IDENTITY_CONTEXT:
                       properties.put("OpenOptions", "PASS_IDENTITY_CONTEXT");
                       break;
                    case CMQC.MQOO_SAVE_ALL_CONTEXT:
                       properties.put("OpenOptions", "SAVE_ALL_CONTEXT");
                       break;
                    case CMQC.MQOO_SET:
                       properties.put("OpenOptions", "SET");
                       break;
                    case CMQC.MQOO_SET_ALL_CONTEXT:
                       properties.put("OpenOptions", "SET_ALL_CONTEXT");
                       break;
                    case CMQC.MQOO_SET_IDENTITY_CONTEXT:
                       properties.put("OpenOptions", "SET_IDENTITY_CONTEXT");
                       break;
                    default:
                       properties.put("OpenOptions", destQueue.getOpenOptions());
                       break;
                 }
              } catch (MQException e) {
                  LOG.warn("Exception when reading OpenOptions. Ignoring" + e.getMessage());
              }

              try {
                 properties.put("OpenInputCount", destQueue.getOpenInputCount());
              } catch (MQException e) {
                  LOG.warn("Exception when reading OpenInputCount. Ignoring" + e.getMessage());
              }

              try {
                 properties.put("OpenOutputCount", destQueue.getOpenOutputCount());
              } catch (MQException e) {
                  LOG.warn("Exception when reading OpenOutputCount. Ignoring" + e.getMessage());
              }

              properties.put("QueueManagerCmdLevel", destQueue.getQueueManagerCmdLevel());

              try {
                 switch (destQueue.getQueueType()) {
                    case CMQC.MQQT_ALIAS:
                       properties.put("QueueType", "ALIAS");
                       break;
                    case CMQC.MQQT_LOCAL:
                       properties.put("QueueType", "LOCAL");
                       break;
                    case CMQC.MQQT_MODEL:
                       properties.put("QueueType", "MODEL");
                       break;
                    case CMQC.MQQT_REMOTE:
                       properties.put("QueueType", "REMOTE");
                       break;
                    default:
                       properties.put("QueueType", destQueue.getQueueType());
                       break;
                 }
              } catch (MQException e) {
                 LOG.warn("Exception when reading QueueType. Ignoring" + e.getMessage());
              }

              properties.put("ResolvedObjectString", destQueue.getResolvedObjectString());
              properties.put("ResolvedQName", destQueue.getResolvedQName());

              switch (destQueue.getResolvedType()) {
                 case CMQC.MQOT_Q:
                    properties.put("ResolvedType", "Queue");
                    break;
                 case CMQC.MQOT_TOPIC:
                    properties.put("ResolvedType", "Topic");
                    break;
                 case CMQC.MQOT_NONE:
                    properties.put("ResolvedType", "None");
                    break;
                 default:
                    properties.put("ResolvedType", destQueue.getResolvedType());
                    break;
              }

              try {
                 switch (destQueue.getShareability()) {
                    case CMQC.MQQA_SHAREABLE:
                       properties.put("Shareability", "SHAREABLE");
                       break;
                    case CMQC.MQQA_NOT_SHAREABLE:
                       properties.put("Shareability", "NOT_SHAREABLE");
                       break;
                    default:
                       properties.put("Shareability", destQueue.getShareability());
                       break;
                 }
              } catch (MQException e) {
                  LOG.warn("Exception when reading Shareability. Ignoring" + e.getMessage());
              }

              try {
                 switch (destQueue.getTriggerControl()) {
                    case CMQC.MQTC_OFF:
                       properties.put("TriggerControl", "OFF");
                       break;
                    case CMQC.MQTC_ON:
                       properties.put("TriggerControl", "ON");
                       break;
                    default:
                       properties.put("TriggerControl", destQueue.getTriggerControl());
                       break;
                 }
              } catch (MQException e) {
                  LOG.warn("Exception when reading TriggerControl. Ignoring" + e.getMessage());
              }

              try {
                 properties.put("TriggerData", destQueue.getTriggerData());
              } catch (MQException e) {
                  LOG.warn("Exception when reading TriggerData. Ignoring" + e.getMessage());
              }

              try {
                 properties.put("TriggerDepth", destQueue.getTriggerDepth());
              } catch (MQException e) {
                  LOG.warn("Exception when reading TriggerDepth. Ignoring" + e.getMessage());
              }

              try {
                 properties.put("TriggerMessagePriority", destQueue.getTriggerMessagePriority());
              } catch (MQException e) {
                  LOG.warn("Exception when reading TriggerMessagePriority. Ignoring" + e.getMessage());
              }

              try {
                 switch (destQueue.getTriggerType()) {
                    case CMQC.MQTT_NONE:
                       properties.put("TriggerType", "NONE");
                       break;
                    case CMQC.MQTT_FIRST:
                       properties.put("TriggerType", "FIRST");
                       break;
                    case CMQC.MQTT_EVERY:
                       properties.put("TriggerType", "EVERY");
                       break;
                    case CMQC.MQTT_DEPTH:
                       properties.put("TriggerType", "DEPTH");
                       break;
                    default:
                       properties.put("TriggerType", destQueue.getTriggerType());
                       break;
                 }
              } catch (MQException e) {
                 LOG.warn("Exception when reading TriggerType. Ignoring" + e.getMessage());
              }

           } catch (MQException e) {
              LOG.error("Exception when reading Queue Information. Ignoring", e);
           }
        } finally {
           if (destQueue != null) {
              try {
                 destQueue.close();
              } catch (MQException e) {}
           }
        }
        LOG.debug("Queue Information : {}", properties);
        return properties;

    }
    
    @Override
    public Map<String, Object> getTopicInformation(String topicName) {

       final SortedMap<String, Object> properties = new TreeMap<>();

       // DF: could be done by
       PCFMessageAgent agent = null;
       try {
          agent = getAgent();

          PCFMessage request = new PCFMessage(CMQCFC.MQCMD_INQUIRE_TOPIC);
          request.addParameter(CMQC.MQCA_TOPIC_NAME, topicName);

          PCFMessage[] responses;
          synchronized (LOCK) {
              responses = agent.send(request);
          }
          PCFMessage m = responses[0];

          try {
             properties.put("Alteration Date", m.getStringParameterValue(CMQC.MQCA_ALTERATION_DATE));
          } catch (PCFException e) {
             LOG.warn("Exception when reading Alteration Date. Ignoring" + e.getMessage());
          }
          try {
             properties.put("Alteration Time", m.getStringParameterValue(CMQC.MQCA_ALTERATION_TIME));
          } catch (PCFException e) {
             LOG.warn("Exception when reading Alteration Time. Ignoring" + e.getMessage());
          }
          try {
             properties.put("Cluster Name", m.getStringParameterValue(CMQC.MQCA_CLUSTER_NAME));
          } catch (PCFException e) {
             LOG.warn("Exception when reading Cluster Name. Ignoring" + e.getMessage());
          }
          try {
             switch (m.getIntParameterValue(CMQC.MQIA_TOPIC_DEF_PERSISTENCE)) {
                case CMQC.MQPER_PERSISTENCE_AS_PARENT:
                   properties.put("Default persistence", "PERSISTENCE_AS_PARENT");
                   break;
                case CMQC.MQPER_PERSISTENT:
                   properties.put("Default persistence", "PERSISTENT");
                   break;
                case CMQC.MQPER_NOT_PERSISTENT:
                   properties.put("Default persistence", "NOT_PERSISTENT");
                   break;
                default:
                   break;
             }
          } catch (PCFException e) {
             LOG.warn("Exception when reading Default persistence. Ignoring" + e.getMessage());
          }
          try {
             properties.put("Default priority", m.getIntParameterValue(CMQC.MQIA_DEF_PRIORITY));
          } catch (PCFException e) {
             LOG.warn("Exception when reading Default priority. Ignoring" + e.getMessage());
          }
          try {

             switch (m.getIntParameterValue(CMQC.MQIA_DEF_PUT_RESPONSE_TYPE)) {
                case CMQC.MQPRT_ASYNC_RESPONSE:
                   properties.put("Default put response", "ASYNC_RESPONSE");
                   break;
                case CMQC.MQPRT_RESPONSE_AS_PARENT:
                   properties.put("Default put response", "RESPONSE_AS_PARENT");
                   break;
                case CMQC.MQPRT_SYNC_RESPONSE:
                   properties.put("Default put response", "SYNC_RESPONSE");
                   break;
                default:
                   break;
             }
          } catch (PCFException e) {
             LOG.warn("Exception when reading Default put response. Ignoring" + e.getMessage());
          }
          try {
             properties.put("Durable Model Q Name", m.getStringParameterValue(CMQC.MQCA_MODEL_DURABLE_Q));
          } catch (PCFException e) {
             LOG.warn("Exception when reading Durable Model Q Name. Ignoring" + e.getMessage());
          }
          try {
             switch (m.getIntParameterValue(CMQC.MQIA_DURABLE_SUB)) {
                case CMQC.MQSUB_DURABLE_AS_PARENT:
                   properties.put("Durable subscriptions", "DURABLE_AS_PARENT");
                   break;
                case CMQC.MQSUB_DURABLE_NO:
                   properties.put("Durable subscriptions", "DURABLE_NO");
                   break;
                case CMQC.MQSUB_DURABLE_YES:
                   properties.put("Durable subscriptions", "DURABLE_YES");
                   break;
                default:
                   break;
             }
          } catch (PCFException e) {
             LOG.warn("Exception when reading Durable subscriptions. Ignoring" + e.getMessage());
          }
          try {
             switch (m.getIntParameterValue(CMQC.MQIA_INHIBIT_PUB)) {
                case CMQC.MQTA_PUB_AS_PARENT:
                   properties.put("Inhibit Publications", "PUB_AS_PARENT");
                   break;
                case CMQC.MQTA_PUB_INHIBITED:
                   properties.put("Inhibit Publications", "PUB_INHIBITED");
                   break;
                case CMQC.MQTA_PUB_ALLOWED:
                   properties.put("Inhibit Publications", "PUB_ALLOWED");
                   break;
                default:
                   break;
             }
          } catch (PCFException e) {
             LOG.warn("Exception when reading Inhibit Publications. Ignoring" + e.getMessage());
          }
          try {
             switch (m.getIntParameterValue(CMQC.MQIA_INHIBIT_SUB)) {
                case CMQC.MQTA_SUB_AS_PARENT:
                   properties.put("Inhibit Subscriptions", "SUB_AS_PARENT");
                   break;
                case CMQC.MQTA_SUB_INHIBITED:
                   properties.put("Inhibit Subscriptions", "SUB_INHIBITED");
                   break;
                case CMQC.MQTA_SUB_ALLOWED:
                   properties.put("Inhibit Subscriptions", "SUB_ALLOWED");
                   break;
                default:
                   break;
             }
          } catch (PCFException e) {
             LOG.warn("Exception when reading Inhibit Subscriptions. Ignoring" + e.getMessage());
          }
          try {
             properties.put("Non Durable Model Q Name", m.getStringParameterValue(CMQC.MQCA_MODEL_NON_DURABLE_Q));
          } catch (PCFException e) {
             LOG.warn("Exception when reading Non Durable Model Q Name. Ignoring" + e.getMessage());
          }
          try {
             switch (m.getIntParameterValue(CMQC.MQIA_NPM_DELIVERY)) {
                case CMQC.MQDLV_AS_PARENT:
                   properties.put("Non Persistent Msg Delivery", "AS_PARENT");
                   break;
                case CMQC.MQDLV_ALL:
                   properties.put("Non Persistent Msg Delivery", "ALL");
                   break;
                case CMQC.MQDLV_ALL_DUR:
                   properties.put("Non Persistent Msg Delivery", "ALL_DUR");
                   break;
                case CMQC.MQDLV_ALL_AVAIL:
                   properties.put("Non Persistent Msg Delivery", "ALL_AVAIL");
                   break;
                default:
                   break;
             }
          } catch (PCFException e) {
             LOG.warn("Exception when reading on Persistent Msg Delivery. Ignoring" + e.getMessage());
          }
          try {
             switch (m.getIntParameterValue(CMQC.MQIA_PM_DELIVERY)) {
                case CMQC.MQDLV_AS_PARENT:
                   properties.put("Persistent Msg Delivery", "AS_PARENT");
                   break;
                case CMQC.MQDLV_ALL:
                   properties.put("Persistent Msg Delivery", "ALL");
                   break;
                case CMQC.MQDLV_ALL_DUR:
                   properties.put("Persistent Msg Delivery", "ALL_DUR");
                   break;
                case CMQC.MQDLV_ALL_AVAIL:
                   properties.put("Persistent Msg Delivery", "ALL_AVAIL");
                   break;
                default:
                   break;
             }
          } catch (PCFException e) {
             LOG.warn("Exception when reading Persistent Msg Delivery. Ignoring" + e.getMessage());
          }
          try {
             switch (m.getIntParameterValue(CMQC.MQIA_PROXY_SUB)) {
                case CMQC.MQTA_PROXY_SUB_FORCE:
                   properties.put("Proxy Subscriptions", "SUB_FORCE");
                   break;
                case CMQC.MQTA_PROXY_SUB_FIRSTUSE:
                   properties.put("Proxy Subscriptions", "SUB_FIRSTUSE");
                   break;
                default:
                   break;
             }
          } catch (PCFException e) {
             LOG.warn("Exception when reading Proxy Subscriptions. Ignoring" + e.getMessage());
          }
          try {
             switch (m.getIntParameterValue(CMQC.MQIA_PUB_SCOPE)) {
                case CMQC.MQSCOPE_ALL:
                   properties.put("Publication Scope", "ALL");
                   break;
                case CMQC.MQSCOPE_AS_PARENT:
                   properties.put("Publication Scope", "AS_PARENT");
                   break;
                case CMQC.MQSCOPE_QMGR:
                   properties.put("Publication Scope", "QMGR");
                   break;
                default:
                   break;
             }
          } catch (PCFException e) {
             LOG.warn("Exception when reading Publication Scope. Ignoring" + e.getMessage());
          }
          try {
             switch (m.getIntParameterValue(CMQC.MQIA_SUB_SCOPE)) {
                case CMQC.MQSCOPE_ALL:
                   properties.put("Subscription Scope", "ALL");
                   break;
                case CMQC.MQSCOPE_AS_PARENT:
                   properties.put("Subscription Scope", "AS_PARENT");
                   break;
                case CMQC.MQSCOPE_QMGR:
                   properties.put("Subscription Scope", "QMGR");
                   break;
                default:
                   break;
             }
          } catch (PCFException e) {
             LOG.warn("Exception when reading Subscription Scope. Ignoring" + e.getMessage());
          }
          try {
             properties.put("Topic Description", m.getStringParameterValue(CMQC.MQCA_TOPIC_DESC));
          } catch (PCFException e) {
             LOG.warn("Exception when readingCluster Name. Ignoring" + e.getMessage());
          }
          try {
             properties.put("Topic String", m.getStringParameterValue(CMQC.MQCA_TOPIC_STRING));
          } catch (PCFException e) {
             LOG.warn("Exception when reading Topic Description. Ignoring" + e.getMessage());
          }
          try {
             switch (m.getIntParameterValue(CMQC.MQIA_TOPIC_TYPE)) {
                case CMQC.MQTOPT_LOCAL:
                   properties.put("Topic Type", "LOCAL");
                   break;
                case CMQC.MQTOPT_CLUSTER:
                   properties.put("Topic Type", "CLUSTER");
                   break;
                default:
                   break;
             }
          } catch (PCFException e) {
             LOG.warn("Exception when reading Topic Type. Ignoring" + e.getMessage());
          }
          try {
             switch (m.getIntParameterValue(CMQC.MQIA_USE_DEAD_LETTER_Q)) {
                case CMQC.MQUSEDLQ_NO:
                   properties.put("Use DLQ ", "NO");
                   break;
                case CMQC.MQUSEDLQ_YES:
                   properties.put("Use DLQ", "YES");
                   break;
                case CMQC.MQUSEDLQ_AS_PARENT:
                   properties.put("Use DLQ", "AS_PARENT");
                   break;
                default:
                   break;
             }
          } catch (PCFException e) {
             LOG.warn("Exception when reading Use DLQ. Ignoring" + e.getMessage());
          }
          try {
             switch (m.getIntParameterValue(CMQC.MQIA_WILDCARD_OPERATION)) {
                case CMQC.MQTA_PASSTHRU:
                   properties.put("Wildcard Operation", "PASSTHRU");
                   break;
                case CMQC.MQTA_BLOCK:
                   properties.put("Wildcard Operation", "BLOCK");
                   break;
                default:
                   break;
             }
          } catch (PCFException e) {
             LOG.warn("Exception when reading Wildcard Operation. Ignoring" + e.getMessage());
          }
       } catch (IOException | MQException | MQDataException e) {
          LOG.warn("Exception when getting PCF Agent. Ignoring" + e.getMessage());
       }

       LOG.debug("Topic Information : {}", properties);
       return properties;
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
        this.poolConnectionFactory.clear();
        this.poolConnectionFactory.stop();
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
                        ibmMqManager.disconnect();
                        ibmMqManager.close();
                    } catch (MQException e) {
                        LOG.info("Error during closing MQQueueManager. {}", e.getMessage());
                    } finally {
                        ibmMqManager = null;
                    }
                }
            }
        }
        LOG.info("{} closed.", this.queueManagerName);
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
