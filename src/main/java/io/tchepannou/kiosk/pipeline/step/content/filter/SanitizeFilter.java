package io.tchepannou.kiosk.pipeline.step.content.filter;

import io.tchepannou.kiosk.pipeline.step.content.Filter;
import io.tchepannou.kiosk.pipeline.support.HtmlHelper;
import io.tchepannou.kiosk.pipeline.support.JsoupHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Remove polluting tags like SCRIPT, IMAGES, etc.
 */
public class SanitizeFilter implements Filter<String> {
    //-- Static
    public static final String[] TAG_WHITELIST =
            "a,b,blockquote,body,br,caption,cite,code,col,colgroup,dd,div,dl,dt,em,footer,h1,h2,h3,h4,h5,h6,i,li,ol,p,pre,q,small,span,strike,strong,sub,sup,table,tbody,td,tfoot,th,thead,tr,u,ul"
                    .split(",");
    public static final String[] CSS_BLACKLIST = new String[]{
            "footer",
            "#footer",
            "#comments",
            ".comments",
            ".mashsb-container",     // from www.mashshare.net plugin
            ".prev-page",
            ".next-page",
            "#menu-ay-side-menu-mine",
            "#top-nav",
            "#related_posts",
            ".related_posts",
            ".share-post"
    };

    private final Whitelist whitelist;

    public SanitizeFilter() {
        this(Arrays.asList(TAG_WHITELIST));
    }

    public SanitizeFilter(final List<String> tags) {
        whitelist = createWhitelist(tags.toArray(new String[]{}));
        whitelist.addAttributes("a", "href");
        whitelist.addAttributes("iframe", "src");
    }

    //-- TextFilter overrides
    @Override
    public String filter(final String html) {

        /* pre-clean */
        Document doc = Jsoup.parse(html);
        final Set<Element> items = new HashSet<>();
        collectTitle(doc.body(), items);
        collectBlaclistCss(doc.body(), items);
        removeAll(items);

        /* keep only whitelist */
        final String xhtml = Jsoup.clean(doc.html(), this.whitelist);

        /* post-clean */
        doc = Jsoup.parse(xhtml);
        cleanup(doc);

        return doc.html();
    }

    private Whitelist createWhitelist(final String... tags) {
        final Whitelist wl = new Whitelist();
        wl.addTags(tags);
        for (final String tag : tags) {
            wl.addAttributes(tag, "id");
        }
        return wl;
    }

    private void collectBlaclistCss(final Element node, final Collection<Element> items) {
        for (final String css : CSS_BLACKLIST) {
            items.addAll(node.select(css));
        }
    }

    private boolean isSocialLink(final Element elt) {
        return "a".equalsIgnoreCase(elt.tagName()) && isSocialLink(elt.attr("href"));
    }

    private boolean isSocialLink(final String href) {
        if (href == null) {
            return false;
        }
        return href.startsWith("https://twitter.com/intent/tweet") ||
                href.startsWith("http://www.facebook.com/sharer.php") ||
                href.startsWith("https://plusone.google.com") ||
                href.startsWith("http://www.linkedin.com/shareArticle") ||
                href.startsWith("http://pinterest.com/pin/create/button")
                ;
    }

    private boolean isEmpty(final Element elt) {
        return !elt.hasText() && elt.children().isEmpty() && !elt.tag().isEmpty();
    }

    private void cleanup(final Element node) {
        final JsoupHelper.Visitor<Element> visitor = elt -> {
            if (elt.parent() != null && (isSocialLink(elt) || isEmpty(elt))) {
                elt.remove();
            }
        };

        JsoupHelper.visit(node, visitor);
    }

    private void collectTitle(final Element node, final Collection<Element> items) {
        for (final String selector : HtmlHelper.TITLE_CSS_SELECTORS) {
            final Elements elts = node.select(selector);
            items.addAll(elts);
        }
    }

    private void removeAll(final Collection<Element> items) {
        for (final Element item : items) {
            item.remove();
        }
    }
}
