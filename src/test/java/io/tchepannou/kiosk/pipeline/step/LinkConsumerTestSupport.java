package io.tchepannou.kiosk.pipeline.step;

import io.tchepannou.kiosk.core.service.FileRepository;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LinkConsumerTestSupport {
    @Mock
    protected FileRepository repository;

    @Mock
    protected LinkRepository linkRepository;
}
