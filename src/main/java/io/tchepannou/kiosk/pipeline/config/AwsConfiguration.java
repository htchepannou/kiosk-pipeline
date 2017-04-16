package io.tchepannou.kiosk.pipeline.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSAsyncClient;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import io.tchepannou.kiosk.core.service.FileRepository;
import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.core.service.aws.S3FileRepository;
import io.tchepannou.kiosk.core.service.aws.SqsMessageQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Configuration
@ConfigurationProperties("kiosk.aws")
@Profile("!local")
public class AwsConfiguration {
    @Autowired
    Environment env;

    private int connectionTimeout;
    private int maxErrorRetries;

    //-- Service Beans
    @Bean
    @ConfigurationProperties("kiosk.aws.service.FileRepository")
    FileRepository fileRepository(){
        return new S3FileRepository();
    }


    //-- Queues
    @Bean(name = "UrlMessageQueue")
    @ConfigurationProperties("kiosk.aws.queue.UrlMessageQueue")
    MessageQueue urlMessageQueue() {
        return new SqsMessageQueue();
    }

    @Bean(name = "MetadataMessageQueue")
    @ConfigurationProperties("kiosk.aws.queue.MetadataMessageQueue")
    MessageQueue metadataMessageQueue() {
        return new SqsMessageQueue();
    }

    @Bean(name = "ContentMessageQueue")
    @ConfigurationProperties("kiosk.aws.queue.ContentMessageQueue")
    MessageQueue contentMessageQueue() {
        return new SqsMessageQueue();
    }

    @Bean(name = "ValidationMessageQueue")
    @ConfigurationProperties("kiosk.aws.queue.ValidationMessageQueue")
    MessageQueue validationMessageQueue() {
        return new SqsMessageQueue();
    }

    @Bean(name = "ImageMessageQueue")
    @ConfigurationProperties("kiosk.aws.queue.ImageMessageQueue")
    MessageQueue imageMessageQueue() {
        return new SqsMessageQueue();
    }

    @Bean(name = "ThumbnailMessageQueue")
    @ConfigurationProperties("kiosk.aws.queue.ThumbnailMessageQueue")
    MessageQueue thumbnailMessageQueue() {
        return new SqsMessageQueue();
    }

    @Bean(name = "VideoMessageQueue")
    @ConfigurationProperties("kiosk.aws.queue.VideoMessageQueue")
    MessageQueue videoMessageQueue() {
        return new SqsMessageQueue();
    }

    @Bean(name = "PublishMessageQueue")
    @ConfigurationProperties("kiosk.aws.queue.PublishMessageQueue")
    MessageQueue publishMessageQueue() {
        return new SqsMessageQueue();
    }

    @Bean(name = "TagMessageQueue")
    @ConfigurationProperties("kiosk.aws.queue.TagMessageQueue")
    MessageQueue tagMessageQueue() {
        return new SqsMessageQueue();
    }

    //-- AWS Bean
    @Bean
    AmazonS3 amazonS3() {
        return new AmazonS3Client(awsCredentialsProvider(), awsClientConfiguration());
    }

    @Bean
    AmazonSQS amazonSQS() {
        return new AmazonSQSClient(awsCredentialsProvider(), awsClientConfiguration());
    }

    @Bean
    AmazonSNS amazonSNS() {
        return new AmazonSNSAsyncClient(awsCredentialsProvider(), awsClientConfiguration());
    }

    @Bean
    AWSCredentialsProvider awsCredentialsProvider() {
        if (env.acceptsProfiles("ci")) {
            return new SystemPropertiesCredentialsProvider();
        } else if (env.acceptsProfiles("dev")) {
            final String home = System.getProperty("user.home");
            return new PropertiesFileCredentialsProvider(home + "/.aws/credentials");
        } else {
            return new DefaultAWSCredentialsProviderChain();
        }
    }

    @Bean
    ClientConfiguration awsClientConfiguration() {
        return new ClientConfiguration()
                .withConnectionTimeout(connectionTimeout)
                .withGzip(true)
                .withMaxErrorRetry(maxErrorRetries)
                ;
    }

    //-- Getter/Setter
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(final int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getMaxErrorRetries() {
        return maxErrorRetries;
    }

    public void setMaxErrorRetries(final int maxErrorRetries) {
        this.maxErrorRetries = maxErrorRetries;
    }
}
