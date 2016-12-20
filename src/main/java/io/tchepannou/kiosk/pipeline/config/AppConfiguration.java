package io.tchepannou.kiosk.pipeline.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import io.tchepannou.kiosk.pipeline.processor.LoadFeedsProcessor;
import io.tchepannou.kiosk.pipeline.service.HttpService;
import io.tchepannou.kiosk.pipeline.service.ShutdownService;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import javax.sql.DataSource;
import java.time.Clock;
import java.util.TimeZone;

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

    @Bean(destroyMethod = "close")
    @ConfigurationProperties(prefix = "spring.datasource")
    @Primary
    DataSource dataSource() {
        final HikariDataSource source = (HikariDataSource) DataSourceBuilder
                .create()
                .type(HikariDataSource.class)
                .build();
        return source;
    }



    @Bean
    ObjectMapper objectMapper() {
        return jackson2ObjectMapperBuilder().build();
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    //-- Pipeline
    @Bean
    LoadFeedsProcessor loadFeedsProcessor() {
        return new LoadFeedsProcessor();
    }


    //-- Services
    @Bean
    public ShutdownService shutdownService() {
        return new ShutdownService();
    }

    @Bean
    public HttpService httpService() {
        return new HttpService();
    }
}
