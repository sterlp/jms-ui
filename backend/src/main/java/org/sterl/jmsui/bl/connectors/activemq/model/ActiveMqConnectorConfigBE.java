package org.sterl.jmsui.bl.connectors.activemq.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE) @Builder 
public class ActiveMqConnectorConfigBE {

    @Getter
    private final long defaultTimeout;
    private final boolean hasStatisticsPlugin;

    public boolean hasStatisticsPlugin() {
        return hasStatisticsPlugin;
    }
}
