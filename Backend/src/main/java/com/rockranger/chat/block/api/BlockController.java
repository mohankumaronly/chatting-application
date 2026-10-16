package com.rockranger.chat.block.api;

import com.rockranger.chat.block.application.BlockService;
import com.rockranger.chat.block.dto.BlockResponse;
import com.rockranger.chat.block.dto.BlockStatusResponse;
import com.rockranger.chat.block.dto.BlockUserRequest;
import com.rockranger.chat.block.dto.PagedBlockResponse;
import com.rockranger.chat.common.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/me/blocks")
public class BlockController {

    private final BlockService blockService;

    public BlockController(BlockService blockService) {
        this.blockService = blockService;
    }

    @PostMapping
    public ResponseEntity<BlockResponse> blockUser(@AuthenticationPrincipal CurrentUser currentUser,
                                                   @Valid @RequestBody BlockUserRequest request) {
        BlockService.BlockExecutionResult result = blockService.blockUser(currentUser, request.getBlockedUserId(), request.getReason());
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.response());
    }

    @DeleteMapping("/{blockedUserId}")
    public ResponseEntity<Void> unblockUser(@AuthenticationPrincipal CurrentUser currentUser,
                                            @PathVariable("blockedUserId") UUID blockedUserId) {
        blockService.unblockUser(currentUser, blockedUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PagedBlockResponse> listBlocks(@AuthenticationPrincipal CurrentUser currentUser,
                                                         @PageableDefault(size = 20) Pageable pageable) {
        PagedBlockResponse response = blockService.listBlocks(currentUser, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{userId}")
    public ResponseEntity<BlockStatusResponse> getBlockStatus(@AuthenticationPrincipal CurrentUser currentUser,
                                                              @PathVariable("userId") UUID targetUserId) {
        BlockStatusResponse response = blockService.getBlockStatus(currentUser, targetUserId);
        return ResponseEntity.ok(response);
    }
}
