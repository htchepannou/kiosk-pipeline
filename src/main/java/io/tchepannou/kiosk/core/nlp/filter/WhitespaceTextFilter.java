package io.tchepannou.kiosk.core.nlp.filter;

import org.apache.commons.lang3.StringUtils;

public class WhitespaceTextFilter implements TextFilter {
    @Override
    public String filter(final String text) {
        return StringUtils.normalizeSpace(text).trim();
    }
}
