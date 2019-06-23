package org.sterl.jmsui.bl.connectors.ibm;

import static org.sterl.jmsui.bl.common.config.ConfigParser.asInt;
import static org.sterl.jmsui.bl.common.config.ConfigParser.asString;
import static org.sterl.jmsui.bl.common.config.ConfigParser.isSet;
import static org.sterl.jmsui.bl.common.config.ConfigParser.parse;

import java.util.Hashtable;
import java.util.Map;

import org.springframework.jms.core.JmsTemplate;
import org.sterl.jmsui.api.ConfigMetaData;
import org.sterl.jmsui.api.ConfigMetaData.ConfigType;
import org.sterl.jmsui.bl.connection.model.JmsConnection;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

public class IbmMqConnectorFactory {
    
    private static final ConfigMetaData<?>[] CONFIG = {
        ConfigMetaData.<String>builder().property("APPNAME")
            .label("App Name").defaultValue("JMS UI")
            .description("This name is added to the connection to identify this client.").mandatory(false).build(),
        ConfigMetaData.<String>builder().property("queue_manager").label("Queue Manager").build(),
        ConfigMetaData.<String>builder().property("hostname").defaultValue("localhost").build(),
        ConfigMetaData.<Integer>builder().property("port").type(ConfigType.NUMBER) .defaultValue(1414).build(),
        ConfigMetaData.<String>builder().property("channel").defaultValue("localhost").defaultValue("SYSTEM.DEF.SVRCONN") .build(),
        ConfigMetaData.<String>builder().property("userID").label("user").mandatory(false).build(),
        ConfigMetaData.<String>builder().property("password").type(ConfigType.PASSWORD).mandatory(false).build()
    };
    
    public ConfigMetaData<?>[] getConfigMetaData() {
        return CONFIG;
    }

    public IbmMqConnector create(JmsConnection jmsResource) throws Exception {
        Map<String, Object> rawConfig = parse(CONFIG, jmsResource.getConfigValues());
        //System.out.println(rawConfig);
        Hashtable<String, Object> config = new Hashtable<>(rawConfig.size());
        rawConfig.forEach((k, v) -> {
            if (isSet(k) && isSet(v) && !"queue_manager".equals(k)) {
                config.put(k, v);
            }
        });
        
        JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
        JmsConnectionFactory cf = ff.createConnectionFactory();
        cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, asString(rawConfig, "queue_manager"));
        cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, asString(rawConfig, "hostname"));
        cf.setIntProperty(WMQConstants.WMQ_PORT, asInt(rawConfig, "port"));
        cf.setStringProperty(WMQConstants.WMQ_CHANNEL, asString(rawConfig, "channel"));
        cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
        
        if (asString(rawConfig, "APPNAME") != null) {
            cf.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME, asString(rawConfig, "APPNAME"));
        }
        
        if (isSet(rawConfig.get("userID"))) {
            cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
            cf.setStringProperty(WMQConstants.USERID, asString(rawConfig, "userID"));
            cf.setStringProperty(WMQConstants.PASSWORD, asString(rawConfig, "password"));
        }
        JmsTemplate jmsTemplate = new JmsTemplate(cf);
        if (jmsResource.getTimeout() != null) jmsTemplate.setReceiveTimeout(jmsResource.getTimeout());
        
        return new IbmMqConnector(cf.getStringProperty(WMQConstants.WMQ_QUEUE_MANAGER), jmsTemplate, config);
    }
}
