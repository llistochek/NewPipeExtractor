package org.schabi.newpipe.extractor.services.vkvideo;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class FormDataBuilder {
    private final Map<String, String> values = new HashMap<>();
    public FormDataBuilder set(final String key, final String value) {
        values.put(key, value);
        return this;
    }

    public String build() {
        final StringBuilder builder = new StringBuilder();
        for (final Map.Entry<String, String> entry : values.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(entry.getKey())
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return builder.toString();
    }

    public byte[] buildBytes() {
        return build().getBytes(StandardCharsets.UTF_8);
    }
}
