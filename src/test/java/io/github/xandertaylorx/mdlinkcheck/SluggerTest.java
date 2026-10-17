package io.github.xandertaylorx.mdlinkcheck;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class SluggerTest {

    @Test
    void lowercasesAndHyphenatesSpaces() {
        assertEquals("getting-started", Slugger.slug("Getting Started"));
    }

    @Test
    void dropsPunctuationAndFormatting() {
        assertEquals("whats-new-in-v2", Slugger.slug("What's `New` in **v2**?"));
    }

    @Test
    void usesLinkTextOnly() {
        assertEquals("see-docs", Slugger.slug("See [docs](https://x.io)"));
    }

    @Test
    void numbersDuplicateHeadings() {
        Slugger slugger = new Slugger();
        assertEquals("usage", slugger.next("Usage"));
        assertEquals("usage-1", slugger.next("Usage"));
        assertEquals("usage-2", slugger.next("Usage"));
    }

    @Test
    void anchorsIncludeHeadingsAndHtmlIds() {
        Set<String> anchors = Anchors.of("# Title\n## Sub Section ##\n<a name=\"custom\"></a>\n```\n# not a heading\n```");
        assertEquals(Set.of("title", "sub-section", "custom"), anchors);
        assertTrue(!anchors.contains("not-a-heading"));
    }
}
