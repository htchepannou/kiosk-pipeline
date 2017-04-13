package io.tchepannou.kiosk.core.nlp.toolkit;

import io.tchepannou.kiosk.core.nlp.tokenizer.BasicTokenizer;
import io.tchepannou.kiosk.core.nlp.tokenizer.StopWords;
import io.tchepannou.kiosk.core.nlp.tokenizer.Tokenizer;

import java.io.IOException;

public class FrenchToolkit implements NLPToolkit {

    private StopWords stopWords;

    FrenchToolkit (){
        try {
            stopWords = new StopWords();
            stopWords.load(getClass().getResourceAsStream("/nlp/fr/stopwords.txt"));
        } catch (IOException e){
            throw new IllegalStateException("Unable to load stopwored", e);
        }
    }

    @Override
    public Tokenizer getTokenizer(final String text) {
        return new BasicTokenizer(text);
    }

    @Override
    public StopWords getStopWords() {
        return stopWords;
    }
}
