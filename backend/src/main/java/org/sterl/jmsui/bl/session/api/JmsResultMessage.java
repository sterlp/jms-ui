package org.sterl.jmsui.bl.session.api;

import org.sterl.jmsui.api.JmsHeaderResultValues;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data  @AllArgsConstructor @NoArgsConstructor
public class JmsResultMessage {
    private String body;
    private JmsHeaderResultValues header;
}
