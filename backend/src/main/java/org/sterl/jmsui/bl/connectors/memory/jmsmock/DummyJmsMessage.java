package org.sterl.jmsui.bl.connectors.memory.jmsmock;

import java.time.Instant;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.UUID;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.sterl.jmsui.api.JmsHeaderRequestValues;

import lombok.Getter;

public class DummyJmsMessage implements TextMessage {
    @Getter
    private JmsHeaderRequestValues headers;
    private String msg;
    
    public DummyJmsMessage(String msg, JmsHeaderRequestValues headers) {
        this.msg = msg;
        this.headers = headers;
        if (this.headers == null) {
            this.headers = new JmsHeaderRequestValues();
        }
        if (this.headers.getJMSTimestamp() == null) {
            this.headers.setJMSTimestamp(Instant.now().toEpochMilli());
        }
        if (this.headers.getJMSMessageID() == null) {
            this.headers.setJMSMessageID(UUID.randomUUID().toString());
        }
        if (this.headers.getJMSPriority() == null) {
            this.headers.setJMSPriority(4);
        }
    }
    
    @Override
    public void setText(String string) throws JMSException {
        this.msg = string;
    }

    @Override
    public String getText() throws JMSException {
        return this.msg;
    }

    @Override
    public String getJMSMessageID() throws JMSException {
        return this.headers.getJMSMessageID();
    }

    @Override
    public void setJMSMessageID(String id) throws JMSException {
        this.headers.setJMSMessageID(id);
    }

    @Override
    public long getJMSTimestamp() throws JMSException {
        return this.headers.getJMSTimestamp();
    }

    @Override
    public void setJMSTimestamp(long timestamp) throws JMSException {
        this.headers.setJMSTimestamp(timestamp);
    }

    @Override
    public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
        throw new RuntimeException("Not Supported");
    }

    @Override
    public void setJMSCorrelationIDAsBytes(byte[] correlationID) throws JMSException {
        throw new RuntimeException("Not Supported");
    }

    @Override
    public void setJMSCorrelationID(String correlationID) throws JMSException {
        this.headers.setJMSCorrelationID(correlationID);
    }

    @Override
    public String getJMSCorrelationID() throws JMSException {
        return this.headers.getJMSCorrelationID();
    }

    @Override
    public Destination getJMSReplyTo() throws JMSException {
        return new Destination() {
            public String toString() {
                return "MEMORY.QUEUE";
            }
        };
    }

    @Override
    public void setJMSReplyTo(Destination replyTo) throws JMSException {
        throw new RuntimeException("Not Supported");
    }

    @Override
    public Destination getJMSDestination() throws JMSException {
        return new Destination() {
            public String toString() {
                return "MEMORY.QUEUE";
            }
        };
    }

    @Override
    public void setJMSDestination(Destination destination) throws JMSException {
        throw new RuntimeException("Not Supported");
    }

    @Override
    public int getJMSDeliveryMode() throws JMSException {
        return DeliveryMode.NON_PERSISTENT;
    }

    @Override
    public void setJMSDeliveryMode(int deliveryMode) throws JMSException {
        throw new RuntimeException("Not Supported");
    }

    @Override
    public boolean getJMSRedelivered() throws JMSException {
        return false;
    }

    @Override
    public void setJMSRedelivered(boolean redelivered) throws JMSException {
        throw new RuntimeException("Not Supported");
    }

    @Override
    public String getJMSType() throws JMSException {
        return this.headers.getJMSType();
    }

    @Override
    public void setJMSType(String type) throws JMSException {
        this.headers.setJMSType(type);
    }

    @Override
    public long getJMSExpiration() throws JMSException {
        return 0;
    }

    @Override
    public void setJMSExpiration(long expiration) throws JMSException {
        throw new RuntimeException("Not Supported");
    }

    @Override
    public long getJMSDeliveryTime() throws JMSException {
        return Instant.now().toEpochMilli();
    }

    @Override
    public void setJMSDeliveryTime(long deliveryTime) throws JMSException {
        throw new RuntimeException("Not Supported");
    }

    @Override
    public int getJMSPriority() throws JMSException {
        return this.headers.getJMSPriority();
    }

    @Override
    public void setJMSPriority(int priority) throws JMSException {
        throw new RuntimeException("Not Supported");
    }

    @Override
    public void clearProperties() throws JMSException {
        throw new RuntimeException("Not Supported");
    }

    @Override
    public boolean propertyExists(String name) throws JMSException {
        return this.headers.getProperties().containsKey(name);
    }

    @Override
    public boolean getBooleanProperty(String name) throws JMSException {
        return Boolean.parseBoolean(
                valueOf(this.headers.getProperties().get(name)));
    }

    @Override
    public byte getByteProperty(String name) throws JMSException {
        throw new RuntimeException("Not Supported");
    }

    @Override
    public short getShortProperty(String name) throws JMSException {
        throw new RuntimeException("Not Supported");
    }

    @Override
    public int getIntProperty(String name) throws JMSException {
        throw new RuntimeException("Not Supported");
    }

    @Override
    public long getLongProperty(String name) throws JMSException {
        throw new RuntimeException("Not Supported");
    }

    @Override
    public float getFloatProperty(String name) throws JMSException {
        throw new RuntimeException("Not Supported");
    }

    @Override
    public double getDoubleProperty(String name) throws JMSException {
        throw new RuntimeException("Not Supported");
    }

    @Override
    public String getStringProperty(String name) throws JMSException {
        return valueOf(this.headers.getProperties().get(name));
    }

    @Override
    public Object getObjectProperty(String name) throws JMSException {
        return this.headers.getProperties().get(name);
    }

    @Override
    public Enumeration getPropertyNames() throws JMSException {
        final Iterator<String> iterator = this.headers.getProperties().keySet().iterator();
        return new Enumeration<String>() {
            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }
            @Override
            public String nextElement() {
                return iterator.next();
            }
        };
    }

    @Override
    public void setBooleanProperty(String name, boolean value) throws JMSException {
        this.headers.getProperties().put(name, value);
    }

    @Override
    public void setByteProperty(String name, byte value) throws JMSException {
        this.headers.getProperties().put(name, value);
    }

    @Override
    public void setShortProperty(String name, short value) throws JMSException {
        this.headers.getProperties().put(name, value);
    }

    @Override
    public void setIntProperty(String name, int value) throws JMSException {
        this.headers.getProperties().put(name, value);
    }

    @Override
    public void setLongProperty(String name, long value) throws JMSException {
        this.headers.getProperties().put(name, value);
    }

    @Override
    public void setFloatProperty(String name, float value) throws JMSException {
        this.headers.getProperties().put(name, value);
    }

    @Override
    public void setDoubleProperty(String name, double value) throws JMSException {
        this.headers.getProperties().put(name, value);
    }

    @Override
    public void setStringProperty(String name, String value) throws JMSException {
        this.headers.getProperties().put(name, value);
    }

    @Override
    public void setObjectProperty(String name, Object value) throws JMSException {
        this.headers.getProperties().put(name, value);
    }

    @Override
    public void acknowledge() throws JMSException {
        throw new RuntimeException("Not Supported");
    }

    @Override
    public void clearBody() throws JMSException {
        this.msg = null;
    }

    @Override
    public <T> T getBody(Class<T> c) throws JMSException {
        if (c.isAssignableFrom(String.class)) {
            return (T)this.msg;
        }
        throw new RuntimeException(c + " is not supported, only supporting strings.");
    }

    @Override
    public boolean isBodyAssignableTo(Class c) throws JMSException {
        return c.isAssignableFrom(String.class);
    }

    String valueOf(Object v) {
        if (v == null) return null;
        return v.toString();
    }
}
