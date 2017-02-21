package io.tchepannou.kiosk.pipeline.service.validation.article;

import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.service.validation.Rule;

public interface ArticleRule extends Rule<Article> {
    String NO_CONTENT = "no_content";
    String CONTENT_TOO_SHORT = "content_too_short";
    String NO_TITLE = "no_title";
    String BLACKLISTED = "blacklisted";
}
