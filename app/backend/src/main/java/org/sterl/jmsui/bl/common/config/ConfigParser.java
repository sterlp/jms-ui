package org.sterl.jmsui.bl.common.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.sterl.jmsui.api.ConfigMetaData;
import org.sterl.jmsui.api.ConfigMetaData.ConfigType;
import org.sterl.jmsui.bl.connection.model.ConfigValueBE;

public class ConfigParser {

    public static boolean isBlank(String v) {
        if (v == null) return true;
        if (v.length() == 0) return true;
        if (v.strip().length() == 0) return true;
        return false;
    }
    public static boolean isSet(String v) {
        return !isBlank(v);
    }
    public static boolean isSet(Object v) {
        if (v == null) return false;

        if (v instanceof String) return !isBlank((String)v);
        else return null != v;
    }
    
    public static boolean isSet(Map<String, Object> map, String key) {
        return isSet(map.get(key));
    }
    
    public static String asString(Map<String, Object> map, String key) {
        final Object v = map.get(key);
        if (v == null) return null;
        if (v instanceof String) return (String)v;
        else return String.valueOf(v);
    }
    public static Integer asInt(Map<String, Object> map, String key) {
        final Object v = map.get(key);
        if (v == null) return null;
        if (v instanceof Integer) return (Integer)v;
        else return Integer.valueOf( String.valueOf(v) );
    }
    public static boolean asBoolean(Map<String, Object> rawConfig, String key, boolean defaultValue) {
        final Object rawValue = rawConfig.get(key);
        if (rawValue == null) return defaultValue;
        else if (rawValue instanceof Boolean) return (Boolean)(rawValue);
        else return Boolean.parseBoolean(rawValue.toString());
    }
    
    public static Map<String, Object> parse(ConfigMetaData<?>[] configMeta, List<ConfigValueBE> configValueBEs) {
        Map<String, Object> result = new HashMap<>();
        for (int i = 0; i < configMeta.length; i++) {
            final ConfigMetaData<?> cmd = configMeta[i];
            final Optional<ConfigValueBE> cv = configValueBEs.stream().filter(v -> v.getName().equals(cmd.getProperty())).findFirst();

            result.put(cmd.getProperty(), parse(cmd, cv));
        }
        return result;
    }
    
    public static Object parse(ConfigMetaData<?> cmd, Optional<ConfigValueBE> value) {
        if (value.isEmpty()) return null;

        final ConfigValueBE configValueBE = value.get();
        if (cmd.getType() == ConfigType.NUMBER) {
            return Integer.valueOf(configValueBE.getValue());
        } else if (cmd.getType() == ConfigType.BOOLEAN) {
            return Boolean.valueOf(configValueBE.getValue());
        } else {
            return configValueBE.getValue();
        }
    }
}
