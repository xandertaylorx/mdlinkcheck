package io.github.xandertaylorx.mdlinkcheck;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/** Turns headings into GitHub-style anchor slugs, handling duplicates. */
public final class Slugger {

    private static final Pattern LINK_TEXT = Pattern.compile("!?\\[([^\\]]*)\\]\\([^)]*\\)");
    private static final Pattern DISALLOWED = Pattern.compile("[^\\p{L}\\p{N}\\p{M} _-]");

    private final Map<String, Integer> seen = new HashMap<>();

    /** Slug for a heading, appending -1, -2 ... to repeated headings. */
    public String next(String heading) {
        String base = slug(heading);
        int count = seen.merge(base, 1, Integer::sum) - 1;
        return count == 0 ? base : base + "-" + count;
    }

    public static String slug(String heading) {
        String text = LINK_TEXT.matcher(heading).replaceAll("$1");
        text = text.replace("`", "").replace("*", "").strip().toLowerCase(Locale.ROOT);
        text = DISALLOWED.matcher(text).replaceAll("");
        return text.replace(' ', '-');
    }
}
