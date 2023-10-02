package org.schabi.newpipe.extractor.services.vkvideo;

import org.schabi.newpipe.extractor.downloader.Request;

public final class VkVideoParsingHelper {
    private VkVideoParsingHelper() { };
    public static final String API_BASE = "https://api.vk.com/";
    // CHECKSTYLE:OFF
    private static final String MOBILE_USER_AGENT = "VKAndroidApp/1.15.1-522";
    // CHECKSTYLE:ON
    private static final String CLIENT_ID = "51452549";
    private static final String CLIENT_SECRET = "0fjfu5knRBZeiMT6OqPu";

    public static Request.Builder getMobileRequestBuilder() {
        return new Request.Builder()
                .addHeader("user-agent", MOBILE_USER_AGENT);
    }

    public static FormDataBuilder getMobileFormDataBuilder(final String accessToken) {
        final FormDataBuilder builder = new FormDataBuilder()
                .set("v", "5.219")
                .set("api_id", CLIENT_ID)
                .set("https", "1")
                .set("lang", "ru")
                .set("client_id", CLIENT_ID)
                .set("client_secret", CLIENT_SECRET);
        if (accessToken != null) {
            builder.set("access_token", accessToken);
        }
        return builder;
    }
}
