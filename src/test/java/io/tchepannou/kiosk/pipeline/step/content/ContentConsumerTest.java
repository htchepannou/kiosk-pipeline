package io.tchepannou.kiosk.pipeline.step.content;

import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.step.LinkConsumerTestSupport;
import io.tchepannou.kiosk.pipeline.step.content.filter.ContentExtractor;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContentConsumerTest extends LinkConsumerTestSupport {
    @Mock
    MessageQueue messageQueue;

    @Mock
    ContentExtractor extractor;

    @InjectMocks
    ContentConsumer consumer;

    @Before
    public void setUp() {
        consumer.setRawFolder("html");
        consumer.setContentFolder("content");
    }

    @Test
    public void shouldConsume() throws Exception {
        // Given
        final Link link = new Link();
        link.setId(123);
        link.setS3Key("html/2010/10/11/test.html");

        doAnswer(read("hello world")).when(repository).read(any(), any());

        when(extractor.extract("hello world")).thenReturn("HELLO WORLD");

        // When
        consumer.consume(link);

        // Then
        verify(messageQueue).push("123");

        final ArgumentCaptor<InputStream> in = ArgumentCaptor.forClass(InputStream.class);
        verify(repository).write(
                eq("content/2010/10/11/test.html"),
                in.capture()
        );
        assertThat(IOUtils.toString(in.getValue())).isEqualTo("HELLO WORLD");

        final ArgumentCaptor<Link> lk = ArgumentCaptor.forClass(Link.class);
        verify(linkRepository).save(lk.capture());
        assertThat(lk.getValue().getContentKey()).isEqualTo("content/2010/10/11/test.html");
        assertThat(lk.getValue().getContentLength()).isEqualTo(11);
        assertThat(lk.getValue().getContentType()).isEqualTo("text/html");
    }

}
