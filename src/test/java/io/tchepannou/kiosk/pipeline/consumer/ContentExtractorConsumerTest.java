package io.tchepannou.kiosk.pipeline.consumer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.repository.ArticleRepository;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import io.tchepannou.kiosk.pipeline.service.content.ContentExtractor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContentExtractorConsumerTest {
    @Mock
    AmazonS3 s3;

    @Mock
    LinkRepository linkRepository;

    @Mock
    ContentExtractor extractor;

    @Mock
    ArticleRepository articleRepository;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    ContentExtractorConsumer consumer;

    int read;

    @Before
    public void setUp() {
        consumer.setS3Bucket("bucket");
        consumer.setS3Key("dev/content");
        consumer.setS3KeyHtml("dev/html");

        read = 1;
    }

    @Test
    public void shouldConsume() throws Exception {
        // Given
        final String s3Key = "dev/html/2010/10/11/test.html";
        final Link link = new Link ();
        link.setId(123);
        link.setS3Key(s3Key);
        when(linkRepository.findOne(123L)).thenReturn(link);

        final S3Object obj = createS3Object("bucket", s3Key);
        final S3ObjectInputStream in = createS3InputStream("hello");
        when(obj.getObjectContent()).thenReturn(in);
        when(s3.getObject("bucket", s3Key)).thenReturn(obj);

        when(extractor.extract("hello")).thenReturn("world");

        final String body = "{\n"
                + "  \"Type\" : \"Notification\",\n"
                + "  \"MessageId\" : \"63a3f6b6-d533-4a47-aef9-fcf5cf758c76\",\n"
                + "  \"TopicArn\" : \"arn:aws:sns:us-west-2:123456789012:MyTopic\",\n"
                + "  \"Subject\" : \"Testing publish to subscribed queues\",\n"
                + "  \"Message\" : \"123\",\n"
                + "  \"Timestamp\" : \"2012-03-29T05:12:16.901Z\",\n"
                + "  \"SignatureVersion\" : \"1\",\n"
                + "  \"Signature\" : \"EXAMPLEnTrFPa37tnVO0FF9Iau3MGzjlJLRfySEoWz4uZHSj6ycK4ph71Zmdv0NtJ4dC/El9FOGp3VuvchpaTraNHWhhq/OsN1HVz20zxmF9b88R8GtqjfKB5woZZmz87HiM6CYDTo3l7LMwFT4VU7ELtyaBBafhPTg9O5CnKkg=\",\n"
                + "  \"SigningCertURL\" : \"https://sns.us-west-2.amazonaws.com/SimpleNotificationService-f3ecfb7224c7233fe7bb5f59f96de52f.pem\",\n"
                + "  \"UnsubscribeURL\" : \"https://sns.us-west-2.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:us-west-2:123456789012:MyTopic:c7fe3a54-ab0e-4ec2-88e0-db410a0f2bee\"\n"
                + "}";
        final Map snsNotification = new HashMap<>();
        snsNotification.put("Message", "123");
        when(objectMapper.readValue(body, Object.class)).thenReturn(snsNotification);

        // When
        consumer.consume(body);

        // Then
        verify(s3).putObject(
                eq("bucket"),
                eq("dev/content/2010/10/11/test.html"),
                any(InputStream.class),
                any(ObjectMetadata.class)
        );

        ArgumentCaptor<Article> article = ArgumentCaptor.forClass(Article.class);
        verify(articleRepository).save(article.capture());
        assertThat(article.getValue().getLink()).isEqualTo(link);
        assertThat(article.getValue().getS3Key()).isEqualTo("dev/content/2010/10/11/test.html");
    }

    @Test
    public void shouldNotConsumeInvalidLink() throws Exception {
        // Given
        final String body = "{\n"
                + "  \"Type\" : \"Notification\",\n"
                + "  \"MessageId\" : \"63a3f6b6-d533-4a47-aef9-fcf5cf758c76\",\n"
                + "  \"TopicArn\" : \"arn:aws:sns:us-west-2:123456789012:MyTopic\",\n"
                + "  \"Subject\" : \"Testing publish to subscribed queues\",\n"
                + "  \"Message\" : \"123\",\n"
                + "  \"Timestamp\" : \"2012-03-29T05:12:16.901Z\",\n"
                + "  \"SignatureVersion\" : \"1\",\n"
                + "  \"Signature\" : \"EXAMPLEnTrFPa37tnVO0FF9Iau3MGzjlJLRfySEoWz4uZHSj6ycK4ph71Zmdv0NtJ4dC/El9FOGp3VuvchpaTraNHWhhq/OsN1HVz20zxmF9b88R8GtqjfKB5woZZmz87HiM6CYDTo3l7LMwFT4VU7ELtyaBBafhPTg9O5CnKkg=\",\n"
                + "  \"SigningCertURL\" : \"https://sns.us-west-2.amazonaws.com/SimpleNotificationService-f3ecfb7224c7233fe7bb5f59f96de52f.pem\",\n"
                + "  \"UnsubscribeURL\" : \"https://sns.us-west-2.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:us-west-2:123456789012:MyTopic:c7fe3a54-ab0e-4ec2-88e0-db410a0f2bee\"\n"
                + "}";
        final Map snsNotification = new HashMap<>();
        snsNotification.put("Message", "123");
        when(objectMapper.readValue(body, Object.class)).thenReturn(snsNotification);

        when(linkRepository.findOne(123L)).thenReturn(null);

        // When
        consumer.consume(body);

        // Then
        verify(s3, never()).putObject(anyString(), anyString(), any(), any());
        verify(articleRepository, never()).save(any(Article.class));
    }


    @Test
    public void shouldNotConsumeInvalidMessage() throws Exception {
        // Given
        final String body = "This is the body";
        final Map snsNotification = new HashMap<>();
        snsNotification.put("Message", null);
        when(objectMapper.readValue(body, Object.class)).thenReturn(snsNotification);

        // When
        consumer.consume(body);

        // Then
        verify(s3, never()).putObject(anyString(), anyString(), any(), any());
        verify(articleRepository, never()).save(any(Article.class));
    }

    private S3Object createS3Object(final String bucket, final String key) throws Exception {
        final S3Object obj = mock(S3Object.class);
        when(obj.getBucketName()).thenReturn(bucket);
        when(obj.getKey()).thenReturn(key);

        return obj;
    }

    private S3ObjectInputStream createS3InputStream(final String content) throws IOException {
        final S3ObjectInputStream in = mock(S3ObjectInputStream.class);
        when(in.read(any(byte[].class), anyInt(), anyInt())).then(new Answer<Integer>() {
            @Override
            public Integer answer(final InvocationOnMock inv) throws Throwable {
                final byte[] buff = (byte[]) inv.getArguments()[0];
                if (read-- <= 0){
                    return -1;
                }
                for (int i=0 ; i<content.length() ; i++){
                    buff[i] = content.getBytes()[i];
                }
                return content.length();
            }
        });
        return in;
    }
}
