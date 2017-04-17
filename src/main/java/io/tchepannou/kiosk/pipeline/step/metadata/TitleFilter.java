package io.tchepannou.kiosk.pipeline.step.metadata;

import io.tchepannou.kiosk.persistence.domain.Feed;

public interface TitleFilter {
    String filter(String title, Feed feed);
}
