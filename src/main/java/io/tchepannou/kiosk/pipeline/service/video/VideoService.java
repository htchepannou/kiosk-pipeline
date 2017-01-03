package io.tchepannou.kiosk.pipeline.service.video;

public interface VideoService {
    String getVideoId (String url);
    String getEmbedUrl (String videoId);
}
