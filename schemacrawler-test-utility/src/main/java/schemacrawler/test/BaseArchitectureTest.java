/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_TESTS;
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
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.util.Optional;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(PER_CLASS)
public abstract class BaseArchitectureTest {

  protected JavaClasses classes;

  @BeforeAll
  public void findClasses() {
    final String description = "SchemaCrawler production classes";
    classes =
        new ClassFileImporter()
            .withImportOption(DO_NOT_INCLUDE_TESTS)
            .withImportOption(location -> !location.matches(Pattern.compile(".*[Tt]est.*")))
            .importPackages(classesSpecification())
            .as(description);
    assertThat(description + " classes not found", classes.isEmpty(), is(false));
  }

  abstract String classesSpecification();

  @Test
  public void lookupMethods() {
    final Optional<JavaMethod> anyMatchingMethod =
        classes.stream()
            .flatMap(c -> c.getMethods().stream())
            .filter(m -> m.getName().matches("lookup.*"))
            .filter(m -> m.getModifiers().contains(JavaModifier.PUBLIC))
            .findAny();
    assertThat(anyMatchingMethod.isPresent(), is(true));

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
        .that(exceptAllowedToUseStandardStreams())
        .should(ACCESS_STANDARD_STREAMS)
        .because("production code should not write to standard streams")
        .check(classes);
  }

  @Test
  public void notThrowGenericExceptions() {
    noClasses()
        .that(exceptAllowedToThrowGenericExceptions())
        .should(THROW_GENERIC_EXCEPTIONS)
        .because(
            "SchemaCrawler defines it own exceptions, and wraps SQL exceptions with additional"
                + " information")
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

  /**
   * Predicate to filter which classes are checked in {@link #notThrowGenericExceptions()}. Override
   * to exclude specific packages or class names. Default: all classes.
   */
  protected DescribedPredicate<JavaClass> exceptAllowedToThrowGenericExceptions() {
    return alwaysTrue();
  }

  /**
   * Predicate to filter which classes are checked in {@link #notAccessStandardStreams()}. Override
   * to exclude specific packages or class names. Default: all classes.
   */
  protected DescribedPredicate<JavaClass> exceptAllowedToUseStandardStreams() {
    return alwaysTrue();
  }
}
