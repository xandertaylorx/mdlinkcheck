package io.github.xandertaylorx.mdlinkcheck;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class LinkExtractorTest {

    private static List<String> targets(String markdown) {
        return LinkExtractor.extract(Path.of("x.md"), markdown).stream().map(Link::target).toList();
    }

    @Test
    void findsInlineLinksAndImages() {
        assertEquals(List.of("a.md", "img.png"), targets("See [a](a.md) and ![pic](img.png)."));
    }

    @Test
    void ignoresLinkTitles() {
        assertEquals(List.of("https://x.io"), targets("[x](https://x.io \"The title\")"));
    }

    @Test
    void keepsBalancedParenthesesInUrls() {
        assertEquals(List.of("https://en.wikipedia.org/wiki/Java_(language)"),
                targets("[Java](https://en.wikipedia.org/wiki/Java_(language))"));
    }

    @Test
    void findsReferenceDefinitionsAndAutolinks() {
        assertEquals(List.of("docs/guide.md", "https://example.com"),
                targets("[guide]: docs/guide.md \"Guide\"\n<https://example.com>"));
    }

    @Test
    void skipsFencedCodeAndInlineCode() {
        String md = "```\n[nope](fenced.md)\n```\nUse `[nope](inline.md)` but [yes](real.md)";
        assertEquals(List.of("real.md"), targets(md));
    }

    @Test
    void recordsLineNumbers() {
        List<Link> links = LinkExtractor.extract(Path.of("x.md"), "one\n\n[a](a.md)");
        assertEquals(1, links.size());
        assertEquals(3, links.get(0).line());
    }

    @Test
    void emptyTargetsAreIgnored() {
        assertTrue(targets("[nothing]()").isEmpty());
    }
}
