package org.sterl.jmsui.bl.connectors.api;

import org.sterl.jmsui.api.ConfigMetaData;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class SupportedConnector {
    private final String id;
    private final String name;
    private final ConfigMetaData<?>[] configMeta;
}
