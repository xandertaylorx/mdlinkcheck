package io.github.xandertaylorx.mdlinkcheck;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Command line entry point. */
public final class Main {

    static final String VERSION = "0.1.0";

    private static final String USAGE = """
            mdlinkcheck %s: find broken links and anchors in Markdown files

            Usage: java -jar mdlinkcheck.jar [options] <file-or-directory>...

            Options:
              --offline          do not check http(s) links
              --timeout <secs>   timeout per request (default: 10)
              --ignore <regex>   skip links matching the regex (repeatable)
              --root <dir>       directory that "/absolute" links resolve against (default: .)
              --threads <n>      concurrent checks (default: 8)
              --verbose          also print links that are fine
              -h, --help         show this help
              --version          show the version

            Exit status: 0 if every link is fine, 1 if any link is broken, 2 on usage errors.
            """.formatted(VERSION);

    private Main() {
    }

    public static void main(String[] args) {
        System.exit(run(args, System.out, System.err));
    }

    static int run(String[] args, PrintStream out, PrintStream err) {
        List<Path> inputs = new ArrayList<>();
        List<Pattern> ignore = new ArrayList<>();
        Path root = Path.of(".");
        boolean offline = false;
        boolean verbose = false;
        Duration timeout = Duration.ofSeconds(10);
        int threads = 8;

        try {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "-h", "--help" -> {
                        out.print(USAGE);
                        return 0;
                    }
                    case "--version" -> {
                        out.println("mdlinkcheck " + VERSION);
                        return 0;
                    }
                    case "--offline" -> offline = true;
                    case "--verbose" -> verbose = true;
                    case "--timeout" -> timeout = Duration.ofSeconds(Long.parseLong(value(args, ++i)));
                    case "--threads" -> threads = Integer.parseInt(value(args, ++i));
                    case "--root" -> root = Path.of(value(args, ++i));
                    case "--ignore" -> ignore.add(Pattern.compile(value(args, ++i)));
                    default -> {
                        if (args[i].startsWith("-")) {
                            throw new IllegalArgumentException("unknown option " + args[i]);
                        }
                        inputs.add(Path.of(args[i]));
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            err.println("mdlinkcheck: " + e.getMessage());
            err.println("Try --help for usage.");
            return 2;
        }

        if (inputs.isEmpty()) {
            inputs.add(Path.of("."));
        }

        List<Link> links = new ArrayList<>();
        try {
            for (Path file : FileFinder.find(inputs)) {
                links.addAll(LinkExtractor.extract(file));
            }
        } catch (IOException e) {
            err.println("mdlinkcheck: " + e.getMessage());
            return 2;
        }

        Options options = new Options(root, offline, timeout, ignore, threads);
        List<Result> results = new LinkChecker(options).check(links);
        return Report.print(results, verbose, out);
    }

    private static String value(String[] args, int index) {
        if (index >= args.length) {
            throw new IllegalArgumentException("missing value for " + args[index - 1]);
        }
        return args[index];
    }
}
