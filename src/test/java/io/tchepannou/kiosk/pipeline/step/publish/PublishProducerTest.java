package io.tchepannou.kiosk.pipeline.step.publish;

import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.persistence.domain.Link;
import io.tchepannou.kiosk.persistence.domain.LinkStatusEnum;
import io.tchepannou.kiosk.persistence.repository.LinkRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PublishProducerTest {
    @Mock
    LinkRepository linkRepository;

    @Mock
    MessageQueue queue;

    @InjectMocks
    PublishProducer producer;

    @Before
    public void setUp (){
        producer.setLimit(3);
    }

    @Test
    public void testProduce() throws Exception {
        // Given
        when(linkRepository.findByStatus(eq(LinkStatusEnum.valid), any()))
                .thenReturn(Arrays.asList(
                                createLink(1),
                                createLink(2),
                                createLink(3))
                )
                .thenReturn(Arrays.asList(
                                createLink(4),
                                createLink(5)
                )
        );

        // When
        producer.produce();

        // Then
        verify(queue).push("1");
        verify(queue).push("2");
        verify(queue).push("3");
        verify(queue).push("4");
        verify(queue).push("5");
    }

    private Link createLink(long id){
        final Link link = new Link();
        link.setId(id);
        return link;
    }
}
