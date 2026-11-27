/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.state;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static schemacrawler.test.utility.crawl.LightCatalogUtility.lightCatalog;

import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Table;
import schemacrawler.test.utility.crawl.LightTable;

public class AbstractExecutionStateTest {

  private static final class TestExecutionState extends AbstractExecutionState {}

  @Test
  public void tableVisibilityPredicateTracksCatalogReplacement() {
    final TestExecutionState state = new TestExecutionState();
    final Table firstTable = table("first");
    final Table secondTable = table("second");
    state.setCatalog(catalogWithTables(firstTable));
    final Predicate<Table> firstPredicate = state.getTableVisibilityPredicate();

    assertThat(firstPredicate.test(firstTable), is(true));
    assertThat(firstPredicate.test(secondTable), is(false));
    assertThat(firstPredicate.test(null), is(false));

    state.setCatalog(catalogWithTables(secondTable));
    final Predicate<Table> replacementPredicate = state.getTableVisibilityPredicate();

    assertThat(replacementPredicate.test(firstTable), is(false));
    assertThat(replacementPredicate.test(secondTable), is(true));
    assertThat(firstPredicate.test(firstTable), is(true));
  }

  private Catalog catalogWithTables(final Table... tables) {
    return lightCatalog(tables);
  }

  private Table table(final String name) {
    return new LightTable(name);
  }
}
