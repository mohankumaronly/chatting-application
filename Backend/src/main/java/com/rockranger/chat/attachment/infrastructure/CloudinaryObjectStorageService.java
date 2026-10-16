package com.rockranger.chat.attachment.infrastructure;

import com.cloudinary.Cloudinary;
import com.rockranger.chat.common.config.AppProperties;
import com.rockranger.chat.common.error.ApiException;
import com.rockranger.chat.common.error.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryObjectStorageService implements ObjectStorageService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryObjectStorageService.class);

    private final Cloudinary cloudinary;
    private final String uploadFolder;

    public CloudinaryObjectStorageService(Cloudinary cloudinary, AppProperties appProperties) {
        this.cloudinary = cloudinary;
        this.uploadFolder = appProperties.getCloudinary().getUploadFolder();
    }

    @Override
    public UploadIntent createUploadIntent(String filename, String mimeType, long sizeBytes, UUID ownerId) {
        long timestamp = Instant.now().getEpochSecond();
        String folder = uploadFolder + "/" + ownerId;
        String publicId = UUID.randomUUID().toString();

        Map<String, Object> params = new HashMap<>();
        params.put("timestamp", timestamp);
        params.put("folder", folder);
        params.put("public_id", publicId);

        String signature = "";
        try {
            if (cloudinary.config.apiSecret != null && !cloudinary.config.apiSecret.isBlank()) {
                signature = cloudinary.apiSignRequest(params, cloudinary.config.apiSecret);
            }
        } catch (Exception e) {
            log.error("Failed to generate Cloudinary signature", e);
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR, "Could not generate upload signature");
        }

        Map<String, Object> uploadFields = new HashMap<>();
        uploadFields.put("api_key", cloudinary.config.apiKey != null ? cloudinary.config.apiKey : "");
        uploadFields.put("timestamp", timestamp);
        uploadFields.put("folder", folder);
        uploadFields.put("public_id", publicId);
        uploadFields.put("signature", signature);

        String uploadUrl = "https://api.cloudinary.com/v1_1/" + cloudinary.config.cloudName + "/auto/upload";
        Instant expiresAt = Instant.now().plusSeconds(600); // 10 minutes

        return new UploadIntent(uploadUrl, uploadFields, expiresAt);
    }

    @Override
    public AttachmentMetadata verifyUpload(String storageKey, UUID ownerId) {
        try {
            Map<?, ?> result = cloudinary.api().resource(storageKey, Collections.emptyMap());
            long bytes = result.get("bytes") instanceof Number n ? n.longValue() : 0L;
            String format = (String) result.get("format");
            String resourceType = (String) result.get("resource_type");
            Integer width = result.get("width") instanceof Number n ? n.intValue() : null;
            Integer height = result.get("height") instanceof Number n ? n.intValue() : null;
            Integer durationMs = result.get("duration") instanceof Number n ? (int) (n.doubleValue() * 1000) : null;

            return new AttachmentMetadata(storageKey, format, bytes, width, height, durationMs, resourceType);
        } catch (Exception e) {
            log.warn("Cloudinary verification failed for storageKey: {}", storageKey, e);
            throw new ApiException(ErrorCode.ATTACHMENT_VERIFICATION_FAILED, "Failed to verify asset with storage provider");
        }
    }

    @Override
    public String createSignedDownloadUrl(String storageKey, Duration ttl) {
        try {
            return cloudinary.url()
                    .resourceType("auto")
                    .type("upload")
                    .signed(true)
                    .generate(storageKey);
        } catch (Exception e) {
            log.warn("Failed to generate signed url, falling back to secure URL for {}", storageKey);
            return cloudinary.url().secure(true).generate(storageKey);
        }
    }

    @Override
    public void deleteAsset(String storageKey) {
        try {
            cloudinary.uploader().destroy(storageKey, Collections.emptyMap());
        } catch (Exception e) {
            log.warn("Cloudinary delete failed for {}", storageKey, e);
        }
    }
}
