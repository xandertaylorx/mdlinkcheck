package io.github.xandertaylorx.mdlinkcheck;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainTest {

    @TempDir
    Path dir;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private int run(String... args) {
        return Main.run(args, new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8));
    }

    private String stdout() {
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void exitsZeroWhenAllLinksAreFine() throws IOException {
        Files.writeString(dir.resolve("a.md"), "# A\n[self](#a)\n");
        assertEquals(0, run("--offline", dir.toString()));
        assertTrue(stdout().contains("1 links checked: 1 ok, 0 broken, 0 skipped"));
    }

    @Test
    void exitsOneAndReportsBrokenLinks() throws IOException {
        Files.writeString(dir.resolve("a.md"), "[gone](gone.md)\n");
        assertEquals(1, run("--offline", dir.toString()));
        assertTrue(stdout().contains("BROKEN"));
        assertTrue(stdout().contains("file not found"));
    }

    @Test
    void externalLinksAreSkippedOffline() throws IOException {
        Files.writeString(dir.resolve("a.md"), "[x](https://example.invalid)\n");
        assertEquals(0, run("--offline", dir.toString()));
        assertTrue(stdout().contains("1 skipped"));
    }

    @Test
    void ignorePatternSkipsMatchingLinks() throws IOException {
        Files.writeString(dir.resolve("a.md"), "[gone](legacy/gone.md)\n");
        assertEquals(0, run("--offline", "--ignore", "^legacy/", dir.toString()));
    }

    @Test
    void unknownOptionIsAUsageError() {
        assertEquals(2, run("--wat"));
        assertTrue(err.toString(StandardCharsets.UTF_8).contains("unknown option"));
    }

    @Test
    void missingPathIsAUsageError() {
        assertEquals(2, run(dir.resolve("nope").toString()));
    }

    @Test
    void helpAndVersion() {
        assertEquals(0, run("--help"));
        assertTrue(stdout().contains("Usage:"));
        assertEquals(0, run("--version"));
    }
}
