package io.tchepannou.kiosk.pipeline.service.content;

import io.tchepannou.kiosk.pipeline.support.JsoupHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;

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
            "a,b,blockquote,body,br,caption,cite,code,col,colgroup,dd,div,dl,dt,em,h1,h2,h3,h4,h5,h6,i,li,ol,p,pre,q,small,span,strike,strong,sub,sup,table,tbody,td,tfoot,th,thead,tr,u,ul"
                    .split(",");
    public static final String[] CSS_BLACKLIST = new String[]{
            "footer",
            "#footer",
            "#comments",
            ".comments"
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
    public String filter(final String str) {
        /* keep only whitelist */
        final String xhtml = Jsoup.clean(str, this.whitelist);

        /* post-clean */
        final Document doc = Jsoup.parse(xhtml);
        final Set<Element> items = new HashSet<>();
        collectSocialLinks(doc.body(), items);
        collectBlaclistCss(doc.body(), items);
        collectEmpty(doc.body(), items);
        removeAll(items);

        /* clean anchors */
        clearHref(doc);

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

    private void collectSocialLinks(final Element node, final Collection<Element> items) {
        for (final Element elt : node.select("a")) {
            if (isSocialLink(elt.attr("href"))) {
                items.add(elt);
            }
        }
    }

    private void collectBlaclistCss(final Element node, final Collection<Element> items) {
        for (final String css : CSS_BLACKLIST) {
            items.addAll(node.select(css));
        }
    }

    private boolean isSocialLink(final String href) {
        if (href == null) {
            return false;
        }
        return href.startsWith("https://twitter.com/intent/tweet");
    }

    private void clearHref(final Document doc) {
        for (final Element elt : doc.select("a")) {
            elt.removeAttr("href");
        }
    }

    private void collectEmpty(final Element node, final Collection<Element> items) {
        final JsoupHelper.Predicate<Element> predicate = elt -> elt.children().isEmpty()
                    && !"iframe".equalsIgnoreCase(elt.tagName())
                    && node.isBlock()
                    && !node.hasText()
                ;

        JsoupHelper.collect(node, items, predicate);
    }

    private void removeAll(final Collection<Element> items) {
        for (final Element item : items) {
            item.remove();
        }
    }
}
