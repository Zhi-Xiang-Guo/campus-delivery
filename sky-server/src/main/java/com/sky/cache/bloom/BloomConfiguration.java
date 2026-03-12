package com.sky.cache.bloom;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(BloomProperties.class)
public class BloomConfiguration {

    @Bean
    public BloomFilterStrategy bloomFilterStrategy(BloomFilterFactory factory, BloomProperties properties) {
        return factory.create(properties);
    }
}

