package org.sterl.jmsui;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.jms.core.JmsTemplate;
import org.sterl.jmsui.bl.connection.model.JmsConnectionBE;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource;
import org.sterl.jmsui.bl.connectors.ibm.IbmMqConnector;
import org.sterl.jmsui.bl.connectors.ibm.IbmMqConnectorFactory;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.CMQCFC;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFMessageAgent;
import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

/**
 * https://github.com/ibm-messaging/mq-jms-spring 
 *
 */
@Disabled
public class ConnectIbmMqTest {

    private static final String QMGR = "PAUL.DEV";
    private static final String HOST = "paul-dev-eef2.qm.eu-gb.mq.appdomain.cloud";
    private static final int PORT = 30623;
    private static final String CHANNEL = "CLOUD.ADMIN.SVRCONN";

    private static final String APP_USER = "puele";
    private static final String APP_PASSWORD = "";
    
    
    @Test
    public void testConnectionFactory() throws Exception {
        JmsConnectionBE jmsConnection = new JmsConnectionBE();
        jmsConnection.setTimeout(1500L);
        jmsConnection.addOrSetConfig("queue_manager", QMGR)
            .addOrSetConfig("hostname", HOST)
            .addOrSetConfig("port", String.valueOf(PORT))
            .addOrSetConfig("channel", CHANNEL)
            .addOrSetConfig("userID", APP_USER)
            .addOrSetConfig("password", APP_PASSWORD);

        IbmMqConnector ibmMQConnection = new IbmMqConnectorFactory().create(jmsConnection);
        ibmMQConnection.connect();
        assertThat(ibmMQConnection.isClosed()).isFalse();

        /*
        JmsTemplate jmsTemplate = ibmMQConnection.getJmsTemplate();
        jmsTemplate.convertAndSend("DEV.QUEUE.1", "Simple message");
        Message msg = jmsTemplate.receive("DEV.QUEUE.1");
        assertNotNull(msg);
        System.out.println(((TextMessage) msg).getText());
        assertEquals("Simple message", ((TextMessage) msg).getText());
        */

        List<JmsResource> listQueues = ibmMQConnection.listResources();
        listQueues.forEach(System.out::println);
    }

    @Test
    public void simpleSendMessageTest() throws Exception {
        JmsConnectionFactory cf = createConnectionFactory();
        
        final String msgString = "Simple message " + new Date();
        JmsTemplate jmsTemplate = new JmsTemplate(cf);
        jmsTemplate.setPubSubDomain(true);
        jmsTemplate.setReceiveTimeout(2500);
        jmsTemplate.setPriority(7);
        jmsTemplate.setExplicitQosEnabled(true);
        jmsTemplate.convertAndSend("dev/topic/2", msgString);
        Message msg = jmsTemplate.receive("dev/topic/2");
        assertNotNull(msg);
        System.out.println("prio: " + msg.getJMSPriority());
        System.out.println(((TextMessage) msg).getText());
        assertEquals(msgString, ((TextMessage) msg).getText());
    }
    
    @Test
    public void topicTest() throws Exception {
        final JmsConnectionFactory cf = createConnectionFactory();
        final String msgString = "Simple message " + new Date();
        final ExecutorService executor = Executors.newFixedThreadPool(1);
        
        try (JMSContext c = cf.createContext()) {
            final Topic topic = c.createTopic("dev/topic/2");
            Future<Message> listen = executor.submit(new Callable<Message>() {
                @Override
                public Message call() throws Exception {
                    final JMSConsumer consumer = c.createConsumer(topic);
                    c.createProducer().send(topic, msgString);
                    final Message m = consumer.receive(5500);
                    c.acknowledge();
                    return m;
                }
            });
            
            c.createProducer().send(topic, msgString);
            
            Message m = listen.get();
            System.out.println(m);
        }
        
        executor.shutdownNow();
    }

    private JmsConnectionFactory createConnectionFactory() throws JMSException {
        JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
        JmsConnectionFactory cf = ff.createConnectionFactory();
        cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, QMGR);
        cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, HOST);
        cf.setIntProperty(WMQConstants.WMQ_PORT, PORT);
        cf.setStringProperty(WMQConstants.WMQ_CHANNEL, CHANNEL);
        cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
        cf.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME, "JmsPutGet (JMS)");
        cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
        cf.setStringProperty(WMQConstants.USERID, APP_USER);
        cf.setStringProperty(WMQConstants.PASSWORD, APP_PASSWORD);
        return cf;
    }

    @Test
    public void testPfcMessage() throws Exception {
        Hashtable<String, Object> settings = new Hashtable<>();

        settings.put("hostname", HOST);
        settings.put("port", PORT);
        settings.put("channel", "CLOUD.ADMIN.SVRCONN");
        settings.put("userID", APP_USER);
        settings.put("password", APP_PASSWORD);

        try {
            MQQueueManager ibmMqManager = new MQQueueManager(QMGR, settings);
            PCFMessageAgent agent = new PCFMessageAgent(ibmMqManager);

            PCFMessage request = new PCFMessage(CMQCFC.MQCMD_INQUIRE_Q_NAMES);
            request.addParameter(CMQC.MQCA_Q_NAME, "*");
            request.addParameter(CMQC.MQIA_Q_TYPE, CMQC.MQQT_ALL);

            PCFMessage[] response = agent.send(request);
            System.out.println(response.length);
            System.out.println(response[0]);
            
            String[] names = response[0].getStringListParameterValue(MQConstants.MQCACF_Q_NAMES);
            int[] types = response[0].getIntListParameterValue(MQConstants.MQIACF_Q_TYPES);
            
            System.out.println("---------------------");
            for (int i = 0; i < names.length; i++) {
                System.out.println(names[i].trim() + " - " + org.sterl.jmsui.bl.connectors.ibm.model.QTypes.from(types[i]));
            }
        } catch (MQException e) {
            System.err.println(e.completionCode + " " + e.reasonCode);
            if (e.getCompCode() == 2 && e.getReason() == 2035) {
                throw new IllegalArgumentException("User has no permissions to list queues.", e);
            }
            throw e;
        }
    }

    static String getClassType(Object obj) {
        if (obj == null)
            return "None";
        if (obj.getClass().isArray())
            return "Array";
        else
            return obj.getClass().getName();
    }

    static String getValue(Object obj) {
        if (obj == null)
            return null;
        if (obj.getClass().isArray())
            return Arrays.toString((Object[]) obj);
        else
            return obj.toString();
    }
}
