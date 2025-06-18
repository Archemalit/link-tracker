package backend.academy.bot.service.link;

import backend.academy.bot.dto.LinkUpdate;
import org.springframework.http.ResponseEntity;

public interface LinkUpdateService {
    ResponseEntity<Void> updateLink(LinkUpdate linkUpdate);
}
