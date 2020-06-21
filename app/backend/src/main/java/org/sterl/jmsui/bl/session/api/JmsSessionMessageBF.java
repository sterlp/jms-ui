package org.sterl.jmsui.bl.session.api;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.sterl.jmsui.api.JmsMessageConverter.ToJmsResultMessage;
import org.sterl.jmsui.bl.common.spring.JsonRestController;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource.Type;
import org.sterl.jmsui.bl.session.control.JmsSessionBM;

@Transactional
@JsonRestController(JmsSessionMessageBF.BASE_URL)
public class JmsSessionMessageBF {
    public static final String BASE_URL = "/api/sessions";

    @Autowired private JmsSessionBM jmsSessionBM;
    
    @PostMapping("/{connectorId}/message")
    public void sendMessage(@PathVariable long connectorId, 
            @RequestBody @Valid SendJmsMessageCommand message) throws JMSException {
        jmsSessionBM.sendMessage(connectorId, message.getDestination(), message.getDestinationType(), message.getBody(), message.getHeader());
    }
    @PostMapping("/{connectorId}/message/{destination}")
    public void sendMessage(@PathVariable long connectorId,
            @PathVariable String destination,
            @RequestBody @Valid SendJmsMessageCommand message) throws JMSException {
        message.setDestination(destination);
        this.sendMessage(connectorId, message);
    }
    @GetMapping("/{connectorId}/message/{destination}")
    public JmsResultMessage receive(@PathVariable long connectorId, 
            @PathVariable String destination,
            @RequestParam(required = false, defaultValue = "QUEUE") Type type,
            @RequestParam(required = false) Long timeout) throws JMSException {
        final Message msg = jmsSessionBM.receive(connectorId, destination, type, timeout);
        return ToJmsResultMessage.INSTANCE.convert(msg);
    }
    
    @PostMapping("/{connectorId}/queues/{destination}")
    public void sendToQueue(@PathVariable long connectorId, @PathVariable String destination, 
            @RequestBody @Valid SendJmsMessageCommand message) throws JMSException {
        message.setDestination(destination);
        message.setDestinationType(Type.QUEUE);
        this.sendMessage(connectorId, message);
    }
    @PostMapping("/{connectorId}/topics/{destination}")
    public void sendToTopic(@PathVariable long connectorId, @PathVariable String destination, 
            @RequestBody @Valid SendJmsMessageCommand message) throws JMSException {
        message.setDestination(destination);
        message.setDestinationType(Type.TOPIC);
        this.sendMessage(connectorId, message);
    }
    
    @GetMapping("/{connectorId}/queues/{destination}")
    public JmsResultMessage receiveFromQueue(@PathVariable long connectorId, 
            @PathVariable String destination,
            @RequestParam(required = false) Long timeout) throws JMSException {
        return receive(connectorId, destination, Type.QUEUE, timeout);
    }
    @GetMapping("/{connectorId}/topcis/{destination}")
    public JmsResultMessage receiveFromTopic(@PathVariable long connectorId, 
            @PathVariable String destination,
            @RequestParam(required = false) Long timeout) throws JMSException {
        return receive(connectorId, destination, Type.TOPIC, timeout);
    }
}
