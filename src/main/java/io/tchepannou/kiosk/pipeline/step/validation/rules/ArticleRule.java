package io.tchepannou.kiosk.pipeline.step.validation.rules;

import io.tchepannou.kiosk.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.step.validation.Rule;

public interface ArticleRule extends Rule<Link> {
    String NO_CONTENT = "no_content";
    String CONTENT_TOO_SHORT = "content_too_short";
    String NO_TITLE = "no_title";
    String BLACKLISTED = "blacklisted";
}
