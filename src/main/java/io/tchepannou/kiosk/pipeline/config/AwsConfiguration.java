package io.tchepannou.kiosk.pipeline.config;

import io.tchepannou.kiosk.core.service.FileRepository;
import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.core.service.aws.S3FileRepository;
import io.tchepannou.kiosk.core.service.aws.SqsMessageQueue;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ConfigurationProperties("kiosk.aws")
@Profile("!local")
public class AwsConfiguration {
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
}
