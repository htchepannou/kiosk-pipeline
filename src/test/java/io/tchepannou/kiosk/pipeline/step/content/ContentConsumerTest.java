package io.tchepannou.kiosk.pipeline.step.content;

import io.tchepannou.kiosk.core.nlp.language.LanguageDetector;
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

import static io.tchepannou.kiosk.pipeline.Fixtures.readText;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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

    @Mock
    LanguageDetector languageDetector;

    @InjectMocks
    ContentConsumer consumer;

    @Before
    public void setUp() {
        consumer.setRawFolder("html");
        consumer.setContentFolder("content");
        consumer.setDefaultSummaryMaxLength(5);
    }

    @Test
    public void shouldConsume() throws Exception {
        // Given
        final Link link = new Link();
        link.setId(123);
        link.setSummary("summary !!!");
        link.setS3Key("html/2010/10/11/test.html");

        doAnswer(readText("hello world")).when(repository).read(any(), any());

        when(extractor.extract("hello world")).thenReturn("HELLO WORLD");

        when(languageDetector.detect(anyString())).thenReturn("en");

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
        assertThat(lk.getValue().getSummary()).isEqualTo("summary !!!");
        assertThat(lk.getValue().getLanguage()).isEqualToIgnoringCase("en");
    }

    @Test
    public void shouldUpdateSummary() throws Exception {
        // Given
        final Link link = new Link();
        link.setId(123);
        link.setSummary(null);
        link.setS3Key("html/2010/10/11/test.html");

        doAnswer(readText("hello world")).when(repository).read(any(), any());

        when(extractor.extract("hello world")).thenReturn("HELLO WORLD");

        when(languageDetector.detect(anyString())).thenReturn("en");

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
        assertThat(lk.getValue().getSummary()).isEqualTo("HELLO");
    }
}
