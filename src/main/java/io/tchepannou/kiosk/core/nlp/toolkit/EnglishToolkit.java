package io.tchepannou.kiosk.core.nlp.toolkit;

import io.tchepannou.kiosk.core.nlp.stemmer.EnglishStemmer;
import io.tchepannou.kiosk.core.nlp.stemmer.Stemmer;
import io.tchepannou.kiosk.core.nlp.tokenizer.BasicTokenizer;
import io.tchepannou.kiosk.core.nlp.tokenizer.StopWords;
import io.tchepannou.kiosk.core.nlp.tokenizer.Tokenizer;

import java.io.IOException;

public class EnglishToolkit implements NLPToolkit {

    private StopWords stopWords;
    private Stemmer stemmer;

    EnglishToolkit(){
        stemmer = new EnglishStemmer();

        try {
            stopWords = new StopWords();
            stopWords.load(getClass().getResourceAsStream("/nlp/en/stopwords.txt"));
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

    @Override
    public Stemmer getStemmer() {
        return stemmer;
    }
}
