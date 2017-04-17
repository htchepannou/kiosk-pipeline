package io.tchepannou.kiosk.pipeline.step;

import io.tchepannou.kiosk.core.service.FileRepository;
import io.tchepannou.kiosk.persistence.domain.Link;
import io.tchepannou.kiosk.persistence.repository.AssetRepository;
import io.tchepannou.kiosk.persistence.repository.LinkRepository;
import org.apache.commons.io.IOUtils;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;

import java.io.InputStream;
import java.io.OutputStream;

public class LinkConsumerTestSupport {
    @Mock
    protected FileRepository repository;

    @Mock
    protected LinkRepository linkRepository;

    @Mock
    protected AssetRepository assetRepository;

    protected final Answer save(final long id) {
        return (inv) -> {
            final Link img = (Link) inv.getArguments()[0];
            if (img.getId() == 0) {
                img.setId(id);
            }
            return null;
        };
    }

    protected final Answer read(final String path) {
        return (inv) -> {
            final InputStream in = getClass().getResourceAsStream(path);
            final OutputStream out = (OutputStream) inv.getArguments()[1];
            IOUtils.copy(in, out);
            return null;
        };
    }

}
