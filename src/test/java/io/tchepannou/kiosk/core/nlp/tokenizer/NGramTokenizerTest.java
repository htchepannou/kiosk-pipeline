package io.tchepannou.kiosk.core.nlp.tokenizer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NGramTokenizerTest {
    @Mock
    Tokenizer delegate;

    @Test
    public void testNextToken() throws Exception {
        // Given
        when(delegate.nextToken())
                .thenReturn("A")
                .thenReturn("B")
                .thenReturn("C")
                .thenReturn("D")
                .thenReturn(" ")
                .thenReturn("E")
                .thenReturn(null);

        // When
        NGramTokenizer tokenizer = new NGramTokenizer(3, delegate);

        // Then
        assertThat(tokenizer.nextToken()).isEqualTo("A");
        assertThat(tokenizer.nextToken()).isEqualTo("AB");
        assertThat(tokenizer.nextToken()).isEqualTo("ABC");
        assertThat(tokenizer.nextToken()).isEqualTo("B");
        assertThat(tokenizer.nextToken()).isEqualTo("BC");
        assertThat(tokenizer.nextToken()).isEqualTo("BCD");
        assertThat(tokenizer.nextToken()).isEqualTo("C");
        assertThat(tokenizer.nextToken()).isEqualTo("CD");
        assertThat(tokenizer.nextToken()).isEqualTo("CD ");
        assertThat(tokenizer.nextToken()).isEqualTo("D");
        assertThat(tokenizer.nextToken()).isEqualTo("D ");
        assertThat(tokenizer.nextToken()).isEqualTo("D E");
        assertThat(tokenizer.nextToken()).isEqualTo(" ");
        assertThat(tokenizer.nextToken()).isEqualTo(" E");
        assertThat(tokenizer.nextToken()).isEqualTo("E");
        assertThat(tokenizer.nextToken()).isNull();
    }

    @Test
    public void testMinMax() throws Exception {
        // Given
        when(delegate.nextToken())
                .thenReturn("A")
                .thenReturn("B")
                .thenReturn("C")
                .thenReturn("D")
                .thenReturn(" ")
                .thenReturn("E")
                .thenReturn(null);

        // When
        NGramTokenizer tokenizer = new NGramTokenizer(3, 3, delegate);

        // Then
        assertThat(tokenizer.nextToken()).isEqualTo("ABC");
        assertThat(tokenizer.nextToken()).isEqualTo("BCD");
        assertThat(tokenizer.nextToken()).isEqualTo("CD ");
        assertThat(tokenizer.nextToken()).isEqualTo("D E");
        assertThat(tokenizer.nextToken()).isNull();
    }
}
