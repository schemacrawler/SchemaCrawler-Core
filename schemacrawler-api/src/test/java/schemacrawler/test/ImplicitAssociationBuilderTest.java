/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static schemacrawler.test.utility.DatabaseTestUtility.getCatalog;
import static us.fatehi.test.utility.extensions.FileHasContent.classpathResource;
import static us.fatehi.test.utility.extensions.FileHasContent.hasSameContentAs;
import static us.fatehi.test.utility.extensions.FileHasContent.outputOf;

import java.sql.Connection;
import java.util.List;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import schemacrawler.crawl.ImplicitAssociationBuilder;
import schemacrawler.crawl.ImplicitAssociationBuilder.ImplicitAssociationColumn;
import schemacrawler.inclusionrule.RegularExpressionExclusionRule;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableReference;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.LoadOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.schemacrawler.SchemaReference;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;
import schemacrawler.test.utility.DatabaseTestUtility;
import schemacrawler.test.utility.WithTestDatabase;
import us.fatehi.test.utility.TestWriter;
import us.fatehi.test.utility.extensions.ResolveTestContext;
import us.fatehi.test.utility.extensions.TestContext;

@WithTestDatabase
@ResolveTestContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ImplicitAssociationBuilderTest {

  private static ImplicitAssociationColumn newImplicitAssociationColumn(final Column column) {
    return new ImplicitAssociationColumn(
        column.getSchema(), column.getParent().getName(), column.getName());
  }

  private static ImplicitAssociationColumn newImplicitAssociationColumn(
      final Schema schema, final String tableName, final String columnName) {
    return new ImplicitAssociationColumn(schema, tableName, columnName);
  }

  private Catalog catalog;

  @BeforeAll
  public void loadCatalog(final Connection connection) throws Exception {

    final SchemaRetrievalOptions schemaRetrievalOptions =
        DatabaseTestUtility.newSchemaRetrievalOptions();

    final LimitOptionsBuilder limitOptionsBuilder =
        LimitOptionsBuilder.builder()
            .includeSchemas(new RegularExpressionExclusionRule(".*\\.FOR_LINT"))
            .includeAllSynonyms()
            .includeAllSequences()
            .includeAllRoutines();
    final LoadOptionsBuilder loadOptionsBuilder =
        LoadOptionsBuilder.builder().withSchemaInfoLevel(SchemaInfoLevelBuilder.maximum());
    final SchemaCrawlerOptions schemaCrawlerOptions =
        SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
            .withLimitOptions(limitOptionsBuilder.toOptions())
            .withLoadOptions(loadOptionsBuilder.toOptions());

    catalog = getCatalog(connection, schemaRetrievalOptions, schemaCrawlerOptions);
  }

  /**
   * Keep in sync with {@link ImplicitAssociationsAttributesTest#implicitAssociations() LabelName}
   */
  @Test
  public void implicitAssociations(final TestContext testContext) throws Exception {

    final MultiValuedMap<NamedObjectKey, TableReference> implicitAssociations =
        new HashSetValuedHashMap<>();
    TableReference implicitAssociation;

    final Column pkColumn =
        catalog
            .lookupTable(new SchemaReference("PUBLIC", "BOOKS"), "AUTHORS")
            .get()
            .lookupColumn("ID")
            .get();
    final Column fkColumn =
        catalog
            .lookupTable(new SchemaReference("PUBLIC", "BOOKS"), "BOOKS")
            .get()
            .lookupColumn("ID")
            .get();

    final ImplicitAssociationBuilder builder = ImplicitAssociationBuilder.builder(catalog);
    // 1. Happy path - good implicit association
    builder
        .withName("1_weak")
        .addColumnReference(
            newImplicitAssociationColumn(fkColumn), newImplicitAssociationColumn(pkColumn));
    implicitAssociation = builder.build();
    implicitAssociations.put(implicitAssociation.getPrimaryKeyTable().key(), implicitAssociation);
    implicitAssociations.put(implicitAssociation.getForeignKeyTable().key(), implicitAssociation);
    // 2. Partial foreign key
    builder
        .withName("2_weak_partial_fk")
        .addColumnReference(
            newImplicitAssociationColumn(
                new SchemaReference("PRIVATE", "LIBRARY"), "BOOKAUTHORS", "AUTHORID"),
            newImplicitAssociationColumn(pkColumn));
    implicitAssociation = builder.build();
    implicitAssociations.put(implicitAssociation.getPrimaryKeyTable().key(), implicitAssociation);
    implicitAssociations.put(implicitAssociation.getForeignKeyTable().key(), implicitAssociation);
    // 3. Partial primary key
    builder
        .withName("3_weak_partial_pk")
        .addColumnReference(
            newImplicitAssociationColumn(fkColumn),
            newImplicitAssociationColumn(new SchemaReference("PRIVATE", "LIBRARY"), "BOOKS", "ID"));
    implicitAssociation = builder.build();
    implicitAssociations.put(implicitAssociation.getPrimaryKeyTable().key(), implicitAssociation);
    implicitAssociations.put(implicitAssociation.getForeignKeyTable().key(), implicitAssociation);
    // 4. Partial both (not built)
    builder
        .withName("4_weak_partial_both")
        .addColumnReference(
            newImplicitAssociationColumn(
                new SchemaReference("PRIVATE", "LIBRARY"), "BOOKAUTHORS", "AUTHORID"),
            newImplicitAssociationColumn(
                new SchemaReference("PRIVATE", "LIBRARY"), "AUTHORS", "ID"));
    implicitAssociation = builder.build();
    assertThat(implicitAssociation, is(nullValue()));
    // 5. No column references (not built)
    builder.withName("5_weak_no_references").build();
    implicitAssociation = builder.build();
    assertThat(implicitAssociation, is(nullValue()));
    // 6. Multiple tables in play (not built)
    builder
        .withName("6_weak_conflicting")
        .addColumnReference(
            newImplicitAssociationColumn(
                new SchemaReference("PRIVATE", "LIBRARY"), "BOOKAUTHORS", "AUTHORID"),
            newImplicitAssociationColumn(pkColumn));
    builder.addColumnReference(
        newImplicitAssociationColumn(fkColumn),
        newImplicitAssociationColumn(new SchemaReference("PRIVATE", "LIBRARY"), "AUTHORS", "ID"));
    implicitAssociation = builder.build();
    assertThat(implicitAssociation, is(nullValue()));
    // 7. Duplicate column references (only one column reference built)
    builder
        .withName("7_weak_duplicate")
        .addColumnReference(
            newImplicitAssociationColumn(
                new SchemaReference("PRIVATE", "LIBRARY"), "MAGAZINEARTICLES", "AUTHORID"),
            newImplicitAssociationColumn(pkColumn));
    builder.addColumnReference(
        newImplicitAssociationColumn(
            new SchemaReference("PRIVATE", "LIBRARY"), "MAGAZINEARTICLES", "AUTHORID"),
        newImplicitAssociationColumn(pkColumn));
    implicitAssociation = builder.build();
    implicitAssociations.put(implicitAssociation.getPrimaryKeyTable().key(), implicitAssociation);
    implicitAssociations.put(implicitAssociation.getForeignKeyTable().key(), implicitAssociation);
    // 8. Two column references
    builder
        .withName("8_weak_two_references")
        .addColumnReference(
            newImplicitAssociationColumn(
                new SchemaReference("PRIVATE", "ALLSALES"), "REGIONS", "POSTALCODE"),
            newImplicitAssociationColumn(
                new SchemaReference("PUBLIC", "PUBLISHER SALES"), "SALES", "POSTALCODE"));
    builder.addColumnReference(
        newImplicitAssociationColumn(
            new SchemaReference("PRIVATE", "ALLSALES"), "REGIONS", "COUNTRY"),
        newImplicitAssociationColumn(
            new SchemaReference("PUBLIC", "PUBLISHER SALES"), "SALES", "COUNTRY"));
    implicitAssociation = builder.build();
    implicitAssociations.put(implicitAssociation.getPrimaryKeyTable().key(), implicitAssociation);
    implicitAssociations.put(implicitAssociation.getForeignKeyTable().key(), implicitAssociation);
    // 9. Self-reference
    builder
        .withName("9_weak_self_reference")
        .addColumnReference(
            newImplicitAssociationColumn(
                new SchemaReference("PUBLIC", "BOOKS"), "BOOKS", "OTHEREDITIONID"),
            newImplicitAssociationColumn(new SchemaReference("PUBLIC", "BOOKS"), "BOOKS", "ID"));
    implicitAssociation = builder.build();
    implicitAssociations.put(implicitAssociation.getPrimaryKeyTable().key(), implicitAssociation);
    implicitAssociations.put(implicitAssociation.getForeignKeyTable().key(), implicitAssociation);
    // 10. Self-reference in partial table (not built)
    builder
        .withName("10_weak_partial_self_reference")
        .addColumnReference(
            newImplicitAssociationColumn(
                new SchemaReference("PRIVATE", "LIBRARY"), "BOOKS", "PREVIOUSEDITIONID"),
            newImplicitAssociationColumn(new SchemaReference("PRIVATE", "LIBRARY"), "BOOKS", "ID"));
    implicitAssociation = builder.build();
    assertThat(implicitAssociation, is(nullValue()));
    // 11. Duplicate implicit association (not built)
    builder
        .withName("1_weak_duplicate")
        .addColumnReference(
            newImplicitAssociationColumn(fkColumn), newImplicitAssociationColumn(pkColumn));
    implicitAssociation = builder.build();
    implicitAssociations.put(implicitAssociation.getPrimaryKeyTable().key(), implicitAssociation);
    implicitAssociations.put(implicitAssociation.getForeignKeyTable().key(), implicitAssociation);
    // 12. Same as foreign key
    builder
        .withName("12_same_as_fk")
        .addColumnReference(
            newImplicitAssociationColumn(
                new SchemaReference("PUBLIC", "BOOKS"), "BOOKAUTHORS", "AUTHORID"),
            newImplicitAssociationColumn(pkColumn));
    TableReference fk = builder.build();
    assertThat(fk, is(not(nullValue())));
    assertThat(fk, instanceOf(ForeignKey.class));
    assertThat(fk.getName(), is("Z_FK_AUTHOR"));

    final TestWriter testout = new TestWriter();
    try (final TestWriter out = testout) {
      final Schema[] schemas = catalog.getSchemas().toArray(new Schema[0]);
      assertThat("Schema count does not match", schemas, arrayWithSize(5));
      for (final Schema schema : schemas) {
        out.println("schema: " + schema.getFullName());
        final Table[] tables = catalog.getTables(schema).toArray(new Table[0]);
        for (final Table table : tables) {
          out.println("  table: " + table.getFullName());
          for (final TableReference loadedTableReference : implicitAssociations.get(table.key())) {
            out.println("    implicit association: " + loadedTableReference.getName());
            out.println("      column references: ");
            final List<ColumnReference> columnReferences =
                loadedTableReference.getColumnReferences();
            for (int i = 0; i < columnReferences.size(); i++) {
              final ColumnReference columnReference = columnReferences.get(i);
              out.println("        key sequence: " + (i + 1));
              out.println("          " + columnReference);
            }
          }
        }
      }
    }
    assertThat(
        outputOf(testout), hasSameContentAs(classpathResource(testContext.testMethodFullName())));
  }
}
