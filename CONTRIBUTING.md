# Contributing

Thanks for helping improve mdlinkcheck!

1. Fork the repo and create a branch: `git checkout -b my-feature`
2. Make your change and add a JUnit test for it
3. Run `mvn test`
4. Open a pull request describing what and why

## Guidelines

- Keep the tool dependency-free (JDK only at runtime)
- Tests must not need the network; use `@TempDir` and `--offline`
- Small commits with imperative messages ("Add X", not "Added X")

## Reporting bugs

Please include the Markdown snippet, the command you ran, and the output you expected.
