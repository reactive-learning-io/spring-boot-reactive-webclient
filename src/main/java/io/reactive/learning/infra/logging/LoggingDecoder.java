package io.reactive.learning.infra.logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class LoggingDecoder extends Jackson2JsonDecoder {

    private final Consumer<byte[]> consumer;

    public LoggingDecoder(final Consumer<byte[]> consumer) {
        this.consumer = consumer;
    }

    @Override
    public Mono<Object> decodeToMono(final Publisher<DataBuffer> input, final ResolvableType elementType, @Nullable final MimeType mimeType,
            @Nullable final Map<String, Object> hints) {
        // Buffer for bytes from each published DataBuffer
        final ByteArrayOutputStream payload = new ByteArrayOutputStream();

        // Augment the Flux, and intercept each group of bytes buffered
        final Flux<DataBuffer> interceptor = Flux.from(input).doOnNext(buffer -> bufferBytes(payload, buffer)).doOnComplete(() -> consumer.accept(payload.toByteArray()));

        // Return the original method, giving our augmented Publisher
        return super.decodeToMono(interceptor, elementType, mimeType, hints);
    }

    private void bufferBytes(final ByteArrayOutputStream bao, final DataBuffer buffer) {
        try {
            bao.write(LoggingUtil.extractBytesAndReset(buffer));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
} 