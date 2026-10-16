package com.rockranger.chat.block.dto;

public class BlockStatusResponse {

    private boolean blocked;
    private boolean blockedBy;

    public BlockStatusResponse() {}

    public BlockStatusResponse(boolean blocked, boolean blockedBy) {
        this.blocked = blocked;
        this.blockedBy = blockedBy;
    }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public boolean isBlockedBy() { return blockedBy; }
    public void setBlockedBy(boolean blockedBy) { this.blockedBy = blockedBy; }
}
