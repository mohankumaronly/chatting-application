package com.rockranger.chat.notification.domain;

public enum OutboxStatus {
    PENDING,
    SENT,
    FAILED,
    DEAD_LETTER
}
