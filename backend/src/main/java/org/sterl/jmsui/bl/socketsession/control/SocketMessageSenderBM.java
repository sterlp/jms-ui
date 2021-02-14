package org.sterl.jmsui.bl.socketsession.control;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.broker.BrokerAvailabilityEvent;
import org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler;
import org.springframework.messaging.simp.broker.SubscriptionRegistry;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Controller;
import org.sterl.jmsui.bl.session.api.JmsResultMessage;

/**
 * https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#websocket 
 */
@Controller
public class SocketMessageSenderBM implements ApplicationListener<BrokerAvailabilityEvent> {
    
    @Autowired private SimpMessagingTemplate webSocket;
    private SubscriptionRegistry subscriptionRegistry;
    //@SendTo("/topic/{target}")
    public String topicMessageReceived(@DestinationVariable long connectorId, @DestinationVariable String target, String message) {
        webSocket.convertAndSend("/sessions/" + connectorId + "/topics/" + target, new JmsResultMessage(message, null));
        return message;
    }

    public int getSubscriberCount(String topic) {
        Map<String, Object> headers = new HashMap<>();
        headers.put(SimpMessageHeaderAccessor.DESTINATION_HEADER, topic);
        headers.put(SimpMessageHeaderAccessor.MESSAGE_TYPE_HEADER, SimpMessageType.MESSAGE);
        GenericMessage<?> m = new GenericMessage<>("", headers);
        return subscriptionRegistry.findSubscriptions(m).size();
    }
    
    @MessageMapping("/move/{uuid}")
    public void makeMove(@DestinationVariable String uuid, String message) throws IllegalArgumentException {
        System.out.println(uuid + " - " + message);
        webSocket.convertAndSend("/topic/" + uuid, new JmsResultMessage(message, null));
    }

    @Override
    public void onApplicationEvent(BrokerAvailabilityEvent event) {
        if (event.isBrokerAvailable() && event.getSource() instanceof SimpleBrokerMessageHandler) {
            subscriptionRegistry = ((SimpleBrokerMessageHandler)event.getSource()).getSubscriptionRegistry();
        } else {
            subscriptionRegistry = null;
        }
        
    }
}
