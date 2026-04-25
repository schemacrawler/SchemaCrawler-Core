/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.utility;

import static java.util.Objects.requireNonNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.crawl.ResultsCrawler;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.ResultsColumns;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;
import schemacrawler.schemacrawler.exceptions.DatabaseAccessException;
import schemacrawler.loader.catalog.CatalogLoader;
import schemacrawler.loader.catalog.CatalogLoaderRegistry;
import schemacrawler.loader.ermodel.ChainedERModelLoader;
import schemacrawler.loader.ermodel.ERModelLoaderRegistry;
import schemacrawler.tools.options.Config;
import schemacrawler.tools.options.ConfigUtility;
import us.fatehi.utility.UtilityMarker;
import us.fatehi.utility.database.DatabaseUtility;
import us.fatehi.utility.datasource.DatabaseConnectionSource;
import us.fatehi.utility.string.ObjectToStringFormat;
import us.fatehi.utility.string.StringFormat;

/** SchemaCrawler utility methods. */
@UtilityMarker
public final class SchemaCrawlerUtility {

  private static final Logger LOGGER = Logger.getLogger(SchemaCrawlerUtility.class.getName());

  public static ERModel buildERModel(final Catalog catalog) {
    return buildERModel(catalog, ConfigUtility.newConfig());
  }

  public static ERModel buildERModel(final Catalog catalog, final Config additionalConfig) {
    requireNonNull(catalog, "No catalog provided");
    final ERModelLoaderRegistry registry = ERModelLoaderRegistry.getERModelLoaderRegistry();
    final ChainedERModelLoader chainedLoader = registry.newChainedERModelLoader(additionalConfig);
    chainedLoader.setCatalog(catalog);
    chainedLoader.initialize();
    chainedLoader.execute();
    return chainedLoader.getERModel();
  }

  /**
   * Crawls a database, and returns a catalog.
   *
   * @param connectionSource Database connection source.
   * @param schemaCrawlerOptions Options.
   * @return Database catalog.
   */
  public static Catalog getCatalog(
      final DatabaseConnectionSource connectionSource,
      final SchemaCrawlerOptions schemaCrawlerOptions) {
    final SchemaRetrievalOptions schemaRetrievalOptions =
        DatabaseConnectorUtility.matchSchemaRetrievalOptions(connectionSource);
    return getCatalog(
        connectionSource, schemaRetrievalOptions, schemaCrawlerOptions, ConfigUtility.newConfig());
  }

  /**
   * Crawls a database, and returns a catalog.
   *
   * @param connectionSource Database connection source.
   * @param schemaCrawlerOptions Options.
   * @return Database catalog.
   */
  public static Catalog getCatalog(
      final DatabaseConnectionSource connectionSource,
      final SchemaRetrievalOptions schemaRetrievalOptions,
      final SchemaCrawlerOptions schemaCrawlerOptions,
      final Config additionalConfig) {

    LOGGER.log(Level.CONFIG, new ObjectToStringFormat(schemaCrawlerOptions));

    DatabaseConnectorUtility.updateConnectionDataSource(connectionSource, schemaRetrievalOptions);

    final CatalogLoaderRegistry catalogLoaderRegistry =
        CatalogLoaderRegistry.getCatalogLoaderRegistry();
    final CatalogLoader<?> catalogLoader =
        catalogLoaderRegistry.newChainedCatalogLoader(schemaCrawlerOptions, additionalConfig);

    LOGGER.log(Level.CONFIG, new StringFormat("Catalog loader: %s", catalogLoader));

    catalogLoader.initialize();

    // Catalog is set during the execution process

    catalogLoader.setConnectionSource(connectionSource);
    catalogLoader.setSchemaRetrievalOptions(schemaRetrievalOptions);

    catalogLoader.execute();
    final Catalog catalog = catalogLoader.getCatalog();
    requireNonNull(catalog, "Catalog could not be retrieved");
    return catalog;
  }

  /**
   * Obtains result-set metadata from a live result-set.
   *
   * @param resultSet Live result-set.
   * @return Result-set metadata.
   */
  public static ResultsColumns getResultsColumns(final ResultSet resultSet) {
    try {
      // NOTE: Some JDBC drivers like SQLite may not work with closed
      // result-sets
      DatabaseUtility.checkResultSet(resultSet);
      final ResultsCrawler resultSetCrawler = new ResultsCrawler(resultSet);
      final ResultsColumns resultsColumns = resultSetCrawler.crawl();
      return resultsColumns;
    } catch (final SQLException e) {
      throw new DatabaseAccessException("Could not retrieve result-set metadata", e);
    }
  }

  private SchemaCrawlerUtility() {
    // Prevent instantiation
  }
}
