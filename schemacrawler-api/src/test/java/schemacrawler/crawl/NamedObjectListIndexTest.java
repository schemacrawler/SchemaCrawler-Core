/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.crawl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static us.fatehi.test.utility.TestObjectUtility.mockConnection;

import java.sql.Connection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaReference;
import schemacrawler.test.utility.crawl.LightTable;
import schemacrawler.utility.NamedObjectIndex;
import us.fatehi.utility.database.ConnectionInfoBuilder;

public class NamedObjectListIndexTest {

  @Test
  public void completeKeyOperationsReplaceAndRemoveActualObjects() {
    final NamedObjectList<Table> list = new NamedObjectList<>();
    final LightTable original = new LightTable("TABLE");
    final LightTable replacement = new LightTable("TABLE");
    final NamedObjectIndex<Table> index = list;

    assertThat(list.add(original), is(true));
    assertThat(list.add(replacement), is(true));
    assertThat(index.lookup(replacement.key()).orElseThrow(), sameInstance(replacement));
    assertThat(index.containsKey(replacement.key()), is(true));
    assertThat(index.size(), is(1));
    assertThat(list.remove(replacement).orElseThrow(), sameInstance(replacement));
    assertThat(list.remove(replacement), is(Optional.empty()));
    assertThat(index.isEmpty(), is(true));
  }

  @Test
  public void valuesAndIteratorAreDetachedUnmodifiableAndNaturallyRendered() {
    final NamedObjectList<Table> list = new NamedObjectList<>();
    final LightTable first = new LightTable("FIRST");
    final LightTable second = new LightTable("SECOND");
    list.add(first);

    final List<Table> values = list.values();
    final Iterator<Table> iterator = list.iterator();
    list.add(second);

    assertThat(values, contains(first));
    assertThat(iterator.next(), sameInstance(first));
    assertThat(iterator.hasNext(), is(false));
    assertThrows(UnsupportedOperationException.class, () -> values.clear());
    assertThrows(UnsupportedOperationException.class, iterator::remove);
    assertThat(list.values(), contains(first, second));
    assertThat(list.toString(), is("FIRST, SECOND"));
  }

  @Test
  public void schemaLookupDelegatesForBothSchemaAndNamedObjectParentTypes() {
    final SchemaReference schema = new SchemaReference("CATALOG", "PUBLIC");
    final MutableProcedure later = new MutableProcedure(schema, "run", "specific_z");
    final MutableProcedure earlier = new MutableProcedure(schema, "run", "specific_a");
    final NamedObjectList<MutableRoutine> routines = new NamedObjectList<>();
    routines.add(later);
    routines.add(earlier);
    final NamedObject schemaParent = schema;

    assertThat(routines.lookup(schema, "run").orElseThrow(), sameInstance(earlier));
    assertThat(routines.lookup(schemaParent, "run").orElseThrow(), sameInstance(earlier));
    assertThat(routines.lookup(earlier.key()).orElseThrow(), sameInstance(earlier));
    assertThat(routines.lookup(later.key()).orElseThrow(), sameInstance(later));
    assertThat(routines.lookup(schema, "specific_a"), is(Optional.empty()));
    assertThat(routines.lookup(schema.key().with("run")), is(Optional.empty()));

    final MutableProcedure nullSpecificName = new MutableProcedure(schema, "nullable", null);
    final MutableProcedure emptySpecificName = new MutableProcedure(schema, "nullable", "");
    routines.add(nullSpecificName);
    routines.add(emptySpecificName);
    assertThat(routines.lookup(schema, "nullable").isPresent(), is(true));
    assertThat(
        routines.lookup(nullSpecificName.key()).orElseThrow(), sameInstance(nullSpecificName));
    assertThat(
        routines.lookup(emptySpecificName.key()).orElseThrow(), sameInstance(emptySpecificName));
    assertThat(routines.lookup(schema.key().with("nullable")), is(Optional.empty()));

    assertThat(routines.lookup((Schema) null, "run"), is(Optional.empty()));
    assertThat(routines.lookup(schema, null), is(Optional.empty()));
  }

  @Test
  public void namedObjectListRoundTripRetainsLookupAndMutation() {
    final NamedObjectList<Table> list = new NamedObjectList<>();
    final LightTable table = new LightTable("TABLE");
    list.add(table);
    final NamedObjectList<Table> clonedList = SerializationUtils.clone(list);
    final LightTable addedTable = new LightTable("ADDED");

    assertThat(clonedList.lookup(table.key()).isPresent(), is(true));
    assertThat(clonedList.add(addedTable), is(true));
    assertThat(clonedList.values(), contains(addedTable, table));
  }

  @Test
  public void mutableCatalogRoutineLookupKeepsClientNullAndBlankBoundary() throws Exception {
    final MutableCatalog catalog = newCatalog();
    final SchemaReference schema = new SchemaReference("CATALOG", "PUBLIC");
    final MutableProcedure later = new MutableProcedure(schema, "run", "specific_z");
    final MutableProcedure earlier = new MutableProcedure(schema, "run", "specific_a");
    final MutableProcedure nullSpecificName = new MutableProcedure(schema, "nullable", null);
    final MutableProcedure emptySpecificName = new MutableProcedure(schema, "nullable", "");
    catalog.addRoutine(later);
    catalog.addRoutine(earlier);
    catalog.addRoutine(nullSpecificName);
    catalog.addRoutine(emptySpecificName);
    final MutableTable table = new MutableTable(schema, "TABLE");
    catalog.addTable(table);

    assertThat(catalog.lookupRoutine(schema, "run").orElseThrow(), sameInstance(earlier));
    assertThat(catalog.lookupRoutine(schema, "specific_a"), is(Optional.empty()));
    assertThat(catalog.lookupRoutine(schema, "nullable").isPresent(), is(true));
    assertThat(
        catalog.lookupRoutine(nullSpecificName.key()).orElseThrow(),
        sameInstance(nullSpecificName));
    assertThat(
        catalog.lookupRoutine(emptySpecificName.key()).orElseThrow(),
        sameInstance(emptySpecificName));
    assertThat(catalog.lookupRoutine(schema, null), is(Optional.empty()));
    assertThat(catalog.lookupRoutine(schema, " "), is(Optional.empty()));
    assertThat(catalog.lookupRoutine(earlier.key()).orElseThrow(), sameInstance(earlier));
    assertThat(catalog.lookupRoutine(later.key()).orElseThrow(), sameInstance(later));
    assertThrows(NullPointerException.class, () -> catalog.lookupRoutine(null, "run"));
    final Collection<Table> schemaTables = catalog.getTables(schema);
    schemaTables.clear();
    assertThat(catalog.getTables(schema), contains(table));
  }

  @Test
  public void emptyListKeepsLegacyEmptyState() {
    final NamedObjectList<NamedObject> list = new NamedObjectList<>();

    assertThat(list.values(), is(empty()));
    assertThat(list.toString(), is(""));
  }

  private MutableCatalog newCatalog() throws Exception {
    final Connection connection = mockConnection();
    final ConnectionInfoBuilder connectionInfoBuilder = ConnectionInfoBuilder.builder(connection);
    final MutableDatabaseInfo databaseInfo =
        new MutableDatabaseInfo(connectionInfoBuilder.buildDatabaseInformation());
    final MutableJdbcDriverInfo jdbcDriverInfo =
        new MutableJdbcDriverInfo(connectionInfoBuilder.buildJdbcDriverInformation());
    return new MutableCatalog("", databaseInfo, jdbcDriverInfo);
  }
}
