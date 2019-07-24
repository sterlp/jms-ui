package org.sterl.jmsui.bl.session.api;

import javax.validation.constraints.NotNull;

import org.sterl.jmsui.api.JmsHeaderRequestValues;

import lombok.Data;

@Data
public class SendJmsMessageCommand {
    @NotNull
    private String body;
    private JmsHeaderRequestValues header;
}
