package com.rockranger.chat.attachment.infrastructure;

import java.time.Duration;
import java.util.UUID;

public interface ObjectStorageService {

    UploadIntent createUploadIntent(String filename, String mimeType, long sizeBytes, UUID ownerId);

    AttachmentMetadata verifyUpload(String storageKey, UUID ownerId);

    String createSignedDownloadUrl(String storageKey, Duration ttl);

    void deleteAsset(String storageKey);
}
