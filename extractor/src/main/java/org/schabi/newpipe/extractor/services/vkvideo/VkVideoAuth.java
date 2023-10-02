package org.schabi.newpipe.extractor.services.vkvideo;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Request;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.io.IOException;

public final class VkVideoAuth {
    private VkVideoAuth() { }

    private String token;
    private Long expiresAt = 0L;
    private static VkVideoAuth instance;

    public static VkVideoAuth getInstance() {
        synchronized (VkVideoAuth.class) {
            if (instance == null) {
                instance = new VkVideoAuth();
            }
        }
        return instance;
    }

    public String getToken(final Downloader downloader)
            throws IOException, ExtractionException {
        if (token == null || expiresAt < System.currentTimeMillis() / 1000) {
            updateToken(downloader);
        }
        return token;
    }


    private void updateToken(final Downloader downloader)
            throws IOException, ExtractionException {
        final FormDataBuilder formDataBuilder = VkVideoParsingHelper.getMobileFormDataBuilder(null);
        final Request request = VkVideoParsingHelper.getMobileRequestBuilder()
                        .post(
                            VkVideoParsingHelper.API_BASE + "oauth/get_anonym_token",
                            formDataBuilder.buildBytes()
                        )
                        .build();
        final Response response = downloader.execute(request);
        final JsonObject data;
        try {
            data = JsonParser.object().from(response.responseBody());
        } catch (final JsonParserException e) {
            throw new ParsingException("Could not get auth token", e);
        }
        expiresAt = data.getLong("expired_at");
        token = data.getString("token");
    }
}
