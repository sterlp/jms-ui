package org.sterl.jmsui.bl.connectors.util;

import java.util.Map.Entry;

import javax.jms.JMSException;
import javax.jms.Message;

import org.sterl.jmsui.api.JmsHeaderRequestValues;

public class JmsHeaderUtil {

    public static void setMeassageHeader(JmsHeaderRequestValues header, Message message) throws JMSException {
        if (header != null) {
            if (header.getJMSExpiration() != null) message.setJMSExpiration(header.getJMSExpiration());
            if (header.getJMSCorrelationID() != null) message.setJMSCorrelationID(header.getJMSCorrelationID());
            if (header.getJMSDeliveryMode() != null) message.setJMSDeliveryMode(header.getJMSDeliveryMode());
            if (header.getJMSMessageID() != null) message.setJMSMessageID(header.getJMSMessageID());
            if (header.getJMSPriority() != null) message.setJMSPriority(header.getJMSPriority());
            if (header.getJMSTimestamp() != null) message.setJMSTimestamp(header.getJMSTimestamp());
            if (header.getJMSType() != null) message.setJMSType(header.getJMSType());
            if (header.getProperties() != null && !header.getProperties().isEmpty()) {
                for (Entry<String, Object> headerValue : header.getProperties().entrySet()) {
                    if (headerValue.getValue() instanceof Boolean) {
                        message.setBooleanProperty(headerValue.getKey(), (Boolean)headerValue.getValue());
                    } if (headerValue.getValue() instanceof Double) {
                        message.setDoubleProperty(headerValue.getKey(), (Double)headerValue.getValue());
                    } if (headerValue.getValue() instanceof String) {
                        message.setStringProperty(headerValue.getKey(), (String)headerValue.getValue());
                    } else {
                        message.setObjectProperty(headerValue.getKey(), headerValue.getValue());
                    }
                }
            }
        }
    }
}
