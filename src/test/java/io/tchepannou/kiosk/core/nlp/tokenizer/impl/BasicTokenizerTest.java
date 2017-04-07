package io.tchepannou.kiosk.core.nlp.tokenizer.impl;

import io.tchepannou.kiosk.core.nlp.tokenizer.TokenFilter;
import io.tchepannou.kiosk.core.nlp.tokenizer.Tokenizer;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BasicTokenizerTest {

    @Test
    public void testSentence() throws Exception {
        String str = "C’est une histoire morbide!!! Jean-Paul et Boko Haram.";

        Tokenizer tokenizer = new BasicTokenizer(str);

        assertEquals("C", tokenizer.nextToken());
        assertEquals("’", tokenizer.nextToken());
        assertEquals("est", tokenizer.nextToken());
        assertEquals(" ", tokenizer.nextToken());
        assertEquals("une", tokenizer.nextToken());
        assertEquals(" ", tokenizer.nextToken());
        assertEquals("histoire", tokenizer.nextToken());
        assertEquals(" ", tokenizer.nextToken());
        assertEquals("morbide", tokenizer.nextToken());
        assertEquals("!", tokenizer.nextToken());
        assertEquals("!", tokenizer.nextToken());
        assertEquals("!", tokenizer.nextToken());
        assertEquals(" ", tokenizer.nextToken());
        assertEquals("Jean-Paul", tokenizer.nextToken());
        assertEquals(" ", tokenizer.nextToken());
        assertEquals("et", tokenizer.nextToken());
        assertEquals(" ", tokenizer.nextToken());
        assertEquals("Boko", tokenizer.nextToken());
        assertEquals(" ", tokenizer.nextToken());
        assertEquals("Haram", tokenizer.nextToken());
        assertEquals(".", tokenizer.nextToken());
        assertNull(tokenizer.nextToken());
    }


    @Test
    public void testFilter() throws Exception {
        String str = "C’est une histoire morbide!!! Jean-Paul et Boko Haram.";

        final List<String> stopwords = Arrays.asList("c", "est", "une", "et");
        final TokenFilter filter = new TokenFilter() {
            @Override
            public boolean accept(final String text) {
                return !stopwords.contains(text.toLowerCase());
            }
        };
        Tokenizer tokenizer = new BasicTokenizer(str, filter);

        assertEquals("’", tokenizer.nextToken());
        assertEquals(" ", tokenizer.nextToken());
        assertEquals(" ", tokenizer.nextToken());
        assertEquals("histoire", tokenizer.nextToken());
        assertEquals(" ", tokenizer.nextToken());
        assertEquals("morbide", tokenizer.nextToken());
        assertEquals("!", tokenizer.nextToken());
        assertEquals("!", tokenizer.nextToken());
        assertEquals("!", tokenizer.nextToken());
        assertEquals(" ", tokenizer.nextToken());
        assertEquals("Jean-Paul", tokenizer.nextToken());
        assertEquals(" ", tokenizer.nextToken());
        assertEquals(" ", tokenizer.nextToken());
        assertEquals("Boko", tokenizer.nextToken());
        assertEquals(" ", tokenizer.nextToken());
        assertEquals("Haram", tokenizer.nextToken());
        assertEquals(".", tokenizer.nextToken());
        assertNull(tokenizer.nextToken());
    }

    @Test
    public void testMultiline() throws Exception {
        String str = "C’est une histoire morbide\n Jean-Paul\tet Boko Haram.\n";

        BasicTokenizer tokenizer = new BasicTokenizer(str);

        assertEquals("C", tokenizer.nextToken());
        assertEquals("’", tokenizer.nextToken());
        assertEquals("est", tokenizer.nextToken());
        assertEquals(" ", tokenizer.nextToken());
        assertEquals("une", tokenizer.nextToken());
        assertEquals(" ", tokenizer.nextToken());
        assertEquals("histoire", tokenizer.nextToken());
        assertEquals(" ", tokenizer.nextToken());
        assertEquals("morbide", tokenizer.nextToken());
        assertEquals("\n", tokenizer.nextToken());
        assertEquals(" ", tokenizer.nextToken());
        assertEquals("Jean-Paul", tokenizer.nextToken());
        assertEquals("\t", tokenizer.nextToken());
        assertEquals("et", tokenizer.nextToken());
        assertEquals(" ", tokenizer.nextToken());
        assertEquals("Boko", tokenizer.nextToken());
        assertEquals(" ", tokenizer.nextToken());
        assertEquals("Haram", tokenizer.nextToken());
        assertEquals(".", tokenizer.nextToken());
        assertEquals("\n", tokenizer.nextToken());
        assertNull(tokenizer.nextToken());
    }
}
