package org.sterl.jmsui.bl.connectors.api;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.sterl.jmsui.bl.common.spring.JsonRestController;
import org.sterl.jmsui.bl.session.control.JmsSessionBM;

@JsonRestController("/api/connectors")
public class ConnectorBF {
    public static final String BASE_URL = "/api/connectors";

    @Autowired JmsSessionBM sessionBM;

    @GetMapping
    public Collection<SupportedConnector> get() {
        return sessionBM.getSupportedJmsFactories();
    }
}
