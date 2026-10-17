package io.github.xandertaylorx.mdlinkcheck;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Expands files and directories into a sorted list of Markdown files. */
public final class FileFinder {

    private static final Set<String> SKIPPED_DIRS = Set.of("node_modules", "target", "build", "vendor");

    private FileFinder() {
    }

    public static List<Path> find(List<Path> inputs) throws IOException {
        List<Path> files = new ArrayList<>();
        for (Path input : inputs) {
            if (Files.isDirectory(input)) {
                Files.walkFileTree(input, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                        String name = dir.getFileName() == null ? "" : dir.getFileName().toString();
                        boolean skip = !dir.equals(input) && (name.startsWith(".") || SKIPPED_DIRS.contains(name));
                        return skip ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (isMarkdown(file)) {
                            files.add(file);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else if (Files.isRegularFile(input)) {
                files.add(input);
            } else {
                throw new IOException("No such file or directory: " + input);
            }
        }
        files.sort(null);
        return files;
    }

    static boolean isMarkdown(Path file) {
        String name = file.getFileName().toString().toLowerCase();
        return name.endsWith(".md") || name.endsWith(".markdown");
    }
}
