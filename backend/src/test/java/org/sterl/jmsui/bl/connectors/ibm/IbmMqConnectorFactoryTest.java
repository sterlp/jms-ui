package org.sterl.jmsui.bl.connectors.ibm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;
import org.sterl.jmsui.bl.connection.model.JmsConnection;
import org.sterl.jmsui.bl.connectors.ibm.IbmMqConnector;
import org.sterl.jmsui.bl.connectors.ibm.IbmMqConnectorFactory;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;

public class IbmMqConnectorFactoryTest {

    @Test
    public void contextLoads() {
    }
    
    @Test
    public void testSettings() throws Exception {
        JmsConnection jmsConnection = new JmsConnection();
        jmsConnection.setTimeout(1500L);
        jmsConnection.addOrSetConfig("queue_manager", "qmgr")
            .addOrSetConfig("hostname", "host")
            .addOrSetConfig("port", "111")
            .addOrSetConfig("channel", "a.channel")
            .addOrSetConfig("userID", "user")
            .addOrSetConfig("password", "pass")
            .addOrSetConfig("APPNAME", "myApp");

        IbmMqConnector ibmMQConnection = new IbmMqConnectorFactory().create(jmsConnection);
        JmsTemplate jmsTemplate = ibmMQConnection.getJmsTemplate();
        
        JmsConnectionFactory cf = (JmsConnectionFactory)jmsTemplate.getConnectionFactory();
        
        assertEquals("qmgr", cf.getStringProperty(WMQConstants.WMQ_QUEUE_MANAGER));
        assertEquals("host", cf.getStringProperty(WMQConstants.WMQ_HOST_NAME));
        assertEquals(111, cf.getIntProperty(WMQConstants.WMQ_PORT));
        assertEquals("a.channel", cf.getStringProperty(WMQConstants.WMQ_CHANNEL));
        assertEquals(WMQConstants.WMQ_CM_CLIENT, cf.getIntProperty(WMQConstants.WMQ_CONNECTION_MODE));
        
        
        assertEquals(true, cf.getBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP));
        assertEquals("user", cf.getStringProperty(WMQConstants.USERID));
        assertEquals("pass", cf.getStringProperty(WMQConstants.PASSWORD));
        
        assertEquals("myApp", cf.getStringProperty(WMQConstants.WMQ_APPLICATIONNAME));
        
        assertEquals("host", ibmMQConnection.getConfig().get("hostname"));
        assertEquals(111, ibmMQConnection.getConfig().get("port"));
        assertEquals("a.channel", ibmMQConnection.getConfig().get("channel"));
        assertEquals("user", ibmMQConnection.getConfig().get("userID"));
        assertEquals("pass", ibmMQConnection.getConfig().get("password"));
        assertEquals("myApp", ibmMQConnection.getConfig().get("APPNAME"));
    }
}
