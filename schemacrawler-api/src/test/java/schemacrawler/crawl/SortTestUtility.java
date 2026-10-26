/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.crawl;

import java.util.Collection;
import java.util.List;
import schemacrawler.namedobject.BuildableDatabaseObjectIndex;
import schemacrawler.namedobject.NamedObjectIndexes;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.Grant;
import schemacrawler.schema.Index;
import schemacrawler.schema.IndexColumn;
import schemacrawler.schema.IndexType;
import schemacrawler.schema.Procedure;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableReference;
import schemacrawler.schema.TableType;
import schemacrawler.schema.View;
import us.fatehi.utility.UtilityMarker;
import us.fatehi.utility.property.Property;

/** Test-only access to production model objects, exposed through their public interfaces. */
@UtilityMarker
public final class SortTestUtility {

  public static void addForeignKey(final Table table, final ForeignKey foreignKey) {
    ((MutableTable) table).addForeignKey((MutableForeignKey) foreignKey);
  }

  public static Column newColumn(final Table table, final String name) {
    return new MutableColumn(table, name);
  }

  public static ColumnReference newColumnReference(
      final int sequence, final Column foreign, final Column primary) {
    return new ImmutableColumnReference(sequence, foreign, primary);
  }

  public static DatabaseObject newDatabaseObject(final Schema schema, final String name) {
    return new MutableSequence(schema, name);
  }

  public static Property newDatabaseProperty(final String name, final Object value) {
    return new ImmutableDatabaseProperty(name, value);
  }

  public static ForeignKey newForeignKey(final String name, final Table child, final Table parent) {
    return newForeignKey(name, child, parent, "COLUMN");
  }

  public static ForeignKey newForeignKey(
      final String name, final Table child, final Table parent, final String columnName) {
    final MutableColumn childColumn = new MutableColumn(child, columnName);
    final MutableColumn parentColumn = new MutableColumn(parent, columnName);
    ((MutableTable) child).addColumn(childColumn);
    ((MutableTable) parent).addColumn(parentColumn);
    return new MutableForeignKey(name, new ImmutableColumnReference(1, childColumn, parentColumn));
  }

  public static Grant<Table> newGrant(
      final Table table,
      final String privilegeName,
      final String grantor,
      final String grantee,
      final boolean grantable) {
    final MutablePrivilege<Table> privilege =
        new MutablePrivilege<>(new TablePointer(table), privilegeName);
    return privilege.new PrivilegeGrant(grantor, grantee, grantable);
  }

  public static TableReference newImplicitAssociation(
      final String name, final Table child, final Table parent, final String columnName) {
    return new MutableImplicitAssociation(
        name, newColumnReference(1, newColumn(child, columnName), newColumn(parent, columnName)));
  }

  public static Index newIndex(final Table table, final String name) {
    return new MutableIndex(table, name);
  }

  public static IndexColumn newIndexColumn(final Index index, final Column column) {
    return new MutableIndexColumn(index, column);
  }

  public static Procedure newProcedure(
      final Schema schema, final String name, final String specificName) {
    return new MutableProcedure(schema, name, specificName);
  }

  public static Property newServerInfoProperty(
      final String name, final String value, final String description) {
    return new ImmutableServerInfoProperty(name, value, description);
  }

  public static Table newTable(final Schema schema, final String name) {
    return new MutableTable(schema, name);
  }

  public static View newView(final Schema schema, final String name) {
    return new MutableView(schema, name);
  }

  public static void setIndexType(final Index index, final IndexType type) {
    ((MutableIndex) index).setIndexType(type);
  }

  public static void setKeyOrdinalPosition(final IndexColumn column, final int position) {
    ((MutableIndexColumn) column).setKeyOrdinalPosition(position);
  }

  public static void setOrdinalPosition(final Column column, final int position) {
    ((MutableColumn) column).setOrdinalPosition(position);
  }

  public static void setSortIndex(final Table table, final int position) {
    ((MutableTable) table).setSortIndex(position);
  }

  public static void setTableType(final Table table, final TableType type) {
    ((MutableTable) table).setTableType(type);
  }

  public static void setUnique(final Index index, final boolean unique) {
    ((MutableIndex) index).setUnique(unique);
  }

  public static List<Table> sortTablesByGraph(final Collection<Table> tables) {
    final BuildableDatabaseObjectIndex<MutableTable> index =
        NamedObjectIndexes.newBuildableDatabaseObjectIndex();
    tables.forEach(table -> index.add((MutableTable) table));
    new TablesGraph(index).setTablesSortIndexes();
    return List.copyOf(index.getObjects());
  }

  private SortTestUtility() {
    throw new UnsupportedOperationException();
  }
}
