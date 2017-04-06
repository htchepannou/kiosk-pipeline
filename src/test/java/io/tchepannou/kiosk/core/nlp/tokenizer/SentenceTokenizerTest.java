package io.tchepannou.kiosk.core.nlp.tokenizer;

import io.tchepannou.kiosk.core.nlp.tokenizer.impl.SentenceTokenizer;
import io.tchepannou.kiosk.core.nlp.tokenizer.impl.BasicTokenizer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SentenceTokenizerTest {

    @Test
    public void testNextToken() throws Exception {
        String str = "C’est une histoire morbide!!! Jean-Paul et Boko Haram on 2 points en commun:\n" +
                "1) Ils aiment la sappe." +
                "2) Ils aiment les filles.";

        Tokenizer tokenizer = new SentenceTokenizer(new BasicTokenizer(str));

        assertEquals("C’est une histoire morbide", tokenizer.nextToken());
        assertEquals("Jean-Paul et Boko Haram on 2 points en commun", tokenizer.nextToken());
        assertEquals("1) Ils aiment la sappe", tokenizer.nextToken());
        assertEquals("2) Ils aiment les filles", tokenizer.nextToken());
        assertNull(tokenizer.nextToken());
    }
}
