package backend.academy.scrapper.service.impl.sender;

import backend.academy.scrapper.dto.request.LinkUpdateRequest;
import java.util.List;
import java.util.Set;

public interface DigestStorage {
    void appendToDigest(Long chatId, LinkUpdateRequest update);

    List<LinkUpdateRequest> getDigestAndClear(Long chatId);

    Set<Long> getAllChatIdsWithDigests();
}
