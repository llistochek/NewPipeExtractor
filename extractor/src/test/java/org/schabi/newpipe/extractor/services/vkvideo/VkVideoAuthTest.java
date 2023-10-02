package org.schabi.newpipe.extractor.services.vkvideo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.downloader.Downloader;

public class VkVideoAuthTest {
    @Test
    public void testToken() throws Exception {
        final Downloader downloader = DownloaderTestImpl.getInstance();
        final String token = VkVideoAuth.getInstance().getToken(downloader);
        Assertions.assertTrue(token.startsWith("anonym."));
    }
}
