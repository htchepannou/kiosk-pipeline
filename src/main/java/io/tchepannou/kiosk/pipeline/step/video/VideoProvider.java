package io.tchepannou.kiosk.pipeline.step.video;

import java.io.IOException;

public interface VideoProvider {
    String getEmbedUrl(String url);

    VideoInfo getInfo(String url) throws IOException;
}
