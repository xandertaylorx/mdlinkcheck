package io.github.xandertaylorx.mdlinkcheck;

import java.nio.file.Path;

/** A link found in a Markdown file. */
public record Link(Path file, int line, String target) {
}
