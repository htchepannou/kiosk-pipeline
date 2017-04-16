package io.tchepannou.kiosk.core.nlp.stemmer;

import org.tartarus.snowball.ext.englishStemmer;

public class EnglishStemmer implements Stemmer {

    @Override
    public String stem(final String str) {
        final englishStemmer delegate = new englishStemmer();
        delegate.setCurrent(str);
        return delegate.stem() ? delegate.getCurrent() : str;
    }
}
