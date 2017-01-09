package io.tchepannou.kiosk.pipeline.service.title;

import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TitleRegexFilter implements TitleFilter{
    @Override
    public String filter(final String title, final Article article) {
        final Feed feed = article.getLink().getFeed();
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
