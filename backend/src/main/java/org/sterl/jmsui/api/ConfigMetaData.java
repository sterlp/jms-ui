package org.sterl.jmsui.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * As the configuration of a connector may contain generic values, this helps a bit to define what we have.
 */
@Data @AllArgsConstructor @Builder
public class ConfigMetaData<T> {
    public static final ConfigMetaData<String> USER = ConfigMetaData.<String>builder().property("user").label("User").description("Optional user name for authentication.").mandatory(false).build();
    public static final ConfigMetaData<String> PASSWORD = ConfigMetaData.<String>builder().property("password").label("Password").type(ConfigType.PASSWORD).mandatory(false).build();

    public enum ConfigType {
        STRING,
        NUMBER,
        BOOLEAN,
        PASSWORD
    }
    @NonNull
    private final String property;
    private final String label;
    private final String description;
    private final T defaultValue;
    @Builder.Default @NonNull
    private final ConfigType type = ConfigType.STRING;
    @Builder.Default
    private final boolean mandatory = true;
}