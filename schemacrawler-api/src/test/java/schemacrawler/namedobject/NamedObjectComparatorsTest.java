/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.namedobject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.DatabaseUser;
import schemacrawler.schema.Function;
import schemacrawler.schema.Index;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Procedure;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Sequence;
import schemacrawler.schema.Synonym;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableType;
import schemacrawler.schemacrawler.SchemaReference;

public class NamedObjectComparatorsTest {

  @Test
  public void compareFamilyOrdersSimpleDatabaseObjectTypes() {
    final Catalog catalog = mock(Catalog.class);
    final Schema schema = new SchemaReference("catalog", "schema");
    final Table table = table("TABLE");
    final Table view = table("VIEW");
    final Procedure procedure = mock(Procedure.class);
    final Function function = mock(Function.class);
    final Index index = mock(Index.class);
    final Sequence sequence = mock(Sequence.class);
    final Synonym synonym = mock(Synonym.class);
    final DatabaseObject unknown = mock(DatabaseObject.class);
    final DatabaseUser user = mock(DatabaseUser.class);
    final NamedObject other = mock(NamedObject.class);

    final List<NamedObject> orderedObjects =
        List.of(
            catalog, schema, table, view, procedure, function, index, sequence, synonym, unknown,
            user, other);
    for (int firstIndex = 0; firstIndex < orderedObjects.size(); firstIndex++) {
      for (int secondIndex = firstIndex + 1; secondIndex < orderedObjects.size(); secondIndex++) {
        assertBefore(orderedObjects.get(firstIndex), orderedObjects.get(secondIndex));
      }
    }
  }

  @Test
  public void schemaOrderingPrecedesDatabaseObjectCategory() {
    final Schema firstSchema = new SchemaReference("catalog", "A_SCHEMA");
    final Schema secondSchema = new SchemaReference("catalog", "Z_SCHEMA");
    final Table firstTable = table("TABLE");
    final Table firstView = table("VIEW");
    final Synonym firstSynonym = mock(Synonym.class);
    final Table secondTable = table("TABLE");
    when(firstTable.getSchema()).thenReturn(firstSchema);
    when(firstView.getSchema()).thenReturn(firstSchema);
    when(firstSynonym.getSchema()).thenReturn(firstSchema);
    when(secondTable.getSchema()).thenReturn(secondSchema);

    final List<NamedObject> orderedObjects =
        List.of(firstTable, firstView, firstSynonym, secondTable);
    for (int firstIndex = 0; firstIndex < orderedObjects.size(); firstIndex++) {
      for (int secondIndex = firstIndex + 1; secondIndex < orderedObjects.size(); secondIndex++) {
        assertBefore(orderedObjects.get(firstIndex), orderedObjects.get(secondIndex));
      }
    }
  }

  @Test
  public void compareNameThenKeyUsesExactKeyAfterNormalizedName() {
    final NamedObject first = mock(NamedObject.class);
    when(first.getFullName()).thenReturn("a-b");
    when(first.key()).thenReturn(new NamedObjectKey("a", "b"));
    final NamedObject second = mock(NamedObject.class);
    when(second.getFullName()).thenReturn("a b");
    when(second.key()).thenReturn(new NamedObjectKey("a", "c"));

    final Comparator<NamedObject> comparator =
        NamedObjectComparators.NORMALIZED_NAME_COMPARATOR.thenComparing(
            NamedObjectComparators.EXACT_KEY_COMPARATOR);

    assertThat(comparator.compare(first, second), lessThan(0));
    assertThat(comparator.compare(second, first), greaterThan(0));
  }

  private void assertBefore(final NamedObject first, final NamedObject second) {
    final Comparator<NamedObject> comparator =
        NamedObjectComparators.DATABASE_OBJECT_ORDER_COMPARATOR;

    assertThat(comparator.compare(first, second), lessThan(0));
    assertThat(comparator.compare(second, first), greaterThan(0));
  }

  private Table table(final String type) {
    final Table table = mock(Table.class);
    when(table.getTableType()).thenReturn(new TableType(type));
    return table;
  }
}
