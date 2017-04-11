package io.tchepannou.kiosk.pipeline.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import io.tchepannou.kiosk.core.nlp.filter.LowercaseTextFilter;
import io.tchepannou.kiosk.core.nlp.filter.TextFilterSet;
import io.tchepannou.kiosk.core.nlp.filter.UnaccentTextFilter;
import io.tchepannou.kiosk.pipeline.service.HttpService;
import io.tchepannou.kiosk.pipeline.service.TagService;
import io.tchepannou.kiosk.pipeline.service.UrlService;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.sql.DataSource;
import java.time.Clock;
import java.util.Arrays;
import java.util.TimeZone;

@Configuration
public class AppConfiguration implements AsyncConfigurer {
    @Value("${kiosk.executor.poolSize}")
    private int poolSize;

    @Override
    @Bean(destroyMethod = "shutdown")
    public ThreadPoolTaskExecutor getAsyncExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);
        executor.setThreadGroupName("KioskExecutor");
        executor.setThreadNamePrefix("KioskExecutor");
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }

    //-- Beans
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
    ObjectMapper objectMapper() {
        return jackson2ObjectMapperBuilder().build();
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
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    RequestConfig requestConfig() {
        return RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
    }

    @Bean(destroyMethod = "close")
    CloseableHttpClient closeableHttpClient() throws Exception {
        final SSLContext sslContext = new SSLContextBuilder()
                .loadTrustMaterial(null, (certificate, authType) -> true).build();

        return HttpClients.custom()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .setDefaultRequestConfig(requestConfig())
                .build();
    }

    //-- Services
    @Bean
    public HttpService httpService() {
        return new HttpService();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public TagService tagService(){
        return new TagService(new TextFilterSet(Arrays.asList(
                new UnaccentTextFilter(),
                new LowercaseTextFilter()
        )));
    }

    @Bean
    @ConfigurationProperties("kiosk.service.UrlService")
    UrlService urlService() {
        return new UrlService();
    }
}
