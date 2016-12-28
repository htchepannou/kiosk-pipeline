package io.tchepannou.kiosk.pipeline.service;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HttpService {
    private static final String VERSION = "1.0";
    private static final String USER_AGENT = String.format("Mozilla/5.0 (compatible; Kioskbot/%s)", VERSION);

    static {
        System.setProperty("http.agent", USER_AGENT);
    }

    public void get(final String url, final OutputStream out) throws IOException {
        try (final CloseableHttpClient client = HttpClients.createDefault()) {
            final HttpGet method = createHttpGet(url);
            try (CloseableHttpResponse response = client.execute(method)) {
                final InputStream in = isText(response)
                        ? getContentTextAsUTF8(response)
                        : response.getEntity().getContent();
                IOUtils.copy(in, out);
            }
        }
    }

    public void getHtml(final String url, final OutputStream out) throws IOException {
        try (final CloseableHttpClient client = HttpClients.createDefault()) {
            final HttpGet method = createHttpGet(url);
            try (CloseableHttpResponse response = client.execute(method)) {
                ensureIsHtml(response);

                final InputStream in = isText(response)
                        ? getContentTextAsUTF8(response)
                        : response.getEntity().getContent();
                IOUtils.copy(in, out);
            }
        }
    }

    private void ensureIsHtml(final CloseableHttpResponse response) throws IOException {
        final Header header = response.getFirstHeader("Content-Type");
        if (header == null) {
            throw new InvalidContentTypeException("No Content-Type header");
        } else {
            final String value = header.getValue();
            if (value == null || !value.contains("text/html")) {
                throw new InvalidContentTypeException("Expecting text/html. Got " + value);
            }
        }
    }

    private HttpGet createHttpGet(final String url) {
        final HttpGet method = new HttpGet(url);
        method.setHeader("Connection", "keep-alive");
        method.setHeader("User-Agent", USER_AGENT);
        return method;
    }

    private InputStream getContentTextAsUTF8(final CloseableHttpResponse response) throws IOException {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        IOUtils.copy(response.getEntity().getContent(), bout);

        final byte[] bytes = bout.toByteArray();
        final CharsetDetector detector = new CharsetDetector();
        detector.setText(bytes);

        final CharsetMatch charset = detector.detect();
        if (charset == null || "utf-8".equalsIgnoreCase(charset.getName())) {
            return new ByteArrayInputStream(bout.toByteArray());
        }

        final String encoding = charset.getName();
        final String html = new String(bytes, encoding);
        final byte[] utf8 = html.getBytes("UTF-8");
        return new ByteArrayInputStream(utf8);
    }

    private boolean isText(final CloseableHttpResponse response) {
        final Header contentType = response.getFirstHeader("Content-Type");
        return contentType != null && contentType.getValue().startsWith("text/");
    }
}
