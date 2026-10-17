package io.github.xandertaylorx.mdlinkcheck;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Verifies http(s) links with a HEAD request, falling back to GET. */
public final class HttpChecker {

    private static final String USER_AGENT = "mdlinkcheck/0.1 (+https://github.com/xandertaylorx/mdlinkcheck)";

    private final HttpClient client;
    private final Duration timeout;

    public HttpChecker(Duration timeout) {
        this.timeout = timeout;
        this.client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(timeout)
                .build();
    }

    public Result check(Link link) {
        URI uri;
        try {
            uri = URI.create(link.target());
        } catch (IllegalArgumentException e) {
            return Result.broken(link, "invalid URL");
        }
        if (uri.getHost() == null) {
            return Result.broken(link, "invalid URL");
        }

        try {
            int status = send(uri, "HEAD");
            // Some servers reject HEAD; retry with GET before calling the link broken.
            if (status >= 400) {
                status = send(uri, "GET");
            }
            return interpret(link, status);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Result.skipped(link, "interrupted");
        } catch (IOException e) {
            String reason = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            return Result.broken(link, "request failed: " + reason);
        }
    }

    private int send(URI uri, String method) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .method(method, HttpRequest.BodyPublishers.noBody())
                .timeout(timeout)
                .header("User-Agent", USER_AGENT)
                .build();
        return client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
    }

    static Result interpret(Link link, int status) {
        if (status < 400) {
            return Result.ok(link);
        }
        if (status == 429) {
            return Result.skipped(link, "rate limited (HTTP 429)");
        }
        return Result.broken(link, "HTTP " + status);
    }
}
