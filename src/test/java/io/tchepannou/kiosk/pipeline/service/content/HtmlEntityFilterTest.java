package io.tchepannou.kiosk.pipeline.service.content;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HtmlEntityFilterTest {

    @Test
    public void testFilter() throws Exception {
        final String str = "La stratégie de programmation de la chaîne à capitaux publics du Cameroun délaisse les télénovelas au grand régal des téléspectateurs";
        final String expected = "La strat&#233;gie de programmation de la cha&#238;ne &#224; capitaux publics du Cameroun d&#233;laisse les t&#233;l&#233;novelas au grand r&#233;gal des t&#233;l&#233;spectateurs";

        final String result = new HtmlEntityFilter().filter(str);

        assertThat(result).isEqualTo(expected);
    }
}
