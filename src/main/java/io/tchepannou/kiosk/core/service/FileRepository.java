package io.tchepannou.kiosk.core.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface FileRepository {
    void read(final String path, OutputStream out) throws IOException;
    void write(final String path, InputStream in) throws IOException;
    void delete(final String path) throws IOException;
}
