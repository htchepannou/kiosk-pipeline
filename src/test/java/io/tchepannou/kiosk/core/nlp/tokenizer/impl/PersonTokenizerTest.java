package io.tchepannou.kiosk.core.nlp.tokenizer.impl;

import io.tchepannou.kiosk.core.nlp.tokenizer.TokenFilter;
import io.tchepannou.kiosk.core.nlp.tokenizer.TokenFilterSet;
import io.tchepannou.kiosk.core.nlp.tokenizer.Tokenizer;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class PersonTokenizerTest {
    TokenFilter filter = new TokenFilterSet(Arrays.asList(
            new StopWordFilter()
    ));

    @Test
    public void testNextToken() throws Exception {
        final String text = "André Nguidjol, qu'on surnomme \"Beretta\", cloué dans l'inconfort d'une retraite survenue aux lendemains de son implication dans l'affaire des primes impayées de Samuel Eto'o en équipe nationale";
        final Tokenizer tokenizer = new PersonTokenizer(
                new BasicTokenizer(text),
                filter
        );

        assertThat(tokenizer.nextToken()).isEqualTo("André Nguidjol");
        assertThat(tokenizer.nextToken()).isEqualTo("Beretta");
        assertThat(tokenizer.nextToken()).isEqualTo("Samuel Eto");
        assertThat(tokenizer.nextToken()).isNull();
    }
}
