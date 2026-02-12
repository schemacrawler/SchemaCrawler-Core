/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static us.fatehi.test.utility.extensions.FileHasContent.classpathResource;
import static us.fatehi.test.utility.extensions.FileHasContent.hasSameContentAs;
import static us.fatehi.test.utility.extensions.FileHasContent.outputOf;

import java.sql.Connection;
import java.util.Collection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import schemacrawler.ermodel.associations.WeakAssociationsAnalyzer;
import schemacrawler.ermodel.associations.WeakAssociationsAnalyzerBuilder;
import schemacrawler.ermodel.associations.WeakColumnReference;
import schemacrawler.inclusionrule.RegularExpressionExclusionRule;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
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
  public void weakAssociations(final TestContext testContext, final Connection connection)
      throws Exception {

    final TestWriter testout = new TestWriter();
    try (final TestWriter out = testout) {
      final WeakAssociationsAnalyzerBuilder builder =
          WeakAssociationsAnalyzerBuilder.builder(catalog.getTables())
              .withIdMatcher()
              .withExtensionTableMatcher();

      final WeakAssociationsAnalyzer weakAssociationsAnalyzer = builder.build();
      final Collection<WeakColumnReference> proposedWeakAssociations =
          weakAssociationsAnalyzer.analyzeTables();
      assertThat(
          "Proposed weak association count does not match",
          proposedWeakAssociations.size(),
          greaterThan(0));
      for (final ColumnReference proposedWeakAssociation : proposedWeakAssociations) {
        out.println("weak association: %s".formatted(proposedWeakAssociation));
      }
    }

    assertThat(
        outputOf(testout), hasSameContentAs(classpathResource(testContext.testMethodFullName())));
  }
}
