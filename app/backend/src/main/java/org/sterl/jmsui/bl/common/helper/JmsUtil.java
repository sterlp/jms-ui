package org.sterl.jmsui.bl.common.helper;

import java.io.Closeable;

import javax.jms.*;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.sterl.jmsui.bl.connectors.api.model.JmsResource.Type;

import com.ibm.msg.client.jms.JmsSession;

public class JmsUtil {
    /**
     * JMS 1.x way to create a {@link Destination}
     */
    public static Destination createDestionation(String destination, Type jmsType, JmsSession s) throws JMSException {
        return jmsType == Type.TOPIC ? s.createTopic(destination) : s.createQueue(destination);
    }
    /**
     * JMS 2.x way to create a {@link Destination}
     */
    public static Destination createDestionation(String destination, Type jmsType, JMSContext c) {
        return jmsType == Type.TOPIC ? c.createTopic(destination) : c.createQueue(destination);
    }

    public static Exception close(MessageConsumer consumer) {
        Exception result = null;
        if (consumer != null) {
            try {
                consumer.close();
            } catch (Exception e) {
                result = e;
            }
        }
        return result;
    }

    public static Exception close(Session session) {
        Exception result = null;
        if (session != null) {
            try {
                session.close();
            } catch (Exception e) {
                result = e;
            }
        }
        return result;
    }

    public static Exception close(Connection connection) {
        Exception result = null;
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                result = e;
            }
        }
        return result;
        
    }

    public static Exception close(JMSContext context) {
        Exception result = null;
        if (context != null) {
            try {
                context.close();
            } catch (Exception e) {
                result = e;
            }
        }
        return result;
    }
    
    public static Exception close(JMSConsumer consomer) {
        Exception result = null;
        if (consomer != null) {
            try {
                consomer.close();
            } catch (Exception e) {
                result = e;
            }
        }
        return result;
    }

    public static Exception close(Closeable toClose) {
        Exception result = null;
        if (toClose != null) {
            try {
                toClose.close();
            } catch (Exception e) {
                result = e;
            }
        }
        return result;
    }
}
