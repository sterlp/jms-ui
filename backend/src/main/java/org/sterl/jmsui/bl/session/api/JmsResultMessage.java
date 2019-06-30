package org.sterl.jmsui.bl.session.api;

import org.sterl.jmsui.api.JmsHeaderResultValues;

import lombok.Data;


@Data
public class JmsResultMessage {
    private final String body;
    private final JmsHeaderResultValues header;
}
