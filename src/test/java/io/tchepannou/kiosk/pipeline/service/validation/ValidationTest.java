package io.tchepannou.kiosk.pipeline.service.validation;

import io.tchepannou.kiosk.pipeline.step.validation.Validation;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidationTest {

    @Test
    public void testSuccess() throws Exception {
        Validation result = Validation.success();

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getReason()).isNull();
    }

    @Test
    public void testFailure() throws Exception {
        Validation result = Validation.failure("test");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getReason()).isEqualTo("test");
    }
}
