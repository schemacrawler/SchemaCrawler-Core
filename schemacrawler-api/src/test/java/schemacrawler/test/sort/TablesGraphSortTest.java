/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.sort;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static schemacrawler.crawl.SortTestUtility.addForeignKey;
import static schemacrawler.crawl.SortTestUtility.newForeignKey;
import static schemacrawler.crawl.SortTestUtility.newTable;
import static schemacrawler.crawl.SortTestUtility.newView;
import static schemacrawler.crawl.SortTestUtility.sortTablesByGraph;

import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableRelationshipType;
import schemacrawler.schema.View;
import schemacrawler.schemacrawler.SchemaReference;

public class TablesGraphSortTest {

  @Test
  public void foreignKeyGroupsAndRelatedTablesKeepTheirExplicitOrdering() {
    final Schema schema = new SchemaReference();
    final Table center = newTable(schema, "CENTER");
    final Table parentA = newTable(schema, "A_PARENT");
    final Table parentZ = newTable(schema, "Z_PARENT");
    final Table childA = newTable(schema, "A_CHILD");
    final Table childZ = newTable(schema, "Z_CHILD");
    final ForeignKey importedA = newForeignKey("A_IMPORTED", center, parentA, "A_COLUMN");
    final ForeignKey importedZ = newForeignKey("Z_IMPORTED", center, parentZ, "Z_COLUMN");
    final ForeignKey exportedA = newForeignKey("A_EXPORTED", childA, center);
    final ForeignKey exportedZ = newForeignKey("Z_EXPORTED", childZ, center);
    final ForeignKey duplicateParent = newForeignKey("B_IMPORTED", center, parentA, "B_COLUMN");
    for (final ForeignKey foreignKey :
        List.of(exportedZ, importedZ, exportedA, duplicateParent, importedA)) {
      addForeignKey(center, foreignKey);
    }

    assertThat(center.getImportedForeignKeys(), contains(importedA, duplicateParent, importedZ));
    assertThat(center.getExportedForeignKeys(), contains(exportedA, exportedZ));
    assertThat(
        center.getForeignKeys(),
        contains(importedA, duplicateParent, importedZ, exportedA, exportedZ));
    assertThat(center.getRelatedTables(TableRelationshipType.parent), contains(parentA, parentZ));
    assertThat(center.getRelatedTables(TableRelationshipType.child), contains(childA, childZ));
    final Collection<ForeignKey> exportedCopy = center.getExportedForeignKeys();
    exportedCopy.clear();
    final Collection<ForeignKey> allCopy = center.getForeignKeys();
    allCopy.clear();
    assertThat(center.getExportedForeignKeys(), contains(exportedA, exportedZ));
    assertThat(center.getForeignKeys().size(), is(5));
  }

  @Test
  public void graphOrdersSameLevelAndDisconnectedTablesBeforeViews() {
    final Schema schema = new SchemaReference();
    final Table root = newTable(schema, "Z_ROOT");
    final Table childA = newTable(schema, "A_CHILD");
    final Table childB = newTable(schema, "B_CHILD");
    final Table disconnected = newTable(schema, "M_FREE");
    final View viewA = newView(schema, "A_VIEW");
    final View viewZ = newView(schema, "Z_VIEW");
    addForeignKey(childA, newForeignKey("FK_A", childA, root));
    addForeignKey(childB, newForeignKey("FK_B", childB, root));

    assertThat(
        sortTablesByGraph(List.of(viewZ, childB, root, viewA, childA, disconnected)),
        contains(disconnected, root, childA, childB, viewA, viewZ));
  }
}
