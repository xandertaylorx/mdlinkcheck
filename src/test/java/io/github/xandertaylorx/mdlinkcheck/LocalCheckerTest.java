package io.github.xandertaylorx.mdlinkcheck;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalCheckerTest {

    @TempDir
    Path dir;

    private LocalChecker checker;
    private Path readme;

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(dir.resolve("docs"));
        Files.writeString(dir.resolve("docs/guide.md"), "# Guide\n\n## Install Steps\n");
        Files.writeString(dir.resolve("logo.png"), "png");
        readme = dir.resolve("README.md");
        Files.writeString(readme, "# Hello\n");
        checker = new LocalChecker(dir);
    }

    private Result check(String target) {
        return checker.check(new Link(readme, 1, target));
    }

    @Test
    void existingRelativeFileIsOk() {
        assertEquals(Result.Status.OK, check("docs/guide.md").status());
        assertEquals(Result.Status.OK, check("./logo.png").status());
    }

    @Test
    void missingFileIsBroken() {
        Result result = check("docs/missing.md");
        assertEquals(Result.Status.BROKEN, result.status());
        assertTrue(result.message().contains("file not found"));
    }

    @Test
    void sameFileAnchors() {
        assertEquals(Result.Status.OK, check("#hello").status());
        assertEquals(Result.Status.BROKEN, check("#goodbye").status());
    }

    @Test
    void anchorsInOtherFiles() {
        assertEquals(Result.Status.OK, check("docs/guide.md#install-steps").status());
        assertEquals(Result.Status.BROKEN, check("docs/guide.md#uninstall").status());
    }

    @Test
    void rootRelativeLinksUseRootDirectory() {
        assertEquals(Result.Status.OK, check("/docs/guide.md").status());
    }

    @Test
    void decodesPercentEncoding() throws IOException {
        Files.writeString(dir.resolve("my file.md"), "# X\n");
        assertEquals(Result.Status.OK, check("my%20file.md").status());
    }

    @Test
    void directoriesCountAsExisting() {
        assertEquals(Result.Status.OK, check("docs").status());
    }

    @Test
    void ignoresQueryStrings() {
        assertEquals(Result.Status.OK, check("logo.png?raw=true").status());
    }
}
