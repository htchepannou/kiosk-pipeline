package io.tchepannou.kiosk.pipeline.service.similarity;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

import static io.tchepannou.kiosk.pipeline.Fixtures.createDocument;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SimilarityServiceTest {
    @Mock
    TextSimilaryAlgorithm similaryAlgorithm;

    @Mock
    ShingleExtractor shingleExtractor;

    @InjectMocks
    SimilarityService service;

    @Before
    public void setUp() {
        service.setShingleSize(3);
        service.similaryAlgorithm = similaryAlgorithm;
        service.shingleExtractor = shingleExtractor;

        TextFilter filter = mock(TextFilter.class);
        service.setFilters(Arrays.asList(filter));
        when(filter.filter(anyString())).thenAnswer((inv) -> inv.getArguments()[0]);
    }

    @Test
    public void shouldOutputSimilarityMatrix() throws Exception {
        // Given
        final Document doc1 = createDocument(11, "doc1");
        final Document doc2 = createDocument(12, "doc2");
        final Document doc3 = createDocument(13, "doc3");

        final List<String> s1 = Arrays.asList("A1 B1 C1", "B1 C1 D1", "E1 F1 G1");
        final List<String> s2 = Arrays.asList("A2 B2 C2", "E2 F2 G2", "F2 G2 H2");
        final List<String> s3 = Arrays.asList("A3 B3 C3", "B3 C3 D3", "E3 F3 G3");
        when(shingleExtractor.extract("doc1", 3)).thenReturn(s1);
        when(shingleExtractor.extract("doc2", 3)).thenReturn(s2);
        when(shingleExtractor.extract("doc3", 3)).thenReturn(s3);

        when(similaryAlgorithm.compute(s1, s2)).thenReturn(.5f);
        when(similaryAlgorithm.compute(s1, s3)).thenReturn(.95f);

        when(similaryAlgorithm.compute(s2, s1)).thenReturn(.5f);
        when(similaryAlgorithm.compute(s2, s3)).thenReturn(.71f);

        when(similaryAlgorithm.compute(s3, s1)).thenReturn(.11f);
        when(similaryAlgorithm.compute(s3, s2)).thenReturn(.12f);

        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        // When
        service.compute(Arrays.asList(doc1, doc2, doc3), out);

        // Then
        assertThat(IOUtils.toString(new ByteArrayInputStream(out.toByteArray()))).isEqualTo(
                        "3\n"
                        + "11\n"
                        + "12\n"
                        + "13\n"
                        + "1.00 0.50 0.95 \n"
                        + "0.50 1.00 0.71 \n"
                        + "0.11 0.12 1.00 \n"
        );
    }
}
