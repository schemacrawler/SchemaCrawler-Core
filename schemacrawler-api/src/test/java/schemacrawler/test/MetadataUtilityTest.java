/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static schemacrawler.schema.IdentifierQuotingStrategy.quote_all;
import static schemacrawler.test.utility.DatabaseTestUtility.getCatalog;
import static schemacrawler.test.utility.DatabaseTestUtility.schemaCrawlerOptionsWithMaximumSchemaInfoLevel;
import static us.fatehi.utility.Utility.isBlank;

import java.sql.Connection;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import schemacrawler.filter.ReducerFactory;
import schemacrawler.inclusionrule.ExcludeAll;
import schemacrawler.inclusionrule.IncludeAll;
import schemacrawler.inclusionrule.InclusionRule;
import schemacrawler.inclusionrule.InclusionRuleWithRegularExpression;
import schemacrawler.inclusionrule.ListExclusionRule;
import schemacrawler.inclusionrule.RegularExpressionInclusionRule;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.CatalogReducer;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.DependantObject;
import schemacrawler.schema.Function;
import schemacrawler.schema.Identifiers;
import schemacrawler.schema.IdentifiersBuilder;
import schemacrawler.schema.Index;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.PartialDatabaseObject;
import schemacrawler.schema.PrimaryKey;
import schemacrawler.schema.Procedure;
import schemacrawler.schema.Routine;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Sequence;
import schemacrawler.schema.Synonym;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableType;
import schemacrawler.schema.TypedObject;
import schemacrawler.schema.View;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaReference;
import schemacrawler.test.utility.WithTestDatabase;
import schemacrawler.test.utility.crawl.LightProcedure;
import schemacrawler.test.utility.crawl.LightTable;
import schemacrawler.utility.MetaDataUtility;
import us.fatehi.test.utility.extensions.ResolveTestContext;

@WithTestDatabase
@ResolveTestContext
@TestInstance(Lifecycle.PER_CLASS)
public class MetadataUtilityTest {

  private static final Identifiers identifiers =
      IdentifiersBuilder.builder()
          .withIdentifierQuotingStrategy(quote_all)
          .withIdentifierQuoteString("'")
          .toOptions();
  private Catalog catalog;

  @Test
  public void columnsListAsStringConstraint() throws Exception {

    final Schema schema = catalog.lookupSchema("PUBLIC.BOOKS").get();
    assertThat("BOOKS Schema not found", schema, notNullValue());

    final Table table = catalog.lookupTable(schema, "BOOKS").get();
    assertThat("BOOKS Table not found", table, notNullValue());

    final PrimaryKey pk = table.getPrimaryKey();
    assertThat("Index not found", pk, notNullValue());

    final String columnsListAsStringChild = MetaDataUtility.getColumnsListAsString(pk, identifiers);
    assertThat(columnsListAsStringChild, is("'ID'"));
  }

  @Test
  public void columnsListAsStringIndex() throws Exception {

    final Schema schema = catalog.lookupSchema("PUBLIC.BOOKS").get();
    assertThat("BOOKS Schema not found", schema, notNullValue());

    final Table table = catalog.lookupTable(schema, "BOOKS").get();
    assertThat("BOOKS Table not found", table, notNullValue());

    final Index index = table.getIndexes().toArray(new Index[0])[0];
    assertThat("Index not found", index, notNullValue());

    final String columnsListAsStringChild =
        MetaDataUtility.getColumnsListAsString(index, identifiers);
    assertThat(columnsListAsStringChild, is("'ID'"));
  }

  @Test
  public void databaseObjectUri() {
    final Schema schema = new SchemaReference("PUBLIC", "BOOKS");

    final Table table = new LightTable(schema, "order details");
    assertThat(
        MetaDataUtility.getDatabaseObjectUri(table).toString(),
        is("catalog://tables/PUBLIC.BOOKS.order%20details"));

    final Routine routine = new LightProcedure(schema, "find order");
    assertThat(
        MetaDataUtility.getDatabaseObjectUri(routine).toString(),
        is("catalog://routines/PUBLIC.BOOKS.find%20order"));
  }

  @Test
  public void databaseObjectUriUnknownTypeAndNull() {
    assertThat(MetaDataUtility.getDatabaseObjectUri(null), is((java.net.URI) null));
    assertThat(
        MetaDataUtility.getDatabaseObjectUri(mock(DatabaseObject.class)), is((java.net.URI) null));
  }

  @Test
  public void detectsViewByInstanceAndTableType() {
    assertThat(MetaDataUtility.isView(null), is(false));

    final Table baseTable = mock(Table.class);
    when(baseTable.getTableType()).thenReturn(new TableType("TABLE"));
    assertThat(MetaDataUtility.isView(baseTable), is(false));

    final Table nonPartialViewTypedTable = mock(Table.class);
    when(nonPartialViewTypedTable.getTableType()).thenReturn(new TableType("VIEW"));
    assertThat(MetaDataUtility.isView(nonPartialViewTypedTable), is(true));

    final Table partialViewTypedTable =
        mock(Table.class, withSettings().extraInterfaces(PartialDatabaseObject.class));
    when(partialViewTypedTable.getTableType()).thenReturn(TableType.UNKNOWN);
    assertThat(MetaDataUtility.isView(partialViewTypedTable), is(false));

    final Table partialBaseTypedTable =
        mock(Table.class, withSettings().extraInterfaces(PartialDatabaseObject.class));
    when(partialBaseTypedTable.getTableType()).thenReturn(new TableType("TABLE"));
    assertThat(MetaDataUtility.isView(partialBaseTypedTable), is(false));

    final View view = mock(View.class);
    assertThat(MetaDataUtility.isView(view), is(true));
  }

  @Test
  public void inclusionRuleString() {
    assertThat(MetaDataUtility.inclusionRuleString(null), is(".*"));

    final InclusionRule includeAll = new IncludeAll();
    assertThat(MetaDataUtility.inclusionRuleString(includeAll), is(".*"));

    final InclusionRule excludeAll = new ExcludeAll();
    assertThat(MetaDataUtility.inclusionRuleString(excludeAll), is(".*"));

    final InclusionRule listExclusionRule = new ListExclusionRule(List.of("BOOKS"));
    assertThat(MetaDataUtility.inclusionRuleString(listExclusionRule), is(".*"));

    final InclusionRule regexRule = new RegularExpressionInclusionRule("BOOKS|AUTHORS");
    assertThat(MetaDataUtility.inclusionRuleString(regexRule), is("BOOKS|AUTHORS"));

    final InclusionRule regexRuleWithNullPattern =
        new RegularExpressionInclusionRule((String) null);
    assertThat(MetaDataUtility.inclusionRuleString(regexRuleWithNullPattern), is(".*"));

    final InclusionRule blankRegexRule =
        new InclusionRuleWithRegularExpression() {
          @Override
          public Pattern getInclusionPattern() {
            return Pattern.compile("  ");
          }

          @Override
          public boolean test(final String text) {
            return true;
          }
        };
    assertThat(MetaDataUtility.inclusionRuleString(blankRegexRule), is(".*"));
  }

  @BeforeAll
  public void loadCatalog(final Connection connection) {
    final SchemaCrawlerOptions schemaCrawlerOptions =
        schemaCrawlerOptionsWithMaximumSchemaInfoLevel;
    try {
      catalog = getCatalog(connection, schemaCrawlerOptions);
    } catch (final Exception e) {
      fail("Catalog not loaded", e);
    }
  }

  @Test
  public void reduceCatalog() throws Exception {

    final LimitOptionsBuilder limitOptionsBuilder = LimitOptionsBuilder.builder();
    limitOptionsBuilder.includeTables(tableName -> !tableName.matches(".*\\.BOOKS"));

    final SchemaCrawlerOptions schemaCrawlerOptions =
        SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
            .withLimitOptions(limitOptionsBuilder.toOptions());

    // Reduce catalog
    final CatalogReducer reducer = ReducerFactory.getCatalogReducer(schemaCrawlerOptions);
    reducer.reduce(catalog);

    final Schema schema = catalog.lookupSchema("PUBLIC.BOOKS").get();
    assertThat("BOOKS Schema not found", schema, notNullValue());

    assertThat("BOOKS Table not found", catalog.lookupTable(schema, "BOOKS").isEmpty());

    // Reset catalog
    reducer.undo(catalog);

    final Table table = catalog.lookupTable(schema, "BOOKS").get();
    assertThat("BOOKS Table not found", table, notNullValue());
  }

  @Test
  public void simpleTypeName() {
    assertThat(
        MetaDataUtility.getSimpleTypeName(null),
        is(MetaDataUtility.SimpleDatabaseObjectType.unknown));
    assertThat(
        MetaDataUtility.getSimpleTypeName(mock(DatabaseObject.class)),
        is(MetaDataUtility.SimpleDatabaseObjectType.unknown));
    assertThat(
        MetaDataUtility.getSimpleTypeName(mock(DependantObject.class)),
        is(MetaDataUtility.SimpleDatabaseObjectType.unknown));
    assertThat(
        MetaDataUtility.getSimpleTypeName(mock(Synonym.class)),
        is(MetaDataUtility.SimpleDatabaseObjectType.synonym));
    assertThat(
        MetaDataUtility.getSimpleTypeName(mock(Sequence.class)),
        is(MetaDataUtility.SimpleDatabaseObjectType.sequence));
    assertThat(
        MetaDataUtility.getSimpleTypeName(mock(Function.class)),
        is(MetaDataUtility.SimpleDatabaseObjectType.function));
    assertThat(
        MetaDataUtility.getSimpleTypeName(mock(Procedure.class)),
        is(MetaDataUtility.SimpleDatabaseObjectType.procedure));
    assertThat(
        MetaDataUtility.getSimpleTypeName(mock(View.class)),
        is(MetaDataUtility.SimpleDatabaseObjectType.view));
    assertThat(
        MetaDataUtility.getSimpleTypeName(new LightTable("table")),
        is(MetaDataUtility.SimpleDatabaseObjectType.table));
  }

  @ParameterizedTest(name = "[{index}] {0}: Check database object name is system-generated")
  @CsvFileSource(resources = "/system_generated_names.csv", numLinesToSkip = 1)
  public void systemGeneratedName(
      final String database,
      final String pattern,
      final String pkExample,
      final String fkExample,
      final String indexExample,
      final String checkConstraintExample) {

    final String[] names = {pkExample, fkExample, indexExample, checkConstraintExample};
    for (final String name : names) {
      if (isBlank(name)) {
        continue;
      }
      final DatabaseObject dbObject = mock(DatabaseObject.class);
      when(dbObject.getName()).thenReturn(fkExample);
      assertThat(
          "%s: Database object name <%s> should be system-generated".formatted(database, name),
          MetaDataUtility.hasSystemGeneratedName(dbObject),
          is(true));
    }
  }

  @Test
  public void systemGeneratedName_nullArg() {
    assertThat(MetaDataUtility.hasSystemGeneratedName(null), is(false));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void typeName() {
    final NamedObject typedNamedObject =
        mock(NamedObject.class, withSettings().extraInterfaces(TypedObject.class));
    final TypedObject<TableType> typedObject = (TypedObject<TableType>) typedNamedObject;
    when(typedObject.getType()).thenReturn(new TableType("MATERIALIZED VIEW"));
    assertThat(MetaDataUtility.getTypeName(typedNamedObject), is("materialized view"));

    final Schema schema = mock(Schema.class);
    assertThat(MetaDataUtility.getTypeName(schema), is("schema"));

    final Table table = new LightTable("table");
    assertThat(MetaDataUtility.getTypeName(table), is("table"));

    final Sequence sequence = mock(Sequence.class);
    assertThat(MetaDataUtility.getTypeName(sequence), is("sequence"));

    final DatabaseObject unknownDatabaseObject = mock(DatabaseObject.class);
    assertThat(MetaDataUtility.getTypeName(unknownDatabaseObject), is(""));

    final NamedObject nonDatabaseNamedObject = mock(NamedObject.class);
    assertThat(MetaDataUtility.getTypeName(nonDatabaseNamedObject), is(""));

    assertThat(MetaDataUtility.getTypeName(null), is(""));
  }
}
