package io.tchepannou.kiosk.pipeline.step.tag;

import io.tchepannou.kiosk.persistence.domain.Link;
import io.tchepannou.kiosk.persistence.repository.LinkTagRepository;
import io.tchepannou.kiosk.pipeline.step.AbstractLinkConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Collection;

@Transactional
public class TagConsumer extends AbstractLinkConsumer{
    private static final Logger LOGGER = LoggerFactory.getLogger(TagConsumer.class);

    @Autowired
    TagService tagService;

    @Autowired
    LinkTagRepository linkTagRepository;

    @Override
    protected void consume(final Link link) throws IOException {
        LOGGER.info("Tagging <{}>", link.getId());

        final String content = getContentDocument(link).text();

        Collection<String> tags = tagService.extractEntities(content, link.getLanguage());

        tagService.tag(link, tags);
    }

}
