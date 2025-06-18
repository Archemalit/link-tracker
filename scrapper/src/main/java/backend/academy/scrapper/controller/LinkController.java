package backend.academy.scrapper.controller;

import backend.academy.scrapper.dto.request.AddLinkRequest;
import backend.academy.scrapper.dto.request.RemoveLinkRequest;
import backend.academy.scrapper.dto.response.LinkResponse;
import backend.academy.scrapper.dto.response.ListLinksResponse;
import backend.academy.scrapper.metric.CountUserMessage;
import backend.academy.scrapper.service.LinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/links")
@RequiredArgsConstructor
@Log4j2
public class LinkController {
    private final LinkService linkService;

    @CountUserMessage
    @GetMapping("/{id}")
    public ListLinksResponse getAllLinks(
            @PathVariable("id") long telegramChatId, @RequestParam(name = "tag", required = false) String tag) {
        return linkService.getAllLinksByChatIdAndTag(telegramChatId, tag);
    }

    @CountUserMessage
    @PostMapping("/{id}")
    public LinkResponse addLink(@PathVariable("id") long telegramChatId, @RequestBody AddLinkRequest addLinkRequest) {
        return linkService.addLinkForChat(telegramChatId, addLinkRequest);
    }

    @CountUserMessage
    @DeleteMapping("/{id}")
    public LinkResponse deleteLink(
            @PathVariable("id") long telegramChatId, @RequestBody RemoveLinkRequest removeLinkRequest) {
        return linkService.deleteLinkFromChat(telegramChatId, removeLinkRequest);
    }
}
