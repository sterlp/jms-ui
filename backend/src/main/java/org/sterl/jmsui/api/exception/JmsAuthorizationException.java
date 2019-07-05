package org.sterl.jmsui.api.exception;

public class JmsAuthorizationException extends RuntimeException {

    public JmsAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public JmsAuthorizationException(String message) {
        super(message);
    }
}
