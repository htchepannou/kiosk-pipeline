package io.tchepannou.kiosk.pipeline.service.similarity;

import com.google.common.annotations.VisibleForTesting;
import io.tchepannou.kiosk.core.nlp.filter.TextFilter;
import io.tchepannou.kiosk.core.nlp.similarity.TextSimilaryCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Deprecated
public class SimilarityService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimilarityService.class);
    @Autowired
    TextSimilaryCalculator similaryAlgorithm;

    @Autowired
    ShingleExtractor shingleExtractor;

    private List<TextFilter> filters = Collections.emptyList();

    private int maxShingles = 200;

    private int shingleSize = 5;

    @VisibleForTesting
    protected SimilarityService() {
    }

    public SimilarityService(final List<TextFilter> filters) {
        this.filters = filters;
    }

    //-- Public

    /**
     * Compute the similarity of documents and store it into an OutputStream.
     * Format of the file:
     * <code>
     * NUMBER OF DOCUMENT
     * DOC-ID #1
     * DOC-ID #2
     * ...
     * DOC-ID #n
     * ratio-1x1 ratio-1x2 ... ration-1xn
     * ratio-2x1 ratio-2x2 ... ration-2xn
     * ...
     * ratio-nx1 ratio-nx2 ... ration-nxn
     * </code>
     */
    public void compute(final List<Document> documents, final OutputStream out) throws IOException {
        // Shingles per document
        final Map<Document, Collection<String>> shingles = new LinkedHashMap<>();
        for (final Document doc : documents) {
            final Collection<String> sh = extractSingles(doc);
            shingles.put(doc, sh);
        }

        // Output document IDS
        try (final PrintWriter writer = new PrintWriter(new OutputStreamWriter(out))) {
            writer.println(String.valueOf(documents.size()));
            for (final Document doc : documents) {
                writer.println(doc.getId());
            }

            // Find similar
            final DecimalFormat df = new DecimalFormat("0.00");
            final int documentCount = documents.size();
            for (int i = 0; i < documentCount; i++) {
                final Document idoc = documents.get(i);
                final Collection<String> ishingles = shingles.get(idoc);
                LOGGER.info("{}.- Processing Document#{}. {} shingles", i, idoc.getId(), ishingles.size());

                for (int j = 0; j < documentCount; j++) {
                    if (i == j) {
                        writer.print(df.format(1));
                    } else {
                        final Document jdoc = documents.get(j);
                        final Collection<String> jshingles = shingles.get(jdoc);

                        final float similary = similaryAlgorithm.compute(ishingles, jshingles);
                        writer.print(df.format(similary));
                    }
                    writer.print(' ');
                }

                writer.println();
            }
        }
    }

    public Collection<Pair> filter(final InputStream in, final float minThreshold, final float maxThreshold) throws IOException {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

            /* load document ids */
            final int documentCount = Integer.parseInt(reader.readLine());
            final List<Long> documentIds = new ArrayList<>();
            for (int i = 0; i < documentCount; i++) {
                final long id = Long.parseLong(reader.readLine());
                documentIds.add(id);
            }

            /* find dedups */
            final Collection<Pair> pairs = new ArrayList<>();
            for (int i = 0; i < documentCount; i++) {
                final long iid = documentIds.get(i);
                final String line = reader.readLine().trim();
                final String[] ratios = line.split(" ");
                LOGGER.info("{}.- Processing Document#{}s", i, iid);

                for (int j = i + 1; j < documentCount; j++) {
                    final long jid = documentIds.get(j);
                    try {
                        final float ratio = Float.parseFloat(ratios[j]);
                        if (ratio >= minThreshold && ratio < maxThreshold) {
                            pairs.add(new Pair(iid, jid, ratio));
                        }
                    } catch (final Exception e) {
                        LOGGER.warn("{}.{}- Invalid line: {}", i, j, line, e);
                    }
                }
            }

            return pairs;
        }
    }

//    public static void main(final String[] args) throws Exception {
//        final File file = new File(System.getProperty("user.home") + "/Downloads/matrix_13.txt");
//        try (final FileInputStream fin = new FileInputStream(file)) {
//            final Collection<Pair> dedups = new SimilarityService().filter(fin, .1f, .3f);
//            System.out.println(dedups.size() + " dedup");
//            for (final Pair dedup : dedups) {
//                System.out.println(String.format("%s - %s - %s", dedup.getId1(), dedup.getId2(), dedup.getRatio()));
//            }
//        }
//    }

    //-- Private
    private Collection<String> extractSingles(final Document doc) {
        // Filter the content
        String content = doc.getContent();
        for (final TextFilter filter : filters) {
            content = filter.filter(content);
        }

        // Extract the shingles
        final List<String> shingles = shingleExtractor.extract(content, shingleSize);

        // Select n shingles
        final List<String> result = new ArrayList<>();
        for (final String shingle : shingles) {
            if (!result.contains(shingle)) {
                result.add(shingle);
            }
            if (result.size() >= maxShingles) {
                break;
            }
        }
        return result;
    }

    // Getter/Setter
    public int getShingleSize() {
        return shingleSize;
    }

    public void setShingleSize(final int shingleSize) {
        this.shingleSize = shingleSize;
    }

    public int getMaxShingles() {
        return maxShingles;
    }

    public void setMaxShingles(final int maxShingles) {
        this.maxShingles = maxShingles;
    }

    public List<TextFilter> getFilters() {
        return filters;
    }

    public void setFilters(final List<TextFilter> filters) {
        this.filters = filters;
    }
}
