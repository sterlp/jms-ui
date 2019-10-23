package org.sterl.jmsui.bl.connection.api;

import java.util.Map.Entry;

import org.springframework.core.convert.converter.Converter;
import org.sterl.jmsui.bl.connection.api.model.JmsConnectionDetails;
import org.sterl.jmsui.bl.connection.model.JmsConnectionBE;

class ConnectionConverter {
    enum ToJmsConnectionDetails  implements Converter<JmsConnectionBE, JmsConnectionDetails> {
        INSTANCE;

        @Override
        public JmsConnectionDetails convert(JmsConnectionBE source) {
            if (source == null) return null;
            final JmsConnectionDetails result = new JmsConnectionDetails();
            result.setClientName(source.getClientName());
            result.setId(source.getId());
            result.setName(source.getName());
            result.setTimeout(source.getTimeout());
            result.setType(source.getType());
            result.setVersion(source.getVersion());
            source.getConfigValues().forEach(cv -> result.addConfig(cv.getName(), cv.getValue()));
            return result;
        }
        
    }
    enum ToJmsConnection implements Converter<JmsConnectionDetails, JmsConnectionBE> {
        INSTANCE;

        @Override
        public JmsConnectionBE convert(JmsConnectionDetails source) {
            if (source == null) return null;
            JmsConnectionBE result = new JmsConnectionBE();
            setValues(source, result);
            return result;
        }
        static void setValues(JmsConnectionDetails s, JmsConnectionBE t) {
            t.setClientName(s.getClientName());
            t.setId(s.getId());
            t.setName(s.getName());
            t.setTimeout(s.getTimeout());
            t.setType(s.getType());
            t.setVersion(s.getVersion());
            if (s.getConfigValues() != null) {
                t.removeOthers(s.getConfigValues().keySet());
                for(Entry<String, String> e : s.getConfigValues().entrySet()) {
                    t.addOrSetConfig(e.getKey(), e.getValue());
                }
            }
        }
    }
}