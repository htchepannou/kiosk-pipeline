package io.tchepannou.kiosk.pipeline.service;

import io.tchepannou.kiosk.core.service.FileRepository;
import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.core.service.MessageQueueProcessor;
import io.tchepannou.kiosk.core.service.ThreadCountDown;
import io.tchepannou.kiosk.persistence.domain.Feed;
import io.tchepannou.kiosk.persistence.domain.Link;
import io.tchepannou.kiosk.persistence.domain.LinkTypeEnum;
import io.tchepannou.kiosk.persistence.repository.FeedRepository;
import io.tchepannou.kiosk.persistence.repository.LinkRepository;
import io.tchepannou.kiosk.pipeline.step.publish.PublishProducer;
import io.tchepannou.kiosk.pipeline.step.url.UrlProducer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Executor;

import static io.tchepannou.kiosk.pipeline.Fixtures.createFeed;
import static io.tchepannou.kiosk.pipeline.Fixtures.createLink;
import static io.tchepannou.kiosk.pipeline.Fixtures.readText;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PipelineServiceTest {
    @Mock
    Executor executor;

    @Mock
    FileRepository fileRepository;

    @Mock
    FeedRepository feedRepository;

    @Mock
    LinkRepository linkRepository;

    @Mock
    ThreadCountDown threadCountDown;

    @Mock
    ShutdownService shutdownService;

    @Mock
    UrlProducer urlProducer;

    @Mock
    PublishProducer publishProducer;

    @Mock
    @Qualifier("MetadataMessageQueue")
    MessageQueue metadataMessageQueue;

    @Mock
    MessageQueueProcessor downloadMessageQueueProcessor;

    @Mock
    MessageQueueProcessor metadataMessageQueueProcessor;

    @Mock
    MessageQueueProcessor contentMessageQueueProcessor;

    @Mock
    MessageQueueProcessor validationMessageQueueProcessor;

    @Mock
    MessageQueueProcessor imageMessageQueueProcessor;

    @Mock
    MessageQueueProcessor thumbnailMessageQueueProcessor;

    @Mock
    MessageQueueProcessor videoMessageQueueProcessor;

    @Mock
    MessageQueueProcessor publishMessageQueueProcessor;

    @InjectMocks
    PipelineService pipelineService;

    @Before
    public void setUp(){
        pipelineService.setMaxDurationSeconds(1);
        pipelineService.setReprocessKey("dev/reprocess/go");
        pipelineService.setWorkers(1);
    }

    @Test
    public void shouldRunPipelineWhenAutostartEnabled() throws Exception{
        // Given
        pipelineService.setAutostart(true);

        // When
        pipelineService.run();

        // Then
        verify(urlProducer).produce();
        verify(executor).execute(downloadMessageQueueProcessor);
        verify(executor).execute(contentMessageQueueProcessor);
        verify(executor).execute(validationMessageQueueProcessor);
        verify(executor).execute(metadataMessageQueueProcessor);
        verify(executor).execute(imageMessageQueueProcessor);
        verify(executor).execute(thumbnailMessageQueueProcessor);
        verify(executor).execute(videoMessageQueueProcessor);

        verify(publishProducer).produce();
        verify(executor).execute(publishMessageQueueProcessor);

        verify(threadCountDown, times(2)).await();

        verify(shutdownService).shutdown(1000);
        verify(shutdownService).shutdownNow();
    }

    @Test
    public void shouldNoRunPipelineWhenAutostartDisabled() throws Exception{
        // Given
        pipelineService.setAutostart(false);

        // When
        pipelineService.run();

        // Then
        verify(urlProducer, never()).produce();
        verify(executor, never()).execute(downloadMessageQueueProcessor);
        verify(executor, never()).execute(contentMessageQueueProcessor);
        verify(executor, never()).execute(validationMessageQueueProcessor);
        verify(executor, never()).execute(metadataMessageQueueProcessor);
        verify(executor, never()).execute(imageMessageQueueProcessor);
        verify(executor, never()).execute(thumbnailMessageQueueProcessor);
        verify(executor, never()).execute(videoMessageQueueProcessor);

        verify(publishProducer, never()).produce();
        verify(executor, never()).execute(publishMessageQueueProcessor);

        verify(threadCountDown, never()).await();

        verify(shutdownService).shutdown(1000);
        verify(shutdownService, never()).shutdownNow();
    }

    @Test
    public void shouldReprocess() throws Exception{
        // Given
        final Feed f1 = createFeed();
        final Feed f2 = createFeed();
        final Feed f3 = createFeed();
        when(feedRepository.findAll()).thenReturn(Arrays.asList(f1, f2, f3));

        final Link l1 = createLink();
        final Link l2 = createLink();
        final Link l3 = createLink();
        when(linkRepository.findByFeedAndType(eq(f1), eq(LinkTypeEnum.article), any()))
                .thenReturn(Arrays.asList(l1))
                .thenReturn(Collections.<Link>emptyList());

        when(linkRepository.findByFeedAndType(eq(f2), eq(LinkTypeEnum.article), any()))
                .thenReturn(Arrays.asList(l2))
                .thenReturn(Collections.<Link>emptyList());

        when(linkRepository.findByFeedAndType(eq(f3), eq(LinkTypeEnum.article), any()))
                .thenReturn(Arrays.asList(l3))
                .thenReturn(Collections.<Link>emptyList());

        doAnswer(readText("foo")).when(fileRepository).read(any(), any());

        // When
        pipelineService.reprocess();

        // Then
        verify(metadataMessageQueue).push(String.valueOf(l1.getId()));
        verify(metadataMessageQueue).push(String.valueOf(l2.getId()));
        verify(metadataMessageQueue).push(String.valueOf(l3.getId()));
    }

    @Test
    public void shouldNeverReprocessInactiveFeed() throws Exception{
        // Given
        final Feed f1 = createFeed();
        f1.setActive(false);
        when(feedRepository.findAll()).thenReturn(Arrays.asList(f1));

        final Link l1 = createLink();
        when(linkRepository.findByFeedAndType(eq(f1), eq(LinkTypeEnum.article), any()))
                .thenReturn(Arrays.asList(l1))
                .thenReturn(Collections.<Link>emptyList());

        doAnswer(readText("foo")).when(fileRepository).read(any(), any());

        // When
        pipelineService.reprocess();

        // Then
        verify(metadataMessageQueue, never()).push(String.valueOf(l1.getId()));
    }

    @Test
    public void shouldNeverReprocessWhenKeyNotAvailable() throws Exception{
        // Given
        final Feed f1 = createFeed();
        when(feedRepository.findAll()).thenReturn(Arrays.asList(f1));

        final Link l1 = createLink();
        when(linkRepository.findByFeedAndType(eq(f1), eq(LinkTypeEnum.article), any()))
                .thenReturn(Arrays.asList(l1))
                .thenReturn(Collections.<Link>emptyList());

        doThrow(IOException.class).when(fileRepository).read(any(), any());

        // When
        pipelineService.reprocess();

        // Then
        verify(metadataMessageQueue, never()).push(String.valueOf(l1.getId()));
    }
}
