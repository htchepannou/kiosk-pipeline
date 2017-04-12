package io.tchepannou.kiosk.core.nlp.language;

import org.apache.tika.language.LanguageIdentifier;

public class LanguageDetector {
    public String detect(final String text) {
        final LanguageIdentifier li = new LanguageIdentifier(text);
        return li.getLanguage();
    }
}
