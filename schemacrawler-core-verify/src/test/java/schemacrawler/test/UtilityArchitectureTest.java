/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleName;
import static com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_TESTS;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.GeneralCodingRules.ACCESS_STANDARD_STREAMS;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(PER_CLASS)
public class UtilityArchitectureTest {

  private JavaClasses classes;

  @BeforeAll
  public void _classes() {
    final String description = "Utility classes";
    classes =
        new ClassFileImporter()
            .withImportOption(DO_NOT_INCLUDE_TESTS)
            .withImportOption(location -> !location.matches(Pattern.compile(".*[Tt]est.*")))
            .importPackages("us.fatehi.utility..")
            .as(description);
    assertThat(description + " classes not found", classes.isEmpty(), is(false));
  }

  @Test
  public void notAccessStandardStreams() {
    noClasses()
        .that(are(not(simpleName("SqlScript"))).and(are(not(simpleName("ConsoleOutputResource")))))
        .should(ACCESS_STANDARD_STREAMS)
        .because("production code should not write to standard streams")
        .check(classes);
  }

  @Test
  public void packageCycles() {
    slices()
        .matching("us.fatehi.utility.(**)..")
        .as("Utility classes")
        .should()
        .beFreeOfCycles()
        .because("packages should have a clear, acyclic dependency structure")
        .check(classes);
  }

  @Test
  public void reflectiveAccessOverride() {
    noClasses()
        .that(are(not(simpleName("ObjectToString"))))
        .should()
        .callMethod(AccessibleObject.class, "setAccessible", boolean.class)
        .orShould()
        .callMethod(
            AccessibleObject.class, "setAccessible", AccessibleObject[].class, boolean.class)
        .because("avoid reflective access override")
        .check(classes);
  }

  @Test
  public void reflectiveClassLoading() {
    noClasses()
        .should()
        .callMethod(Class.class, "forName", String.class)
        .orShould()
        .callMethod(Class.class, "getDeclaredConstructors")
        .orShould()
        .callMethod(Class.class, "getDeclaredConstructor", Class[].class)
        .orShould()
        .callMethod(Class.class, "getConstructor", Class[].class)
        .orShould()
        .callMethod(Constructor.class, "newInstance")
        .because("avoid reflective class loading")
        .check(classes);
  }
}
