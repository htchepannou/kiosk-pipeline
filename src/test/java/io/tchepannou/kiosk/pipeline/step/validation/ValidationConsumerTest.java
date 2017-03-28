package io.tchepannou.kiosk.pipeline.step.validation;

import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.domain.LinkStatusEnum;
import io.tchepannou.kiosk.pipeline.step.LinkConsumerTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationConsumerTest extends LinkConsumerTestSupport {
    @Mock
    Validator validator;

    @Mock
    MessageQueue queue;

    @InjectMocks
    ValidationConsumer consumer;

    @Test
    public void shouldConsumeValidArticle() throws Exception {
        // Given
        final Link link = new Link();
        when(validator.validate(link)).thenReturn(Validation.success());

        // When
        consumer.consume(link);

        // Then
        verify(linkRepository).save(link);
        assertThat(link.getStatus()).isEqualTo(LinkStatusEnum.valid);
        assertThat(link.getInvalidReason()).isNull();
    }

    @Test
    public void shouldNotConsumeValidArticle() throws Exception {
        // Given
        final Link link = new Link();
        when(validator.validate(link)).thenReturn(Validation.failure("foo"));

        // When
        consumer.consume(link);

        // Then
        verify(linkRepository).save(link);
        assertThat(link.getStatus()).isEqualTo(LinkStatusEnum.invalid);
        assertThat(link.getInvalidReason()).isEqualTo("foo");
    }
}
