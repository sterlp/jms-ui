package org.sterl.jmsui.api;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Common class which represents the JMS header values.
 */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class JmsHeaderResultValues {
    private String JMSType;
    
    private int JMSDeliveryMode;
    private int JMSPriority;
    
    private boolean JMSRedelivered;
    private String JMSDestination;
    private String JMSReplyTo;
    
    private long JMSTimestamp;
    private long JMSExpiration;
    /**
     * This is the the difference, measured in milliseconds, 
     * between the delivery time and midnight, January 1, 1970 UTC.
     */
    private Long JMSDeliveryTime;
    
    private String JMSMessageID;
    private String JMSCorrelationID;
    
    @Builder.Default
    private Map<String, Object> properties = new LinkedHashMap<>();

}
