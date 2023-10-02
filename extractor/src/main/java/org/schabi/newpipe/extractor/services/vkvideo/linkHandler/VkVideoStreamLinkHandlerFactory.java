package org.schabi.newpipe.extractor.services.vkvideo.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VkVideoStreamLinkHandlerFactory extends LinkHandlerFactory {
    private static final Pattern VK_VIDEO_ID_REGEX_PATTERN = Pattern.compile("video(-\\d+_\\d+)");

    @Override
    public String getId(final String urlString)
            throws ParsingException, UnsupportedOperationException {
        final Matcher m = VK_VIDEO_ID_REGEX_PATTERN.matcher(urlString);
        if (!m.find()) {
            throw new IllegalArgumentException("The given string is not a VkVideo video ID");
        }
        return m.group(1);
    }

    @Override
    public String getUrl(final String id) throws ParsingException, UnsupportedOperationException {
        return "https://vk.com/video" + id;
    }

    @Override
    public boolean onAcceptUrl(final String url) throws ParsingException {
        if (!url.contains("vk.com")) {
            return false;
        }
        try {
            getId(url);
        } catch (final IllegalArgumentException e) {
            return false;
        }
        return true;
    }
}
