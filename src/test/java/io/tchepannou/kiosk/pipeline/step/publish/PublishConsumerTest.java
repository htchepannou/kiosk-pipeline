package io.tchepannou.kiosk.pipeline.step.publish;

import io.tchepannou.kiosk.persistence.domain.Link;
import io.tchepannou.kiosk.persistence.domain.LinkStatusEnum;
import io.tchepannou.kiosk.pipeline.step.LinkConsumerTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PublishConsumerTest extends LinkConsumerTestSupport {

    @InjectMocks
    PublishConsumer consumer;

    @Test
    public void shouldConsumeValidLink() throws Exception {
        // Given
        final Link link = new Link();
        link.setStatus(LinkStatusEnum.valid);

        // When
        consumer.consume(link);

        // When
        assertThat(link.getStatus()).isEqualTo(LinkStatusEnum.published);
    }

    @Test
    public void shouldNotConsumeNonValidLink() throws Exception {
        // Given
        final Link link = new Link();
        link.setStatus(LinkStatusEnum.invalid);

        // When
        consumer.consume(link);

        // When
        assertThat(link.getStatus()).isEqualTo(LinkStatusEnum.invalid);
    }
}
