package org.schabi.newpipe.extractor.services.vkvideo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.services.DefaultStreamExtractorTest;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.util.List;

import static org.schabi.newpipe.extractor.ServiceList.VkVideo;

public class VkVideoStreamExtractorTest extends DefaultStreamExtractorTest {
    private static final String URL = "https://vk.com/video-211437014_456239399";
    private static final String TIMESTAMP = "t=1h25m30s";
    private static StreamExtractor extractor;

    @BeforeAll
    public static void setUp() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = VkVideo.getStreamExtractor(URL + "?" + TIMESTAMP);
        extractor.fetchPage();
    }

    @Override public StreamExtractor extractor() throws Exception { return extractor; }
    @Override public StreamingService expectedService() throws Exception { return VkVideo; }

    @Override public String expectedName() throws Exception { return "Истории про дизайн. Триколор"; }
    @Override public String expectedId() throws Exception { return "-211437014_456239399"; }
    @Override public String expectedUrlContains() throws Exception { return URL; }
    @Override public String expectedOriginalUrlContains() throws Exception { return URL; }
    @Override public StreamType expectedStreamType() { return StreamType.VIDEO_STREAM; }

    @Override public String expectedUploaderName() { return "Артемий Лебедев"; }
    @Override public String expectedUploaderUrl() { return "https://vk.com/video/@temalebedev"; }

    @Override public List<String> expectedDescriptionContains() {
        return List.of("Что общего у флагов и пучка травы на палке?");
    }

    @Override public long expectedLength() { return 2493; }
    @Override public long expectedViewCountAtLeast() { return 63000; }

    @Override public long expectedTimestamp() { return 60*60 + 25*60 + 30; }
    @Override public String expectedUploadDate() { return "2022-10-24 10:22:08.000"; }
    @Override public String expectedTextualUploadDate() { return null; }

    @Override public long expectedLikeCountAtLeast() { return 293; }
    @Override public long expectedDislikeCountAtLeast() { return -1; }
    @Override public long expectedUploaderSubscriberCountAtLeast() { return 578000; }

    @Override public boolean expectedUploaderVerified() { return true; }

    @Override public boolean expectedHasFrames() { return false; }
    @Override public boolean expectedHasSubtitles() { return false; }
    @Override public boolean expectedHasAudioStreams() { return false;}
    @Override public boolean expectedHasRelatedItems() { return false; }

    @Test
    @Override
    public void testVideoStreams() throws Exception {
        // TODO
        List<VideoStream> streams = extractor.getVideoStreams();
        Assertions.assertNotEquals(streams.size(), 0);
    }
}
