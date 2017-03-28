package io.tchepannou.kiosk.pipeline.step;

import io.tchepannou.kiosk.core.service.FileRepository;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import org.mockito.Mock;

public class LinkConsumerTestSupport {
    @Mock
    protected FileRepository repository;

    @Mock
    protected LinkRepository linkRepository;
}
