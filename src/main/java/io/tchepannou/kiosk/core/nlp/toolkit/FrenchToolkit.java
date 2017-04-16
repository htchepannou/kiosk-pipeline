package io.tchepannou.kiosk.core.nlp.toolkit;

import io.tchepannou.kiosk.core.nlp.stemmer.FrenchStemmer;
import io.tchepannou.kiosk.core.nlp.stemmer.Stemmer;
import io.tchepannou.kiosk.core.nlp.tokenizer.BasicTokenizer;
import io.tchepannou.kiosk.core.nlp.tokenizer.StopWords;
import io.tchepannou.kiosk.core.nlp.tokenizer.Tokenizer;

import java.io.IOException;

public class FrenchToolkit implements NLPToolkit {

    private final StopWords stopWords;
    private final Stemmer stemmer;

    FrenchToolkit() {
        stemmer = new FrenchStemmer();

        try {
            stopWords = new StopWords();
            stopWords.load(getClass().getResourceAsStream("/nlp/fr/stopwords.txt"));
        } catch (final IOException e) {
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
