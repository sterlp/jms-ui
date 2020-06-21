package org.sterl.jmsui.bl.connectors.memory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.jms.ConnectionFactory;
import javax.jms.Message;

import org.sterl.jmsui.api.JmsHeaderRequestValues;
import org.sterl.jmsui.bl.connectors.api.JmsConnectorInstance;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource;
import org.sterl.jmsui.bl.connectors.api.model.JmsResource.Type;
import org.sterl.jmsui.bl.connectors.memory.jmsmock.DummyJmsMessage;

public class MemoryQueueConnector implements JmsConnectorInstance {
    
    private Queue<DummyJmsMessage> memoryQueue = new ConcurrentLinkedQueue<>();

    @Override
    public void close() {
        memoryQueue.clear();
    }
    @Override
    public List<JmsResource> listQueues() {
        return Arrays.asList(new JmsResource("MEMORY.QUEUE", Type.QUEUE, "QUEUE"));
    }
    @Override
    public List<JmsResource> listTopics() {
        return Arrays.asList();
    }
    @Override
    public void sendMessage(String destination, Type type, String message, JmsHeaderRequestValues header) {
        memoryQueue.offer(new DummyJmsMessage(message, header));
    }
    @Override
    public Message receive(String destination, Type type, Long timeout) {
        return memoryQueue.poll();
    }
    @Override
    public boolean isClosed() {
        return false;
    }
    @Override
    public Integer getQueueDepth(String queueName) {
        return memoryQueue.size();
    }
    @Override
    public void connect() {
    }
    @Override
    public Map<String, Object> getQueueInformation(String queueName) {
        return new HashMap<>();
    }
    @Override
    public Map<String, Object> getTopicInformation(String destination) {
        return new HashMap<>();
    }
    @Override
    public ConnectionFactory getConnectionFactory() {
        throw new RuntimeException("Not supporeted");
    }
}
