package backend.academy.bot.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.bot.service.link.LinkUpdateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(LinksUpdateController.class)
@TestPropertySource(properties = {"app.message-transport=http"})
public class LinkUpdateControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LinkUpdateService linkUpdateService;

    @Test
    @DisplayName("Корректная отправка измененных данных с ссылок")
    public void testCorrectUpdateNormalLink() throws Exception {
        // GIVEN
        String json =
                "{\"chatId\":1,\"url\":\"https://stackoverflow.com/questions/123456\",\"description\":\"New answer!\"}";
        when(linkUpdateService.updateLink(any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // EXPECT
        mockMvc.perform(post("/updates").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("Некорректная отправка сообщения с неверным телом запроса")
    public void testCorrectUpdateWithWrongBody() throws Exception {
        // GIVEN
        String json = "\"name\":\"test\"";
        when(linkUpdateService.updateLink(any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // EXPECT
        mockMvc.perform(post("/updates").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Некорректная отправка сообщения без тела запроса")
    public void testCorrectUpdateWithNoBody() throws Exception {
        // GIVEN
        String json = "";
        when(linkUpdateService.updateLink(any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // EXPECT
        mockMvc.perform(post("/updates").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }
}
