package io.reactive.learning.infra;
import java.util.concurrent.TimeUnit;

import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import reactor.netty.http.client.HttpClient;

public class WebClientConfigurer {

    public static ExchangeStrategies objectMapperCodec(final ObjectMapper objectMapper) {
        return ExchangeStrategies.builder().codecs(clientCodecConfigurer -> {
            clientCodecConfigurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
        }).build();
    }

    public static ClientHttpConnector reactorClientHttpConnector(final Integer readTimeOut, final Integer connectTimeOut) {
        final HttpClient httpClient = HttpClient
            .create()
            .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(readTimeOut, TimeUnit.MILLISECONDS)))
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeOut);

        return new ReactorClientHttpConnector(httpClient);
    }
}