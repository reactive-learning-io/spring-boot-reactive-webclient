package io.reactive.learning.infra.logging;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;

/**
 * 
 * Logging util class to support common methods.
 *
 */
public final class LoggingUtil {

    private LoggingUtil() {
        throw new UnsupportedOperationException("Class can't be instantiated.");
    }

    public static String getHeaderString(HttpHeaders headers) {
        if (CollectionUtils.isEmpty(headers)) {
            return "";
        }
        return headers.entrySet().stream().map(LoggingUtil::maskSensitiveHeaders).collect(Collectors.joining(","));
    }

    private static String maskSensitiveHeaders(Map.Entry<String, List<String>> entry) {
        String key = entry.getKey();
        List<String> value = entry.getValue();
        if (HttpHeaders.AUTHORIZATION.equalsIgnoreCase(key)) {
            value = Collections.singletonList("***");
        }
        return new StringBuilder(key).append(": '").append(value).append("'").toString();
    }

    public static byte[] extractBytesAndReset(final DataBuffer data) {
        final byte[] bytes = new byte[data.readableByteCount()];
        data.read(bytes);
        data.readPosition(0);
        return bytes;
    }

}
