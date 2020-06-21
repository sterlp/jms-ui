package org.sterl.jmsui.bl.connectors.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class JmsResource {

    public enum Type {
        QUEUE,
        REMOTE_QUEUE,
        TOPIC,
    }
    
    private String name;
    private Type type;
    private String vendorType;
}
