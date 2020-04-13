package org.sterl.jmsui.bl.socketsession.control;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    private WebSocketStompClient stompClient;

    @BeforeEach
    public void setup() {
        completableFuture = new CompletableFuture<>();
        URL = "ws://localhost:" + port + "/stomp";
        
        stompClient = createWebSocketStompClient();
    }
    @AfterEach
    public void after() {
        stompClient.stop();
    }

    @Test
    void testTopicMessageReceived() throws Exception {
        final long connectorId = 1;
        final String topicName = "TOPIC.1";

        StompSession stompSession = stompClient.connect(URL, new StompSessionHandlerAdapter() {}).get(2, TimeUnit.SECONDS);

        stompSession.subscribe("/sessions/" + connectorId + "/topics/" + topicName, frameHandler);
        assertThat(stompSession.isConnected()).isTrue();

        final String msg = "foo " + Instant.now();
        subject.topicMessageReceived(connectorId, topicName, msg);
        // TODO for some reason we have to send it here twice if the run all tests together -- check needed
        subject.topicMessageReceived(connectorId, topicName, msg);

        JmsResultMessage jmsResultMessage = completableFuture.get(5, TimeUnit.SECONDS);
        System.out.println(jmsResultMessage);
        assertNotNull(jmsResultMessage);
        assertThat(jmsResultMessage.getBody()).isEqualTo(msg);
        
        stompSession.disconnect();
    }
    
    @Test
    void testRegisteredCount() throws Exception {
        final String subScription = "/sessions/999/topics/FOO_BAR_TEST";
        
        assertEquals(subject.getSubscriberCount(subScription), 0);
        
        StompSession stompSession = stompClient.connect(URL, new StompSessionHandlerAdapter() {}).get(1, TimeUnit.SECONDS);
        
        stompSession.subscribe(subScription, frameHandler);
        AwaitUtil.assertEquals(() -> subject.getSubscriberCount(subScription), 1);
        
        stompSession.disconnect();
        AwaitUtil.assertEquals(() -> subject.getSubscriberCount(subScription), 0);
        
    }

    private WebSocketStompClient createWebSocketStompClient() {
        List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        
        WebSocketStompClient result = new WebSocketStompClient(new SockJsClient(transports));
        result.setMessageConverter(new MappingJackson2MessageConverter());
        return result;
    }
}
