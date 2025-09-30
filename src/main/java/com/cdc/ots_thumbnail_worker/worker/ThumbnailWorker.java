package com.cdc.ots_thumbnail_worker.worker;

import com.cdc.ots_thumbnail_worker.entity.Image;
import com.cdc.ots_thumbnail_worker.repository.ImageRepository;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.function.Consumer;

@Configuration
public class ThumbnailWorker {

    private static final Logger logger = LoggerFactory.getLogger(ThumbnailWorker.class);

    private final ImageRepository imageRepository;
    private final Storage storage;

    @Value("${gcs.bucket.raw-images}")
    private String rawImagesBucket;

    @Value("${gcs.bucket.thumbnails}")
    private String thumbnailsBucket;

    public ThumbnailWorker(ImageRepository imageRepository, Storage storage) {
        this.imageRepository = imageRepository;
        this.storage = storage;
    }

    @Bean
    public Consumer<Message<String>> generateThumbnail() {
        return message -> {
            Long imageId = Long.parseLong(message.getPayload());
            logger.info("Received request to generate thumbnail for image ID: {}", imageId);

            Image image = imageRepository.findById(imageId)
                    .orElseThrow(() -> new RuntimeException("Image not found with ID: " + imageId));

            try {
                // 1. Update status to PROCESSING
                image.setThumbnailStatus(Image.ThumbnailStatus.PROCESSING);
                imageRepository.save(image);

                // 2. Download original image from GCS
                BlobId sourceBlobId = BlobId.of(rawImagesBucket, image.getGcsPath());
                Blob sourceBlob = storage.get(sourceBlobId);
                if (sourceBlob == null) {
                    throw new RuntimeException("Original image not found in GCS: " + image.getGcsPath());
                }
                byte[] imageBytes = sourceBlob.getContent();

                // 3. Compress and resize the image
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                Thumbnails.of(new ByteArrayInputStream(imageBytes))
                        .size(200, 200) // Target thumbnail size
                        .outputFormat("jpg")
                        .toOutputStream(os);
                byte[] thumbnailBytes = os.toByteArray();

                // 4. Upload thumbnail to the thumbnails bucket
                String thumbnailGcsPath = "thumb-" + image.getGcsPath();
                BlobId thumbnailBlobId = BlobId.of(thumbnailsBucket, thumbnailGcsPath);
                BlobInfo thumbnailBlobInfo = BlobInfo.newBuilder(thumbnailBlobId).setContentType("image/jpeg").build();
                Blob uploadedThumbnail = storage.create(thumbnailBlobInfo, thumbnailBytes);

                // 5. Update metadata in Cloud SQL with final status and URL
                image.setThumbnailStatus(Image.ThumbnailStatus.DONE);
                image.setThumbnailUrl(uploadedThumbnail.getMediaLink()); // Public URL
                imageRepository.save(image);

                logger.info("Successfully generated thumbnail for image ID: {}", imageId);

            } catch (Exception e) {
                logger.error("Failed to generate thumbnail for image ID: {}", imageId, e);
                // Update status to ERROR on failure
                image.setThumbnailStatus(Image.ThumbnailStatus.ERROR);
                imageRepository.save(image);
            }
        };
    }
}