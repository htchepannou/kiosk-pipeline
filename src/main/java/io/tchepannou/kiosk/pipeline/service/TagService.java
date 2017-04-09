package io.tchepannou.kiosk.pipeline.service;

import com.google.common.collect.Maps;
import io.tchepannou.kiosk.core.nlp.filter.TextFilter;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.domain.LinkTag;
import io.tchepannou.kiosk.pipeline.persistence.domain.Tag;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkTagRepository;
import io.tchepannou.kiosk.pipeline.persistence.repository.TagRepository;
import io.tchepannou.kiosk.pipeline.step.metadata.HtmlTagExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TagService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TagService.class);

    @Autowired
    HtmlTagExtractor htmlTagExtractor;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    LinkTagRepository linkTagRepository;

    TextFilter textFilter;

    public TagService(final TextFilter textFilter) {
        this.textFilter = textFilter;
    }


    public void tag(final Link link, final List<String> names) {
        // Save the tags
        final List<String> tagNames = names.stream()
                .map(name -> textFilter.filter(name))
                .collect(Collectors.toList());

        final List<Tag> tags = save(tagNames);

        // Associate link+tag
        associate(link, tags);
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
                LOGGER.info("...Tagging <{}> with: {}", link.getId(), tag.getName());
                toAdd.add(new LinkTag(link, tag));
            }
        }
        linkTagRepository.save(toAdd);

        // unlink
        final List<LinkTag> toDelete = new ArrayList<>();
        for (final LinkTag linkTag : linkTags) {
            if (!tags.contains(linkTag.getTag())) {
                LOGGER.info("...Untagging <{}> with: {}", link.getId(), linkTag.getTag().getName());
                toDelete.add(linkTag);
            }
        }
        linkTagRepository.delete(toDelete);
    }
}
