package io.github.xandertaylorx.mdlinkcheck;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Verifies relative file links and #anchor links. */
public final class LocalChecker {

    private final Path root;
    private final Map<Path, Set<String>> anchorCache = new ConcurrentHashMap<>();

    /**
     * @param root directory that links starting with "/" are resolved against
     */
    public LocalChecker(Path root) {
        this.root = root.toAbsolutePath().normalize();
    }

    public Result check(Link link) {
        String target = link.target();
        String fragment = null;

        int hash = target.indexOf('#');
        if (hash >= 0) {
            fragment = decode(target.substring(hash + 1));
            target = target.substring(0, hash);
        }
        int query = target.indexOf('?');
        if (query >= 0) {
            target = target.substring(0, query);
        }
        target = decode(target);

        Path file = link.file().toAbsolutePath().normalize();
        Path destination;
        if (target.isEmpty()) {
            destination = file;
        } else if (target.startsWith("/")) {
            destination = root.resolve(target.substring(1)).normalize();
        } else {
            Path parent = file.getParent() == null ? root : file.getParent();
            destination = parent.resolve(target).normalize();
        }

        if (!Files.exists(destination)) {
            return Result.broken(link, "file not found: " + target);
        }
        if (fragment != null && !fragment.isEmpty() && isMarkdown(destination)) {
            if (!anchorsOf(destination).contains(fragment.toLowerCase())
                    && !anchorsOf(destination).contains(fragment)) {
                return Result.broken(link, "anchor not found: #" + fragment);
            }
        }
        return Result.ok(link);
    }

    private Set<String> anchorsOf(Path file) {
        return anchorCache.computeIfAbsent(file, f -> {
            try {
                return Anchors.of(f);
            } catch (IOException e) {
                return Set.of();
            }
        });
    }

    private static boolean isMarkdown(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return Files.isRegularFile(path) && (name.endsWith(".md") || name.endsWith(".markdown"));
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value.replace("+", "%2B"), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return value;
        }
    }
}
