package io.tchepannou.kiosk.core.nlp.filter;

import io.tchepannou.kiosk.core.nlp.filter.TextFilter;
import org.apache.commons.lang3.StringUtils;

public class UnaccentTextFilter implements TextFilter {
    @Override
    public String filter(final String text) {
        return StringUtils.stripAccents(text);
    }
}
