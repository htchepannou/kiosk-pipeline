package io.tchepannou.kiosk.pipeline.consumer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import io.tchepannou.kiosk.pipeline.aws.sqs.SqsSnsConsumer;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.domain.Video;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import io.tchepannou.kiosk.pipeline.persistence.repository.VideoRepository;
import io.tchepannou.kiosk.pipeline.service.video.VideoExtractor;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;

@ConfigurationProperties("kiosk.pipeline.VideoExtractorConsumer")
@Transactional
public class VideoExtractorConsumer extends SqsSnsConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoExtractorConsumer.class);

    @Autowired
    AmazonS3 s3;

    @Autowired
    LinkRepository linkRepository;

    @Autowired
    VideoRepository videoRepository;

    @Autowired
    VideoExtractor videoExtractor;

    private String inputQueue;
    private String s3Bucket;

    //-- SqsConsumer
    @Override
    protected void consumeMessage(final String body) throws IOException {
        final long id = Long.parseLong(body.toString());
        final Link link = linkRepository.findOne(id);
        consume(link);
    }

    private void consume(final Link link) throws IOException {
        LOGGER.info("Extracting video from s3://{}/{}", s3Bucket, link.getS3Key());

        try (final S3Object s3Object = s3.getObject(s3Bucket, link.getS3Key())) {
            final String html = IOUtils.toString(s3Object.getObjectContent());
            final List<String> urls = videoExtractor.extract(html);
            if (!urls.isEmpty()) {
                for (final String url : urls) {
                    if (alreadyDownloaded(link, url)){
                        LOGGER.info("{} already downloaded. Ignoring it", url);
                        continue;
                    }

                    final Video video = toVideo(url, link);
                    videoRepository.save(video);
                }
            }
        }
    }

    private Video toVideo (final String url, final Link link){
        final Video video = new Video();
        video.setEmbedUrl(url);
        video.setLink(link);
        return video;
    }

    private boolean alreadyDownloaded(final Link link, final String url){
        List<Video> videos = videoRepository.findByLinkAndEmbedUrl(link, url);
        return videos != null && !videos.isEmpty();
    }


    //-- Getter/Setter
    public String getInputQueue() {
        return inputQueue;
    }

    public void setInputQueue(final String inputQueue) {
        this.inputQueue = inputQueue;
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public void setS3Bucket(final String s3Bucket) {
        this.s3Bucket = s3Bucket;
    }
}
