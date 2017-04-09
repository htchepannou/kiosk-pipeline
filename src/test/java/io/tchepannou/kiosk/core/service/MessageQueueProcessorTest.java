package io.tchepannou.kiosk.core.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MessageQueueProcessorTest {
    @Mock
    private MessageQueue queue;

    @Mock
    private Consumer consumer;

    @Mock
    private Delay delay;

    @Mock
    private ThreadCountDown latch;

    @InjectMocks
    MessageQueueProcessor processor;

    @Test
    public void shouldProcessMessages() throws Exception {
        // Given
        when(queue.poll()).thenReturn(Arrays.asList("1", "2", "3"));

        // Then
        final int result = processor.process();

        // Then
        assertThat(result).isEqualTo(3);
        verify(consumer).consume("1");
        verify(consumer).consume("2");
        verify(consumer).consume("3");
    }

    @Test
    public void shouldIgnoreConsumerErrors() throws Exception {
        // Given
        when(queue.poll()).thenReturn(Arrays.asList("1", "2", "3"));

        doThrow(RuntimeException.class).when(consumer).consume("1");

        // Then
        final int result = processor.process();

        // Then
        assertThat(result).isEqualTo(2);
        verify(consumer).consume("1");
        verify(consumer).consume("2");
        verify(consumer).consume("3");
    }


    @Test
    public void testRun() throws Exception {
        // Given
        when(queue.poll())
                .thenReturn(Arrays.asList("1", "2"))
                .thenReturn(Collections.emptyList())
                .thenReturn(Arrays.asList("3"))
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.emptyList())
                .thenThrow(IOException.class)
        ;

        doNothing().when(delay).sleep();

        // When
        processor.run();

        // Then
        verify(consumer).consume("1");
        verify(consumer).consume("2");
        verify(consumer).consume("3");

        verify(delay, times(2)).reset();

        verify(delay, times(3)).sleep();

        verify(latch).countUp();
        verify(latch).countDown();
    }
}
