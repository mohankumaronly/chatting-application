package com.rockranger.chat.realtime.domain;

public enum OutboxStatus {
    PENDING,
    PUBLISHED,
    FAILED,
    DEAD_LETTER
}
