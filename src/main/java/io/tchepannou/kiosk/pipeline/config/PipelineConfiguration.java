package io.tchepannou.kiosk.pipeline.config;

import io.tchepannou.kiosk.pipeline.step.url.FeedUrlProducer;
import io.tchepannou.kiosk.pipeline.step.url.UrlProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class PipelineConfiguration {
    @PostConstruct
    public void run() {
        // Fetch URLs
        urlProducer().produce();
    }

    //-- Beans
    @Bean
    UrlProducer urlProducer(){
        return new UrlProducer();
    }

    @Bean
    FeedUrlProducer feedUrlProducer(){
        return new FeedUrlProducer();
    }
}
