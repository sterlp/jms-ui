package org.sterl.jmsui.bl.connection.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.sterl.jmsui.bl.connection.api.model.JmsConnectionView;
import org.sterl.jmsui.bl.connection.model.JmsConnectionBE;

@SpringBootTest
class JmsConnectionDaoTest {

    @Autowired JmsConnectionDao subject;

    @Test
    void testRead() {
        JmsConnectionBE s1 = subject.save(new JmsConnectionBE("Foo1", "Bar1"));
        JmsConnectionBE s2 = subject.save(new JmsConnectionBE("Foo2", "Bar1"));
        JmsConnectionBE s3 = subject.save(new JmsConnectionBE("Foo3", "Bar1"));
        
        List<JmsConnectionView> results = subject.findByIdIn(Arrays.asList(s2.getId(), s3.getId()));
        assertNotNull(results);
        assertThat(results.size()).isEqualTo(2);
        assertThat(results.get(0).getId()).isEqualTo(s2.getId());
        assertThat(results.get(1).getId()).isEqualTo(s3.getId());
    }
}
