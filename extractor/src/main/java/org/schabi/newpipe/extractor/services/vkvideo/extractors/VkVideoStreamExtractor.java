package org.schabi.newpipe.extractor.services.vkvideo.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Request;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.vkvideo.FormDataBuilder;
import org.schabi.newpipe.extractor.services.vkvideo.VkVideoAuth;
import org.schabi.newpipe.extractor.services.vkvideo.VkVideoParsingHelper;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.DeliveryMethod;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VkVideoStreamExtractor extends StreamExtractor {
    private static final String METHOD_ENDPOINT = VkVideoParsingHelper.API_BASE + "method/";
    private JsonObject json;
    public VkVideoStreamExtractor(final StreamingService service, final LinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @SuppressWarnings("checkstyle:LineLength")
    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) throws IOException, ExtractionException {
        final String[] ids = getId().split("_");
        final String ownerId = ids[0];
        final String videoId = ids[1];
        final String token = VkVideoAuth.getInstance().getToken(downloader);
        final FormDataBuilder formBuilder = VkVideoParsingHelper.getMobileFormDataBuilder(token)
                .set("owner_id", ownerId)
                .set("video_id", videoId)
                .set("extended", "1")
                .set("fields", "first_name,last_name,photo_50,photo_100,photo_200,name,friend_status,member_status,verified,trending,image_status,is_nft,is_nft_photo,owner_state,deactivated,members_count,followers_count,video_notifications_status")
                .set("with_comments", "0")
                .set("func_v", "8");
        final Request request = VkVideoParsingHelper.getMobileRequestBuilder()
                .post(METHOD_ENDPOINT + "execute.getVideoById", formBuilder.buildBytes())
                .build();
        final Response response = downloader.execute(request);
        try {
            json = JsonParser.object().from(response.responseBody()).getObject("response");
        } catch (final JsonParserException e) {
            throw new ParsingException("Could parse json data for stream", e);
        }
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return JsonUtils.getString(json, "video.title");
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() throws ParsingException {
        final List<Image> result = new LinkedList<>();
        final JsonArray imageArray = JsonUtils.getArray(json, "video.image");
        for (int i = 0; i < imageArray.size(); i++) {
            final JsonObject obj = imageArray.getObject(i);
            final int height = obj.getInt("height");
            result.add(new Image(
                obj.getString("url"),
                height,
                obj.getInt("width"),
                Image.ResolutionLevel.fromHeight(height)
            ));
        }
        return result;
    }

    @Nonnull
    @Override
    public String getUploaderUrl() throws ParsingException {
        final String username = JsonUtils.getString(json, "owner.screen_name");
        return "https://vk.com/video/@" + username;
    }

    @Nonnull
    @Override
    public String getUploaderName() throws ParsingException {
        return JsonUtils.getString(json, "owner.name");
    }

    @Override
    public List<AudioStream> getAudioStreams() throws IOException, ExtractionException {
        return List.of();
    }

    @Override
    public List<VideoStream> getVideoStreams() throws IOException, ExtractionException {
        final List<VideoStream> result = new LinkedList<>();
        final JsonObject info = JsonUtils.getObject(json, "video.files");
        for (final String key : info.keySet()) {
            final String url = info.getString(key);
            final DeliveryMethod deliveryMethod;
            final MediaFormat mediaFormat;
            String resolution = VideoStream.RESOLUTION_UNKNOWN;
            if (key.startsWith("mp4")) {
                deliveryMethod = DeliveryMethod.PROGRESSIVE_HTTP;
                mediaFormat = MediaFormat.MPEG_4;
                resolution = key.split("_")[1];
            } else {
                switch (key) {
                    case "hls":
                        deliveryMethod = DeliveryMethod.HLS;
                        mediaFormat = MediaFormat.MPEG_4;
                        break;
                    case "dash_sep":
                        deliveryMethod = DeliveryMethod.DASH;
                        mediaFormat = MediaFormat.MPEG_4;
                        break;
                    case "dash_webm":
                        deliveryMethod = DeliveryMethod.DASH;
                        mediaFormat = MediaFormat.WEBM;
                        break;
                    default:
                        continue;
                }
            }


            final VideoStream stream = new VideoStream.Builder()
                    .setContent(url, true)
                    .setIsVideoOnly(false)
                    .setId(key)
                    .setDeliveryMethod(deliveryMethod)
                    .setMediaFormat(mediaFormat)
                    .setResolution(resolution)
                    .build();
            result.add(stream);
        }
        return result;
    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() throws IOException, ExtractionException {
        return List.of();
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        return StreamType.VIDEO_STREAM;
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        // Create offsetDateTime from timestamp
        final long timestamp = JsonUtils.getNumber(json, "video.date").longValue();
        final Instant instant = Instant.ofEpochSecond(timestamp);
        return new DateWrapper(OffsetDateTime.ofInstant(instant, ZoneId.systemDefault()));
    }

    @Nonnull
    @Override
    public Description getDescription() throws ParsingException {
        final String text = JsonUtils.getString(json, "video.description");
        return new Description(text, Description.PLAIN_TEXT);
    }

    @Override
    public long getLength() throws ParsingException {
        return JsonUtils.getNumber(json, "video.duration").longValue();
    }

    @Override
    public long getViewCount() throws ParsingException {
        return JsonUtils.getNumber(json, "video.views").longValue();
    }

    @Override
    public long getLikeCount() throws ParsingException {
        return JsonUtils.getNumber(json, "video.likes.count").longValue();
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return JsonUtils.getNumber(json, "owner.verified").intValue() == 1;
    }

    @Override
    public long getUploaderSubscriberCount() throws ParsingException {
        return JsonUtils.getNumber(json, "owner.members_count").longValue();
    }

    @Nonnull
    @Override
    public List<Image> getUploaderAvatars() throws ParsingException {
        final List<Image> result = new LinkedList<>();
        final JsonObject owner = JsonUtils.getObject(json, "owner");
        for (final String key : owner.keySet()) {
            if (!key.startsWith("photo")) {
                continue;
            }
            final int size = Integer.parseInt(key.split("_")[1]);
            final String url = owner.getString(key);
            result.add(new Image(
                url, size, size, Image.ResolutionLevel.fromHeight(size)
            ));
        }
        return result;
    }

    @Override
    public long getTimeStamp() throws ParsingException {
        final URL url;
        try {
            url = Utils.stringToURL(getOriginalUrl());
        } catch (final MalformedURLException e) {
            throw new ParsingException("Could not parse url", e);
        }
        final String t = Utils.getQueryValue(url, "t");
        if (t == null) {
            return 0;
        }
        return parsePeriod(t);
    }

    // Thanks! https://stackoverflow.com/a/56395975
    private static final Pattern PERIOD_PATTERN = Pattern.compile("([0-9]+)([smhd])");
    public static Long parsePeriod(@Nonnull final String period) {
        final Matcher matcher = PERIOD_PATTERN.matcher(period.toLowerCase(Locale.ENGLISH));
        Instant instant = Instant.EPOCH;
        while (matcher.find()) {
            final int num = Integer.parseInt(matcher.group(1));
            final String typ = matcher.group(2);
            switch (typ) {
                case "s":
                    instant = instant.plus(Duration.ofSeconds(num));
                    break;
                case "m":
                    instant = instant.plus(Duration.ofMinutes(num));
                    break;
                case "h":
                    instant = instant.plus(Duration.ofHours(num));
                    break;
                case "d":
                    instant = instant.plus(Duration.ofDays(num));
                    break;
            }
        }
        return instant.getEpochSecond();
    }
}
