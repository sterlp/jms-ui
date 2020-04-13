package org.sterl.jmsui.bl.connectors.control;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sterl.jmsui.bl.connectors.api.JmsConnectorInstanceFactory;
import org.sterl.jmsui.bl.connectors.api.SupportedConnector;

@Component
public class JmsConnectorBM {

    @Autowired private List<JmsConnectorInstanceFactory> factories;
    
    public Collection<SupportedConnector> getSupportedJmsFactories() {
        return factories.stream().map(f -> {
            return SupportedConnector.builder()
                .id(f.getId())
                .name(f.getName())
                .configMeta(f.getConfigMetaData())
                .build();
            }
        ).collect(Collectors.toList());
    }
    
    public Optional<JmsConnectorInstanceFactory> getFactory(String name) {
        return factories.stream().filter(f -> f.getId().equals(name)).findFirst();
    }
}
