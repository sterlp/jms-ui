package org.sterl.jmsui.api;

import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.springframework.core.convert.converter.Converter;
import org.sterl.jmsui.bl.session.api.JmsResultMessage;

public class JmsMessageConverter {
    public enum ToJmsResultMessage implements Converter<Message, JmsResultMessage> {
        INSTANCE;

        @Override
        public JmsResultMessage convert(Message source) {
            if (source == null) return null;
            
            String body;
            try {
                if (source instanceof TextMessage) {
                    body = ((TextMessage)source).getText();
                } else if (source instanceof BytesMessage) {
                    BytesMessage message = (BytesMessage)source;
                    byte[] bytes = new byte[(int) message.getBodyLength()];
                    message.readBytes(bytes);
                    body = new String(bytes, Charset.forName("UTF-8"));
                } else {
                    body = source.getBody(String.class);
                }
            } catch (JMSException e) {
                throw new JMSRuntimeException("Failed convert JMS message.", e.getErrorCode(), e);
            }
            return new JmsResultMessage(body, ToJmsHeaderResultValues.INSTANCE.convert(source));
        }
    }

    public enum ToJmsHeaderResultValues implements Converter<Message, JmsHeaderResultValues> {
        INSTANCE;

        @Override
        public JmsHeaderResultValues convert(Message source) {
            if (source == null) return null;
            try {
                LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
                final Enumeration<String> propertyNames = source.getPropertyNames();
                while (propertyNames.hasMoreElements()) {
                    final String key = propertyNames.nextElement();
                    properties.put(key, source.getObjectProperty(key));
                }
                // sort by key
                properties = properties.entrySet().stream().sorted(Map.Entry.comparingByKey())
                                       .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2, LinkedHashMap::new));


                return JmsHeaderResultValues.builder()
                        .JMSType(source.getJMSType())
                        .JMSDeliveryMode(source.getJMSDeliveryMode())
                        .JMSPriority(source.getJMSPriority())
                        .JMSRedelivered(source.getJMSRedelivered())
                        .JMSDestination(valueOf(source.getJMSDestination()))
                        .JMSReplyTo(valueOf(source.getJMSReplyTo()))
                        .JMSTimestamp(source.getJMSTimestamp())
                        .JMSExpiration(source.getJMSExpiration())
                        .JMSDeliveryTime(source.getJMSDeliveryTime())
                        .JMSMessageID(source.getJMSMessageID())
                        .JMSCorrelationID(source.getJMSCorrelationID())
                        .properties(properties)
                        .build();

            } catch (JMSException e) {
                throw new JMSRuntimeException("Failed access JMS message header values.", e.getErrorCode(), e);
            }
        }
        static String valueOf(Destination jmsDestination) {
            return jmsDestination == null ? null : jmsDestination.toString();
        }
    }
}
