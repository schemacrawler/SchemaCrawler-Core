/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackages;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleName;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import java.util.List;
import org.junit.jupiter.api.Test;
import schemacrawler.crawl.SchemaCrawler;
import schemacrawler.namedobject.BuildableIndex;
import schemacrawler.namedobject.NamedObjectIndex;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;

public class CoreArchitectureTest extends BaseArchitectureTest {

  @Test
  public void crawlClassesUseOnlyCrawlOptions() {
    noClasses()
        .that()
        .resideInAPackage("schemacrawler.crawl")
        .and()
        .doNotHaveFullyQualifiedName(SchemaCrawler.class.getName())
        .should()
        .dependOnClassesThat()
        .haveFullyQualifiedName(SchemaCrawlerOptions.class.getName())
        .because("full options belong only at the public crawl adapter")
        .check(classes);

    noFields()
        .that()
        .areDeclaredInClassesThat()
        .resideInAPackage("schemacrawler.crawl")
        .should()
        .haveRawType(SchemaCrawlerOptions.class)
        .because("crawl implementations must not retain full SchemaCrawlerOptions")
        .check(classes);
  }

  @Test
  public void crawlInternalSignaturesExcludeFullOptions() {
    final List<String> forbiddenMethods =
        classes.stream()
            .filter(javaClass -> javaClass.getPackageName().equals("schemacrawler.crawl"))
            .flatMap(javaClass -> javaClass.getMethods().stream())
            .filter(
                method ->
                    method.getRawReturnType().isEquivalentTo(SchemaCrawlerOptions.class)
                        || method.getRawParameterTypes().stream()
                            .anyMatch(type -> type.isEquivalentTo(SchemaCrawlerOptions.class)))
            .map(method -> method.getFullName())
            .toList();
    assertThat("Crawl methods must not accept or return full options", forbiddenMethods, empty());

    final List<String> forbiddenConstructors =
        classes.stream()
            .filter(javaClass -> javaClass.getPackageName().equals("schemacrawler.crawl"))
            .flatMap(javaClass -> javaClass.getConstructors().stream())
            .filter(constructor -> !constructor.getModifiers().contains(JavaModifier.PUBLIC))
            .filter(
                constructor ->
                    constructor.getRawParameterTypes().stream()
                        .anyMatch(type -> type.isEquivalentTo(SchemaCrawlerOptions.class)))
            .map(constructor -> constructor.getFullName())
            .toList();
    assertThat(
        "Only public crawl constructors may adapt full options", forbiddenConstructors, empty());
  }

  @Test
  public void namedObjectIndexHierarchyIsSealed() {
    final List<String> extensibleIndexes =
        classes.stream()
            .filter(
                javaClass ->
                    javaClass.isAssignableTo(NamedObjectIndex.class)
                        || javaClass.isAssignableTo(BuildableIndex.class))
            .filter(
                javaClass ->
                    !javaClass.reflect().isSealed()
                        && !javaClass.getModifiers().contains(JavaModifier.FINAL))
            .map(JavaClass::getName)
            .toList();
    assertThat("Index types must be sealed or final", extensibleIndexes, empty());
  }

  @Override
  protected String classesSpecification() {
    return "schemacrawler..";
  }

  @Override
  protected DescribedPredicate<JavaClass> exceptAllowedToThrowGenericExceptions() {
    return resideOutsideOfPackages("schemacrawler.testdb");
  }

  @Override
  protected DescribedPredicate<JavaClass> exceptAllowedToUseStandardStreams() {
    return resideOutsideOfPackages("schemacrawler.testdb")
        .and(are(not(simpleName("GraphvizProcessExecutor"))));
  }

  @Override
  protected DescribedPredicate<JavaClass> exceptAllowedToPrintStackTrace() {
    return resideOutsideOfPackages("schemacrawler.testdb");
  }
}
