package io.tchepannou.kiosk.pipeline.processor;

import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.tchepannou.kiosk.pipeline.model.Feed;
import io.tchepannou.kiosk.pipeline.service.HttpService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.OutputStream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UrlExtractorProcessorTest {
    private static final String HTML = "<body>"
            + "<a href='/article/test_123.html'>"
            + "<a href='http://www.google.ca/article/test_456.html'>"
            + "<a href='http://www.google.ca/link.html'>"
            + "</body>";

    @Mock
    AmazonSQS sqs;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    HttpService http;

    @InjectMocks
    UrlExtractorProcessor processor;

    @Before
    public void setUp() {
        processor.setInputQueue("input-queue");
        processor.setOutputQueue("output-queue");
    }

    @Test
    public void testProcess() throws Exception {
        // Given
        final Feed feed = new Feed();
        feed.setUrl("http://www.google.ca");
        feed.setPath("/article/*.html");
        when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(feed);

        doAnswer(httpGet()).when(http).get(any(), any());

        // When
        processor.process("Sample-Message");

        // Then
        verify(sqs).sendMessage("output-queue", "http://www.google.ca/article/test_123.html");
        verify(sqs).sendMessage("output-queue", "http://www.google.ca/article/test_456.html");
    }

    private Answer httpGet() {
        return (invocationOnMock) -> {
            final OutputStream out = (OutputStream) invocationOnMock.getArguments()[1];
            out.write((HTML).getBytes());
            return null;
        };
    }
}
