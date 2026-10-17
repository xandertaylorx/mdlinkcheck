package io.github.xandertaylorx.mdlinkcheck;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Collects the anchors (heading slugs and explicit ids) available in a Markdown file. */
public final class Anchors {

    private static final Pattern HEADING = Pattern.compile("^\\s{0,3}#{1,6}\\s+(.*?)\\s*#*\\s*$");
    private static final Pattern HTML_ID = Pattern.compile("<a\\s+[^>]*?(?:name|id)=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
    private static final Pattern FENCE = Pattern.compile("^\\s{0,3}(```|~~~)");

    private Anchors() {
    }

    public static Set<String> of(Path file) throws IOException {
        return of(Files.readString(file));
    }

    public static Set<String> of(String markdown) {
        Set<String> anchors = new HashSet<>();
        Slugger slugger = new Slugger();
        boolean inFence = false;
        for (String line : markdown.split("\\R")) {
            if (FENCE.matcher(line).find()) {
                inFence = !inFence;
                continue;
            }
            if (inFence) {
                continue;
            }
            Matcher heading = HEADING.matcher(line);
            if (heading.find()) {
                anchors.add(slugger.next(heading.group(1)));
            }
            Matcher id = HTML_ID.matcher(line);
            while (id.find()) {
                anchors.add(id.group(1));
            }
        }
        return anchors;
    }
}
