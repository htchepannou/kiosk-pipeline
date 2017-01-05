package io.tchepannou.kiosk.pipeline.service.similarity;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static io.tchepannou.kiosk.pipeline.Fixtures.createS3InputStream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArticleDocumentFactoryTest {
    @Mock
    AmazonS3 s3;

    @InjectMocks
    ArticleDocumentFactory factory;

    @Before
    public void setUp() {
        factory.setS3Bucket("bucket");
    }

    @Test
    public void shouldReturnDocument() throws Exception {
        // Given
        final String s3Key = "dev/content/2010/10/11/test.html";
        final Article article = new Article();
        article.setId(123);
        article.setS3Key(s3Key);

        final S3ObjectInputStream in = createS3InputStream("<p>Hello world</p>");
        final S3Object obj = mock(S3Object.class);
        when(obj.getObjectContent()).thenReturn(in);
        when(s3.getObject("bucket", "dev/content/2010/10/11/test.html")).thenReturn(obj);

        // When
        final Document doc = factory.createDocument(article);

        // Then
        assertThat(doc.getId()).isEqualTo(123);
        assertThat(doc.getContent()).isEqualTo("Hello world");
    }


    @Test
    public void shouldReturnNullWhenExcetionWhileLoadingContent() throws Exception {
        // Given
        final String s3Key = "dev/content/2010/10/11/test.html";
        final Article article = new Article();
        article.setId(123);
        article.setS3Key(s3Key);

        when(s3.getObject("bucket", "dev/content/2010/10/11/test.html")).thenThrow(IOException.class);

        // When
        final Document doc = factory.createDocument(article);

        // Then
        assertThat(doc).isNull();
    }
}
