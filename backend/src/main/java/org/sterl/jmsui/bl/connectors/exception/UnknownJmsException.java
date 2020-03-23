package org.sterl.jmsui.bl.connectors.exception;

import javax.jms.JMSException;

public class UnknownJmsException extends JMSException {

    public UnknownJmsException(String message, Exception cause) {
        this(message, null, cause);
    }
    
    public UnknownJmsException(String message, String errorCode, Exception cause) {
        super(message, errorCode);
        setLinkedException(cause);
        if (cause != null && cause != this) initCause(cause);
    }
}
