package io.tchepannou.kiosk.pipeline.service.validation;

import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ValidatorTest {
    Validator validator;

    @Test
    public void shouldReturnSuccessWhenAllRulesAreSuccessful() throws Exception {
        validator = new Validator(Arrays.asList(
                createSuccessfullRule(),
                createSuccessfullRule()
        ));

        final Validation result = validator.validate(new Object());

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    public void shouldReturnFailureWhereAnyRuleIsFailed() throws Exception {
        validator = new Validator(Arrays.asList(
                createSuccessfullRule(),
                createSuccessfullRule(),
                createFailureRule("error1"),
                createSuccessfullRule(),
                createSuccessfullRule(),
                createFailureRule("error2")
        ));

        final Validation result = validator.validate(new Object());

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getReason()).isEqualTo("error1");
    }

    private Rule createSuccessfullRule (){
        final Rule rule = mock(Rule.class);
        when(rule.validate(any())).thenReturn(Validation.success());
        return rule;
    }

    private Rule createFailureRule (final String code){
        final Rule rule = mock(Rule.class);
        when(rule.validate(any())).thenReturn(Validation.failure(code));
        return rule;
    }
}
