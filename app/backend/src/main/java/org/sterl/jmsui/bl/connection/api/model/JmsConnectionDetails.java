package org.sterl.jmsui.bl.connection.api.model;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class JmsConnectionDetails {
    private Long id;
    @NotNull
    private String type;
    private Long version;
    @NotNull @Size(min = 1)
    private String name;
    private Long timeout;
    private Map<String, String> configValues = new LinkedHashMap<>();
    
    public JmsConnectionDetails addConfig(String key, String value) {
        configValues.put(key, value);
        return this;
    }
}
