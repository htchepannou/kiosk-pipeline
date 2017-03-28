package io.tchepannou.kiosk.pipeline.step.metadata;

import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.domain.LinkTypeEnum;
import io.tchepannou.kiosk.pipeline.step.LinkConsumerTestSupport;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MetadataConsumerTest extends LinkConsumerTestSupport  {
    @Mock
    MessageQueue queue;

    @Mock
    Clock clock;

    @Mock
    TitleFilter titleFilter;

    @Mock
    Feed feed;

    @InjectMocks
    MetadataConsumer consumer;

    final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    @Before
    public void setUp() {
        consumer.setDefaultPublishDateOffsetDays(-2);
    }

    @Test
    public void testConsume() throws Exception {
        // Given
        final Link link = createLink();

        when(titleFilter.filter(any(), any())).thenReturn("This is the sanitized title");

        doAnswer(read("/meta/article.html")).when(repository).read(any(), any());
        doAnswer(save(890)).when(linkRepository).save(any(Link.class));

        // When
        consumer.consume(link);

        // Then
        verify(queue).push("890");


        final ArgumentCaptor<Link> lk = ArgumentCaptor.forClass(Link.class);
        verify(linkRepository).save(lk.capture());
        assertThat(lk.getValue().getTitle()).isEqualTo("Rigobert Song : « Je suis vraiment revenu de très loin »");
        assertThat(lk.getValue().getDisplayTitle()).isEqualTo("This is the sanitized title");
        assertThat(lk.getValue().getSummary()).isEqualTo(
                "Et soudain, Rigobert Song apparaît dans l’embrasure de la porte. Quelques kilos en moins, des cheveu...");
        assertThat(fmt.format(lk.getValue().getPublishedDate())).startsWith("2016-12-29");
        assertThat(fmt.format(lk.getValue().getType())).isEqualTo(LinkTypeEnum.article);
    }


    @Test
    public void shouldExtractTitleFromSparkCameroon() throws Exception {
        final Document doc = loadDocument("/meta/sparkcameroon.html");
        assertThat(consumer.extractTitle(doc)).isEqualTo("Les motos-taxis en ordre de bataille contre le sida");
    }

    @Test
    public void shouldExtractPublishedDateFromSparkCameroon() throws Exception {
        final Document doc = loadDocument("/meta/sparkcameroon.html");
        final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        assertThat(fmt.format(consumer.extractPublishedDate(doc, feed))).isEqualTo("2016-12-04");
    }

    @Test
    public void shouldSetDefaultPublishedDateInPastWhenFeedOnboardDateIsToday() throws Exception {
        final Date now = DateUtils.addDays(new Date(), -10);

        when(feed.getOnboardDate()).thenReturn(now);

        when(clock.millis()).thenReturn(now.getTime());

        final Document doc = loadDocument("/meta/no_published_date.html");
        assertThat(consumer.extractPublishedDate(doc, feed)).isEqualTo(
                DateUtils.addDays(now, consumer.getDefaultPublishDateOffsetDays())
        );
    }

    @Test
    public void shouldSetDefaultPublishedDateToNowWhenFeedOnboardDateIsNotToday() throws Exception {
        final Date now = new Date();

        when(feed.getOnboardDate()).thenReturn(DateUtils.addDays(now, -10));

        when(clock.millis()).thenReturn(now.getTime());

        final Document doc = loadDocument("/meta/no_published_date.html");
        assertThat(consumer.extractPublishedDate(doc, feed)).isEqualTo(now);
    }

    @Test
    public void shouldExtractPublishedDateFromMamafika() throws Exception {
        final Document doc = loadDocument("/meta/mamafrika.html");
        final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        assertThat(fmt.format(consumer.extractPublishedDate(doc, feed))).isEqualTo("2017-01-07");
    }

    @Test
    public void shouldExtractPublishedDateFromLFCamerounais() throws Exception {
        final Document doc = loadDocument("/meta/lefilmcamerounais.html");
        final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        assertThat(fmt.format(consumer.extractPublishedDate(doc, feed))).isEqualTo("2017-01-06");
    }

    @Test
    public void shouldExtractPublishedDateFromEtoudiBlog() throws Exception {
        final Document doc = loadDocument("/meta/etoudiblog.html");
        final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        assertThat(fmt.format(consumer.extractPublishedDate(doc, feed))).isEqualTo("2016-09-19");
    }

    //-- Private
    private Document loadDocument(final String path) throws Exception {
        final String html = IOUtils.toString(getClass().getResourceAsStream(path));
        return Jsoup.parse(html);
    }

    private Link createLink() {
        final Link link = new Link();
        link.setS3Key("dev/html/2011/01/01/foo.html");
        return link;
    }

    private Answer read(final String path){
        return (inv) -> {
            final InputStream in = getClass().getResourceAsStream(path);
            final OutputStream out = (OutputStream)inv.getArguments()[1];
            IOUtils.copy(in, out);
            return null;
        };
    }

    private Answer save(final long id) {
        return (inv) -> {
            final Object obj = inv.getArguments()[0];
            if (obj instanceof Article){
                ((Article)obj).setId(id);
            } else if  (obj instanceof Link){
                ((Link)obj).setId(id);
            }
            return null;
        };
    }

}
