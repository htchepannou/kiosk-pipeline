package io.tchepannou.kiosk.pipeline.config;

import io.tchepannou.kiosk.core.service.FileRepository;
import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.core.service.local.LocalFileRepository;
import io.tchepannou.kiosk.core.service.local.LocalMessageQueue;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ConfigurationProperties("kiosk.local")
@Profile("local")
public class LocalConfiguration {
    private String home;

    //-- Service Beans
    @Bean
    @ConfigurationProperties("kiosk.local.service.FileRepository")
    FileRepository fileRepository(){
        return new LocalFileRepository();
    }

    //-- Queues
    @Bean(name = "UrlMessageQueue")
    @ConfigurationProperties("kiosk.local.queue.UrlMessageQueue")
    MessageQueue urlMessageQueue() {
        return new LocalMessageQueue();
    }

    @Bean(name = "MetadataMessageQueue")
    @ConfigurationProperties("kiosk.local.queue.MetadataMessageQueue")
    MessageQueue metadataMessageQueue() {
        return new LocalMessageQueue();
    }

    @Bean(name = "ContentMessageQueue")
    @ConfigurationProperties("kiosk.local.queue.ContentMessageQueue")
    MessageQueue contentMessageQueue() {
        return new LocalMessageQueue();
    }

    @Bean(name = "ValidationMessageQueue")
    @ConfigurationProperties("kiosk.local.queue.ValidationMessageQueue")
    MessageQueue validationMessageQueue() {
        return new LocalMessageQueue();
    }

    @Bean(name = "ImageMessageQueue")
    @ConfigurationProperties("kiosk.local.queue.ImageMessageQueue")
    MessageQueue imageMessageQueue() {
        return new LocalMessageQueue();
    }

    @Bean(name = "ThumbnailMessageQueue")
    @ConfigurationProperties("kiosk.local.queue.ThumbnailMessageQueue")
    MessageQueue thumbnailMessageQueue() {
        return new LocalMessageQueue();
    }

    @Bean(name = "VideoMessageQueue")
    @ConfigurationProperties("kiosk.local.queue.VideoMessageQueue")
    MessageQueue videoMessageQueue() {
        return new LocalMessageQueue();
    }

    @Bean(name = "PublishMessageQueue")
    @ConfigurationProperties("kiosk.local.queue.PublishMessageQueue")
    MessageQueue publishMessageQueue() {
        return new LocalMessageQueue();
    }

    //-- Getter/Setter
    public String getHome() {
        return home;
    }

    public void setHome(final String home) {
        this.home = home;
    }
}
