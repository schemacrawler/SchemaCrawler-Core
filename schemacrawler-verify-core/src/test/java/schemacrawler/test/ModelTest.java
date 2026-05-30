/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_TESTS;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(PER_CLASS)
public class ModelTest {

  private static final String MODEL_IMPLEMENTATION =
      "schemacrawler.schemacrawler.ModelImplementation";
  private static final String RETRIEVER = "schemacrawler.schemacrawler.Retriever";

  protected JavaClasses classes;

  @BeforeAll
  public void findClasses() {
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
  // This prevents both module-path and classpath users from constructing or referencing
  // these internal classes.
  //
  // Java package-private visibility is strictly per-package. Splitting into subpackages would
  // require making these classes at least public — immediately exposing them.
  // Although schemacrawler.crawl is exported from the JPMS module (for MetadataResultSet and
  // ResultsCrawler access), the package-private Mutable* and @Retriever classes remain
  // inaccessible. These tests enforce the architectural boundary.
  @Test
  public void model() {

    noClasses()
        .that()
        .resideOutsideOfPackage("schemacrawler.crawl")
        .should()
        .dependOnClassesThat(
            resideInAPackage("schemacrawler.crawl").and(annotatedWith(MODEL_IMPLEMENTATION)))
        .because(
            """
            @ModelImplementation classes in schemacrawler.crawl are package-private internal
              schema model implementations; they must only be used within schemacrawler.crawl
              to prevent classpath clients from constructing schema model objects directly
            """)
        .check(classes);

    noClasses()
        .that()
        .resideOutsideOfPackage("schemacrawler.ermodel.implementation")
        .should()
        .dependOnClassesThat(
            resideInAPackage("schemacrawler.ermodel.implementation")
                .and(annotatedWith(MODEL_IMPLEMENTATION)))
        .because(
            """
            @ModelImplementation classes in schemacrawler.ermodel.implementation are internal
              ER model implementations; they must only be used within that package
            """)
        .check(classes);

    noClasses()
        .that()
        .resideOutsideOfPackages(
            "schemacrawler.loader.catalog.model", "schemacrawler.loader.ermodel.attributes")
        .should()
        .dependOnClassesThat(
            resideInAPackage("schemacrawler.loader.catalog.model")
                .and(annotatedWith(MODEL_IMPLEMENTATION)))
        .because(
            """
            @ModelImplementation classes in schemacrawler.loader.catalog.model are internal YAML
              deserialization DTOs; only AttributesLoader in ermodel.attributes (the designated
              processor) is permitted to reference them outside the model package
            """)
        .check(classes);

    noClasses()
        .that()
        .resideOutsideOfPackage("schemacrawler.crawl")
        .should()
        .dependOnClassesThat(annotatedWith(RETRIEVER))
        .because(
            """
            @Retriever classes in schemacrawler.crawl are package-private JDBC metadata
              extractors; they must only be used within schemacrawler.crawl
            """)
        .check(classes);

    classes()
        .that()
        .areAnnotatedWith(MODEL_IMPLEMENTATION)
        .should()
        .notBePublic()
        .because(
            """
            @ModelImplementation classes are package-private internal implementations;
              declaring them public exposes them to classpath clients
            """)
        .check(classes);

    classes()
        .that()
        .areAnnotatedWith(RETRIEVER)
        .should()
        .notBePublic()
        .because(
            """
            @Retriever classes are package-private JDBC metadata extractors;
              declaring them public exposes them to classpath clients
            """)
        .check(classes);
  }

  @Test
  public void packageCycles() {
    slices()
        .matching("schemacrawler.(**)..")
        .as("SchemaCrawler production classes")
        .should()
        .beFreeOfCycles()
        .because("packages should have a clear, acyclic dependency structure")
        .check(classes);
  }
}
