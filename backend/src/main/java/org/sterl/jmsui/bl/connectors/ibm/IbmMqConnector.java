package org.sterl.jmsui.bl.connectors.ibm;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.jms.JMSContext;
import javax.jms.JMSException;

import org.springframework.jms.core.JmsTemplate;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource;
import org.sterl.jmsui.bl.connectors.ibm.converter.IbmConverter.ToJmsResourceType;
import org.sterl.jmsui.bl.connectors.ibm.model.QTypes;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.CMQCFC;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFMessageAgent;

import lombok.AccessLevel;
import lombok.Getter;

public class IbmMqConnector {
    
    private final String queueManagerName;
    @Getter
    private final JmsTemplate jmsTemplate;
    
    private final Object LOCK = new Object();
    @Getter(value = AccessLevel.PACKAGE)
    private final Hashtable<String, Object> config;
    private MQQueueManager ibmMqManager;
    private PCFMessageAgent agent;
    
    public void testConnection() throws JMSException {
        jmsTemplate.getConnectionFactory().createConnection().close();
    }


    public IbmMqConnector(String queueManagerName, JmsTemplate jmsTemplate, Hashtable<String, Object> config) {
        this.queueManagerName = queueManagerName;
        this.jmsTemplate = jmsTemplate;
        this.config = config;
    }

    public List<JmsResource> listQueues() {
        List<JmsResource> result = new ArrayList<>();
        try {
            PCFMessageAgent messageAgent = getAgent();
            final PCFMessage request = new PCFMessage(CMQCFC.MQCMD_INQUIRE_Q_NAMES);
            request.addParameter(CMQC.MQCA_Q_NAME, "*");
            request.addParameter(CMQC.MQIA_Q_TYPE, CMQC.MQQT_ALL);
            synchronized (this.LOCK) {
                PCFMessage[] response = messageAgent.send(request);
                
                String[] names = response[0].getStringListParameterValue(MQConstants.MQCACF_Q_NAMES);
                int[] types = response[0].getIntListParameterValue(MQConstants.MQIACF_Q_TYPES);
                
                for (int i = 0; i < names.length; i++) {
                    QTypes type = QTypes.from(types[i]);
                    result.add(new JmsResource(names[i], ToJmsResourceType.INSTANCE.convert(type), type.name()));
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
    
    private PCFMessageAgent getAgent() throws MQException, MQDataException {
        synchronized (LOCK) {
            if (ibmMqManager == null) {
                ibmMqManager = new MQQueueManager(queueManagerName, config);
                agent = new PCFMessageAgent(ibmMqManager);
            }
            return agent;
        }
    }
    
    private RuntimeException parseException(MQException e, String message) {
        if (e.getCompCode() == 2 && e.getReason() == 2035) {
            return new IllegalArgumentException("User has no permissions to list queues.", e);
        }
        return new RuntimeException(message, e);
    }
}
