package io.reactive.learning.infra;

import static java.util.Objects.requireNonNull;

import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import io.reactive.learning.infra.logging.LoggingDecoder;
import io.reactive.learning.infra.logging.LoggingEncoder;
import io.reactive.learning.infra.logging.LoggingUtil;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class WebClientFactory {

    private final WebClient.Builder builder;

    public WebClientFactory(final WebClient.Builder builder) {
        this.builder = requireNonNull(builder);
    }

    public WebClient create() {
        return decoratedBulider().build();
    }

    public WebClient fromBaseUrl(final String baseUrl) {
        return decoratedBulider().baseUrl(baseUrl).build();
    }

    protected WebClient.Builder decoratedBulider() {
    	LoggingEncoder loggingEncoder = new LoggingEncoder(bytes -> {
            if (log.isTraceEnabled())
                log.trace("WebClient request body: {}", new String(bytes));
            });
    	LoggingDecoder loggingDecoder = new LoggingDecoder(bytes -> {
            if(log.isTraceEnabled())
                log.trace("WebClient response body: \n{}", new String(bytes));
        });

        return builder
                .clone()
                .filter(logRequest())
                .filter(logResponseStatus())
                .codecs(codecConfigurer -> {
                    codecConfigurer.defaultCodecs().jackson2JsonEncoder(loggingEncoder);
                    codecConfigurer.defaultCodecs().jackson2JsonDecoder(loggingDecoder);
                });
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.debug("WebClient request: {}: {}", clientRequest.method(), new String(clientRequest.url().toString()));
            if(log.isTraceEnabled()) {
                log.trace("WebClient request headers: {}", LoggingUtil.getHeaderString(clientRequest.headers()));
            }
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponseStatus() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.debug("WebClient response status: [{}: {}]", clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase());
            if(log.isTraceEnabled()) {
                log.trace("WebClient response headers: {}", clientResponse.headers().asHttpHeaders().toString());
            }
            return Mono.just(clientResponse);
        });
    }

}