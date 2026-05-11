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

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;

public class CoreArchitectureTest extends BaseArchitectureTest {

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
}
