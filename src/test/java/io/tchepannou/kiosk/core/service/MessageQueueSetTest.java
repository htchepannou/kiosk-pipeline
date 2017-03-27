package io.tchepannou.kiosk.core.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MessageQueueSetTest {
    @Mock
    MessageQueue queue1;

    @Mock
    MessageQueue queue2;

    MessageQueueSet set;

    @Before
    public void setUp (){
        set = new MessageQueueSet("test", Arrays.asList(queue1, queue2));
    }

    @Test
    public void shouldBroadcaseMessages() throws Exception {
        set.push("msg");

        verify(queue1).push("msg");
        verify(queue2).push("msg");
    }

    @Test
    public void shouldReturnName () {
        assertThat(set.getName()).isEqualTo("test");
    }
}
