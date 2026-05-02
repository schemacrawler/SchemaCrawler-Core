# AGENTS.md — SchemaCrawler-Core

SchemaCrawler-Core is the foundational API, JDBC metadata loader, schema domain model, and tools framework on which all other SchemaCrawler projects depend. It is published to Maven Central and consumed as a versioned dependency by SchemaCrawler, SchemaCrawler-AI, and other extensions.

## Build and Test

```bash
# Standard build and unit tests
mvn clean verify

# With Testcontainers integration tests (requires Docker)
mvn clean verify -Dheavydb

# With architectural verification tests
mvn clean verify -Dverify

# Run a single test class or method
mvn test -Dtest=ClassName
mvn test -Dtest=ClassName#methodName
```

## Module Layout

Modules must be built in dependency order:

| Module | Purpose |
|--------|---------|
| `schemacrawler-utility` | Low-level utilities in `us.fatehi.utility`; no SchemaCrawler dependencies |
| `schemacrawler-api` | Core domain interfaces: `Catalog`, `Schema`, `Table`, `Column`, `Index`, `ForeignKey`, `View`, `Routine` |
| `schemacrawler-ermodel` | Entity-relationship model built on top of the API (`schemacrawler.ermodel.*`) |
| `schemacrawler-loader` | JDBC metadata loading; catalog and ER model loaders and their registries |
| `schemacrawler-tools` | Command execution framework, plugin registries, database connector infrastructure, output formatting |
| `schemacrawler` | Shaded distribution JAR — shades api, ermodel, loader, and tools into one artifact |
| `schemacrawler-testdb` | In-memory HSQLDB test database shared across test suites; test-scoped only |
| `schemacrawler-test-utility` | Shared test helpers and JUnit extensions; test-scoped only, never a production dependency |
| `schemacrawler-jdbc-drivers` | JDBC driver dependencies for use in tests |
| `schemacrawler-core-verify` | ArchUnit architectural verification (`CoreArchitectureTest`, `ModuleInfoTest`) — activated by `-Dverify` |

**Dependency order:** `utility` → `api` → `ermodel` / `loader` → `tools` → `schemacrawler` (shaded)

## Architecture

### Key Packages

| Package | Contents |
|---------|---------|
| `schemacrawler.schema` | Read-only public domain interfaces (`Table`, `Column`, `Index`, `ForeignKey`, etc.) |
| `schemacrawler.schemacrawler` | Configuration options and builder classes (`SchemaCrawlerOptions`, `LimitOptions`, etc.) |
| `schemacrawler.filter` / `schemacrawler.inclusionrule` | Filtering and inclusion/exclusion rule logic |
| `schemacrawler.crawl` | **Intentionally flat, all package-private** — `Mutable*` model implementations and `*Retriever` JDBC extractors. Do not split this package; these classes must remain package-private. |
| `schemacrawler.ermodel.implementation` | Internal ER model implementations (`Mutable*`); must remain package-private |
| `schemacrawler.loader.catalog.model` | YAML deserialization DTOs; only `schemacrawler.loader.ermodel.attributes` may reference them outside this package |
| `us.fatehi.utility` | Reusable utilities with no SchemaCrawler API dependencies |

### Registry / Plugin Loading

`*Registry` classes use **string-based loading** via `Class.forName` in `BasePluginCommandRegistry.instantiateProviders` rather than direct instantiation. This keeps compile-time edges out of `loader.*` packages and prevents package cycles.

## ArchUnit Constraints (`schemacrawler-core-verify`)

These rules are enforced by `CoreArchitectureTest` and must not be violated:

- **`lookup*` methods must return `Optional<T>`** — never `null`, never a bare concrete type.
- **No standard stream access** — no `System.out` or `System.err` in production code.
- **No generic exceptions** — never `throw new RuntimeException(...)`, `Exception`, or `Throwable`. Use SchemaCrawler's own exception hierarchy.
- **`@ModelImplementation` classes are package-private** — confined to `schemacrawler.crawl`, `schemacrawler.ermodel.implementation`, or `schemacrawler.loader.catalog.model`. No code outside those packages may reference them directly.
- **`@Retriever` classes are package-private** — JDBC metadata extractors confined to `schemacrawler.crawl`.
- **`Mutable*` classes carry `@ModelImplementation`** — always, without exception.
- **`Class.forName` is restricted** — only `BasePluginCommandRegistry` (registry loading) and `MutableColumnDataType` (Java type resolution) may use reflective class loading. All other code must not.
- **No package cycles** — slices on `schemacrawler.(**).*` must be acyclic. Document any approved exceptions in `schemacrawler-verified-cycles.md`.
- **No `setAccessible()` calls** — reflective access bypasses is prohibited.

When introducing new structural patterns or constraints, update `CoreArchitectureTest` in `schemacrawler-core-verify`.

## Coding Guidelines

- Prefer **immutability**: use `final` on fields, parameters, and local variables.
- Use `Optional`, streams, and functional programming idioms.
- Ensure **thread safety**: avoid mutable shared state.
- Write meaningful **Javadoc** for all public API.
- Tests use **JUnit 6** with **Hamcrest** matchers; mock with **Mockito**.
- All dependency versions are managed in `schemacrawler-parent/pom.xml`; do not declare versions in sub-module POMs.
- `schemacrawler-test-utility` and `schemacrawler-testdb` are test-scoped dependencies only; never add them as compile dependencies.
