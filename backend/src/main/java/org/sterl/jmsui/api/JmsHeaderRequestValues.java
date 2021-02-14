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
public class JmsHeaderRequestValues {
    private String JMSType;
    
    private Integer JMSDeliveryMode;
    private Integer JMSPriority;
    
    private Long JMSTimestamp;
    private Long JMSExpiration;
    
    private String JMSMessageID;
    private String JMSCorrelationID;
    
    @Builder.Default
    private Map<String, Object> properties = new LinkedHashMap<>();

}
