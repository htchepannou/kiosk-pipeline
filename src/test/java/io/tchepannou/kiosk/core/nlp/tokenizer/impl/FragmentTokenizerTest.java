package io.tchepannou.kiosk.core.nlp.tokenizer.impl;

import io.tchepannou.kiosk.core.nlp.tokenizer.Tokenizer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FragmentTokenizerTest {

    @Test
    public void testSentence() throws Exception {
        String str = "C’est une histoire morbide!!! Jean-Paul et Boko Haram, sont ils des amis? Il dit: «Evidemment qu'ils le sont»\n"
                + "Cher(e) compatriote, resistez a la tentation";

        Tokenizer tokenizer = new FragmentTokenizer(new BasicTokenizer(str));

        assertEquals("C’est une histoire morbide", tokenizer.nextToken());
        assertEquals("Jean-Paul et Boko Haram", tokenizer.nextToken());
        assertEquals("sont ils des amis", tokenizer.nextToken());
        assertEquals("Il dit", tokenizer.nextToken());
        assertEquals("Evidemment qu'ils le sont", tokenizer.nextToken());
        assertEquals("Cher", tokenizer.nextToken());
        assertEquals("e", tokenizer.nextToken());
        assertEquals("compatriote", tokenizer.nextToken());
        assertEquals("resistez a la tentation", tokenizer.nextToken());
        assertNull(tokenizer.nextToken());
    }

    @Test
    public void testSentenceWithHypen() throws Exception {
        String str = "C’est une histoire morbide - Jean-Paul et Boko Haram, sont ils des amis?";

        Tokenizer tokenizer = new FragmentTokenizer(new BasicTokenizer(str));

        assertEquals("C’est une histoire morbide", tokenizer.nextToken());
        assertEquals("Jean-Paul et Boko Haram", tokenizer.nextToken());
        assertEquals("sont ils des amis", tokenizer.nextToken());
        assertNull(tokenizer.nextToken());
    }

}
