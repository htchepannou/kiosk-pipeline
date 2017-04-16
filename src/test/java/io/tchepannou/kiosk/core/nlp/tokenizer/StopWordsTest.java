package io.tchepannou.kiosk.core.nlp.tokenizer;

import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StopWordsTest {
    StopWords filter = new StopWords();

    @Test
    public void testIs() throws Exception {
        final String in = "le\n"
                + "la\n"
                + "un\n"
                + "une\n"
                + "les\n";

        filter.load(new ByteArrayInputStream(in.getBytes()));

        assertTrue(filter.is("le"));
        assertFalse(filter.is("machine"));

    }

    @Test
    public void testIs_CaseInsensitive() throws Exception {
        final String in = "le\n"
                + "la\n"
                + "Un\n"
                + "une\n"
                + "les\n";

        filter.load(new ByteArrayInputStream(in.getBytes()));

        assertTrue(filter.is("un"));
        assertTrue(filter.is("Un"));

    }
}
