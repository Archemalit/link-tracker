package backend.academy.scrapper.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.scrapper.dto.request.AddLinkRequest;
import backend.academy.scrapper.dto.request.RemoveLinkRequest;
import backend.academy.scrapper.dto.response.LinkResponse;
import backend.academy.scrapper.dto.response.ListLinksResponse;
import backend.academy.scrapper.exception.EmptyLinkException;
import backend.academy.scrapper.interceptor.IpRateLimiterInterceptor;
import backend.academy.scrapper.service.LinkService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(
        controllers = {
            LinkController.class,
        })
public class LinkControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LinkService linkService;

    @MockitoBean
    private IpRateLimiterInterceptor ipRateLimiterInterceptor;

    @Test
    @DisplayName("Получение всех ссылок для чата - успешный сценарий")
    public void testGetAllLinks() throws Exception {
        // GIVEN
        long chatId = 1L;
        String link1 = "https://github.com/example_owner/example_repo";
        String link2 = "https://stackoverflow.com/questions/123456678/example";

        ListLinksResponse listLinksResponse =
                new ListLinksResponse(List.of(new LinkResponse(chatId, link1), new LinkResponse(chatId, link2)));

        when(linkService.getAllLinksByChatIdAndTag(chatId, null)).thenReturn(listLinksResponse);
        when(ipRateLimiterInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        // WHEN & THEN
        mockMvc.perform(get("/links/{id}", chatId))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content()
                        .string("{\"links\":[{\"id\":1,\"url\":\"" + link1 + "\"},{\"id\":1,\"url\":\"" + link2
                                + "\"}],\"size\":2}"));
    }

    @Test
    @DisplayName("Добавление ссылки для чата - успешный сценарий")
    public void testAddLinkForChat() throws Exception {
        // GIVEN
        long chatId = 1L;
        String link = "https://stackoverflow.com/questions/123/new";
        String json = "{\"link\": \"" + link + "\"}";
        AddLinkRequest addLinkRequest = new AddLinkRequest(link, null, null);

        LinkResponse linkResponse = new LinkResponse(chatId, link);

        when(linkService.addLinkForChat(chatId, addLinkRequest)).thenReturn(linkResponse);
        when(ipRateLimiterInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        // WHEN & THEN
        mockMvc.perform(post("/links/{id}", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"id\":1,\"url\":\"" + link + "\"}"));
    }

    @Test
    @DisplayName("Добавление ссылки без тела запроса - должно вернуть 400")
    public void testAddLinkForChatWithOutBody() throws Exception {
        // GIVEN
        long chatId = 1L;
        when(ipRateLimiterInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        // WHEN & THEN
        mockMvc.perform(post("/links/{id}", chatId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Добавление ссылки с некорректным JSON - должно вернуть 400")
    public void testAddLinkForChatWithInvalidJson() throws Exception {
        // GIVEN
        long chatId = 1L;
        AddLinkRequest addLinkRequest = new AddLinkRequest(null, null, null);
        String invalidJson = "{\"name\": \"\"}";

        when(linkService.addLinkForChat(chatId, addLinkRequest)).thenThrow(EmptyLinkException.class);
        when(ipRateLimiterInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        // WHEN & THEN
        mockMvc.perform(post("/links/{id}", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Удаление ссылки без тела запроса - должно вернуть 400")
    public void testDeleteLinkWithOutBody() throws Exception {
        // GIVEN
        long chatId = 1L;
        when(ipRateLimiterInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        // WHEN & THEN
        mockMvc.perform(delete("/links/{id}", chatId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Удаление ссылки с некорректным JSON - должно вернуть 400")
    public void testDeleteLinkForChatWithInvalidJson() throws Exception {
        // GIVEN
        long chatId = 1L;
        String invalidJson = "{\"name\": \"\"}";
        RemoveLinkRequest request = new RemoveLinkRequest(null);

        when(linkService.deleteLinkFromChat(chatId, request)).thenThrow(EmptyLinkException.class);
        when(ipRateLimiterInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        // WHEN & THEN
        mockMvc.perform(delete("/links/{id}", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Удаление ссылки для чата - успешный сценарий")
    public void testDeleteLinkForChat() throws Exception {
        // GIVEN
        long chatId = 1L;
        String link = "https://github.com/someuser/somerepo";
        String json = "{\"link\": \"" + link + "\"}";
        RemoveLinkRequest request = new RemoveLinkRequest(link);
        LinkResponse response = new LinkResponse(chatId, link);

        when(linkService.deleteLinkFromChat(chatId, request)).thenReturn(response);
        when(ipRateLimiterInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        // WHEN & THEN
        mockMvc.perform(delete("/links/{id}", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{\"id\":1,\"url\":\"" + link + "\"}"));
    }
}
