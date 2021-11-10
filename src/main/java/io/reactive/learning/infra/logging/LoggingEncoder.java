package io.reactive.learning.infra.logging;

import java.util.Map;
import java.util.function.Consumer;

import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;

public class LoggingEncoder extends Jackson2JsonEncoder {

    private final Consumer<byte[]> consumer;

    public LoggingEncoder(final Consumer<byte[]> consumer) {
        this.consumer = consumer;
    }

    @Override
    public DataBuffer encodeValue(final Object value, final DataBufferFactory bufferFactory, final ResolvableType valueType, @Nullable final MimeType mimeType,
            @Nullable final Map<String, Object> hints) {

        // Encode/Serialize data to JSON
        final DataBuffer data = super.encodeValue(value, bufferFactory, valueType, mimeType, hints);

        // Interception: Generate Signature and inject header into request
        consumer.accept(LoggingUtil.extractBytesAndReset(data));

        return data;
    }

} 
