package backend.academy.scrapper.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.dto.request.AddNotificationTimeRequest;
import backend.academy.scrapper.exception.ChatNotFoundException;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.jpa.entity.Chat;
import backend.academy.scrapper.service.impl.ChatServiceImpl;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("jpa")
public class ChatServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @InjectMocks
    private ChatServiceImpl chatService;

    @Test
    @DisplayName("Регистрация нового чата, которого ещё нет в базе")
    public void testRegisterNewChat() {
        // GIVEN
        long telegramChatId = 12345L;
        when(chatRepository.existsChatByTelegramChatId(telegramChatId)).thenReturn(false);

        // WHEN
        chatService.registerChat(telegramChatId);

        // THEN
        verify(chatRepository, times(1)).save(argThat(chat -> chat.telegramChatId() == telegramChatId));
    }

    @Test
    @DisplayName("Регистрация уже существующего чата — не вызывает save")
    public void testRegisterExistingChat() {
        // GIVEN
        long telegramChatId = 12345L;
        when(chatRepository.existsChatByTelegramChatId(telegramChatId)).thenReturn(true);

        // WHEN
        chatService.registerChat(telegramChatId);

        // THEN
        verify(chatRepository, never()).save(any());
    }

    @Test
    @DisplayName("Удаление чата, который существует в базе")
    public void testDeleteExistingChat() {
        // GIVEN
        long telegramChatId = 12345L;
        Chat chat = Chat.builder().id(1L).telegramChatId(telegramChatId).build();
        when(chatRepository.deleteByTelegramChatId(telegramChatId)).thenReturn(1);

        // WHEN
        chatService.deleteChat(telegramChatId);

        // THEN
        verify(chatRepository, times(1)).deleteByTelegramChatId(telegramChatId);
    }

    @Test
    @DisplayName("Удаление несуществующего чата — выбрасывает исключение")
    public void testDeleteNonExistingChat() {
        // GIVEN
        long telegramChatId = 12345L;
        when(chatRepository.deleteByTelegramChatId(telegramChatId)).thenReturn(0);

        // TODO: заменить в тестах WHEN & THEN на EXPECT
        // WHEN & THEN
        assertThatThrownBy(() -> chatService.deleteChat(telegramChatId)).isInstanceOf(ChatNotFoundException.class);
    }

    @Test
    @DisplayName("Изменение режима отправки обновлений - выбрасывает исключение")
    public void testAddNotificationType() {
        // GIVEN
        long telegramChatId = 12345L;
        String time = "09:30";
        LocalTime notificationTime = LocalTime.parse(time);
        AddNotificationTimeRequest addNotificationTimeRequest = new AddNotificationTimeRequest(notificationTime);

        when(chatRepository.updateNotificationTimeByTelegramChatId(telegramChatId, notificationTime))
                .thenReturn(0);

        // WHEN & THEN
        assertThatThrownBy(() -> chatService.addNotificationType(telegramChatId, addNotificationTimeRequest))
                .isInstanceOf(ChatNotFoundException.class);
    }
}
