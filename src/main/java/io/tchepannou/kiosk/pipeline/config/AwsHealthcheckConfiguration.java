package io.tchepannou.kiosk.pipeline.config;

import com.amazonaws.services.s3.AmazonS3;
import io.tchepannou.kiosk.pipeline.healthcheck.S3HealthCheck;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!local")
public class AwsHealthcheckConfiguration {
    @Bean
    S3HealthCheck s3HealthCheck(
            @Value("${kiosk.aws.s3.bucket}") final String bucket,
            final AmazonS3 s3
    ) {
        return new S3HealthCheck(bucket, s3);
    }
}
