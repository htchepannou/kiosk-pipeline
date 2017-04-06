package io.tchepannou.kiosk.pipeline.service;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import org.springframework.beans.factory.annotation.Autowired;

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

    @Autowired
    CloseableHttpClient client;

    public HttpService() {
    }

    protected HttpService(final CloseableHttpClient client) {
        this.client = client;
    }

    /**
     * Download the content of a web resource
     *
     * @param url - URL of the web resource to download
     * @param out - OutputStream where to store the content of the link
     * @return content type
     * @throws IOException
     */
    public String get(final String url, final OutputStream out) throws IOException {
        final HttpGet method = createHttpGet(url);
        try (CloseableHttpResponse response = client.execute(method)) {
            final InputStream in = isText(response)
                    ? getContentTextAsUTF8(response)
                    : response.getEntity().getContent();
            IOUtils.copy(in, out);

            return getContentType(response);
        }
    }

    /**
     * Download the content of a HTML page
     *
     * @param url - URL of the HTML resource to download
     * @param out - OutputStream where to store the content of the link
     * @return content type
     * @throws IOException
     */
    public String getHtml(final String url, final OutputStream out) throws IOException {
        final String contentType = get(url, out);
        ensureIsHtml(contentType);
        return contentType;
    }

    private void ensureIsHtml(final String contentType) throws IOException {
        if (contentType == null || !contentType.contains("text/html")) {
            throw new InvalidContentTypeException("Expecting text/html. Got " + contentType);
        }
    }

    private String getContentType(final CloseableHttpResponse response) {
        final Header header = response.getFirstHeader("Content-Type");
        return header != null ? header.getValue() : null;
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
        String encoding = getEncoding(response);
        if (encoding == null) {
            encoding = detectEncoding(bytes);
        }

        if (encoding == null || "UTF-8".equalsIgnoreCase(encoding)) {
            return new ByteArrayInputStream(bytes);
        } else {
            final String html = new String(bytes, encoding);
            final byte[] utf8 = html.getBytes("UTF-8");
            return new ByteArrayInputStream(utf8);
        }
    }

    private String getEncoding(final CloseableHttpResponse response) {
        final Header header = response.getFirstHeader("Content-Type");
        if (header == null) {
            return null;
        }

        final String contentType = header.getValue();
        final String prefix = "charset=";
        final int i = contentType.indexOf(prefix);
        return i > 0 ? contentType.substring(i + prefix.length()) : null;
    }

    private String detectEncoding(final byte[] bytes) {
        final CharsetDetector detector = new CharsetDetector();
        detector.setText(bytes);

        final CharsetMatch charset = detector.detect();
        return charset != null ? charset.getName() : null;
    }

    private boolean isText(final CloseableHttpResponse response) {
        final Header contentType = response.getFirstHeader("Content-Type");
        return contentType != null && contentType.getValue().startsWith("text/");
    }
}
