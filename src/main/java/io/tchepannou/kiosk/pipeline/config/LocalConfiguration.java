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
    @ConfigurationProperties("kiosk.local.services.FileRepository")
    FileRepository fileRepository(){
        return new LocalFileRepository();
    }

    //-- Queues
    @Bean(name = "UrlMessageQueue")
    @ConfigurationProperties("kiosk.local.queues.UrlMessageQueue")
    MessageQueue urlMessageQueue() {
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
