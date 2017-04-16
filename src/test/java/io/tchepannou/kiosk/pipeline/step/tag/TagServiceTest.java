package io.tchepannou.kiosk.pipeline.step.tag;

import io.tchepannou.kiosk.core.nlp.filter.TextFilter;
import io.tchepannou.kiosk.core.nlp.tokenizer.BasicTokenizer;
import io.tchepannou.kiosk.core.nlp.tokenizer.StopWords;
import io.tchepannou.kiosk.core.nlp.toolkit.NLPToolkit;
import io.tchepannou.kiosk.core.nlp.toolkit.NLPToolkitFactory;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.domain.LinkTag;
import io.tchepannou.kiosk.pipeline.persistence.domain.Tag;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkTagRepository;
import io.tchepannou.kiosk.pipeline.persistence.repository.TagRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.tchepannou.kiosk.pipeline.Fixtures.createTag;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TagServiceTest {
    @Mock
    TagRepository tagRepository;

    @Mock
    LinkTagRepository linkTagRepository;

    @Mock
    TextFilter textFilter;

    @Mock
    StopWords stopWords;

    @Mock
    NLPToolkit toolkit;

    @Mock
    NLPToolkitFactory nlpToolkitFactory;

    @InjectMocks
    TagService service;

    @Before
    public void setUp() {
        when(textFilter.filter(anyString())).then((inv) -> inv.getArguments()[0]);

        when(stopWords.is("est")).thenReturn(true);
        when(stopWords.is("son")).thenReturn(true);
        when(stopWords.is("a")).thenReturn(true);
        when(stopWords.is("Les")).thenReturn(true);
        when(stopWords.is("s")).thenReturn(true);
        when(stopWords.is("est")).thenReturn(true);
        when(stopWords.is("de")).thenReturn(true);
        when(stopWords.is("la")).thenReturn(true);

        when(toolkit.getTokenizer(anyString())).then((inv) -> new BasicTokenizer((String) inv.getArguments()[0]));
        when(toolkit.getStopWords()).thenReturn(stopWords);

        when(nlpToolkitFactory.get(anyString())).thenReturn(toolkit);
    }


    @Test
    public void shouldExtractEntitties() {
        // Given

        // When
        final String text = "Jean-Paul Lemaire est alle voir son ami a Yaounde." +
                "Les assises de la Fecafoot s'est deroule a Douala";
        final Collection<String> result = service.extractEntities(text, "fr");

        // Then
        assertThat(result).contains("Jean-Paul Lemaire", "Yaounde", "Fecafoot", "Douala");
    }

    @Test
    public void shouldTag() throws Exception {
        // Given
        final Link link = new Link();

        when(tagRepository.findByNameIn(anyList())).thenReturn(Collections.emptyList());
        when(linkTagRepository.findByLink(link)).thenReturn(Collections.emptyList());

        // When
        service.tag(link, Arrays.asList("a", "b", "c", "d"));

        // Then
        final ArgumentCaptor<List> tags = ArgumentCaptor.forClass(List.class);
        verify(tagRepository).save(tags.capture());
        assertThat(toNames(tags.getValue())).containsExactly("a", "b", "c", "d");

        final ArgumentCaptor<List> linkTags = ArgumentCaptor.forClass(List.class);
        verify(linkTagRepository).save(linkTags.capture());
        assertThat(linkTags.getValue()).hasSize(4);
        for (final LinkTag linkTag : (List<LinkTag>) linkTags.getValue()) {
            assertThat(linkTag.getLink()).isEqualTo(link);
            assertThat(Arrays.asList("a", "b", "c", "d")).contains(linkTag.getTag().getName());
        }
    }

    @Test
    public void shouldUntag() throws Exception {
        // Given
        final Link link = new Link();

        final Tag x = createTag("x");
        final Tag y = createTag("y");
        when(tagRepository.findByNameIn(anyList())).thenReturn(Arrays.asList(x, y));

        final LinkTag lx = new LinkTag(link, x);
        final LinkTag ly = new LinkTag(link, y);
        when(linkTagRepository.findByLink(link)).thenReturn(Arrays.asList(lx, ly));

        // When
        service.tag(link, Arrays.asList("a"));

        // Then
        final ArgumentCaptor<List> tags = ArgumentCaptor.forClass(List.class);
        verify(tagRepository).save(tags.capture());
        assertThat(toNames(tags.getValue())).containsExactly("a");

        final ArgumentCaptor<List> linkTags = ArgumentCaptor.forClass(List.class);
        verify(linkTagRepository).save(linkTags.capture());
        assertThat(linkTags.getValue()).hasSize(1);

        verify(linkTagRepository).delete(linkTags.capture());
        assertThat(linkTags.getValue()).hasSize(2);
        for (final LinkTag linkTag : (List<LinkTag>) linkTags.getValue()) {
            assertThat(linkTag.getLink()).isEqualTo(link);
            assertThat(Arrays.asList("x", "y")).contains(linkTag.getTag().getName());
        }
    }

    private List<String> toNames(final List<Tag> tags) {
        return tags.stream().map(t -> t.getName()).collect(Collectors.toList());
    }
}
