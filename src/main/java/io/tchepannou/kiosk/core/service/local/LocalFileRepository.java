package io.tchepannou.kiosk.core.service.local;

import com.amazonaws.util.IOUtils;
import io.tchepannou.kiosk.core.service.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;

public class LocalFileRepository implements FileRepository {
    public static final Logger LOGGER = LoggerFactory.getLogger(LocalFileRepository.class);

    private String home;

    @Override
    public void read(final String path, final OutputStream out) throws IOException {
        try (FileInputStream in = new FileInputStream(toFile(path))){
            IOUtils.copy(in, out);
        }
    }

    @Override
    public void write(final String path, final InputStream in) throws IOException {
        final File file = toFile(path);
        final File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (FileOutputStream out = new FileOutputStream(file)){
            LOGGER.info("Storing {}", file.getAbsolutePath());
            IOUtils.copy(in, out);
        }
    }

    @Override
    public void delete(final String path) throws IOException {
        toFile(path).delete();
    }

    private File toFile (final String path){
        return Paths.get(home, path).toFile();
    }

    public String getHome() {
        return home;
    }

    public void setHome(final String home) {
        this.home = home;
    }
}
