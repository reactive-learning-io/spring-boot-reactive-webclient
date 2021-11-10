package io.reactive.learning.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class WebClientConfig {

    @Bean
    public ClientHttpConnector clientHttpConnector(@Value("${http.read.timeout.ms:3000}") final Integer readTimeOut,
            @Value("${http.connection.timeout.ms:3000}") final Integer connectTimeOut) {
        return WebClientConfigurer.reactorClientHttpConnector(readTimeOut, connectTimeOut);
    }

    @Bean
    public ExchangeStrategies exchangeStrategies(final ObjectMapper objectMapper) {
        return WebClientConfigurer.objectMapperCodec(objectMapper);
    }

    @Bean
    public WebClientFactory webClientFactory(final WebClient.Builder builder, final ClientHttpConnector clientHttpConnector, final ExchangeStrategies exchangeStrategies) {
        builder.clientConnector(clientHttpConnector).exchangeStrategies(exchangeStrategies);
        return new WebClientFactory(builder);
    }
}