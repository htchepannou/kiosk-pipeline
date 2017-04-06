package io.tchepannou.kiosk.core.nlp.tokenizer.impl;

import io.tchepannou.kiosk.core.nlp.tokenizer.TokenFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class StopWordFilter implements TokenFilter {
    //-- Attributes
    private final Set<String> words = new HashSet<>();

    //-- Public
    public void load(final InputStream in) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String text;
            while ((text = reader.readLine()) != null) {
                words.add(text.trim().toLowerCase());
            }
        }
    }

    //-- StopWords implementation
    @Override
    public boolean accept(final String text) {
        return !words.contains(text.toLowerCase());
    }
}
