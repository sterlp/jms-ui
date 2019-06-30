package org.sterl.jmsui.api;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import org.springframework.core.convert.converter.Converter;

public class JmsMessageConverter {
    public enum ToJmsHeaderResultValues implements Converter<Message, JmsHeaderResultValues> {
        INSTANCE;

        @Override
        public JmsHeaderResultValues convert(Message source) {
            if (source == null) return null;
            try {
                final Map<String, Object> properties = new LinkedHashMap<>();
                final Enumeration<String> propertyNames = source.getPropertyNames();
                while (propertyNames.hasMoreElements()) {
                    String key = propertyNames.nextElement();
                    properties.put(key, source.getObjectProperty(key));
                }
                
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
                throw new RuntimeException(e);
            }
        }
        static String valueOf(Destination jmsDestination) {
            return jmsDestination == null ? null : jmsDestination.toString();
        }
    }
    
}
