package io.github.xandertaylorx.mdlinkcheck;

/** Outcome of checking one {@link Link}. */
public record Result(Link link, Status status, String message) {

    public enum Status { OK, BROKEN, SKIPPED }

    public static Result ok(Link link) {
        return new Result(link, Status.OK, "");
    }

    public static Result broken(Link link, String message) {
        return new Result(link, Status.BROKEN, message);
    }

    public static Result skipped(Link link, String message) {
        return new Result(link, Status.SKIPPED, message);
    }
}
