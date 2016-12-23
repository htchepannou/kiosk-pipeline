package io.tchepannou.kiosk.pipeline.support;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class JsoupHelperTest {
    @Test
    public void testCollect() throws Exception {
        String html = "<html><body>" +
                "<div>Hello world</div>" +
                "<a href='http://www.google.ca'>Google</a> is an internet company." +
                "But, like <a href=''>Any</a> other companies, They love swiss banks" +
                "</body></html>";
        Document doc = Jsoup.parse(html);
        List<Element> result = new ArrayList<>();

        JsoupHelper.collect(doc.body(), result, f -> "a".equals(f.tagName()));

        assertThat(result).hasSize(2);
    }

}
