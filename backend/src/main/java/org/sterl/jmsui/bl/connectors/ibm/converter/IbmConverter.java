package org.sterl.jmsui.bl.connectors.ibm.converter;

import org.springframework.core.convert.converter.Converter;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource.Type;
import org.sterl.jmsui.bl.connectors.ibm.model.QTypes;

public class IbmConverter {

    public enum ToJmsResourceType implements Converter<QTypes, JmsResource.Type> {
        INSTANCE;

        @Override
        public Type convert(QTypes source) {
            if (source == QTypes.MQQT_REMOTE) return Type.REMOTE_QUEUE;
            else return Type.QUEUE;
        }
    }
}
