package org.sterl.jmsui.bl.connectors.memory;

import org.sterl.jmsui.api.ConfigMetaData;
import org.sterl.jmsui.bl.connection.model.JmsConnectionBE;
import org.sterl.jmsui.bl.connectors.api.JmsConnectorInstanceFactory;

public class MemoryQueueConnectorFactory implements JmsConnectorInstanceFactory {
    
    private static final ConfigMetaData<?>[] CONFIG = {
        ConfigMetaData.<String>builder().property("example")
            .label("Example Property")
            .description("Just one configuration property which isn't used.").mandatory(false).build(),
    };
    
    public String getName() {
        return "Memory Queue";
    }
    
    public ConfigMetaData<?>[] getConfigMetaData() {
        return CONFIG;
    }

    public MemoryQueueConnector create(JmsConnectionBE jmsResource) throws Exception {
        return new MemoryQueueConnector();
    }
}
