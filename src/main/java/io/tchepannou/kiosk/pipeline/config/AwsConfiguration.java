package io.tchepannou.kiosk.pipeline.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSAsyncClient;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class AwsConfiguration {
    @Autowired
    Environment env;

    //-- Beans
    @Bean
    AmazonS3 amazonS3() {
        return new AmazonS3Client(awsCredentialsProvider());
    }

    @Bean
    AmazonSQS amazonSQS() {
        return new AmazonSQSClient(awsCredentialsProvider());
    }

    @Bean
    AmazonSNS amazonSNS (){
        return new AmazonSNSAsyncClient(awsCredentialsProvider());
    }

    @Bean
    AWSCredentialsProvider awsCredentialsProvider() {
        if (env.acceptsProfiles("dev")) {
            final String home = System.getProperty("user.home");
            return new PropertiesFileCredentialsProvider(home + "/.aws/credentials");
        } else {
            return new DefaultAWSCredentialsProviderChain();
        }
    }
}
