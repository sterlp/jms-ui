package org.sterl.jmsui.bl.connectors.memory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.jms.JMSException;
import javax.jms.Message;

import org.springframework.jms.core.JmsTemplate;
import org.sterl.jmsui.api.JmsHeaderRequestValues;
import org.sterl.jmsui.bl.connectors.api.JmsConnectorInstance;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource.Type;
import org.sterl.jmsui.bl.connectors.memory.jmsmock.DummyJmsMessage;

public class MemoryQueueConnector implements JmsConnectorInstance {
    
    private Queue<DummyJmsMessage> memoryQueue = new ConcurrentLinkedQueue<>();

    public MemoryQueueConnector() {
    }
    @Override
    public void close() throws IOException {
        memoryQueue.clear();
    }

    @Override
    public JmsTemplate getJmsTemplate() {
        throw new RuntimeException("Not supported.");
    }
    @Override
    public void testConnection() throws JMSException {
    }
    @Override
    public List<JmsResource> listResources() {
        return Arrays.asList(new JmsResource("MEMORY.QUEUE", Type.QUEUE, "QUEUE"));
    }
    @Override
    public void sendMessage(String destination, String message, JmsHeaderRequestValues header) {
        memoryQueue.offer(new DummyJmsMessage(message, header));
    }
    @Override
    public Message receive(String destination, Long timeout) {
        return memoryQueue.poll();
    }
    @Override
    public boolean isClosed() {
        return false;
    }
}
