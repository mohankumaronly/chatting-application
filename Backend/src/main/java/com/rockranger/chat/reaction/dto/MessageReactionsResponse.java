package com.rockranger.chat.reaction.dto;

import java.util.List;
import java.util.UUID;

public record MessageReactionsResponse(
        UUID messageId,
        List<ReactionGroupResponse> reactions
) {}
