package org.sterl.jmsui.bl.connectors.common;

import java.util.Map.Entry;

import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;

import org.sterl.jmsui.api.JmsHeaderRequestValues;

public class JmsHeaderUtil {
    
    public static <T> T getOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    public static void setMeassageHeader(JmsHeaderRequestValues header, Message message) throws JMSRuntimeException {
        if (header != null) {
            try {
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
                        } if (headerValue.getValue() instanceof Integer) {
                            message.setIntProperty(headerValue.getKey(), (Integer)headerValue.getValue());
                        } if (headerValue.getValue() instanceof Long) {
                            message.setLongProperty(headerValue.getKey(), (Long)headerValue.getValue());
                        } if (headerValue.getValue() instanceof Double) {
                            message.setDoubleProperty(headerValue.getKey(), (Double)headerValue.getValue());
                        } if (headerValue.getValue() instanceof Number) {
                            message.setDoubleProperty(headerValue.getKey(), ((Number)headerValue.getValue()).doubleValue());
                        } if (headerValue.getValue() instanceof String) {
                            message.setStringProperty(headerValue.getKey(), (String)headerValue.getValue());
                        } else {
                            message.setObjectProperty(headerValue.getKey(), headerValue.getValue());
                        }
                    }
                }
            } catch (JMSException e) {
                throw new JMSRuntimeException("Failed to set JMS headers to JMS message.", e.getErrorCode(), e);
            }
        }
    }
}
