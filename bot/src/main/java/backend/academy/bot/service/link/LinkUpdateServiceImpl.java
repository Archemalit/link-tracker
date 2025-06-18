package backend.academy.bot.service.link;

import backend.academy.bot.dto.LinkUpdate;
import backend.academy.bot.sender.ChatSender;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinkUpdateServiceImpl implements LinkUpdateService {
    private final ChatSender chatSender;

    @Override
    public ResponseEntity<Void> updateLink(LinkUpdate linkUpdate) {
        String resultDescription = linkUpdate.url() + "\n" + linkUpdate.description();
        chatSender.sendMessage(new SendMessage(linkUpdate.telegramChatId(), resultDescription));

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
