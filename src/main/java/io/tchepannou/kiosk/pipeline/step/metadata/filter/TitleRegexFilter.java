package io.tchepannou.kiosk.pipeline.step.metadata.filter;

import io.tchepannou.kiosk.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.step.metadata.TitleFilter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TitleRegexFilter implements TitleFilter {
    @Override
    public String filter(final String title, final Feed feed) {
        final String regex = feed.getDisplayTitleRegex();
        if (regex != null) {
            final Pattern pattern = Pattern.compile(regex);
            final Matcher matcher = pattern.matcher(title);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        }
        return title;
    }
}
