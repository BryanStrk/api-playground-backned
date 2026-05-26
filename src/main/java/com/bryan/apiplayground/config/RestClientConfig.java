package com.bryan.apiplayground.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .requestFactory(factory(Duration.ofSeconds(10), Duration.ofSeconds(15)))
                .defaultHeader("User-Agent", "api-playground/1.0")
                .build();
    }

    @Bean("healthRestClient")
    public RestClient healthRestClient() {
        return RestClient.builder()
                .requestFactory(factory(Duration.ofSeconds(4), Duration.ofSeconds(4)))
                .defaultHeader("User-Agent", "api-playground-health/1.0")
                .build();
    }

    private ClientHttpRequestFactory factory(Duration connect, Duration read) {
        var f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(connect);
        f.setReadTimeout(read);
        return f;
    }
}
