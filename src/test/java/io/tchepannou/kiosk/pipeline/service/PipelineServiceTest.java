package io.tchepannou.kiosk.pipeline.service;

import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.repository.FeedRepository;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PipelineServiceTest {

    @Mock
    FeedRepository feedRepository;

    @Mock
    LinkRepository linkRepository;

    @Mock
    MessageQueue metadataMessageQueue;

    @InjectMocks
    PipelineService service;

    @Test
    public void shouldReprocessLinks() throws Exception {
        // Given
        when(feedRepository.findOne(1L)).thenReturn(new Feed());

        when(linkRepository.findByFeed(any(), any())).thenReturn(Arrays.asList(
                createLink(1),
                createLink(2),
                createLink(3),
                createLink(4)
        ));

        // When
        service.reprocess(1);

        // Then
        verify(metadataMessageQueue).push("1");
        verify(metadataMessageQueue).push("2");
        verify(metadataMessageQueue).push("3");
        verify(metadataMessageQueue).push("4");
    }


    private Link createLink(long id){
        final Link link = new Link();
        link.setId(id);
        return link;
    }
}
