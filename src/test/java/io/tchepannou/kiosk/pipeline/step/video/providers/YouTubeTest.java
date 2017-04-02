package io.tchepannou.kiosk.pipeline.step.video.providers;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class YouTubeTest {
    private YouTube service = new YouTube();

    @Test
    public void testEmbedUrl ()
    {
        String expected = "https://www.youtube.com/embed/XOcCOBe8PTc";

        assertEquals(expected, service.getEmbedUrl("https://youtu.be/XOcCOBe8PTc"));
        assertEquals(expected, service.getEmbedUrl("https://www.youtube.com/watch?v=XOcCOBe8PTc"));
        assertEquals(expected, service.getEmbedUrl("https://www.youtube.com/embed/XOcCOBe8PTc"));
        assertEquals(expected, service.getEmbedUrl("https://www.youtube.com/embed/XOcCOBe8PTc?enablejsapi=1&feature=oembed&wmode=opaque&vq=hd720"));

        assertEquals(expected, service.getEmbedUrl("http://youtu.be/XOcCOBe8PTc"));
        assertEquals(expected, service.getEmbedUrl("http://www.youtube.com/watch?v=XOcCOBe8PTc"));
        assertEquals(expected, service.getEmbedUrl("http://www.youtube.com/embed/XOcCOBe8PTc"));
        assertEquals(expected, service.getEmbedUrl("http://www.youtube.com/embed/XOcCOBe8PTc?enablejsapi=1&feature=oembed&wmode=opaque&vq=hd720"));
    }

//    @Test
//    public void getEmbedUrl_null ()
//    {
//        assertNull(service.getEmbedUrl(null));
//    }
//
//    @Test
//    public void getEmbedUrl ()
//    {
//        assertEquals("https://www.youtube.com/embed/123", service.getEmbedUrl("123"));
//    }

}
