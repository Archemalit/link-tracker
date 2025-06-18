package backend.academy.scrapper.util;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.model.LinkType;
import backend.academy.scrapper.model.ParsedLink;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LinkParserTest {

    @Test
    @DisplayName("Парсинг ссылки StackOverflow возвращает правильный результат")
    public void testParseStackOverflowLink() {
        // GIVEN
        String url = "https://stackoverflow.com/questions/123456789/how-to-parse-urls-in-java-hey";

        // WHEN
        ParsedLink parsedLink = LinkParser.parseLinkType(url);

        // THEN
        assertThat(parsedLink).isNotNull();
        assertThat(parsedLink.type()).isEqualTo(LinkType.STACK_OVER_FLOW);
        assertThat(parsedLink.owner()).isNull();
        assertThat(parsedLink.id()).isEqualTo("123456789");
    }

    @Test
    @DisplayName("Парсинг ссылки GitHub возвращает правильный результат")
    public void testParseGitHubLink() {
        // GIVEN
        String url = "https://github.com/tbank/academy";

        // WHEN
        ParsedLink parsedLink = LinkParser.parseLinkType(url);

        // THEN
        assertThat(parsedLink).isNotNull();
        assertThat(parsedLink.type()).isEqualTo(LinkType.GITHUB);
        assertThat(parsedLink.owner()).isEqualTo("tbank");
        assertThat(parsedLink.id()).isEqualTo("academy");
    }

    @Test
    @DisplayName("Парсинг произвольной ссылки возвращает тип OTHER")
    public void testParseOtherLink() {
        // GIVEN
        String url = "https://example.com/some/path/to/resource";

        // WHEN
        ParsedLink parsedLink = LinkParser.parseLinkType(url);

        // THEN
        assertThat(parsedLink).isNotNull();
        assertThat(parsedLink.type()).isEqualTo(LinkType.OTHER);
        assertThat(parsedLink.owner()).isNull();
        assertThat(parsedLink.id()).isNull();
    }

    @Test
    @DisplayName("Неверная ссылка возвращает тип OTHER")
    public void testInvalidUrl() {
        // GIVEN
        String url = "invalid-url";

        // WHEN
        ParsedLink parsedLink = LinkParser.parseLinkType(url);

        // THEN
        assertThat(parsedLink).isNotNull();
        assertThat(parsedLink.type()).isEqualTo(LinkType.OTHER);
        assertThat(parsedLink.owner()).isNull();
        assertThat(parsedLink.id()).isNull();
    }

    @Test
    @DisplayName("Пустая строка как ссылка возвращает тип OTHER")
    public void testEmptyUrl() {
        // GIVEN
        String url = "";

        // WHEN
        ParsedLink parsedLink = LinkParser.parseLinkType(url);

        // THEN
        assertThat(parsedLink).isNotNull();
        assertThat(parsedLink.type()).isEqualTo(LinkType.OTHER);
        assertThat(parsedLink.owner()).isNull();
        assertThat(parsedLink.id()).isNull();
    }
}
