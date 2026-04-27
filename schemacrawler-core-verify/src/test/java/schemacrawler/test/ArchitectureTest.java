/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackages;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleName;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_TESTS;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.GeneralCodingRules.ACCESS_STANDARD_STREAMS;
import static com.tngtech.archunit.library.GeneralCodingRules.THROW_GENERIC_EXCEPTIONS;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.util.Optional;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import schemacrawler.schemacrawler.ModelImplementation;
import schemacrawler.schemacrawler.Retriever;

@TestInstance(PER_CLASS)
public class ArchitectureTest {

  private JavaClasses classes;

  @BeforeAll
  public void _classes() {
    final String description = "SchemaCrawler production classes";
    classes =
        new ClassFileImporter()
            .withImportOption(DO_NOT_INCLUDE_TESTS)
            .withImportOption(location -> !location.matches(Pattern.compile(".*[Tt]est.*")))
            .importPackages("schemacrawler..")
            .as(description);
    assertThat(description + " classes not found", classes.isEmpty(), is(false));
  }

  // The schemacrawler.crawl package is intentionally kept flat (not split into subpackages).
  //
  // All Mutable* model implementations and *Retriever JDBC extractors are package-private.
  // This prevents classpath users (those loading SchemaCrawler as a plain jar rather than
  // via the JPMS module path) from constructing or referencing these internal classes.
  //
  // Java package-private visibility is strictly per-package. Splitting into subpackages would
  // require making these classes at least public — immediately exposing them to classpath
  // clients. The JPMS module system's "do not export schemacrawler.crawl" already protects
  // module-path users; these tests enforce the same architectural boundary for all users.
  @Test
  public void architecture() {

    final DescribedPredicate<JavaClass> modelImplInCrawl =
        resideInAPackage("schemacrawler.crawl").and(annotatedWith(ModelImplementation.class));
    noClasses()
        .that()
        .resideOutsideOfPackage("schemacrawler.crawl")
        .should()
        .dependOnClassesThat(modelImplInCrawl)
        .because(
            """
            @ModelImplementation classes in schemacrawler.crawl are package-private internal
              schema model implementations; they must only be used within schemacrawler.crawl
              to prevent classpath clients from constructing schema model objects directly
            """)
        .check(classes);

    final DescribedPredicate<JavaClass> modelImplInERModel =
        resideInAPackage("schemacrawler.ermodel.implementation")
            .and(annotatedWith(ModelImplementation.class));
    noClasses()
        .that()
        .resideOutsideOfPackage("schemacrawler.ermodel.implementation")
        .should()
        .dependOnClassesThat(modelImplInERModel)
        .because(
            """
            @ModelImplementation classes in schemacrawler.ermodel.implementation are internal
              ER model implementations; they must only be used within that package
            """)
        .check(classes);

    final DescribedPredicate<JavaClass> modelImplInLoaderCatalog =
        resideInAPackage("schemacrawler.loader.catalog.model")
            .and(annotatedWith(ModelImplementation.class));
    noClasses()
        .that()
        .resideOutsideOfPackages(
            "schemacrawler.loader.catalog.model", "schemacrawler.loader.ermodel.attributes")
        .should()
        .dependOnClassesThat(modelImplInLoaderCatalog)
        .because(
            """
            @ModelImplementation classes in schemacrawler.loader.catalog.model are internal YAML
              deserialization DTOs; only AttributesLoader in ermodel.attributes (the designated
              processor) is permitted to reference them outside the model package
            """)
        .check(classes);

    final DescribedPredicate<JavaClass> retrieverInCrawl =
        resideInAPackage("schemacrawler.crawl").and(annotatedWith(Retriever.class));
    noClasses()
        .that()
        .resideOutsideOfPackage("schemacrawler.crawl")
        .should()
        .dependOnClassesThat(retrieverInCrawl)
        .because(
            """
            @Retriever classes in schemacrawler.crawl are package-private JDBC metadata
              extractors; they must only be used within schemacrawler.crawl
            """)
        .check(classes);
  }

  // 6 structural cycles verified by a live ArchUnit run on 2026-04-26.
  // See schemacrawler-verified-cycles.md in the repository root for full details.
  //
  // History:
  //   - Originally 10 cycles. Cycles 1-4 (MetaDataUtility.reduceCatalog) fixed by CatalogReducer.
  //   - FilterFactory collapsed into StandardCatalogReducer (no cycle impact).
  //   - Old cycles 4,5,7,8 (loader registry direct sub-package instantiation) fixed by
  //     replacing 'new ProviderClass()' with string-based Class.forName loading in registries.
  //     This eliminated compile-time loader.catalog→subpackage and loader.ermodel→subpackage
  //     edges. One previously-hidden cycle became visible: loader.ermodel ↔ tools.utility.
  //
  // schemacrawler-api:
  //   Cycle 1  crawl → filter → schemacrawler → crawl  [3-way]
  //            crawl retrievers accept InclusionRuleFilter (filter) as parameters;
  //            filter classes accept SchemaCrawlerOptions/LimitOptions/GrepOptions (schemacrawler);
  //            MetadataResultSet (schemacrawler) constructs ResultsCrawler (crawl).
  //            Fix: move InclusionRuleFilter out of filter (task: cycle-fix-inclusion-rule-filter)
  //
  //   Cycle 2  crawl ↔ schemacrawler  [2-way]
  //            crawl uses SchemaCrawlerOptions, annotations, etc. from schemacrawler;
  //            MetadataResultSet (schemacrawler) constructs and calls ResultsCrawler (crawl).
  //            Fix: move ResultsCrawler to schemacrawler (task: cycle-fix-results-crawler)
  //
  // schemacrawler-ermodel:
  //   Cycle 3  ermodel.implementation ↔ ermodel.utility  [2-way]
  //            MutableEntityAttribute calls ERModelUtility.inferEntityAttributeType();
  //            ERModelUtility constructs ERModelBuilder and TableEntityModelInferrer
  // (implementation).
  //            Fix: move inferEntityAttributeType() into ermodel.implementation
  //            (task: cycle-fix-infer-entity-attr)
  //
  // schemacrawler-loader:
  //   Cycle 4  loader.catalog ↔ tools.utility  [cross-module]
  //            ChainedCatalogLoader.execute() calls ExecutionStateUtility.transferState();
  //            SchemaCrawlerUtility.getCatalog() uses CatalogLoaderRegistry/CatalogLoader.
  //            Fix: move transferState() into loader.catalog, or extract a facade interface
  //
  //   Cycle 5  loader.ermodel ↔ tools.utility  [cross-module]
  //            ChainedERModelLoader.execute() calls ExecutionStateUtility.transferState();
  //            SchemaCrawlerUtility.buildERModel() uses ERModelLoaderRegistry/ChainedERModelLoader.
  //            Fix: same approach as Cycle 4
  //
  // schemacrawler-tools:
  //   Cycle 6  tools.command ↔ tools.registry  [2-way]
  //            CommandRegistry (command) extends BasePluginCommandRegistry (registry);
  //            BasePluginCommandRegistry/PluginCommandRegistry type params bound to CommandProvider
  // (command).
  //            Fix: move CommandProvider to tools.registry (task: cycle-fix-command-registry)
  //
  // Re-enable this test once the above cycles have been refactored away.
  @Disabled(
      "6 verified structural cycles remain — see comment above and"
          + " schemacrawler-verified-cycles.md")
  @Test
  public void architectureCycles() {
    slices()
        .matching("schemacrawler.(**)..")
        .as("SchemaCrawler core")
        .should()
        .beFreeOfCycles()
        .because("packages should have a clear, acyclic dependency structure")
        .check(classes);
  }

  @Test
  public void lookupMethods() {
    methods()
        .that()
        .haveNameMatching("lookup.*")
        .and()
        .arePublic()
        .should()
        .haveRawReturnType(Optional.class)
        .because("lookups may not return a value")
        .check(classes);
  }

  @Test
  public void notAccessStandardStreams() {
    noClasses()
        .that(
            resideOutsideOfPackages("schemacrawler.testdb")
                .and(are(not(simpleName("Version"))))
                .and(are(not(simpleName("GraphvizProcessExecutor")))))
        .should(ACCESS_STANDARD_STREAMS)
        .because("production code should not write to standard streams")
        .check(classes);
  }

  @Test
  public void notThrowGenericExceptions() {
    noClasses()
        .that(resideOutsideOfPackages("schemacrawler.testdb", "sf.util"))
        .should(THROW_GENERIC_EXCEPTIONS)
        .because(
            "SchemaCrawler defines it own exceptions, and wraps SQL exceptions with additional"
                + " information")
        .check(classes);
  }
}
