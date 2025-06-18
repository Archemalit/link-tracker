package backend.academy.scrapper.util;

import backend.academy.scrapper.model.LinkType;
import backend.academy.scrapper.model.ParsedLink;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkParser {
    private static final Pattern STACK_PATTERN =
            Pattern.compile("https?://(?:www\\.)?stackoverflow\\.com/questions/(\\d+)(?:/.*)?");
    private static final Pattern GITHUB_PATTERN =
            Pattern.compile("https?://(?:www\\.)?github\\.com/([^/]+)/([^/]+)(?:/.*)?");

    public static ParsedLink parseLinkType(String url) {
        Matcher matcher = STACK_PATTERN.matcher(url);
        if (matcher.matches()) {
            String questionId = matcher.group(1);

            return new ParsedLink(LinkType.STACK_OVER_FLOW, null, questionId);
        }

        matcher = GITHUB_PATTERN.matcher(url);
        if (matcher.matches()) {
            String owner = matcher.group(1);
            String repos = matcher.group(2);

            return new ParsedLink(LinkType.GITHUB, owner, repos);
        }

        return new ParsedLink(LinkType.OTHER, null, null);
    }
}
