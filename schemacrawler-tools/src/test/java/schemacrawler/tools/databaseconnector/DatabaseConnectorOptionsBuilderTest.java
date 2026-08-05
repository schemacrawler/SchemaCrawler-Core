/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.databaseconnector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;
import schemacrawler.schemacrawler.SchemaRetrievalOptionsBuilder;
import us.fatehi.utility.datasource.DatabaseServerType;

public class DatabaseConnectorOptionsBuilderTest {

  @Test
  @DisplayName("Composes schema retrieval option builders in registration order")
  public void composesSchemaRetrievalOptionsBuildersInOrder() {
    final List<String> invocations = new ArrayList<>();

    final DatabaseConnectorOptions options =
        DatabaseConnectorOptionsBuilder.builder(new DatabaseServerType("test", "Test"))
            .withSchemaRetrievalOptionsBuilder(
                (schemaRetrievalOptionsBuilder, connection) -> {
                  invocations.add("yaml");
                  schemaRetrievalOptionsBuilder.withSupportsSchemas();
                })
            .withSchemaRetrievalOptionsBuilder(
                (schemaRetrievalOptionsBuilder, connection) -> {
                  invocations.add("java");
                  schemaRetrievalOptionsBuilder.withDoesNotSupportSchemas();
                })
            .toOptions();

    final SchemaRetrievalOptionsBuilder schemaRetrievalOptionsBuilder =
        SchemaRetrievalOptionsBuilder.builder();
    options.schemaRetrievalOptionsBuildProcess().accept(schemaRetrievalOptionsBuilder, null);
    final SchemaRetrievalOptions schemaRetrievalOptions = schemaRetrievalOptionsBuilder.toOptions();

    assertThat(invocations, contains("yaml", "java"));
    assertThat(schemaRetrievalOptions.isSupportsSchemas(), is(false));
  }
}
