package org.sterl.jmsui.bl.connectors.activemq;

import static org.sterl.jmsui.bl.common.config.ConfigParser.asString;
import static org.sterl.jmsui.bl.common.config.ConfigParser.*;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.sterl.jmsui.api.ConfigMetaData;
import org.sterl.jmsui.api.ConfigMetaData.ConfigType;
import org.sterl.jmsui.bl.connection.model.JmsConnectionBE;
import org.sterl.jmsui.bl.connectors.activemq.control.ActiveMqConnector;
import org.sterl.jmsui.bl.connectors.activemq.model.ActiveMqConnectorConfigBE;
import org.sterl.jmsui.bl.connectors.activemq.model.ActiveMqConnectorConfigBE.ActiveMqConnectorConfigBEBuilder;
import org.sterl.jmsui.bl.connectors.api.JmsConnectorInstanceFactory;


/**
 * https://spring.io/guides/gs/messaging-jms/
 */
@Component
public class ActiveMqConnectorFactory implements JmsConnectorInstanceFactory {
    @Value("${jms-ui.embedded.activemq.port:61616}")
    private int activeMqPort;

    private ConfigMetaData<?>[] CONFIG;
    @PostConstruct
    void created() {
        CONFIG = new ConfigMetaData<?>[] {
                ConfigMetaData.<String>builder().property("clientID")
                    .label("Client ID").defaultValue("JMS UI")
                    .description("This name is added to the connection to identify this client.").mandatory(false).build(),
                ConfigMetaData.<String>builder().property("brokerURL").label("Broker URL") .defaultValue("tcp://127.0.0.1:" + activeMqPort).mandatory(true).build(),
                ConfigMetaData.<String>builder().property("hasStatisticsPlugin")
                              .description("If the ActiveMQ server has the StatisticsPlugin enabled to query Queue and Topic statistics.")
                              .label("Statistics Plugin Supported").mandatory(true).type(ConfigType.BOOLEAN).build(),
                ConfigMetaData.USER,
                ConfigMetaData.PASSWORD
            };
    }

    @Override
    public String getId() {
        return ActiveMqConnectorFactory.class.getName();
    }
    public String getName() {
        return "ActiveMQ";
    }
    public ConfigMetaData<?>[] getConfigMetaData() {
        return CONFIG;
    }

    public ActiveMqConnector create(JmsConnectionBE jmsResource) throws Exception {
        final Map<String, Object> rawConfig = parse(CONFIG, jmsResource.getConfigValues());
        
        final ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(asString(rawConfig, "brokerURL"));
        if (rawConfig.containsKey("user")) {
            cf.setUserName(asString(rawConfig, "user"));
        }
        if (rawConfig.containsKey("password")) {
            cf.setUserName(asString(rawConfig, "password"));
        }
        final ActiveMqConnectorConfigBEBuilder config = ActiveMqConnectorConfigBE.builder(); 
        config.defaultTimeout(jmsResource.getTimeout() == null ? 500L : jmsResource.getTimeout());
        config.hasStatisticsPlugin(asBoolean(rawConfig, "hasStatisticsPlugin", false));

        return new ActiveMqConnector(cf, config.build());
    }
}
