<h1 align="center">mdlinkcheck</h1>

<p align="center">
  Find broken links and anchors in your Markdown files, before your readers do.
</p>

<p align="center">
  <a href="https://github.com/xandertaylorx/mdlinkcheck/actions/workflows/ci.yml"><img alt="CI" src="https://github.com/xandertaylorx/mdlinkcheck/actions/workflows/ci.yml/badge.svg"></a>
  <img alt="Java 17+" src="https://img.shields.io/badge/java-17%2B-orange">
  <img alt="License: MIT" src="https://img.shields.io/badge/license-MIT-green">
  <img alt="Dependencies: none" src="https://img.shields.io/badge/dependencies-none-brightgreen">
</p>

## Features

- Checks **relative file links**, **images** and **`#anchor` links** (including anchors in other files)
- Checks **http(s) links** concurrently, using `HEAD` and falling back to `GET`
- Understands GitHub-style heading slugs, duplicate headings (`usage-1`) and `<a name="...">`
- Ignores links inside fenced code blocks and inline code
- Supports inline links, reference definitions (`[id]: url`) and autolinks (`<https://...>`)
- Plain exit codes, so it drops straight into CI
- No runtime dependencies, just the JDK

## Build

Requires JDK 17+ and Maven.

```bash
git clone https://github.com/xandertaylorx/mdlinkcheck.git
cd mdlinkcheck
mvn package
```

This produces `target/mdlinkcheck.jar`.

## Usage

```bash
java -jar target/mdlinkcheck.jar README.md docs/      # files and/or directories
java -jar target/mdlinkcheck.jar --offline .          # skip http(s) links
java -jar target/mdlinkcheck.jar --ignore 'localhost' --ignore '^https://intranet\.' .
```

| Option | Description |
| --- | --- |
| `--offline` | Do not check http(s) links |
| `--timeout <secs>` | Timeout per request (default: 10) |
| `--ignore <regex>` | Skip links matching the regex (repeatable) |
| `--root <dir>` | Directory that `/absolute` links resolve against (default: `.`) |
| `--threads <n>` | Concurrent checks (default: 8) |
| `--verbose` | Also print links that are fine |
| `--help`, `--version` | Show help or the version |

With no paths, the current directory is scanned. Hidden folders, `node_modules`, `target`, `build` and `vendor` are skipped.

### Example output

```text
  BROKEN   docs/index.md:12  setup.md#install
           -> anchor not found: #install
  BROKEN   README.md:30  https://example.com/old-page
           -> HTTP 404

148 links checked: 146 ok, 2 broken, 0 skipped
```

### Exit codes

| Code | Meaning |
| --- | --- |
| `0` | Every link is fine |
| `1` | At least one broken link |
| `2` | Usage error (bad option, missing path) |

### Use in GitHub Actions

```yaml
- uses: actions/setup-java@v4
  with:
    distribution: temurin
    java-version: 17
- run: |
    git clone --depth 1 https://github.com/xandertaylorx/mdlinkcheck.git /tmp/mdlinkcheck
    mvn -q -f /tmp/mdlinkcheck/pom.xml package -DskipTests
    java -jar /tmp/mdlinkcheck/target/mdlinkcheck.jar .
```

## How it works

```
*.md  ->  LinkExtractor  ->  LinkChecker  ->  LocalChecker (files, anchors)  ->  Report
                                         \->  HttpChecker  (http, https)
```

| Class | Responsibility |
| --- | --- |
| `LinkExtractor` | Finds links in Markdown text, skipping code |
| `Slugger`, `Anchors` | Build GitHub-style anchors for each file |
| `LocalChecker` | Verifies files and anchors on disk |
| `HttpChecker` | Verifies web links with `java.net.http` |
| `LinkChecker` | Routes links to the right checker, in parallel |
| `Main`, `Report` | Command line interface and output |

## Development

```bash
mvn test
```

Local checks run against temporary directories, so the test suite needs no network. See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## Roadmap

- [ ] Optional JSON output
- [ ] Cache for repeated external URLs
- [ ] Configuration file (`.mdlinkcheck.yml`)
- [ ] Check HTML `<a href>` and `<img src>` inside Markdown

## Licence

[MIT](LICENSE) © Xander Taylor
