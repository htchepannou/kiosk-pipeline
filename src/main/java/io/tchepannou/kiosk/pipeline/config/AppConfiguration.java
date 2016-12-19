package io.tchepannou.kiosk.pipeline.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.tchepannou.kiosk.pipeline.processor.LoadFeedsProcessor;
import io.tchepannou.kiosk.pipeline.processor.UrlExtractorProcessor;
import io.tchepannou.kiosk.pipeline.service.HttpService;
import io.tchepannou.kiosk.pipeline.service.ShutdownService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class AppConfiguration {
    //-- Spring
    @Bean
    Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
        return new Jackson2ObjectMapperBuilder()
                .simpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .timeZone(TimeZone.getTimeZone("GMT"))
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .featuresToDisable(
                        DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES,
                        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
                );
    }

    @Bean
    ObjectMapper objectMapper(){
        return jackson2ObjectMapperBuilder().build();
    }

    @Bean
    Executor executor (@Value("kiosk.threadPool.size") int size){
        return Executors.newFixedThreadPool(size);
    }

    //-- Pipeline
    @Bean
    LoadFeedsProcessor loadFeedsProcessor(){
        return new LoadFeedsProcessor();
    }

    @Bean
    UrlExtractorProcessor urlExtractorProcessor(){
        return new UrlExtractorProcessor();
    }

    //-- Services
    @Bean
    public ShutdownService shutdownService(){
        return new ShutdownService();
    }

    @Bean
    public HttpService httpService(){
        return new HttpService();
    }
}
