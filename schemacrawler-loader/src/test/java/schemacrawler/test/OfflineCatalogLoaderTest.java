/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static schemacrawler.tools.utility.SchemaCrawlerUtility.getCatalog;
import static us.fatehi.test.utility.TestUtility.failTestSetup;
import static us.fatehi.utility.IOUtility.isFileReadable;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Schema;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.LoadOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.schemacrawler.SchemaRetrievalOptionsBuilder;
import schemacrawler.test.utility.WithTestDatabase;
import schemacrawler.tools.loader.catalog.CatalogLoader;
import schemacrawler.tools.loader.catalog.offline.OfflineCatalogLoaderProvider;
import schemacrawler.tools.offline.jdbc.OfflineConnection;
import schemacrawler.tools.offline.jdbc.OfflineConnectionUtility;
import schemacrawler.tools.options.ConfigUtility;
import schemacrawler.utility.SerializedCatalogUtility;
import us.fatehi.test.utility.TestDatabaseDriver;
import us.fatehi.utility.IOUtility;
import us.fatehi.utility.datasource.DatabaseConnectionSource;
import us.fatehi.utility.datasource.DatabaseConnectionSources;

@WithTestDatabase
public class OfflineCatalogLoaderTest {

  private Path serializedCatalogFile;

  @Test
  public void connection() throws SQLException {
    final CatalogLoader<?> catalogLoader =
        new OfflineCatalogLoaderProvider().newCommand("offlineloader", ConfigUtility.newConfig());

    assertThat(catalogLoader.getConnectionSource(), is(nullValue()));

    final Connection connection = new TestDatabaseDriver().connect("jdbc:test-db:test", null);
    final DatabaseConnectionSource connectionSource =
        DatabaseConnectionSources.fromConnection(connection);
    catalogLoader.setConnectionSource(connectionSource);

    assertThat(catalogLoader.getConnectionSource(), is(not(nullValue())));
  }

  @Test
  public void getOfflineCatalog() throws Exception {
    final OfflineConnection offlineConnection =
        OfflineConnectionUtility.newOfflineConnection(serializedCatalogFile);
    final DatabaseConnectionSource connectionSource =
        DatabaseConnectionSources.fromConnection(offlineConnection);

    final SchemaCrawlerOptions schemaCrawlerOptions =
        SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions();

    final Catalog catalog =
        getCatalog(
            connectionSource,
            SchemaRetrievalOptionsBuilder.newSchemaRetrievalOptions(),
            schemaCrawlerOptions,
            ConfigUtility.newConfig());
    validateCatalog(catalog);
  }

  @BeforeEach
  public void serializeCatalog(final DatabaseConnectionSource connectionSource) {
    try {
      final LimitOptionsBuilder limitOptionsBuilder =
          LimitOptionsBuilder.builder().includeAllRoutines();
      final LoadOptionsBuilder loadOptionsBuilder =
          LoadOptionsBuilder.builder().withSchemaInfoLevel(SchemaInfoLevelBuilder.maximum());
      final SchemaCrawlerOptions schemaCrawlerOptions =
          SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
              .withLimitOptions(limitOptionsBuilder.toOptions())
              .withLoadOptions(loadOptionsBuilder.toOptions());

      final Catalog catalog =
          getCatalog(
              connectionSource,
              SchemaRetrievalOptionsBuilder.newSchemaRetrievalOptions(),
              schemaCrawlerOptions,
              ConfigUtility.newConfig());
      validateCatalog(catalog);

      serializedCatalogFile = IOUtility.createTempFilePath("schemacrawler", "ser");
      final OutputStream outputStream =
          new GZIPOutputStream(
              Files.newOutputStream(serializedCatalogFile, WRITE, CREATE, TRUNCATE_EXISTING));
      SerializedCatalogUtility.saveCatalog(catalog, outputStream);
      assertThat("Database was not serialized", isFileReadable(serializedCatalogFile), is(true));
    } catch (final IOException e) {
      failTestSetup("Could not serialize catalog", e);
    }
  }

  private void validateCatalog(final Catalog catalog) {
    assertThat("Could not obtain catalog", catalog, notNullValue());
    assertThat("Could not find any schemas", catalog.getSchemas(), not(empty()));

    final Schema schema = catalog.lookupSchema("PUBLIC.BOOKS").orElse(null);
    assertThat("Could not obtain schema", schema, notNullValue());
    assertThat("Unexpected number of tables in the schema", catalog.getTables(schema), hasSize(11));
  }
}
