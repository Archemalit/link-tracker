package backend.academy.scrapper.service.impl.sender;

import backend.academy.scrapper.dto.request.LinkUpdateRequest;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

// TODO: подумать, как тут можно обойтись без `@SuppressFBWarnings`

@Service
@RequiredArgsConstructor
public class RedisDigestStorage implements DigestStorage {

    private final RedisTemplate<String, LinkUpdateRequest> redisTemplate;
    private static final String DIGEST_PREFIX = "digest:";

    private String key(Long chatId, String url) {
        return DIGEST_PREFIX + chatId + ":" + url;
    }

    @Override
    @SuppressFBWarnings(
            value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
            justification = "Возврат null — это ожидаемое поведение, обрабатывается вызывающей стороной")
    public void appendToDigest(Long telegramChatId, LinkUpdateRequest update) {
        redisTemplate.opsForList().rightPush(key(telegramChatId, update.url()), update);
    }

    @Override
    @SuppressFBWarnings(
            value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
            justification = "Возврат null — это ожидаемое поведение, обрабатывается вызывающей стороной")
    public List<LinkUpdateRequest> getDigestAndClear(Long telegramChatId) {
        String pattern = DIGEST_PREFIX + telegramChatId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);

        List<LinkUpdateRequest> result = new ArrayList<>();
        for (String key : keys) {
            List<LinkUpdateRequest> updatesForLink = redisTemplate.opsForList().range(key, 0, -1);
            StringBuilder commonDescription = new StringBuilder();
            for (LinkUpdateRequest linkUpdateRequest : updatesForLink) {
                commonDescription.append(linkUpdateRequest.description());
            }
            result.add(new LinkUpdateRequest(
                    updatesForLink.getFirst().telegramChatId(),
                    updatesForLink.getFirst().url(),
                    commonDescription.toString()));
            redisTemplate.delete(key);
        }

        return result;
    }

    @Override
    @SuppressFBWarnings(
            value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
            justification = "Возврат null — это ожидаемое поведение, обрабатывается вызывающей стороной")
    public Set<Long> getAllChatIdsWithDigests() {
        Set<String> keys = redisTemplate.keys(DIGEST_PREFIX + "*");
        return keys.stream().map(key -> Long.valueOf(key.split(":", 3)[1])).collect(Collectors.toSet());
    }
}
