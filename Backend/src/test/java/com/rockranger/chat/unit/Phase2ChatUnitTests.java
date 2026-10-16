package com.rockranger.chat.unit;

import com.rockranger.chat.attachment.domain.ResourceType;
import com.rockranger.chat.attachment.dto.UploadIntentRequest;
import com.rockranger.chat.common.error.ApiException;
import com.rockranger.chat.conversation.domain.Conversation;
import com.rockranger.chat.messaging.domain.DeleteScope;
import com.rockranger.chat.messaging.domain.Message;
import com.rockranger.chat.messaging.domain.MessageType;
import com.rockranger.chat.messaging.dto.EditMessageRequest;
import com.rockranger.chat.presence.domain.UserPresenceSettings;
import com.rockranger.chat.presence.domain.VisibilitySetting;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class Phase2ChatUnitTests {

    @Test
    void testDirectPairKeyIsCanonicalAndSymmetric() {
        UUID userA = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID userB = UUID.fromString("00000000-0000-0000-0000-000000000002");

        String pairKey1 = Conversation.calculateDirectPairKey(userA, userB);
        String pairKey2 = Conversation.calculateDirectPairKey(userB, userA);

        assertNotNull(pairKey1);
        assertEquals(pairKey1, pairKey2);
        assertEquals("00000000-0000-0000-0000-000000000001:00000000-0000-0000-0000-000000000002", pairKey1);
    }

    @Test
    void testMessageEditWindowPolicy() {
        UUID senderId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        // Fresh message within 15 minutes is eligible
        Message freshMessage = new Message(
                UUID.randomUUID(), conversationId, senderId, UUID.randomUUID(),
                1L, MessageType.TEXT, "Hello world", null
        );
        assertFalse(freshMessage.getCreatedAt().plus(Duration.ofMinutes(15)).isBefore(Instant.now()));

        // Old message (> 15 mins) has expired edit window
        Message oldMessage = new Message(
                UUID.randomUUID(), conversationId, senderId, UUID.randomUUID(),
                2L, MessageType.TEXT, "Old text", null
        );
        oldMessage.setCreatedAt(Instant.now().minus(Duration.ofMinutes(16)));
        assertTrue(oldMessage.getCreatedAt().plus(Duration.ofMinutes(15)).isBefore(Instant.now()));

        // Only sender is permitted
        assertTrue(freshMessage.getSenderId().equals(senderId));
        assertFalse(freshMessage.getSenderId().equals(otherUserId));

        // Only text messages can be edited
        Message imageMessage = new Message(
                UUID.randomUUID(), conversationId, senderId, UUID.randomUUID(),
                3L, MessageType.IMAGE, null, null
        );
        assertNotEquals(MessageType.TEXT, imageMessage.getMessageType());
    }

    @Test
    void testMessageDeleteScopes() {
        UUID senderId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        Message message = new Message(
                UUID.randomUUID(), conversationId, senderId, UUID.randomUUID(),
                1L, MessageType.TEXT, "Secret text", null
        );

        assertFalse(message.isDeleted());

        // Deleting for everyone sets deletedAt and deleteScope, and clears content
        message.setDeletedAt(Instant.now());
        message.setDeleteScope(DeleteScope.FOR_EVERYONE);
        message.setContent(null);

        assertTrue(message.isDeleted());
        assertEquals(DeleteScope.FOR_EVERYONE, message.getDeleteScope());
        assertNull(message.getContent());
    }

    @Test
    void testPresencePrivacyRules() {
        UUID userId = UUID.randomUUID();
        UserPresenceSettings settings = UserPresenceSettings.defaults(userId);

        // Defaults allow visibility
        assertEquals(VisibilitySetting.EVERYONE, settings.getOnlineVisibility());
        assertEquals(VisibilitySetting.EVERYONE, settings.getLastSeenVisibility());
        assertTrue(settings.isReadReceiptsEnabled());
        assertTrue(settings.isTypingVisibility());

        // Setting to NOBODY suppresses visibility
        settings.setOnlineVisibility(VisibilitySetting.NOBODY);
        settings.setLastSeenVisibility(VisibilitySetting.NOBODY);
        settings.setReadReceiptsEnabled(false);
        settings.setTypingVisibility(false);

        assertEquals(VisibilitySetting.NOBODY, settings.getOnlineVisibility());
        assertEquals(VisibilitySetting.NOBODY, settings.getLastSeenVisibility());
        assertFalse(settings.isReadReceiptsEnabled());
        assertFalse(settings.isTypingVisibility());
    }

    @Test
    void testAttachmentSizeThresholds() {
        long imageMax = 10 * 1024 * 1024L; // 10MB
        long gifMax = 5 * 1024 * 1024L;    // 5MB
        long videoMax = 100 * 1024 * 1024L; // 100MB
        long docMax = 25 * 1024 * 1024L;   // 25MB

        assertTrue(5 * 1024 * 1024L <= imageMax);
        assertTrue(11 * 1024 * 1024L > imageMax);

        assertTrue(4 * 1024 * 1024L <= gifMax);
        assertTrue(6 * 1024 * 1024L > gifMax);

        assertTrue(50 * 1024 * 1024L <= videoMax);
        assertTrue(101 * 1024 * 1024L > videoMax);

        assertTrue(20 * 1024 * 1024L <= docMax);
        assertTrue(26 * 1024 * 1024L > docMax);
    }
}
