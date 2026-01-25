/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility;

import static org.hamcrest.MatcherAssert.assertThat;
import static us.fatehi.test.utility.extensions.FileHasContent.classpathResource;
import static us.fatehi.test.utility.extensions.FileHasContent.hasSameContentAs;
import static us.fatehi.test.utility.extensions.FileHasContent.outputOf;

import java.util.Arrays;
import java.util.Collection;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schema.WeakAssociation;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaReference;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;
import schemacrawler.schemacrawler.SchemaRetrievalOptionsBuilder;
import schemacrawler.tools.options.Config;
import schemacrawler.tools.options.ConfigUtility;
import schemacrawler.tools.utility.SchemaCrawlerUtility;
import schemacrawler.utility.NamedObjectSort;
import us.fatehi.test.utility.TestWriter;
import us.fatehi.test.utility.extensions.TestContext;
import us.fatehi.utility.datasource.DatabaseConnectionSource;

public class ProposedWeakAssociationsTestUtility {

  public static void weakAssociations(
      final TestContext testContext,
      final DatabaseConnectionSource dataSource,
      final boolean inferExtensionTables)
      throws Exception {

    final String currentMethodFullName;
    if (inferExtensionTables) {
      currentMethodFullName = testContext.testMethodFullName() + ".infer-extension-tables";
    } else {
      currentMethodFullName = testContext.testMethodFullName();
    }

    final TestWriter testout = new TestWriter();
    try (final TestWriter out = testout) {
      final SchemaCrawlerOptions schemaCrawlerOptions =
          DatabaseTestUtility.schemaCrawlerOptionsWithMaximumSchemaInfoLevel;

      final SchemaRetrievalOptions schemaRetrievalOptions =
          SchemaRetrievalOptionsBuilder.newSchemaRetrievalOptions();

      final Config config = ConfigUtility.newConfig();
      config.put("weak-associations", true);
      config.put("infer-extension-tables", Boolean.valueOf(inferExtensionTables));

      final Catalog catalog =
          SchemaCrawlerUtility.getCatalog(
              dataSource, schemaRetrievalOptions, schemaCrawlerOptions, config);

      final Schema schema = new SchemaReference("PUBLIC", "PUBLIC");
      final Table[] tables = catalog.getTables(schema).toArray(new Table[0]);
      Arrays.sort(tables, NamedObjectSort.alphabetical);
      for (final Table table : tables) {
        out.println("table: " + table.getFullName());
        final Collection<WeakAssociation> weakAssociations = table.getWeakAssociations();
        for (final WeakAssociation weakFk : weakAssociations) {
          out.println("  weak association:");
          for (final ColumnReference weakAssociationColumnReference : weakFk) {
            out.println("    column reference: %s".formatted(weakAssociationColumnReference));
            final Column fkColumn = weakAssociationColumnReference.getForeignKeyColumn();
            final Column pkColumn = weakAssociationColumnReference.getPrimaryKeyColumn();
            if (fkColumn.isPartOfPrimaryKey()) {
              out.println("                      (fk is part of pk)");
            } else if (fkColumn.getName().equalsIgnoreCase(pkColumn.getName())) {
              out.println("                      (fk is NOT part of pk)");
            }
          }
        }
      }
    }
    assertThat(outputOf(testout), hasSameContentAs(classpathResource(currentMethodFullName)));
  }

  private ProposedWeakAssociationsTestUtility() {
    // Prevent instantiation
  }
}
