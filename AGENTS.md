# Instructions for SchemaCrawler Core Project

## Project Structure and Build
- SchemaCrawler is set up as a multi-module **Apache Maven** project.
- Standard Apache Maven commands can be used to build and test the project.
- Integration tests use **Testcontainers**, and can be triggered with an additional `-Dheavydb` flag to the Apache Maven commands.

## Module Layout
- `schemacrawler-api` — core schema model, SQL metadata crawling (`schemacrawler.crawl`), public API (`schemacrawler.schema`, `schemacrawler.schemacrawler`, `schemacrawler.filter`).
- `schemacrawler-ermodel` — ER model construction and inference (`schemacrawler.ermodel.*`).
- `schemacrawler-tools` — command execution framework, registries, database connectors, output formatting (`schemacrawler.tools.*`).
- `schemacrawler-loader` — catalog and ER model loaders and their registries (`schemacrawler.loader.*`).
- `schemacrawler-utility` — low-level utilities in the `us.fatehi.utility` namespace (not `schemacrawler.*`).
- `schemacrawler-test-utility` — shared test infrastructure only; never a production compile-time dependency.
- `schemacrawler-core-verify` — ArchUnit architecture verification tests (`CoreArchitectureTest`, `ModuleInfoTest`).

## General Coding Guidelines
- Prefer **immutability** and use the `final` keyword for fields, parameters, and local variables wherever possible.
- Follow **Java best practices**, including usage of `Optional`, `Streams`, and functional programming where applicable.
- Ensure **thread safety** by avoiding mutable shared state and using synchronized wrappers or concurrency utilities when necessary.
- Use **meaningful names** for classes, methods, and variables to improve code readability.
- Follow **SOLID principles** to enhance maintainability and scalability.
- Write meaningful **javadocs** for functions and classes.

## Code Conventions
The following conventions are enforced by ArchUnit and must not be violated:

- **`lookup*` public methods must return `Optional<T>`** — never `null`, never a concrete type.
- **No standard stream access** — production code must not write to `System.out` or `System.err`.
- **No generic exceptions** — never `throw new RuntimeException(...)`, `Exception`, or `Throwable`. Use SchemaCrawler's own exception hierarchy.
- **`@ModelImplementation` classes are package-private** — annotated classes are internal schema model implementations confined to `schemacrawler.crawl`, `schemacrawler.ermodel.implementation`, or `schemacrawler.loader.catalog.model`. No code outside those packages may reference them.
- **`@Retriever` classes are package-private** — JDBC metadata extractors in `schemacrawler.crawl`; must not be referenced from any other package.
- **`Mutable*` classes carry `@ModelImplementation`** — classes named `Mutable*` are always internal model implementations and must be annotated accordingly.
- **`Class.forName` is restricted** — only `schemacrawler.tools.command` (registry loading via `BasePluginCommandRegistry`) and `schemacrawler.crawl` (`MutableColumnDataType` Java-type resolution) may call `Class.forName`. All other code must not use reflective class loading.
- **No package cycles** — slices on `schemacrawler.(**).*` must be acyclic. Track any exceptions in `schemacrawler-verified-cycles.md` in the repository root.

## Internal Package Conventions
- `schemacrawler.crawl` — intentionally flat (not split into subpackages). All `Mutable*` model implementations and `*Retriever` JDBC extractors are **package-private**. Do not split this package; doing so would require making these classes public.
- `schemacrawler.ermodel.implementation` — internal ER model implementations (`Mutable*`); must remain package-private.
- `schemacrawler.loader.catalog.model` — YAML deserialization DTOs; only `schemacrawler.loader.ermodel.attributes` may reference them outside the model package.
- Registry classes (`*Registry`) use **string-based loading** (`Class.forName` in `BasePluginCommandRegistry.instantiateProviders`) instead of direct `new ProviderClass()` instantiation. This keeps compile-time edges out of `loader.*` packages and prevents package cycles.

## Dependencies and Versions
- Define **explicit versions** for dependencies to prevent compatibility issues.
- Prefer **dependency management** using `dependencyManagement` in `pom.xml` for centralized version control.
- Use **dependency exclusions** where necessary to avoid unwanted transitive dependencies.

## Testing and Quality
- Write **unit tests** for business logic using **JUnit 5** with **Hamcrest** matchers.
- Use **Mockito** for mocking dependencies in tests.
- Maintain **high test coverage** to ensure reliability.
- Architecture tests live in `schemacrawler-core-verify`; update `CoreArchitectureTest` when introducing new structural patterns or constraints.
