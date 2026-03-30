/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.emptyString;
import static schemacrawler.schema.TableConstraintType.implicit_association;
import static us.fatehi.test.utility.extensions.FileHasContent.classpathResource;
import static us.fatehi.test.utility.extensions.FileHasContent.hasSameContentAs;
import static us.fatehi.test.utility.extensions.FileHasContent.outputOf;

import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.ermodel.utility.ERModelUtility;
import schemacrawler.inclusionrule.RegularExpressionExclusionRule;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableReference;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.LoadOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;
import schemacrawler.test.utility.DatabaseTestUtility;
import schemacrawler.test.utility.WithTestDatabase;
import schemacrawler.tools.options.Config;
import schemacrawler.tools.options.ConfigUtility;
import schemacrawler.tools.utility.SchemaCrawlerUtility;
import us.fatehi.test.utility.TestWriter;
import us.fatehi.test.utility.extensions.ResolveTestContext;
import us.fatehi.test.utility.extensions.TestContext;
import us.fatehi.utility.datasource.DatabaseConnectionSource;

@WithTestDatabase
@ResolveTestContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WeakAssociationsTest {

  private Catalog catalog;
  private ERModel erModel;

  @BeforeAll
  public void loadCatalog(final DatabaseConnectionSource connectionSource) throws Exception {

    final SchemaRetrievalOptions schemaRetrievalOptions =
        DatabaseTestUtility.newSchemaRetrievalOptions();

    final LimitOptionsBuilder limitOptionsBuilder =
        LimitOptionsBuilder.builder()
            .includeSchemas(new RegularExpressionExclusionRule(".*\\.FOR_LINT"))
            .includeAllSynonyms()
            .includeAllSequences()
            .includeAllRoutines();
    final LoadOptionsBuilder loadOptionsBuilder =
        LoadOptionsBuilder.builder().withSchemaInfoLevel(SchemaInfoLevelBuilder.standard());
    final SchemaCrawlerOptions schemaCrawlerOptions =
        SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
            .withLimitOptions(limitOptionsBuilder.toOptions())
            .withLoadOptions(loadOptionsBuilder.toOptions());

    final Config additionalConfig = ConfigUtility.newConfig();
    additionalConfig.put("implicit-associations", true);

    catalog =
        SchemaCrawlerUtility.getCatalog(
            connectionSource, schemaRetrievalOptions, schemaCrawlerOptions, additionalConfig);
    erModel = SchemaCrawlerUtility.buildERModel(catalog, additionalConfig);
  }

  @Test
  public void weakAssociations(final TestContext testContext) throws Exception {
    final TestWriter testout = new TestWriter();
    try (final TestWriter out = testout) {
      final Schema[] schemas = catalog.getSchemas().toArray(new Schema[0]);
      assertThat("Schema count does not match", schemas, arrayWithSize(5));
      for (final Schema schema : schemas) {
        out.println("schema: " + schema.getFullName());
        final Table[] tables = catalog.getTables(schema).toArray(new Table[0]);
        for (final Table table : tables) {
          out.println("  table: " + table.getFullName());
          final Collection<? extends TableReference> implicitAssociations =
              ERModelUtility.collectImplicitAssociations(table, erModel);
          for (final TableReference implicitAssociation : implicitAssociations) {
            out.println("    weak association: " + implicitAssociation.getName());
            out.println("      column references: ");
            final List<ColumnReference> columnReferences =
                implicitAssociation.getColumnReferences();
            for (int i = 0; i < columnReferences.size(); i++) {
              final ColumnReference columnReference = columnReferences.get(i);
              out.println("        key sequence: " + (i + 1));
              out.println("          " + columnReference);
            }
            assertThat(implicitAssociation.getDefinition(), is(emptyString()));
            assertThat(implicitAssociation.getType(), is(implicit_association));
            assertThat(implicitAssociation.hasDefinition(), is(false));
            assertThat(implicitAssociation.isDeferrable(), is(false));
            assertThat(implicitAssociation.isInitiallyDeferred(), is(false));
          }
        }
      }
    }
    assertThat(
        outputOf(testout), hasSameContentAs(classpathResource(testContext.testMethodFullName())));
  }
}
