package org.sterl.jmsui.bl.connectors.api;

import org.sterl.jmsui.api.ConfigMetaData;
import org.sterl.jmsui.bl.connection.model.JmsConnectionBE;

public interface JmsConnectorInstanceFactory {
    String getId();
    String getName();
    ConfigMetaData<?>[] getConfigMetaData();
    JmsConnectorInstance create(JmsConnectionBE jmsResource) throws Exception;
}
