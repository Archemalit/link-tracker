package backend.academy.scrapper.repository;

import backend.academy.scrapper.repository.jpa.entity.ParseRequest;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

public interface ParseRequestRepository {
    ParseRequest save(ParseRequest parseRequest);

    ParseRequest findFirstByChatIdAndLinkId(Long chatId, Long linkId);

    ParseRequest findByChatIdAndLinkUrl(Long chatId, String linkUrl);

    List<ParseRequest> findAllByChatTelegramChatId(Long telegramChatId);

    List<ParseRequest> findAll();

    int countByLinkUrl(String linkUrl);

    @Transactional
    int deleteByChatIdAndLinkUrl(Long chatId, String linkUrl);

    void deleteByChatIdAndLinkId(Long chatId, Long linkId);
}
