package io.tchepannou.kiosk.pipeline.step.metadata;

import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;

public interface TitleFilter {
    String filter(String title, Feed feed);
}
