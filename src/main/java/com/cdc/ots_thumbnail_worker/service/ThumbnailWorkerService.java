package com.cdc.ots_thumbnail_worker.service;

import com.cdc.ots_thumbnail_worker.entity.Image;
import com.cdc.ots_thumbnail_worker.exception.ImageNotFoundException;
import com.cdc.ots_thumbnail_worker.repository.ImageRepository;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import net.coobird.thumbnailator.Thumbnails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.channels.Channels;

@Service
public class ThumbnailWorkerService {

    private static final Logger logger = LoggerFactory.getLogger(ThumbnailWorkerService.class);

    private final ImageRepository imageRepository;
    private final Storage storage;

    @Value("${gcs.bucket.raw-images}")
    private String rawImagesBucket;

    @Value("${gcs.bucket.thumbnails}")
    private String thumbnailsBucket;

    public ThumbnailWorkerService(ImageRepository imageRepository, Storage storage) {
        this.imageRepository = imageRepository;
        this.storage = storage;
    }

    public void generateThumbnail(String gcsPath) {
        logger.info("Received job to generate thumbnail for file: {}", gcsPath);

        Image image = imageRepository.findByGcsPath(gcsPath)
                .orElseThrow(() -> new ImageNotFoundException("Image metadata not found for GCS path: " + gcsPath));

        try {
            // 1. Update status to PROCESSING
            image.setThumbnailStatus(Image.ThumbnailStatus.PROCESSING);
            imageRepository.save(image);

            // 2. Download original image from GCS
            BlobId sourceBlobId = BlobId.of(rawImagesBucket, gcsPath);
            Blob sourceBlob = storage.get(sourceBlobId);
            if (sourceBlob == null) {
                throw new RuntimeException("Original image not found in GCS: " + gcsPath);
            }

            // 3. Compress and resize the image using streaming to conserve memory
            byte[] thumbnailBytes;
            try (InputStream inputStream = Channels.newInputStream(sourceBlob.reader());
                 ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                Thumbnails.of(inputStream).size(200, 200).outputFormat("jpg").toOutputStream(os);
                thumbnailBytes = os.toByteArray();
            }

            // 4. Upload thumbnail to the thumbnails bucket
            String thumbnailGcsPath = "thumb-" + image.getGcsPath();
            BlobId thumbnailBlobId = BlobId.of(thumbnailsBucket, thumbnailGcsPath);
            BlobInfo thumbnailBlobInfo = BlobInfo.newBuilder(thumbnailBlobId).setContentType("image/jpeg").build();
            Blob uploadedThumbnail = storage.create(thumbnailBlobInfo, thumbnailBytes);

            // 5. Update metadata in Cloud SQL with final status and URL
            image.setThumbnailStatus(Image.ThumbnailStatus.DONE);
            image.setThumbnailUrl(uploadedThumbnail.getMediaLink()); // Public URL
            imageRepository.save(image);

            logger.info("Successfully generated thumbnail for image ID: {}", image.getId());

        } catch (Exception e) {
            logger.error("Failed to generate thumbnail for image ID: {}", image.getId(), e);
            // Update status to ERROR on failure
            image.setThumbnailStatus(Image.ThumbnailStatus.ERROR);
            imageRepository.save(image);
        }
    }
}