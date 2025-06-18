package backend.academy.scrapper.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.scrapper.client.RestClientStackOverFlow;
import backend.academy.scrapper.exception.ChatNotFoundException;
import backend.academy.scrapper.interceptor.IpRateLimiterInterceptor;
import backend.academy.scrapper.service.ChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ChatController.class)
public class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatService chatService;

    @MockitoBean
    private RestClientStackOverFlow restClientStackOverFlow;

    @MockitoBean
    private IpRateLimiterInterceptor ipRateLimiterInterceptor;

    @Test
    @DisplayName("Регистрация чата должна возвращать статус 200")
    public void testRegisterChat() throws Exception {
        // GIVEN
        long telegramChatId = 1L;
        when(ipRateLimiterInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        // WHEN
        mockMvc.perform(post("/tg-chat/{id}", telegramChatId)).andExpect(status().isOk());

        // THEN
        verify(chatService).registerChat(telegramChatId);
    }

    @Test
    @DisplayName("Удаление существующего чата должно возвращать статус 200")
    public void testDeleteChat() throws Exception {
        // GIVEN
        long telegramChatId = 1L;
        when(ipRateLimiterInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        // WHEN
        mockMvc.perform(delete("/tg-chat/{id}", telegramChatId)).andExpect(status().isOk());

        // THEN
        verify(chatService).deleteChat(telegramChatId);
    }

    @Test
    @DisplayName("Удаление несуществующего чата должно возвращать 404")
    public void testDeleteNonExistentChat() throws Exception {
        // GIVEN
        long telegramChatId = 1L;
        when(ipRateLimiterInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        // WHEN
        doThrow(new ChatNotFoundException(telegramChatId)).when(chatService).deleteChat(telegramChatId);

        // THEN
        mockMvc.perform(delete("/tg-chat/{id}", telegramChatId)).andExpect(status().isNotFound());
    }
}
