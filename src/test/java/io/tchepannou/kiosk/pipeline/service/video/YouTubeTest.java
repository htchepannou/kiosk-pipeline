package io.tchepannou.kiosk.pipeline.service.video;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class YouTubeTest {
    private YouTube service = new YouTube();

    @Test
    public void getVideoId ()
    {
        String expected = "XOcCOBe8PTc";

        assertEquals(expected, service.getVideoId("https://youtu.be/XOcCOBe8PTc"));
        assertEquals(expected, service.getVideoId("https://www.youtube.com/watch?v=XOcCOBe8PTc"));
        assertEquals(expected, service.getVideoId("https://www.youtube.com/embed/XOcCOBe8PTc"));
        assertEquals(expected, service.getVideoId("https://www.youtube.com/embed/XOcCOBe8PTc?enablejsapi=1&feature=oembed&wmode=opaque&vq=hd720"));

        assertEquals(expected, service.getVideoId("http://youtu.be/XOcCOBe8PTc"));
        assertEquals(expected, service.getVideoId("http://www.youtube.com/watch?v=XOcCOBe8PTc"));
        assertEquals(expected, service.getVideoId("http://www.youtube.com/embed/XOcCOBe8PTc"));
        assertEquals(expected, service.getVideoId("http://www.youtube.com/embed/XOcCOBe8PTc?enablejsapi=1&feature=oembed&wmode=opaque&vq=hd720"));
    }

    @Test
    public void getVideoId_null ()
    {
        assertNull(service.getVideoId(null));
    }

    @Test
    public void getEmbedUrl ()
    {
        assertEquals("https://www.youtube.com/embed/123", service.getEmbedUrl("123"));
    }

}