package io.reactive.learning.filter;

import static io.reactive.learning.infra.logging.LoggingUtil.getHeaderString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.function.Consumer;

import org.apache.commons.io.IOUtils;
import org.reactivestreams.Publisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

/**
 * Filter to log all the incoming requests (before it reaches to
 * {@link RestController} and outgoing responses.
 * 
 * @author dev@reactivelearning.io
 *
 */
@Component
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
public class RequestResponseLoggingFilter implements WebFilter {

	@Override
	public Mono<Void> filter(final ServerWebExchange exchange, final WebFilterChain chain) {
		final ServerHttpRequest request = exchange.getRequest();
		final ServerHttpResponse response = exchange.getResponse();

		if (log.isTraceEnabled()) {
			return chain.filter(
					exchange.mutate().request(logRequestBody(request)).response(logResponseBody(response)).build())
					.doFirst(logRequest(request)).doFinally(logResponseStatus(response));
		} else {
			return chain.filter(exchange).doFirst(logRequest(request)).doFinally(logResponseStatus(response));
		}
	}

	public Runnable logRequest(final ServerHttpRequest request) {
		return () -> log.debug("Incoming request: {}:{}, queryParams: [{}]", request.getMethod(),
				request.getURI().getPath(), request.getQueryParams());
	}

	private Consumer<SignalType> logResponseStatus(final ServerHttpResponse response) {
		return signal -> log.debug("Outgoing response status: [{}]", response.getStatusCode());
	}

	private ServerHttpRequestDecorator logRequestBody(final ServerHttpRequest request) {
		return new ServerHttpRequestDecorator(request) {
			String requestBody = "";

			@Override
			public Flux<DataBuffer> getBody() {
				return super.getBody().doOnNext(dataBuffer -> {
					try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
						Channels.newChannel(byteArrayOutputStream).write(dataBuffer.asByteBuffer().asReadOnlyBuffer());
						requestBody = IOUtils.toString(byteArrayOutputStream.toByteArray(), "UTF-8");
						log.trace("Incoming request body: {}, headers: [{}]", requestBody,
								getHeaderString(request.getHeaders()));
					} catch (IOException ex) {
						log.trace("Incoming request body: {}, Exception: {}", requestBody, ex);
					}
				});
			}
		};
	}

	private ServerHttpResponseDecorator logResponseBody(final ServerHttpResponse response) {
		return new ServerHttpResponseDecorator(response) {
			String responseBody = "";

			@Override
			public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
				Mono<DataBuffer> buffer = Mono.from(body);
				return super.writeWith(buffer.doOnNext(dataBuffer -> {
					try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
						Channels.newChannel(byteArrayOutputStream).write(dataBuffer.asByteBuffer().asReadOnlyBuffer());
						responseBody = IOUtils.toString(byteArrayOutputStream.toByteArray(), "UTF-8");
						log.trace("Outgoing response body: {}, headers: [{}]", responseBody,
								getHeaderString(response.getHeaders()));
					} catch (Exception ex) {
						log.trace("Outgoing response body: {}, Exception: {}", responseBody, ex);
					}
				}));
			}
		};
	}

}