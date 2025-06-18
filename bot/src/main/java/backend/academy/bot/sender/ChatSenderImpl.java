package backend.academy.bot.sender;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatSenderImpl implements ChatSender {
    private final TelegramBot bot;

    @Override
    public void sendMessage(SendMessage message) {
        bot.execute(message);
    }
}
