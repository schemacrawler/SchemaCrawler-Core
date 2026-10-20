/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import schemacrawler.namedobject.AbstractCatalog;
import schemacrawler.namedobject.ImmutableDatabaseObjectIndex;
import schemacrawler.namedobject.MapBackedNamedObjectIndex;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.CrawlInfo;
import schemacrawler.schema.DatabaseInfo;
import schemacrawler.schema.DatabaseUser;
import schemacrawler.schema.JdbcDriverInfo;
import schemacrawler.schema.Routine;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Sequence;
import schemacrawler.schema.Synonym;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaReference;

/** Fixed membership with independently mutable, shallowly copied catalog attributes. */
final class CatalogProjection
    extends AbstractCatalog<ColumnDataType, Table, Routine, Sequence, Synonym> {

  @Serial private static final long serialVersionUID = 6637656400067202011L;

  private final CrawlInfo crawlInfo;
  private final DatabaseInfo databaseInfo;
  private final JdbcDriverInfo jdbcDriverInfo;

  CatalogProjection(
      final String name,
      final CrawlInfo crawlInfo,
      final DatabaseInfo databaseInfo,
      final JdbcDriverInfo jdbcDriverInfo,
      final Map<String, Object> attributesMap) {
    this(
        crawlInfo,
        databaseInfo,
        jdbcDriverInfo,
        attributesMap,
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        List.of());
  }

  CatalogProjection(
      final CrawlInfo crawlInfo,
      final DatabaseInfo databaseInfo,
      final JdbcDriverInfo jdbcDriverInfo,
      final Map<String, Object> attributesMap,
      final Collection<DatabaseUser> databaseUsers,
      final Collection<Schema> schemas,
      final Collection<ColumnDataType> columnDataTypes,
      final Collection<Table> tables,
      final Collection<Routine> routines,
      final Collection<Sequence> sequences,
      final Collection<Synonym> synonyms) {
    super(
        new SchemaReference(),
        new MapBackedNamedObjectIndex<>(databaseUsers),
        new MapBackedNamedObjectIndex<>(schemas),
        new ImmutableDatabaseObjectIndex<>(columnDataTypes),
        new ImmutableDatabaseObjectIndex<>(tables),
        new ImmutableDatabaseObjectIndex<>(routines),
        new ImmutableDatabaseObjectIndex<>(sequences),
        new ImmutableDatabaseObjectIndex<>(synonyms));
    this.crawlInfo = crawlInfo;
    this.databaseInfo = databaseInfo;
    this.jdbcDriverInfo = jdbcDriverInfo;
    addAttributes(attributesMap);
  }

  @Override
  public CrawlInfo getCrawlInfo() {
    return crawlInfo;
  }

  @Override
  public DatabaseInfo getDatabaseInfo() {
    return databaseInfo;
  }

  /** {@inheritDoc} */
  @Override
  public JdbcDriverInfo getJdbcDriverInfo() {
    return jdbcDriverInfo;
  }
}
