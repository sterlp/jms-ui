package org.sterl.jmsui.bl.connectors.ibm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Hashtable;

import javax.jms.JMSException;
import javax.jms.JMSSecurityException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.pcf.PCFMessageAgent;

@ExtendWith(MockitoExtension.class)
public class IbmMqConnectorTest {

    Hashtable<String, Object> config = new Hashtable<>();
    @Mock JmsTemplate jmsTemplate;
    @Mock MQQueueManager mqQueueManager;
    @Mock PCFMessageAgent agent;
    IbmMqConnector subject;
    
    MQException exception;

    class IbmMqConnectorMock extends IbmMqConnector {
        public IbmMqConnectorMock() {
            super("Test", 10L, jmsTemplate, config);
        }
        @Override
        protected MQQueueManager getMQQueueManager() throws MQException {
            if (exception != null) throw exception;
            super.ibmMqManager = mqQueueManager;
            return super.ibmMqManager;
        }
        @Override
        protected PCFMessageAgent getAgent() throws MQException, MQDataException {
            super.agent = this.agent;
            return super.agent;
        }
    }
    
    @BeforeEach
    void before() {
        exception = null;
        subject = new IbmMqConnectorMock();
    }
    
    @Test
    void testConnect() throws JMSException {
        when(mqQueueManager.isConnected()).thenReturn(true);
        when(mqQueueManager.isOpen()).thenReturn(true);

        assertThat(subject.isClosed()).isTrue();
        subject.connect();
        assertThat(subject.isClosed()).isFalse();
        
        subject.close();
        assertThat(subject.isClosed()).isTrue();
    }
    
    @Test
    void testNotAllowedException() throws JMSException {
        exception = new MQException(2, 2035, mqQueueManager);
        assertThrows(JMSSecurityException.class, () -> subject.connect());
    }
}
