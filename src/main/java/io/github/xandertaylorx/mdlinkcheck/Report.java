package io.github.xandertaylorx.mdlinkcheck;

import java.io.PrintStream;
import java.util.List;

/** Prints results and returns the process exit status. */
public final class Report {

    private Report() {
    }

    public static int print(List<Result> results, boolean verbose, PrintStream out) {
        int ok = 0;
        int broken = 0;
        int skipped = 0;

        for (Result result : results) {
            Link link = result.link();
            String where = link.file() + ":" + link.line();
            switch (result.status()) {
                case OK -> {
                    ok++;
                    if (verbose) {
                        out.printf("  ok       %s  %s%n", where, link.target());
                    }
                }
                case SKIPPED -> {
                    skipped++;
                    if (verbose) {
                        out.printf("  skipped  %s  %s (%s)%n", where, link.target(), result.message());
                    }
                }
                case BROKEN -> {
                    broken++;
                    out.printf("  BROKEN   %s  %s%n           -> %s%n", where, link.target(), result.message());
                }
            }
        }

        out.printf("%n%d links checked: %d ok, %d broken, %d skipped%n", results.size(), ok, broken, skipped);
        return broken == 0 ? 0 : 1;
    }
}
