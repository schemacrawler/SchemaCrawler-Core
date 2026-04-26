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

  // The following 10 structural cycles were verified by running architectureCycles() and
  // capturing the ArchUnit output. Each entry shows the cycle path and its root cause.
  //
  // schemacrawler-api cycles:
  //   Cycle 1  crawl → utility → schemacrawler → crawl
  //            crawl uses JavaSqlTypes/TypeMap/MetaDataUtility/NamedObjectSort from utility;
  //            utility.MetaDataUtility.reduceCatalog() accepts SchemaCrawlerOptions;
  //            schemacrawler.MetadataResultSet uses ResultsCrawler from crawl
  //
  //   Cycle 2  filter → schemacrawler → utility → filter
  //            filter classes accept SchemaCrawlerOptions/LimitOptions/GrepOptions;
  //            schemacrawler uses TypeMap/NamedObjectSort/BinaryData/MetaDataUtility from utility;
  //            utility.MetaDataUtility.reduceCatalog() calls ReducerFactory from filter
  //
  //   Cycle 3  filter → utility → filter
  //            filter.TablesReducer.includeRelatedTables() calls MetaDataUtility.isPartial();
  //            utility.MetaDataUtility.reduceCatalog() calls ReducerFactory from filter
  //
  //   Cycle 4  schemacrawler → utility → schemacrawler
  //            SchemaRetrievalOptions/Builder, QueryUtility, MetadataResultSet use TypeMap,
  //            NamedObjectSort, BinaryData, MetaDataUtility from utility;
  //            utility.MetaDataUtility.reduceCatalog() accepts SchemaCrawlerOptions
  //
  // schemacrawler-ermodel cycles:
  //   Cycle 5  ermodel.implementation → ermodel.utility → ermodel.implementation
  //            MutableEntityAttribute calls ERModelUtility.inferEntityAttributeType();
  //            ERModelUtility uses ERModelBuilder and TableEntityModelInferrer from implementation
  //
  // schemacrawler-loader cycles:
  //   Cycle 6  loader.catalog → loader.catalog.counts → loader.catalog
  //            CatalogLoaderRegistry instantiates TableRowCountsLoaderProvider;
  //            counts subpackage extends AbstractCatalogLoader/AbstractCatalogLoaderProvider
  //
  //   Cycle 7  loader.catalog → loader.catalog.offline → loader.catalog
  //            CatalogLoaderRegistry instantiates OfflineCatalogLoaderProvider;
  //            offline subpackage extends AbstractCatalogLoader/AbstractCatalogLoaderProvider
  //
  //   Cycle 8  loader.catalog → tools.utility → loader.catalog
  //            ChainedCatalogLoader calls ExecutionStateUtility.transferState();
  //            SchemaCrawlerUtility.getCatalog() uses CatalogLoaderRegistry
  //
  //   Cycle 9  loader.ermodel → loader.ermodel.implicitassociations → loader.ermodel
  //            ERModelLoaderRegistry instantiates ImplicitAssociationsLoaderProvider;
  //            implicitassociations subpackage extends
  // AbstractERModelLoader/AbstractERModelLoaderProvider
  //
  // schemacrawler-tools cycles:
  //   Cycle 10 tools.command → tools.registry → tools.command
  //            CommandRegistry extends BasePluginCommandRegistry (from registry);
  //            BasePluginCommandRegistry/PluginCommandRegistry use CommandProvider (from command)
  //
  // Re-enable this test once the above cycles have been refactored away.
  @Disabled("10 verified structural cycles remain — see comment above")
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
