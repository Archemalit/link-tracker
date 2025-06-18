package backend.academy.scrapper.dto.response;

import java.util.List;

public record ListLinksResponse(List<LinkResponse> links, int size) {
    public ListLinksResponse(List<LinkResponse> links) {
        this(links, links.size());
    }
}
