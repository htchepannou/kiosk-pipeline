package io.tchepannou.kiosk.core.nlp.tokenizer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class WordTokenizerTest {

    @Test
    public void testSentence() throws Exception {
        String str = "C’est une histoire morbide!!! Jean-Paul et Boko Haram.";

        Tokenizer tokenizer = new WordTokenizer(str);

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
        assertEquals("Jean", tokenizer.nextToken());
        assertEquals("-", tokenizer.nextToken());
        assertEquals("Paul", tokenizer.nextToken());
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
    public void testMultiline() throws Exception {
        String str = "C’est une histoire morbide\n Jean-Paul\tet Boko Haram.\n";

        WordTokenizer tokenizer = new WordTokenizer(str);

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
        assertEquals("Jean", tokenizer.nextToken());
        assertEquals("-", tokenizer.nextToken());
        assertEquals("Paul", tokenizer.nextToken());
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
