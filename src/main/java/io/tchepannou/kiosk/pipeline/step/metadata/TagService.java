package io.tchepannou.kiosk.pipeline.step.metadata;

import com.google.common.collect.Maps;
import io.tchepannou.kiosk.core.nlp.filter.TextFilter;
import io.tchepannou.kiosk.core.nlp.tokenizer.Delimiters;
import io.tchepannou.kiosk.core.nlp.tokenizer.NGramTokenizer;
import io.tchepannou.kiosk.core.nlp.tokenizer.StopWords;
import io.tchepannou.kiosk.core.nlp.tokenizer.Tokenizer;
import io.tchepannou.kiosk.core.nlp.toolkit.NLPToolkit;
import io.tchepannou.kiosk.core.nlp.toolkit.NLPToolkitFactory;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.domain.LinkTag;
import io.tchepannou.kiosk.pipeline.persistence.domain.Tag;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkTagRepository;
import io.tchepannou.kiosk.pipeline.persistence.repository.TagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TagService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TagService.class);

    @Autowired
    TagRepository tagRepository;

    @Autowired
    LinkTagRepository linkTagRepository;

    @Autowired
    NLPToolkitFactory nlpToolkitFactory;

    @Autowired
    @Qualifier("TagTextFilter")
    TextFilter textFilter;

    public List<String> extractTags(final String text, final String language) {
        String token;
        final NLPToolkit nlp = nlpToolkitFactory.get(language);
        if (nlp == null){
            return Collections.emptyList();
        }

        // Tokenize
        final StringBuilder buff = new StringBuilder();
        final Tokenizer tokenizer = nlp.getTokenizer(text);
        final StopWords stopWords = nlp.getStopWords();
        while ((token = tokenizer.nextToken()) != null) {
            if (Delimiters.isDelimiter(token) || stopWords.is(token)) {
                continue;
            }
            if (buff.length() > 0) {
                buff.append(' ');
            }
            buff.append(textFilter.filter(token));
        }

        // n-grams
        final List<String> tags = new ArrayList<>();
        final NGramTokenizer ng = new NGramTokenizer(1, 4, nlp.getTokenizer(buff.toString()));
        while ((token = ng.nextToken()) != null) {
            if (isTag(token)) {
                tags.add(token);
            }
        }
        return tags;
    }

    public void tag(final Link link, final List<String> names) {
        // Save the tags
        final List<String> tagNames = names.stream()
                .map(name -> textFilter.filter(name).trim())
                .collect(Collectors.toList());

        final List<Tag> tags = save(tagNames);

        // Associate link+tag
        associate(link, tags);
    }

    private boolean isTag(final String name) {
        return tagRepository.findByName(name) != null;
    }

    private List<Tag> save(final List<String> names) {
        // Get the tags from DB
        final Map<String, Tag> tagMap = Maps.uniqueIndex(
                tagRepository.findByNameIn(names),
                t -> t.getName()
        );

        // Filter tags not already persisted
        final List<Tag> result = new ArrayList<>();
        final List<Tag> tags = new ArrayList<>();
        for (final String name : names) {
            Tag tag = tagMap.get(name);
            if (tag == null) {
                tag = new Tag(name);
                tags.add(tag);
            }

            result.add(tag);
        }

        // Save
        tagRepository.save(tags);

        return result;
    }

    private void associate(final Link link, final List<Tag> tags) {

        // link
        final List<LinkTag> linkTags = linkTagRepository.findByLink(link);
        final Map<Tag, LinkTag> linkTagMap = Maps.uniqueIndex(linkTags, t -> t.getTag());
        final List<LinkTag> toAdd = new ArrayList<>();
        for (final Tag tag : tags) {
            if (!linkTagMap.containsKey(tag)) {
                LOGGER.info("...Tagging <{}> - <{}>", link.getId(), tag.getName());
                toAdd.add(new LinkTag(link, tag));
            }
        }
        linkTagRepository.save(toAdd);

        // unlink
        final List<LinkTag> toDelete = new ArrayList<>();
        for (final LinkTag linkTag : linkTags) {
            if (!tags.contains(linkTag.getTag())) {
                LOGGER.info("...Untagging <{}> - <{}>", link.getId(), linkTag.getTag().getName());
                toDelete.add(linkTag);
            }
        }
        linkTagRepository.delete(toDelete);
    }
}
