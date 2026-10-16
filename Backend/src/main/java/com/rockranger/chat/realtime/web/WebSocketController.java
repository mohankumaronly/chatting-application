package com.rockranger.chat.realtime.web;

import com.rockranger.chat.common.security.CurrentUser;
import com.rockranger.chat.messaging.application.MessageService;
import com.rockranger.chat.messaging.dto.SendMessageRequest;
import com.rockranger.chat.presence.application.TypingService;
import com.rockranger.chat.realtime.dto.ReadCursorCommand;
import com.rockranger.chat.realtime.dto.TypingCommand;
import com.rockranger.chat.realtime.dto.WebSocketSendMessageCommand;
import com.rockranger.chat.receipt.application.ReceiptService;
import com.rockranger.chat.receipt.dto.ReceiptCursorRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class WebSocketController {

    private static final Logger log = LoggerFactory.getLogger(WebSocketController.class);

    private final MessageService messageService;
    private final ReceiptService receiptService;
    private final TypingService typingService;

    public WebSocketController(
            MessageService messageService,
            ReceiptService receiptService,
            TypingService typingService
    ) {
        this.messageService = messageService;
        this.receiptService = receiptService;
        this.typingService = typingService;
    }

    @MessageMapping("/messages.send")
    public void handleSendMessage(WebSocketSendMessageCommand command, Principal principal) {
        CurrentUser user = extractUser(principal);
        if (user == null) return;

        SendMessageRequest req = new SendMessageRequest(
                command.clientMessageId(),
                command.type(),
                command.content(),
                command.replyToMessageId(),
                command.attachments()
        );

        messageService.sendMessage(user.getUserId(), command.conversationId(), req);
    }

    @MessageMapping("/messages.read")
    public void handleMessageRead(ReadCursorCommand command, Principal principal) {
        CurrentUser user = extractUser(principal);
        if (user == null) return;

        ReceiptCursorRequest req = new ReceiptCursorRequest(
                command.throughMessageId(),
                command.throughSequenceNumber()
        );
        receiptService.markRead(user.getUserId(), command.conversationId(), req);
    }

    @MessageMapping("/typing.start")
    public void handleTypingStart(TypingCommand command, Principal principal) {
        CurrentUser user = extractUser(principal);
        if (user == null) return;

        typingService.startTyping(user.getUserId(), command.conversationId());
    }

    @MessageMapping("/typing.stop")
    public void handleTypingStop(TypingCommand command, Principal principal) {
        CurrentUser user = extractUser(principal);
        if (user == null) return;

        typingService.stopTyping(user.getUserId(), command.conversationId());
    }

    private CurrentUser extractUser(Principal principal) {
        if (principal instanceof Authentication auth && auth.getPrincipal() instanceof CurrentUser currentUser) {
            return currentUser;
        }
        log.warn("Principal is not authenticated CurrentUser: {}", principal);
        return null;
    }
}
