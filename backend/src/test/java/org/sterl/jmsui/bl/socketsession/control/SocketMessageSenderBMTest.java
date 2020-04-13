package org.sterl.jmsui.bl.socketsession.control;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.sterl.jmsui.AwaitUtil;
import org.sterl.jmsui.bl.session.api.JmsResultMessage;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SocketMessageSenderBMTest {
    
    @LocalServerPort private int port;
    private String URL;

    @Autowired SocketMessageSenderBM subject;

    private CompletableFuture<JmsResultMessage> completableFuture;
    private final StompFrameHandler frameHandler = new StompFrameHandler() {
        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            completableFuture.complete((JmsResultMessage)payload);
        }
        
        @Override
        public Type getPayloadType(StompHeaders headers) {
            return JmsResultMessage.class;
        }
    };
    
    @BeforeEach
    public void setup() {
        completableFuture = new CompletableFuture<>();
        URL = "ws://localhost:" + port + "/stomp";
    }

    @Test
    void testTopicMessageReceived() throws Exception {
        final long connectorId = 1;
        final String topicName = "TOPIC.1";
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSession stompSession = stompClient.connect(URL, new StompSessionHandlerAdapter() {}).get(1, TimeUnit.SECONDS);
        stompSession.subscribe("/sessions/" + connectorId + "/topics/" + topicName, frameHandler);
        
        final String msg = "foo " + Instant.now();
        subject.topicMessageReceived(connectorId, topicName, msg);
        //stompSession.send("/move/test", "Foo " + Instant.now());

        JmsResultMessage jmsResultMessage = completableFuture.get(5, TimeUnit.SECONDS);
        System.out.println(jmsResultMessage);
        assertNotNull(jmsResultMessage);
        assertThat(jmsResultMessage.getBody()).isEqualTo(msg);
        
        stompSession.disconnect();
    }
    
    @Test
    void testRegisteredCound() throws Exception {
        final String subScription = "/sessions/999/topics/FOO_BAR_TEST";
        
        assertEquals(subject.getSubscriberCount(subScription), 0);
        
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        StompSession stompSession = stompClient.connect(URL, new StompSessionHandlerAdapter() {}).get(1, TimeUnit.SECONDS);
        
        stompSession.subscribe(subScription, frameHandler);
        AwaitUtil.assertEquals(() -> subject.getSubscriberCount(subScription), 1);
        
        stompSession.disconnect();
        AwaitUtil.assertEquals(() -> subject.getSubscriberCount(subScription), 0);
        
    }

    private List<Transport> createTransportClient() {
        List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
    }
}
