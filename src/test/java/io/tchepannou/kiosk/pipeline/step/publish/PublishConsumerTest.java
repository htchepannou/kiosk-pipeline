package io.tchepannou.kiosk.pipeline.step.publish;

import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.domain.LinkStatusEnum;
import io.tchepannou.kiosk.pipeline.step.LinkConsumerTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PublishConsumerTest extends LinkConsumerTestSupport {

    @Mock
    Clock clock;

    @InjectMocks
    PublishConsumer consumer;

    @Test
    public void shouldConsumeValidLink() throws Exception {
        // Given
        final Date now = new Date();
        when(clock.millis()).thenReturn(now.getTime());

        final Link link = new Link();
        link.setStatus(LinkStatusEnum.valid);

        // When
        consumer.consume(link);

        // When
        assertThat(link.getStatus()).isEqualTo(LinkStatusEnum.published);
        assertThat(link.getPublishedDate()).isEqualTo(now);
    }

    @Test
    public void shouldNotConsumeNonValidLink() throws Exception {
        // Given
        final Date now = new Date();
        when(clock.millis()).thenReturn(now.getTime());

        final Link link = new Link();
        link.setStatus(LinkStatusEnum.invalid);

        // When
        consumer.consume(link);

        // When
        assertThat(link.getStatus()).isEqualTo(LinkStatusEnum.invalid);
    }
}
