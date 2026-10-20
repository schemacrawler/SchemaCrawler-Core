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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static us.fatehi.test.utility.TestObjectUtility.mockConnection;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;
import schemacrawler.namedobject.BuildableDatabaseObjectIndex;
import schemacrawler.namedobject.BuildableDependantObjectIndex;
import schemacrawler.namedobject.ConcurrentDatabaseObjectIndex;
import schemacrawler.namedobject.NamedObjectIndex;
import schemacrawler.schema.Column;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Schema;
import schemacrawler.schema.TableRelationshipType;
import schemacrawler.schemacrawler.SchemaReference;
import us.fatehi.utility.database.ConnectionInfoBuilder;

public class CrawlObjectIndexTest {

  @Test
  public void routineIndexDispatchesSchemaParentsAndPreservesSpecificKeys() {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final MutableProcedure later = new MutableProcedure(schema, "run", "specific_z");
    final MutableProcedure earlier = new MutableProcedure(schema, "run", "specific_a");
    final BuildableDependantObjectIndex<MutableRoutine> routines =
        new ConcurrentDatabaseObjectIndex<>();
    routines.add(later);
    routines.add(earlier);
    final NamedObject schemaParent = schema;

    assertThat(routines.lookup(schema, "run").orElseThrow(), sameInstance(earlier));
    assertThat(routines.lookup(schemaParent, "run").orElseThrow(), sameInstance(earlier));
    assertThat(routines.getObjects(schema, "run"), contains(earlier, later));
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
  public void compositeIndexesPreserveParentLookupAndMembershipContracts() {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final MutableTable parent = new MutableTable(schema, "TABLE");
    final MutableTable otherParent = new MutableTable(schema, "OTHER");
    final MutableColumn first = new MutableColumn(parent, "FIRST");
    final MutableColumn second = new MutableColumn(parent, "SECOND");
    first.setOrdinalPosition(1);
    second.setOrdinalPosition(2);
    final BuildableDependantObjectIndex<MutableColumn> children =
        new ConcurrentDatabaseObjectIndex<>();
    final BuildableDatabaseObjectIndex<MutableColumn> databaseObjects = children;
    assertThat(children.add(null), is(false));
    assertThat(children.remove(null), is(Optional.empty()));
    databaseObjects.add(second);
    children.add(first);
    final List<MutableColumn> before = children.getObjects();
    assertThat(before, contains(first, second));
    assertThat(children.lookup(parent, "FIRST").orElseThrow(), sameInstance(first));
    assertThat(children.lookup(otherParent, "FIRST"), is(Optional.empty()));
    assertThat(children.lookup((NamedObject) null, "FIRST"), is(Optional.empty()));
    assertThat(children.lookup(parent, null), is(Optional.empty()));
    assertThat(databaseObjects.lookup(schema, "FIRST").orElseThrow(), sameInstance(first));
    final MutableColumn replacement = new MutableColumn(parent, "FIRST");
    replacement.setOrdinalPosition(3);
    databaseObjects.add(replacement);
    assertThat(children.lookup(first.key()).orElseThrow(), sameInstance(replacement));
    assertThat(children.lookup(parent, "FIRST").orElseThrow(), sameInstance(replacement));
    assertThat(children.getObjects(), contains(second, replacement));
    assertThat(before, contains(first, second));
    assertThrows(UnsupportedOperationException.class, before::clear);
    assertThat(children.remove(first).orElseThrow(), sameInstance(replacement));
    assertThat(databaseObjects.lookup(first.key()), is(Optional.empty()));
    assertThat(children.getObjects(), contains(second));
  }

  @Test
  public void catalogRoutineLookupKeepsClientNullAndBlankBoundary() throws Exception {
    final MutableCatalog catalog = newCatalog();
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
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
    assertThat(catalog.getRoutines(schema, "run"), contains(earlier, later));
    assertThat(catalog.lookupRoutine(schema, "specific_a"), is(Optional.empty()));
    assertThat(catalog.lookupRoutine(schema, "nullable").isPresent(), is(true));
    assertThat(
        catalog.lookupRoutine(nullSpecificName.key()).orElseThrow(),
        sameInstance(nullSpecificName));
    assertThat(
        catalog.lookupRoutine(emptySpecificName.key()).orElseThrow(),
        sameInstance(emptySpecificName));
    assertThat(catalog.lookupRoutine(schema, null), is(Optional.empty()));
    assertThat(catalog.lookupRoutine(schema, ""), is(Optional.empty()));
    assertThat(catalog.lookupRoutine(schema, " "), is(Optional.empty()));
    assertThat(catalog.lookupRoutine(earlier.key()).orElseThrow(), sameInstance(earlier));
    assertThat(catalog.lookupRoutine(later.key()).orElseThrow(), sameInstance(later));
    assertThat(catalog.lookupRoutine(null, "run"), is(Optional.empty()));
    assertThat(catalog.getTables(schema), contains(table));
  }

  @Test
  public void tableColumnsReplaceByKeyAndExposeDetachedSortedMembership() {
    final MutableTable table = new MutableTable(new SchemaReference(), "TABLE");
    final MutableColumn first = new MutableColumn(table, "FIRST");
    final MutableColumn second = new MutableColumn(table, "SECOND");
    first.setOrdinalPosition(1);
    second.setOrdinalPosition(2);
    table.addColumn(second);
    table.addColumn(first);
    final NamedObjectIndex<MutableColumn> index = table.getAllColumns();
    final List<Column> previous = table.getColumns();
    final MutableColumn replacement = new MutableColumn(table, "FIRST");
    replacement.setOrdinalPosition(3);
    table.addColumn(replacement);
    final MutableColumn hidden = new MutableColumn(table, "HIDDEN");
    table.addHiddenColumn(hidden);

    assertThat(previous, contains(sameInstance(first), sameInstance(second)));
    assertThat(table.getColumns(), contains(sameInstance(second), sameInstance(replacement)));
    assertThat(index.lookup(first.key()).orElseThrow(), sameInstance(replacement));
    assertThat(table.lookupColumn("FIRST").orElseThrow(), sameInstance(replacement));
    assertThat(table.lookupColumn("HIDDEN").orElseThrow(), sameInstance(hidden));
    assertThat(table.lookupColumn(null), is(Optional.empty()));
    assertThrows(UnsupportedOperationException.class, previous::clear);
  }

  @Test
  public void foreignKeyFilteringKeepsMutableWorkingCopiesAndGraphOrdering() throws Exception {
    final Schema schema = new SchemaReference();
    final MutableTable parent = new MutableTable(schema, "Z_PARENT");
    final MutableTable middle = new MutableTable(schema, "M_MIDDLE");
    final MutableTable child = new MutableTable(schema, "A_CHILD");
    final MutableForeignKey imported = foreignKey("IMPORTED", middle, parent);
    final MutableForeignKey exported = foreignKey("EXPORTED", child, middle);
    middle.addForeignKey(exported);
    middle.addForeignKey(imported);
    parent.addForeignKey(imported);
    child.addForeignKey(exported);

    final Collection<ForeignKey> importedCopy = middle.getImportedForeignKeys();
    assertThat(importedCopy, contains(sameInstance(imported)));
    importedCopy.clear();
    assertThat(middle.getImportedForeignKeys(), contains(sameInstance(imported)));
    assertThat(middle.getExportedForeignKeys(), contains(sameInstance(exported)));
    assertThat(middle.getForeignKeys(), contains(imported, exported));
    assertThat(middle.getRelatedTables(TableRelationshipType.parent), contains(parent));
    assertThat(middle.getRelatedTables(TableRelationshipType.child), contains(child));

    final MutableCatalog catalog = newCatalog();
    catalog.addTable(child);
    catalog.addTable(middle);
    catalog.addTable(parent);
    final NamedObjectIndex<MutableTable> tables = catalog.getAllTables();
    new TablesGraph(tables).setTablesSortIndexes();
    assertThat(tables.getObjects(), contains(parent, middle, child));
  }

  @Test
  public void catalogRoundTripRetainsNestedIndexLookupsAndMutation() throws Exception {
    final MutableCatalog catalog = newCatalog();
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    catalog.addSchema(schema);
    final MutableTable table = new MutableTable(schema, "TABLE");
    table.addColumn(new MutableColumn(table, "COLUMN"));
    catalog.addTable(table);
    final MutableProcedure routine = new MutableProcedure(schema, "run", "specific");
    routine.addParameter(new MutableProcedureParameter(routine, "PARAMETER"));
    catalog.addRoutine(routine);

    final MutableCatalog copy = SerializationUtils.clone(catalog);
    final MutableTable copiedTable = copy.lookupTable(schema, "TABLE").orElseThrow();
    assertThat(copiedTable.lookupColumn("COLUMN").isPresent(), is(true));
    final MutableColumn addedColumn = new MutableColumn(copiedTable, "ADDED");
    copiedTable.addColumn(addedColumn);
    assertThat(copiedTable.lookupColumn("ADDED").orElseThrow(), sameInstance(addedColumn));
    final MutableProcedure copiedRoutine =
        (MutableProcedure) copy.lookupRoutine(routine.key()).orElseThrow();
    assertThat(copiedRoutine.lookupParameter("PARAMETER").isPresent(), is(true));
    final MutableProcedureParameter addedParameter =
        new MutableProcedureParameter(copiedRoutine, "ADDED");
    copiedRoutine.addParameter(addedParameter);
    assertThat(copiedRoutine.lookupParameter("ADDED").orElseThrow(), sameInstance(addedParameter));
    final MutableTable addedTable = new MutableTable(schema, "ADDED");
    copy.addTable(addedTable);
    assertThat(
        copy.getAllTables().lookup(addedTable.key()).orElseThrow(), sameInstance(addedTable));
    assertThat(copy.getTables(), contains(addedTable, copiedTable));
    assertThat(copy.lookupTable(schema, "ADDED").orElseThrow(), sameInstance(addedTable));
    final Schema addedSchema = new SchemaReference("CATALOG", "ADDED");
    copy.addSchema(addedSchema);
    assertThat(
        copy.lookupSchema(addedSchema.getFullName()).orElseThrow(), sameInstance(addedSchema));
    assertThat(copy.getSchemas(), contains(addedSchema, schema));
    final MutableProcedure addedRoutine = new MutableProcedure(schema, "added", "specific");
    copy.addRoutine(addedRoutine);
    assertThat(copy.getRoutines(schema), contains(addedRoutine, copiedRoutine));
    assertThat(copy.lookupRoutine(schema, "added").orElseThrow(), sameInstance(addedRoutine));
    assertThat(catalog.lookupTable(schema, "ADDED"), is(Optional.empty()));
  }

  private static MutableForeignKey foreignKey(
      final String name, final MutableTable child, final MutableTable parent) {
    final MutableColumn childColumn = new MutableColumn(child, "COLUMN");
    final MutableColumn parentColumn = new MutableColumn(parent, "COLUMN");
    child.addColumn(childColumn);
    parent.addColumn(parentColumn);
    return new MutableForeignKey(name, new ImmutableColumnReference(1, childColumn, parentColumn));
  }

  private static MutableCatalog newCatalog() throws Exception {
    final Connection connection = mockConnection();
    final ConnectionInfoBuilder connectionInfoBuilder = ConnectionInfoBuilder.builder(connection);
    final MutableDatabaseInfo databaseInfo =
        new MutableDatabaseInfo(connectionInfoBuilder.buildDatabaseInformation());
    final MutableJdbcDriverInfo jdbcDriverInfo =
        new MutableJdbcDriverInfo(connectionInfoBuilder.buildJdbcDriverInformation());
    return new MutableCatalog("", databaseInfo, jdbcDriverInfo);
  }
}
