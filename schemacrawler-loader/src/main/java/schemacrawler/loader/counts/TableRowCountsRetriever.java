/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.counts;

import static java.util.Objects.requireNonNull;
import static schemacrawler.loader.utility.TableRowCountsUtility.TABLE_ROW_COUNT_KEY;
import static schemacrawler.schema.IdentifierQuotingStrategy.quote_all;
import static schemacrawler.schemacrawler.QueryUtility.executeForLong;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Identifiers;
import schemacrawler.schema.IdentifiersBuilder;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.Query;
import schemacrawler.schemacrawler.Retriever;
import us.fatehi.utility.datasource.DatabaseConnectionSource;
import us.fatehi.utility.string.StringFormat;

@Retriever
public final class TableRowCountsRetriever {

  private static final Logger LOGGER = Logger.getLogger(TableRowCountsRetriever.class.getName());

  private final DatabaseConnectionSource connectionSource;
  private final Catalog catalog;

  public TableRowCountsRetriever(
      final DatabaseConnectionSource connectionSource, final Catalog catalog) throws SQLException {
    this.connectionSource =
        requireNonNull(connectionSource, "No database connection source provided");
    this.catalog = requireNonNull(catalog, "No catalog provided");
  }

  public void retrieveTableRowCounts() {

    Identifiers identifiers;
    try (Connection connection = connectionSource.get(); ) {
      identifiers =
          IdentifiersBuilder.builder()
              .fromConnection(connection)
              .withIdentifierQuotingStrategy(quote_all)
              .toOptions();
    } catch (final SQLException e) {
      // The offline snapshot executable may not have a live connection,
      // so we cannot fail with an exception. Log and continue.
      LOGGER.log(Level.WARNING, "No connection provided", e);
      return;
    }

    try (Connection connection = connectionSource.get(); ) {
      final Query query =
          new Query("schemacrawler.table.row_counts", "SELECT COUNT(*) FROM ${table}");
      final List<Table> allTables = new ArrayList<>(catalog.getTables());
      for (final Table table : allTables) {
        try {
          final long count = executeForLong(query, connection, table, identifiers);
          addRowCountToTable(table, count);
        } catch (final SQLException e) {
          LOGGER.log(
              Level.WARNING, e, new StringFormat("Could not get count for table <%s>", table));
        }
      }
    } catch (final SQLException e) {
      LOGGER.log(Level.WARNING, "Could not get table row counts", e);
    }
  }

  private void addRowCountToTable(final Table table, final long rowCount) {
    if (table != null) {
      if (rowCount >= 0) {
        table.setAttribute(TABLE_ROW_COUNT_KEY, rowCount);
      } else {
        table.removeAttribute(TABLE_ROW_COUNT_KEY);
      }
    }
  }
}
