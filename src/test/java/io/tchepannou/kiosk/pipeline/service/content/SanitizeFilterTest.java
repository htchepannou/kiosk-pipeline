package io.tchepannou.kiosk.pipeline.service.content;

import io.tchepannou.kiosk.pipeline.step.content.filter.SanitizeFilter;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SanitizeFilterTest {
    public static String[] TAGS = ("body\n" +
            "div\n" +
            "span\n" +
            "title\n" +
            "link\n" +
            "meta\n" +
            "style\n" +
            "p\n" +
            "h1\n" +
            "h2\n" +
            "h3\n" +
            "h4\n" +
            "h5\n" +
            "h6\n" +
            "strong\n" +
            "em\n" +
            "abbr\n" +
            "acronym\n" +
            "address\n" +
            "bdo\n" +
            "blockquote\n" +
            "cite\n" +
            "q\n" +
            "code\n" +
            "ins\n" +
            "del\n" +
            "dfn\n" +
            "kbd\n" +
            "pre\n" +
            "samp\n" +
            "var\n" +
            "br\n" +
            "a\n" +
            "base\n" +
            "img\n" +
            "area\n" +
            "map\n" +
            "object\n" +
            "param\n" +
            "ul\n" +
            "ol\n" +
            "li\n" +
            "dl\n" +
            "dt\n" +
            "dd\n" +
            "table\n" +
            "tr\n" +
            "td\n" +
            "th\n" +
            "tbody\n" +
            "thead\n" +
            "tfoot\n" +
            "col\n" +
            "colgroup\n" +
            "caption\n" +
            "form\n" +
            "input\n" +
            "textarea\n" +
            "select\n" +
            "option\n" +
            "optgroup\n" +
            "button\n" +
            "label\n" +
            "fieldset\n" +
            "legend\n" +
            "script\n" +
            "noscript\n" +
            "b\n" +
            "i\n" +
            "tt\n" +
            "sub\n" +
            "sup\n" +
            "big\n" +
            "small\n" +
            "hr\n" +
            "a\n" +
            "abbr\n" +
            "acronym\n" +
            "address\n" +
            "area\n" +
            "b\n" +
            "base\n" +
            "bdo\n" +
            "big\n" +
            "blockquote\n" +
            "body\n" +
            "br\n" +
            "button\n" +
            "caption\n" +
            "cite\n" +
            "code\n" +
            "col\n" +
            "colgroup\n" +
            "dd\n" +
            "del\n" +
            "dfn\n" +
            "div\n" +
            "dl\n" +
            "DOCTYPE\n" +
            "dt\n" +
            "em\n" +
            "fieldset\n" +
            "form\n" +
            "hr\n" +
            "i\n" +
            "img\n" +
            "input\n" +
            "ins\n" +
            "kbd\n" +
            "label\n" +
            "legend\n" +
            "li\n" +
            "link\n" +
            "map\n" +
            "meta\n" +
            "noscript\n" +
            "object\n" +
            "ol\n" +
            "optgroup\n" +
            "option\n" +
            "p\n" +
            "param\n" +
            "pre\n" +
            "q\n" +
            "samp\n" +
            "script\n" +
            "select\n" +
            "small\n" +
            "span\n" +
            "strong\n" +
            "style\n" +
            "sub\n" +
            "sup\n" +
            "table\n" +
            "tbody\n" +
            "td\n" +
            "textarea\n" +
            "tfoot\n" +
            "th\n" +
            "thead\n" +
            "title\n" +
            "tr\n" +
            "tt\n" +
            "ul\n" +
            "var").split("\\n");

    private final SanitizeFilter filter = new SanitizeFilter();

    @Test
    public void shouldFilterHtml() throws Exception {
        // Given
        final String html = IOUtils.toString(getClass().getResourceAsStream("/extractor/sanitize.html"));

        // When
        final String result = filter.filter(html);

        // Then
        final Document doc = Jsoup.parse(result);
        final List<String> wl = Arrays.asList(SanitizeFilter.TAG_WHITELIST);
        for (final String tag : TAGS) {
            if (!wl.contains(tag)) {
                final Elements elts = doc.select(tag);
                assertThat(elts).isEmpty();
            }
        }
    }

    @Test
    public void shouldFilterSocialLinks() throws Exception {
        // Given
        final String html = IOUtils.toString(getClass().getResourceAsStream("/extractor/sanitize_social-links.html"));

        // When
        final String result = filter.filter(html);

        // Then
        final Document doc = Jsoup.parse(result);
        assertThat(doc.body().text().trim())
                .startsWith(
                        "Des jeunes gens qui scrutent le  babillard.  A  l’aide  d’un stylo et d’un calepin, ils recopient  leur  emploi  du temps. Tandis que les plus futés ont préféré troquer une pièce de 100 F contre un exemplaire. « Je vais as- sister à mes premiers cours d’Allemand dans l’après-midi (Ndlr : hier) dans l’amphi A 203. Je me sens à l’aise, même si j’éprouve quelques difficultés à me retrouver parce que l’université  est  grande.");
    }
}
