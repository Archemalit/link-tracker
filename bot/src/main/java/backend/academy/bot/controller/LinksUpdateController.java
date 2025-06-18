package backend.academy.bot.controller;

import backend.academy.bot.dto.LinkUpdate;
import backend.academy.bot.service.link.LinkUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/updates")
@RequiredArgsConstructor
@Log4j2
public class LinksUpdateController {
    private final LinkUpdateService linkUpdateService;

    @PostMapping
    public ResponseEntity<Void> updateLink(@RequestBody LinkUpdate linkUpdate) {
        return linkUpdateService.updateLink(linkUpdate);
    }
}
