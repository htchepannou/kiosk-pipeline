package io.tchepannou.kiosk.pipeline.service.video;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YouTube implements VideoService {
    private static final String URL_REGEX = "^(https?)?(://)?(www.)?(m.)?((youtube.com)|(youtu.be))/";
    private static final String[] VIDEO_ID_REGEX = { "\\?vi?=([^&]*)","watch\\?.*v=([^&]*)", "(?:embed|vi?)/([^/?]*)", "^([A-Za-z0-9\\-]*)"};
    private static final String EMBED_URL_FORMAT = "https://www.youtube.com/embed/%s";

    //--  VideoPlatform overrides

    /**
     * See https://gist.github.com/jvanderwee/b30fdb496acff43aef8e
     */
    public String getVideoId(String url) {
        if (url == null) {
            return null;
        }

        return extractVideoIdFromUrl(url);
    }

    public String getEmbedUrl(String videoId) {
        return String.format(EMBED_URL_FORMAT, videoId);
    }


    public String extractVideoIdFromUrl(String url) {
        String youTubeLinkWithoutProtocolAndDomain = youTubeLinkWithoutProtocolAndDomain(url);
        if (youTubeLinkWithoutProtocolAndDomain == null){
            return null;
        }

        for(String regex : VIDEO_ID_REGEX) {
            Pattern compiledPattern = Pattern.compile(regex);
            Matcher matcher = compiledPattern.matcher(youTubeLinkWithoutProtocolAndDomain);

            if(matcher.find()){
                return matcher.group(1);
            }
        }

        return null;
    }

    private String youTubeLinkWithoutProtocolAndDomain(String url) {
        Pattern compiledPattern = Pattern.compile(URL_REGEX);
        Matcher matcher = compiledPattern.matcher(url);

        if(matcher.find()){
            return url.replace(matcher.group(), "");
        }
        return null;
    }

}