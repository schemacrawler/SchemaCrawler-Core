/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility.crawl;

import static us.fatehi.utility.Utility.trimToEmpty;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import schemacrawler.schema.Column;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.Index;
import schemacrawler.schema.PrimaryKey;
import schemacrawler.schema.Privilege;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableConstraint;
import schemacrawler.schema.TableRelationshipType;
import schemacrawler.schema.TableType;
import schemacrawler.schema.Trigger;
import schemacrawler.schema.WeakAssociation;
import schemacrawler.schemacrawler.SchemaReference;

public final class LightTable extends AbstractLightDatabaseObject implements Table {

  @Serial private static final long serialVersionUID = -309232480533750613L;

  private final List<Column> columns;
  private final List<Column> hiddenColumns;
  private final Collection<Trigger> triggers;
  private String definition;

  public LightTable(final Schema schema, final String name) {
    super(schema, name);
    columns = new ArrayList<>();
    hiddenColumns = new ArrayList<>();
    triggers = new ArrayList<>();
  }

  public LightTable(final String name) {
    this(new SchemaReference(), name);
  }

  public LightColumn addColumn(final String name) {
    final LightColumn column = LightColumn.newColumn(this, name);
    columns.add(column);
    return column;
  }

  public LightColumn addEnumeratedColumn(final String name) {
    final LightColumn column = LightColumn.newEnumeratedColumn(this, name);
    columns.add(column);
    return column;
  }

  public LightColumn addGeneratedColumn(final String name) {
    final LightColumn column = LightColumn.newGeneratedColumn(this, name);
    columns.add(column);
    return column;
  }

  public LightColumn addHiddenColumn(final String name) {
    final LightColumn column = LightColumn.newHiddenColumn(this, name);
    hiddenColumns.add(column);
    return column;
  }

  public void addTrigger(final Trigger trigger) {
    if (trigger != null) {
      triggers.add(trigger);
    }
  }

  @Override
  public Collection<PrimaryKey> getAlternateKeys() {
    return List.of();
  }

  @Override
  public List<Column> getColumns() {
    return List.copyOf(columns);
  }

  @Override
  public String getDefinition() {
    return trimToEmpty(definition);
  }

  @Override
  public Collection<ForeignKey> getExportedForeignKeys() {
    return List.of();
  }

  @Override
  public Collection<ForeignKey> getForeignKeys() {
    return List.of();
  }

  @Override
  public Collection<Column> getHiddenColumns() {
    return Set.copyOf(hiddenColumns);
  }

  @Override
  public Collection<ForeignKey> getImportedForeignKeys() {
    return List.of();
  }

  @Override
  public Collection<Index> getIndexes() {
    return List.of();
  }

  @Override
  public PrimaryKey getPrimaryKey() {
    return null;
  }

  @Override
  public Collection<Privilege<Table>> getPrivileges() {
    return List.of();
  }

  @Override
  public Collection<Table> getRelatedTables(final TableRelationshipType tableRelationshipType) {
    return List.of();
  }

  @Override
  public Collection<TableConstraint> getTableConstraints() {
    return List.of();
  }

  @Override
  public TableType getTableType() {
    return getType();
  }

  @Override
  public Collection<Trigger> getTriggers() {
    return List.copyOf(triggers);
  }

  @Override
  public TableType getType() {
    return TableType.UNKNOWN;
  }

  @Override
  public Collection<DatabaseObject> getUsedByObjects() {
    return List.of();
  }

  @Override
  @Deprecated(forRemoval = true)
  public Collection<WeakAssociation> getWeakAssociations() {
    return List.of();
  }

  @Override
  public boolean hasDefinition() {
    return false;
  }

  @Override
  public boolean hasForeignKeys() {
    return false;
  }

  @Override
  public boolean hasIndexes() {
    return false;
  }

  @Override
  public boolean hasPrimaryKey() {
    return false;
  }

  @Override
  public boolean hasTriggers() {
    return !triggers.isEmpty();
  }

  @Override
  public boolean isSelfReferencing() {
    return false;
  }

  @Override
  public <A extends PrimaryKey> Optional<A> lookupAlternateKey(final String name) {
    return Optional.empty();
  }

  @Override
  public <C extends Column> Optional<C> lookupColumn(final String name) {
    for (final Column column : columns) {
      if (column.getName().equals(name)) {
        return (Optional<C>) Optional.of(column);
      }
    }
    return Optional.empty();
  }

  @Override
  public <F extends ForeignKey> Optional<F> lookupForeignKey(final String name) {
    return Optional.empty();
  }

  @Override
  public <I extends Index> Optional<I> lookupIndex(final String name) {
    return Optional.empty();
  }

  @Override
  public <P extends Privilege<Table>> Optional<P> lookupPrivilege(final String name) {
    return Optional.empty();
  }

  @Override
  public <C extends TableConstraint> Optional<C> lookupTableConstraint(final String name) {
    return Optional.empty();
  }

  @Override
  public <T extends Trigger> Optional<T> lookupTrigger(final String name) {
    return Optional.empty();
  }

  public void setDefinition(String definition) {
    this.definition = definition;
  }
}
