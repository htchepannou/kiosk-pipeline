package io.tchepannou.kiosk.pipeline.step.metadata.filter;

import io.tchepannou.kiosk.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.step.metadata.TitleFilter;

/**
 * Filter for site like camber.be where the title is <code>COUNTRY::title::COUNTRY</code>
 */
public class TitleCountryFilter implements TitleFilter {
    private static final String SEPARATOR = "::";

    @Override
    public String filter(final String title, final Feed feed) {
        return extract(extract(title)).trim();
    }

    private String extract(final String title){
        int i = title.indexOf(SEPARATOR);
        if (i <= 0){
            return title;
        } else {
            final String part1 = title.substring(0, i);
            final String part2 = title.substring(i+SEPARATOR.length());
            return part1.length()>part2.length() ? part1 : part2;
        }
    }
}
