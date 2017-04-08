package io.tchepannou.kiosk.pipeline.step.shingle;

import io.tchepannou.kiosk.core.nlp.filter.TextFilter;
import io.tchepannou.kiosk.core.service.FileRepository;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ShingleConsumerTest {
    @Mock
    FileRepository repository;

    @Mock
    LinkRepository linkRepository;

    @Mock
    TextFilter filter;

    @InjectMocks
    ShingleConsumer consumer;

    @Before
    public void setUp(){
        consumer.setContentFolder("content");
        consumer.setShingleFolder("shingle");
        consumer.setMaxShingles(10);
        consumer.setShingleSize(3);
    }

    @Test
    public void testConsume() throws Exception {
        // Given
        doAnswer(readText("This is an example of text. Have fun")).when(repository).read(any(), any());

        final Link link = new Link();
        link.setContentKey("content/1/2/3/foo.html");

        when(filter.filter(any())).thenAnswer((inv) -> inv.getArguments()[0]);

        // When
        consumer.consume(link);

        // Then
        verify(linkRepository).save(link);
        assertThat(link.getShingleKey()).isEqualTo("shingle/1/2/3/foo.txt");

        ArgumentCaptor<InputStream> in = ArgumentCaptor.forClass(InputStream.class);
        verify(repository).write(eq("shingle/1/2/3/foo.txt"), in.capture());
        assertThat(IOUtils.toString(in.getValue())).isEqualTo(
                "This is an\n" +
                "is an example\n" +
                "an example of\n" +
                "example of text\n" +
                "of text Have\n" +
                "text Have fun"
        );
    }


    protected final Answer readText(final String txt) {
        return (inv) -> {
            final OutputStream out = (OutputStream) inv.getArguments()[1];
            IOUtils.copy(new ByteArrayInputStream(txt.getBytes()), out);
            return null;
        };
    }

}
