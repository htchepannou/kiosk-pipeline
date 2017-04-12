package io.tchepannou.kiosk.core.nlp.language;

import org.apache.tika.language.LanguageIdentifier;

import java.util.ArrayList;
import java.util.List;

public class LanguageDetector {
    private List<String> languageList = new ArrayList<>();

    public String detect(final String text) {
        final LanguageIdentifier li = new LanguageIdentifier(text);
        final String language = li.getLanguage();

        return languageList.contains(language) ? language : null;
    }

    public List<String> getLanguageList() {
        return languageList;
    }

    public void setLanguageList(final List<String> languageList) {
        this.languageList = languageList;
    }
}
