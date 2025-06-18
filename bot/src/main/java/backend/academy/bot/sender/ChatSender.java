package backend.academy.bot.sender;

import com.pengrad.telegrambot.request.SendMessage;

public interface ChatSender {
    void sendMessage(SendMessage message);
}
