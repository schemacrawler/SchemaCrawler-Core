/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static schemacrawler.test.utility.DatabaseTestUtility.getCatalog;
import static schemacrawler.test.utility.DatabaseTestUtility.schemaCrawlerOptionsWithMaximumSchemaInfoLevel;
import static schemacrawler.test.utility.DatabaseTestUtility.validateSchema;
import static us.fatehi.test.utility.TestUtility.fileHeaderOf;
import static us.fatehi.utility.IOUtility.isFileReadable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import schemacrawler.schema.Catalog;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.test.utility.WithTestDatabase;
import schemacrawler.utility.SerializedCatalogUtility;
import us.fatehi.utility.IOUtility;
import us.fatehi.utility.datasource.DatabaseConnectionSource;

@WithTestDatabase
public class CatalogSerializationTest {

  private Catalog catalog;

  @BeforeEach
  public void loadCatalog(final Connection connection) {
    final SchemaCrawlerOptions schemaCrawlerOptions =
        schemaCrawlerOptionsWithMaximumSchemaInfoLevel;
    try {
      catalog = getCatalog(connection, schemaCrawlerOptions);
    } catch (final Exception e) {
      fail("Catalog not loaded", e);
    }
    validateSchema(catalog);
  }

  @Test
  public void catalogSerializationWithJava(final DatabaseConnectionSource connectionSource)
      throws Exception {

    final Path testOutputFile = IOUtility.createTempFilePath("sc_ermodel_serialization", "ser");
    SerializedCatalogUtility.saveCatalog(
        catalog, Files.newOutputStream(testOutputFile, WRITE, CREATE, TRUNCATE_EXISTING));
    assertThat("Catalog was not serialized", isFileReadable(testOutputFile), is(true));
    assertThat(fileHeaderOf(testOutputFile), is("ACED"));

    // Deserialize generated JSON file, and assert load
    final Catalog catalogDeserialized =
        SerializedCatalogUtility.readCatalog(newInputStream(testOutputFile, READ));
    validateSchema(catalogDeserialized);
  }
}
