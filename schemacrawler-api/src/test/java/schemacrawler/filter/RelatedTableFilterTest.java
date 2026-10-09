/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import schemacrawler.schema.PartialDatabaseObject;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableRelationshipType;
import schemacrawler.schemacrawler.SchemaReference;
import schemacrawler.test.utility.crawl.LightTable;

class RelatedTableFilterTest {

  @Test
  void expandsIndependentlyWithinBaselineAndSkipsPartialTables() {
    final RelatedTable seed = new RelatedTable("seed");
    final RelatedTable parent = new RelatedTable("parent");
    final RelatedTable grandparent = new RelatedTable("grandparent");
    final RelatedTable child = new RelatedTable("child");
    final RelatedTable grandchild = new RelatedTable("grandchild");
    final PartialRelatedTable partial = new PartialRelatedTable("partial");
    final RelatedTable outsideBaseline = new RelatedTable("outside_baseline");
    seed.setRelatedTables(TableRelationshipType.parent, List.of(parent, partial, outsideBaseline));
    seed.setRelatedTables(TableRelationshipType.child, List.of(child));
    parent.setRelatedTables(TableRelationshipType.parent, List.of(seed, grandparent));
    child.setRelatedTables(TableRelationshipType.child, List.of(grandchild));

    final List<Table> eligibleTables =
        List.of(seed, parent, grandparent, child, grandchild, partial);
    final NamedObjectFilter<Table> relatedTableFilter =
        new RelatedTableFilter(eligibleTables, List.of(seed), 2, 1);
    final List<Table> selected = eligibleTables.stream().filter(relatedTableFilter).toList();

    assertThat(selected, containsInAnyOrder(seed, parent, grandparent, child));
  }

  private static class RelatedTable extends LightTable {

    private final Map<TableRelationshipType, List<Table>> relatedTables =
        new EnumMap<>(TableRelationshipType.class);

    RelatedTable(final String name) {
      super(new SchemaReference("CATALOG", "PUBLIC"), name);
    }

    @Override
    public Collection<Table> getRelatedTables(final TableRelationshipType relationshipType) {
      return relatedTables.getOrDefault(relationshipType, List.of());
    }

    void setRelatedTables(
        final TableRelationshipType relationshipType, final List<Table> relatedTables) {
      this.relatedTables.put(relationshipType, List.copyOf(relatedTables));
    }
  }

  private static final class PartialRelatedTable extends RelatedTable
      implements PartialDatabaseObject {

    PartialRelatedTable(final String name) {
      super(name);
    }
  }
}
