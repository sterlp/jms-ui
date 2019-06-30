package org.sterl.jmsui.bl.session.api;

import java.nio.charset.Charset;
import java.util.List;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.sterl.jmsui.api.JmsMessageConverter.ToJmsHeaderResultValues;
import org.sterl.jmsui.bl.common.spring.JsonRestController;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource;
import org.sterl.jmsui.bl.session.control.JmsSessionBM;

@Transactional
@JsonRestController("api/jms/sessions")
public class JmsSessionBF {

    @Autowired JmsSessionBM jmsSessionBM;

    @PostMapping("/{connectorId}")
    public long open(@PathVariable long connectorId) throws Exception {
        return jmsSessionBM.connect(connectorId);
    }
    
    @GetMapping("/{connectorId}/queues")
    public List<JmsResource> listQueues(@PathVariable long connectorId) {
        return jmsSessionBM.listQueues(connectorId);
    }
    
    @DeleteMapping("/{connectorId}")
    public void close(@PathVariable long connectorId) throws Exception {
        jmsSessionBM.disconnect(connectorId);
    }
    
    @PostMapping("/{connectorId}/message/{destination}")
    public void sendMessage(@PathVariable long connectorId, @PathVariable String desintation, @RequestBody @Valid SendJmsMessageCommand message) {
        jmsSessionBM.sendMessage(connectorId, desintation, message.getMessage(), message.getHeader());
    }
    
    @GetMapping("/{connectorId}/message/{destination}")
    public JmsResultMessage sendMessage(@PathVariable long connectorId, @PathVariable String desintation, 
            @RequestParam(required = false) Long timeout) throws JMSException {
        Message msg = jmsSessionBM.receive(connectorId, desintation, timeout);
        return new JmsResultMessage(getJmsBody(msg), ToJmsHeaderResultValues.INSTANCE.convert(msg));
    }
    
    private static String getJmsBody(Message msg) throws JMSException {
        if (msg == null) return null;
        if (msg instanceof TextMessage) {
            return ((TextMessage)msg).getText();
        } else if (msg instanceof BytesMessage) {
            BytesMessage message = (BytesMessage)msg;
            byte[] bytes = new byte[(int) message.getBodyLength()];
            message.readBytes(bytes);
            return new String(bytes, Charset.forName("UTF-8"));
        } else {
            throw new IllegalStateException("Result message of type " + msg.getClass() + " isn't supported.");
        }
    }
}
