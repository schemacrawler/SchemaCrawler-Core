/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static us.fatehi.test.utility.extensions.FileHasContent.classpathResource;
import static us.fatehi.test.utility.extensions.FileHasContent.hasSameContentAs;
import static us.fatehi.test.utility.extensions.FileHasContent.outputOf;

import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import schemacrawler.ermodel.weakassociations.WeakAssociationsAnalyzer;
import schemacrawler.ermodel.weakassociations.WeakAssociationsAnalyzerBuilder;
import schemacrawler.inclusionrule.RegularExpressionExclusionRule;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaReference;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;
import schemacrawler.test.utility.DatabaseTestUtility;
import schemacrawler.test.utility.WithTestDatabase;
import us.fatehi.test.utility.TestWriter;
import us.fatehi.test.utility.extensions.ResolveTestContext;
import us.fatehi.test.utility.extensions.TestContext;

@WithTestDatabase
@ResolveTestContext
@TestInstance(PER_CLASS)
public class WeakAssociationsAnalyzerTest {

  private Catalog catalog;

  @BeforeAll
  public void loadCatalog(final Connection connection) throws Exception {
    final SchemaRetrievalOptions schemaRetrievalOptions =
        DatabaseTestUtility.newSchemaRetrievalOptions();

    final LimitOptionsBuilder limitOptionsBuilder =
        LimitOptionsBuilder.builder()
            .includeSchemas(new RegularExpressionExclusionRule(".*\\.FOR_LINT"));
    final SchemaCrawlerOptions schemaCrawlerOptions =
        SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
            .withLimitOptions(limitOptionsBuilder.toOptions());

    catalog =
        DatabaseTestUtility.getCatalog(connection, schemaRetrievalOptions, schemaCrawlerOptions);
  }

  @Test
  @Disabled
  public void weakAssociations(final TestContext testContext, final Connection connection)
      throws Exception {

    final TestWriter testout = new TestWriter();
    try (final TestWriter out = testout) {
      final WeakAssociationsAnalyzerBuilder builder =
          WeakAssociationsAnalyzerBuilder.builder(catalog.getTables())
              .withIdMatcher()
              .withExtensionTableMatcher();

      final WeakAssociationsAnalyzer weakAssociationsAnalyzer = builder.build();
      final Collection<ColumnReference> proposedWeakAssociations =
          weakAssociationsAnalyzer.analyzeTables();
      assertThat(
          "Proposed weak association count does not match", proposedWeakAssociations, hasSize(7));
      for (final ColumnReference proposedWeakAssociation : proposedWeakAssociations) {
        out.println("weak association: %s".formatted(proposedWeakAssociation));
      }
    }

    assertThat(
        outputOf(testout), hasSameContentAs(classpathResource(testContext.testMethodFullName())));
  }

  @Test
  public void weakAssociationsFewTables() throws Exception {

    WeakAssociationsAnalyzerBuilder builder;

    builder =
        WeakAssociationsAnalyzerBuilder.builder(Collections.emptySet())
            .withIdMatcher()
            .withExtensionTableMatcher();
    assertThat(builder.build().analyzeTables(), hasSize(0));

    final Table booksTable =
        catalog.lookupTable(new SchemaReference("PUBLIC", "BOOKS"), "BOOKS").get();
    final Table bookAuthorsTable =
        catalog.lookupTable(new SchemaReference("PUBLIC", "BOOKS"), "BOOKAUTHORS").get();

    builder =
        WeakAssociationsAnalyzerBuilder.builder(List.of(booksTable))
            .withIdMatcher()
            .withExtensionTableMatcher();
    assertThat(builder.build().analyzeTables(), hasSize(0));

    builder =
        WeakAssociationsAnalyzerBuilder.builder(List.of(booksTable, bookAuthorsTable))
            .withIdMatcher()
            .withExtensionTableMatcher();
    assertThat(builder.build().analyzeTables(), hasSize(1));
  }
}
