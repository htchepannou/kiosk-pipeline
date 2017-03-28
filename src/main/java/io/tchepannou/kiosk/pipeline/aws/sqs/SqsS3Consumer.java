package io.tchepannou.kiosk.pipeline.aws.sqs;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@Deprecated
public abstract class SqsS3Consumer implements SqsConsumer {
    @Autowired
    protected AmazonS3 s3;

    protected abstract void consume (S3Object s3Object) throws IOException;

    @Override
    public void consume(final String body) throws IOException {
        final S3EventNotification s3Event = S3EventNotification.parseJson(body);
        if (s3Event == null || s3Event.getRecords() == null) {
            return;
        }

        s3Event.getRecords().forEach(record -> {
            final String key = getKey(record);
            final String bucket = getBucket(record);
            try (final S3Object s3Object = s3.getObject(bucket, key)){
                consume(s3Object);
            } catch (Exception e){
                onException(record, e);
            }
        });
    }

    protected void onException (S3EventNotification.S3EventNotificationRecord record, Throwable e){
        // Nothing
    }

    protected String getBucket(final S3EventNotification.S3EventNotificationRecord record) {
        return record.getS3().getBucket().getName();
    }

    protected String getKey(final S3EventNotification.S3EventNotificationRecord record) {
        return record.getS3().getObject().getKey();
    }

}
