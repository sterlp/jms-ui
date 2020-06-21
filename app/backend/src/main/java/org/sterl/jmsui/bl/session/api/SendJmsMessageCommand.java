package org.sterl.jmsui.bl.session.api;

import javax.validation.constraints.NotNull;

import org.sterl.jmsui.api.JmsHeaderRequestValues;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource.Type;

import lombok.Data;

@Data
public class SendJmsMessageCommand {
    @NotNull
    private String body;
    private String destination;
    private Type destinationType = Type.QUEUE;
    private JmsHeaderRequestValues header;
}
