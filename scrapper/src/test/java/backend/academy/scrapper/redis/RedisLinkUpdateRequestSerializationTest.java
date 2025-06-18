package backend.academy.scrapper.redis;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.IntegrationEnvironment;
import backend.academy.scrapper.config.RedisConfig;
import backend.academy.scrapper.dto.request.LinkUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

@DataRedisTest
@Import(value = {RedisConfig.class})
public class RedisLinkUpdateRequestSerializationTest extends IntegrationEnvironment {
    @Autowired
    private RedisTemplate<String, LinkUpdateRequest> redisTemplate;

    @Test
    @DisplayName("Получение информации по обновлению из кеша и дальнейшая её сериализация")
    public void redisTemplateShouldSerializeAndDeserializeLinkUpdateRequest() {
        // GIVEN
        long telegramChatId = 12345L;
        String url = "https://github.com/user/repo";
        String key = "digest:" + telegramChatId + ":" + url;
        LinkUpdateRequest linkUpdate = new LinkUpdateRequest(telegramChatId, url, "New Issue!");

        // WHEN
        redisTemplate.opsForValue().set(key, linkUpdate);
        LinkUpdateRequest linkUpdateFromCache = redisTemplate.opsForValue().get(key);

        // THEN
        assertThat(linkUpdateFromCache).isNotNull();
        assertThat(linkUpdateFromCache.telegramChatId()).isEqualTo(linkUpdate.telegramChatId());
        assertThat(linkUpdateFromCache.url()).isEqualTo(linkUpdate.url());
        assertThat(linkUpdateFromCache.description()).isEqualTo(linkUpdate.description());
    }
}
