package com.rockranger.chat.reaction.dto;

public record ReactionGroupResponse(
        String emoji,
        long count,
        boolean reactedByMe
) {}
