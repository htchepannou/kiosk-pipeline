package io.tchepannou.kiosk.core.nlp.stemmer;

import org.tartarus.snowball.ext.frenchStemmer;

public class FrenchStemmer implements Stemmer {

    @Override
    public String stem(final String str) {
        final frenchStemmer delegate = new frenchStemmer();
        delegate.setCurrent(str);
        return delegate.stem() ? delegate.getCurrent() : str;
    }
}
