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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(PER_CLASS)
public abstract class BaseNoReflectionTest {

  protected JavaClasses classes;

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

  @Test
  public void noClassLoading() {
    noClasses()
        .that(
            are(not(simpleName("BasePluginCommandRegistry")))
                .and(are(not(simpleName("MutableColumnDataType")))))
        .should()
        .callMethod(Class.class, "forName", String.class)
        .orShould()
        .callMethod(Class.class, "forName", String.class, boolean.class, ClassLoader.class)
        .orShould()
        .callMethod(Class.class, "getDeclaredConstructors")
        .orShould()
        .callMethod(Class.class, "getDeclaredConstructor", Class[].class)
        .orShould()
        .callMethod(Class.class, "getConstructor", Class[].class)
        .orShould()
        .callMethod(Constructor.class, "newInstance")
        .because("avoid reflective class loading and construction")
        .check(classes);
  }

  @Test
  public void noFieldAccess() {
    noClasses()
        .should()
        .callMethod(Class.class, "getField", String.class)
        .orShould()
        .callMethod(Class.class, "getDeclaredField", String.class)
        .orShould()
        .callMethod(Class.class, "getFields")
        .orShould()
        .callMethod(Class.class, "getDeclaredFields")
        .orShould()
        .callMethod(Field.class, "get", Object.class)
        .orShould()
        .callMethod(Field.class, "set", Object.class, Object.class)
        .because("avoid reflective field access")
        .check(classes);
  }

  @Test
  public void noFieldAccessOverride() {
    noClasses()
        .should()
        .callMethod(AccessibleObject.class, "setAccessible", boolean.class)
        .orShould()
        .callMethod(
            AccessibleObject.class, "setAccessible", AccessibleObject[].class, boolean.class)
        .because("avoid reflective field/method/constructor access override")
        .check(classes);
  }

  @Test
  public void noMethodAccess() {
    noClasses()
        .that(are(not(simpleName("DatabaseInfoRetriever"))))
        .should()
        .callMethod(Class.class, "getMethod", String.class, Class[].class)
        .orShould()
        .callMethod(Class.class, "getDeclaredMethod", String.class, Class[].class)
        .orShould()
        .callMethod(Class.class, "getMethods")
        .orShould()
        .callMethod(Class.class, "getDeclaredMethods")
        .orShould()
        .callMethod(Method.class, "invoke", Object.class, Object[].class)
        .because("avoid reflective method access and invocation")
        .check(classes);
  }
}
