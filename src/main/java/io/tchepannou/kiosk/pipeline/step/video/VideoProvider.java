package io.tchepannou.kiosk.pipeline.step.video;

public interface VideoProvider {
    String getEmbedUrl(String videoId);
}
