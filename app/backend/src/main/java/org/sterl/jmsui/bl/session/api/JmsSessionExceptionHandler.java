package org.sterl.jmsui.bl.session.api;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSSecurityException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class JmsSessionExceptionHandler {

    /**
timestamp: "2020-03-22T14:33:10.584+0000"
status: 500
error: "Internal Server Error"
message: "Failed to connect to JmsConnectionBE(id=44, type=org.sterl.jmsui.bl.connectors.ibm.IbmMqConnectorFactory, version=1, name=IBM User, clientName=JMS UI, timeout=10000)"
trace: "java.lang.RuntimeException: Failed to connect to JmsConnectionBE(id=44, type=org.sterl.jmsui.bl.connectors.ibm.IbmMqConnectorFactory, version=1, name=IBM User, clientName=JMS UI, timeout=10000)
↵   at org.sterl.jmsui.bl.session.control.SessionBA.connect(SessionBA.java:72)
↵   at org.sterl.jmsui.bl.session.control.JmsSessionBM.connect(JmsSessionBM.java:67)
path: "/api/sessions/44"
     */
    
    @ExceptionHandler(JMSSecurityException.class)
    public ResponseEntity<?> handleConstraintViolation(JMSSecurityException e, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", Instant.now());
        result.put("error", e.getClass().getSimpleName());
        result.put("message", e.getMessage());
        result.put("trace", getStackTrace(e));
        result.put("path", request.getRequestURL());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
    }
    
    String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
