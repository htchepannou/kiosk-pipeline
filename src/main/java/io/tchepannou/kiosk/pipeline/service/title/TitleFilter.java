package io.tchepannou.kiosk.pipeline.service.title;

import io.tchepannou.kiosk.pipeline.persistence.domain.Article;

public interface TitleFilter {
    String filter(String title, Article article);
}
