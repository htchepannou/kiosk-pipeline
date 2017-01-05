package io.tchepannou.kiosk.pipeline.service.similarity.filter;

import io.tchepannou.kiosk.pipeline.service.similarity.TextFilter;
import org.apache.commons.lang3.StringUtils;

public class WhitespaceTextFilter implements TextFilter {
    @Override
    public String filter(final String text) {
        return StringUtils.normalizeSpace(text).trim();
    }
}
