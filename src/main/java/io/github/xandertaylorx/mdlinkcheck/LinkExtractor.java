package io.github.xandertaylorx.mdlinkcheck;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Finds links inside Markdown text. */
public final class LinkExtractor {

    /** Inline links and images: [text](target "title") or ![alt](target). */
    private static final Pattern INLINE = Pattern.compile(
            "!?\\[[^\\]]*\\]\\(\\s*<?([^()\\s>]*(?:\\([^()\\s]*\\)[^()\\s>]*)*)>?(?:\\s+(?:\"[^\"]*\"|'[^']*'))?\\s*\\)");

    /** Reference definitions: [id]: target "title". */
    private static final Pattern REFERENCE = Pattern.compile(
            "^\\s{0,3}\\[[^\\]]+\\]:\\s*<?(\\S+?)>?(?:\\s+.*)?$");

    /** Autolinks: <https://example.com>. */
    private static final Pattern AUTOLINK = Pattern.compile("<(https?://[^>\\s]+)>");

    /** Opening or closing line of a fenced code block. */
    private static final Pattern FENCE = Pattern.compile("^\\s{0,3}(```|~~~)");

    /** Inline code spans, which may contain link-like text that is not a link. */
    private static final Pattern CODE_SPAN = Pattern.compile("`+[^`]*`+");

    private LinkExtractor() {
    }

    public static List<Link> extract(Path file) throws IOException {
        return extract(file, Files.readString(file));
    }

    public static List<Link> extract(Path file, String markdown) {
        List<Link> links = new ArrayList<>();
        String[] lines = markdown.split("\\R", -1);
        boolean inFence = false;
        for (int i = 0; i < lines.length; i++) {
            int lineNo = i + 1;
            if (FENCE.matcher(lines[i]).find()) {
                inFence = !inFence;
                continue;
            }
            if (inFence) {
                continue;
            }
            String line = CODE_SPAN.matcher(lines[i]).replaceAll(" ");

            Matcher ref = REFERENCE.matcher(line);
            if (ref.find()) {
                links.add(new Link(file, lineNo, ref.group(1)));
                continue;
            }
            collect(INLINE, line, file, lineNo, links);
            collect(AUTOLINK, line, file, lineNo, links);
        }
        return links;
    }

    private static void collect(Pattern pattern, String line, Path file, int lineNo, List<Link> out) {
        Matcher m = pattern.matcher(line);
        while (m.find()) {
            String target = m.group(1).trim();
            if (!target.isEmpty()) {
                out.add(new Link(file, lineNo, target));
            }
        }
    }
}
