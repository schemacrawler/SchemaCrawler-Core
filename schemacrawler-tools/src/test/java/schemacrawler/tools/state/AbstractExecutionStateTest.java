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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Table;

public class AbstractExecutionStateTest {

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
    final Catalog catalog = mock(Catalog.class);
    when(catalog.getTables()).thenReturn(List.of(tables));
    return catalog;
  }

  private Table table(final String name) {
    final Table table = mock(Table.class);
    when(table.key()).thenReturn(new NamedObjectKey(name));
    return table;
  }

  private static final class TestExecutionState extends AbstractExecutionState {}
}
