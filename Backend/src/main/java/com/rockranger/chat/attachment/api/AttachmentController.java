package com.rockranger.chat.attachment.api;

import com.rockranger.chat.attachment.application.AttachmentService;
import com.rockranger.chat.attachment.dto.AttachmentResponse;
import com.rockranger.chat.attachment.dto.UploadIntentRequest;
import com.rockranger.chat.attachment.dto.UploadIntentResponse;
import com.rockranger.chat.common.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping("/upload-intents")
    public ResponseEntity<UploadIntentResponse> createUploadIntent(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody UploadIntentRequest request
    ) {
        UploadIntentResponse response = attachmentService.createUploadIntent(currentUser.getUserId(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{attachmentId}/complete")
    public ResponseEntity<AttachmentResponse> completeUpload(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID attachmentId
    ) {
        AttachmentResponse response = attachmentService.completeUpload(currentUser.getUserId(), attachmentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{attachmentId}")
    public ResponseEntity<AttachmentResponse> getAttachment(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID attachmentId
    ) {
        AttachmentResponse response = attachmentService.getAttachment(currentUser.getUserId(), attachmentId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Map<String, String>> deleteAttachment(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID attachmentId
    ) {
        attachmentService.deleteAttachment(currentUser.getUserId(), attachmentId);
        return ResponseEntity.ok(Map.of("message", "Attachment deleted successfully"));
    }
}
