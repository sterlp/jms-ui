package org.sterl.jmsui.bl.connectors.ibm;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.sterl.jmsui.bl.connection.model.JmsConnectionBE;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;

public class IbmMqConnectorFactoryTest {

    @Test
    public void testSettings() throws Exception {
        JmsConnectionBE jmsConnection = new JmsConnectionBE();
        jmsConnection.setTimeout(1500L);
        jmsConnection.addOrSetConfig("queue_manager", "qmgr")
            .addOrSetConfig("hostname", "host")
            .addOrSetConfig("port", "111")
            .addOrSetConfig("channel", "a.channel")
            .addOrSetConfig("userID", "user")
            .addOrSetConfig("password", "pass")
            .addOrSetConfig("APPNAME", "myApp");

        IbmMqConnector ibmConnector = new IbmMqConnectorFactory().create(jmsConnection);
        
        JmsConnectionFactory cf = ibmConnector.connectionFactory;
        
        assertEquals("qmgr", cf.getStringProperty(WMQConstants.WMQ_QUEUE_MANAGER));
        assertEquals("host", cf.getStringProperty(WMQConstants.WMQ_HOST_NAME));
        assertEquals(111, cf.getIntProperty(WMQConstants.WMQ_PORT));
        assertEquals("a.channel", cf.getStringProperty(WMQConstants.WMQ_CHANNEL));
        assertEquals(WMQConstants.WMQ_CM_CLIENT, cf.getIntProperty(WMQConstants.WMQ_CONNECTION_MODE));
        
        
        assertEquals(true, cf.getBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP));
        assertEquals("user", cf.getStringProperty(WMQConstants.USERID));
        assertEquals("pass", cf.getStringProperty(WMQConstants.PASSWORD));
        
        assertEquals("myApp", cf.getStringProperty(WMQConstants.WMQ_APPLICATIONNAME));
        
        assertEquals("host", ibmConnector.getConfig().get("hostname"));
        assertEquals(111, ibmConnector.getConfig().get("port"));
        assertEquals("a.channel", ibmConnector.getConfig().get("channel"));
        assertEquals("user", ibmConnector.getConfig().get("userID"));
        assertEquals("pass", ibmConnector.getConfig().get("password"));
        assertEquals("myApp", ibmConnector.getConfig().get("APPNAME"));
    }
}
