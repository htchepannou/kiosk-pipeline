package io.tchepannou.kiosk.pipeline.step.video.providers;

import io.tchepannou.kiosk.pipeline.step.video.VideoInfo;
import io.tchepannou.kiosk.pipeline.step.video.VideoProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YouTube implements VideoProvider {
    private static final String URL_REGEX = "^(https?)?(://)?(www.)?(m.)?((youtube.com)|(youtu.be))/";
    private static final String[] VIDEO_ID_REGEX = {"\\?vi?=([^&]*)", "watch\\?.*v=([^&]*)", "(?:embed|vi?)/([^/?]*)", "^([A-Za-z0-9\\-]*)"};
    private static final String EMBED_URL_FORMAT = "https://www.youtube.com/embed/%s";

    @Autowired
    RestTemplate rest;

    String apiKey;

    @Override
    public String getEmbedUrl(final String url) {
        final String id = getVideoId(url);
        return id != null ? String.format(EMBED_URL_FORMAT, id) : null;
    }

    @Override
    public VideoInfo getInfo(final String url) throws IOException {
        final String id = getVideoId(url);
        if (id == null) {
            return null;
        }

        final String apiUrl = String.format("https://www.googleapis.com/youtube/v3/videos?id=%s&key=%s&part=snippet", id, apiKey);
        final Map<String, Object> result = (Map) rest.getForEntity(apiUrl, Object.class);
        final Map<String, Object> items = (Map)((List<Map<String, Object>>) result.get("items")).get(0).get("snippets");

        final VideoInfo info = new VideoInfo();
        info.setTitle((String) items.get("title"));
        info.setDescription((String) items.get("description"));
        return info;
    }

    /**
     * See https://gist.github.com/jvanderwee/b30fdb496acff43aef8e
     */
    private String getVideoId(final String url) {
        if (url == null) {
            return null;
        }

        return extractVideoIdFromUrl(url);
    }

    private String extractVideoIdFromUrl(final String url) {
        final String youTubeLinkWithoutProtocolAndDomain = youTubeLinkWithoutProtocolAndDomain(url);
        if (youTubeLinkWithoutProtocolAndDomain == null) {
            return null;
        }

        for (final String regex : VIDEO_ID_REGEX) {
            final Pattern compiledPattern = Pattern.compile(regex);
            final Matcher matcher = compiledPattern.matcher(youTubeLinkWithoutProtocolAndDomain);

            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        return null;
    }

    private String youTubeLinkWithoutProtocolAndDomain(final String url) {
        final Pattern compiledPattern = Pattern.compile(URL_REGEX);
        final Matcher matcher = compiledPattern.matcher(url);

        if (matcher.find()) {
            return url.replace(matcher.group(), "");
        }
        return null;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(final String apiKey) {
        this.apiKey = apiKey;
    }
}
