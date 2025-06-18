package backend.academy.scrapper.controller;

import backend.academy.scrapper.dto.request.AddNotificationTimeRequest;
import backend.academy.scrapper.metric.CountUserMessage;
import backend.academy.scrapper.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tg-chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @CountUserMessage
    @PostMapping("/{id}")
    public void registerChat(@PathVariable("id") long telegramChatId) {
        chatService.registerChat(telegramChatId);
    }

    @CountUserMessage
    @DeleteMapping("/{id}")
    public void deleteChat(@PathVariable("id") long telegramChatId) {
        chatService.deleteChat(telegramChatId);
    }

    @CountUserMessage
    @PostMapping("/{id}/notification")
    public void addNotificationType(
            @PathVariable("id") long telegramChatId,
            @RequestBody AddNotificationTimeRequest addNotificationTimeRequest) {
        chatService.addNotificationType(telegramChatId, addNotificationTimeRequest);
    }
}
