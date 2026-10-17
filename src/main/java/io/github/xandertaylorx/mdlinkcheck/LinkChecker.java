package io.github.xandertaylorx.mdlinkcheck;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

/** Routes each link to the right checker and runs external checks concurrently. */
public final class LinkChecker {

    private static final List<String> IGNORED_SCHEMES = List.of("mailto:", "tel:", "data:", "javascript:", "sms:");

    private final Options options;
    private final LocalChecker local;
    private final HttpChecker http;

    public LinkChecker(Options options) {
        this.options = options;
        this.local = new LocalChecker(options.root());
        this.http = new HttpChecker(options.timeout());
    }

    public List<Result> check(List<Link> links) {
        ExecutorService pool = Executors.newFixedThreadPool(Math.max(1, options.threads()));
        try {
            List<Future<Result>> futures = new ArrayList<>();
            for (Link link : links) {
                futures.add(pool.submit(() -> checkOne(link)));
            }
            List<Result> results = new ArrayList<>();
            for (int i = 0; i < futures.size(); i++) {
                try {
                    results.add(futures.get(i).get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    results.add(Result.skipped(links.get(i), "interrupted"));
                } catch (ExecutionException e) {
                    results.add(Result.broken(links.get(i), "checker error: " + e.getCause()));
                }
            }
            return results;
        } finally {
            pool.shutdownNow();
        }
    }

    Result checkOne(Link link) {
        String target = link.target();
        for (Pattern pattern : options.ignore()) {
            if (pattern.matcher(target).find()) {
                return Result.skipped(link, "ignored by pattern");
            }
        }
        String lower = target.toLowerCase(Locale.ROOT);
        for (String scheme : IGNORED_SCHEMES) {
            if (lower.startsWith(scheme)) {
                return Result.skipped(link, scheme + " links are not checked");
            }
        }
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return options.offline() ? Result.skipped(link, "offline mode") : http.check(link);
        }
        return local.check(link);
    }
}
