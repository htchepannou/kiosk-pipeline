package io.tchepannou.kiosk.pipeline.step.tag;

import com.google.common.collect.Maps;
import io.tchepannou.kiosk.core.nlp.filter.TextFilter;
import io.tchepannou.kiosk.core.nlp.tokenizer.Delimiters;
import io.tchepannou.kiosk.core.nlp.tokenizer.FragmentTokenizer;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public Collection<String> extractEntities(final String text, final String language) {
        final NLPToolkit nlp = nlpToolkitFactory.get(language);
        if (nlp == null) {
            return Collections.emptyList();
        }

        final Set<String> result = new HashSet<>();
        final FragmentTokenizer tokenizer = new FragmentTokenizer(nlp.getTokenizer(text));
        String fragment;
        while ((fragment = tokenizer.nextToken()) != null) {
            final Collection entities = extractEntitiesFromFragment(fragment, nlp);
            result.addAll(entities);
        }
        return result;
    }

    private Collection extractEntitiesFromFragment(final String text, final NLPToolkit nlp) {
        final Set<String> tags = new HashSet<>();
        final Tokenizer tokeninzer = nlp.getTokenizer(text);
        final StopWords stopWords = nlp.getStopWords();
        final StringBuilder entity = new StringBuilder();

        String token;
        for (int i=0 ; (token = tokeninzer.nextToken()) != null ; i++) {
            if (Delimiters.isWhitespace(token)){
                continue;
            } else if (isEntity(token)) {
                if (i==0 && stopWords.is(token)){
                    continue;
                } else{
                    if (entity.length() > 0){
                        entity.append(' ');
                    }
                    entity.append(token);
                }
            } else {
                if (entity.length() > 0){
                    tags.add(entity.toString());
                    entity.setLength(0);
                }
            }
        }

        if (entity.length() > 0){
            tags.add(entity.toString());
        }
        return tags;
    }

    public void tag(final Link link, final Collection<String> names) {
        // Save the tags
        final List<String> tagNames = names.stream()
                .map(name -> textFilter.filter(name).trim())
                .collect(Collectors.toList());

        final List<Tag> tags = save(tagNames);

        // Associate link+tag
        associate(link, tags);
    }

    //-- Private
    private boolean isEntity(final String text) {
        final String[] parts = text.split("\\s+");
        for (final String part : parts) {
            if (!Character.isUpperCase(part.charAt(0))) {
                return false;
            }
        }
        return true;
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
