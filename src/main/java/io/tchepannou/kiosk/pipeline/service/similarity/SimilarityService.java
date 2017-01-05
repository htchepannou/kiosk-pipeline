package io.tchepannou.kiosk.pipeline.service.similarity;

import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
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

public class SimilarityService {
    @Autowired
    TextSimilaryAlgorithm similaryAlgorithm;

    @Autowired
    ShingleExtractor shingleExtractor;

    private List<TextFilter> filters = Collections.emptyList();

    private int maxShingles = 200;

    private int shingleSize = 5;

    @VisibleForTesting
    protected SimilarityService(){
    }

    public SimilarityService(final List<TextFilter> filters) {
        this.filters = filters;
    }

    //-- Public

    /**
     * Compute the similarity of documents and store it into an OutputStream
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
