package io.github.xandertaylorx.mdlinkcheck;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

/** Settings for a link check run. */
public record Options(Path root, boolean offline, Duration timeout, List<Pattern> ignore, int threads) {

    public static Options defaults(Path root) {
        return new Options(root, false, Duration.ofSeconds(10), List.of(), 8);
    }
}
