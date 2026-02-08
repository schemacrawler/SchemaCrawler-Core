/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import schemacrawler.plugin.EnumDataTypeInfo;
import schemacrawler.plugin.EnumDataTypeInfo.EnumDataTypeTypes;
import schemacrawler.schemacrawler.InformationSchemaKey;
import schemacrawler.schemacrawler.InformationSchemaViews;
import schemacrawler.schemacrawler.InformationSchemaViewsBuilder;
import schemacrawler.schemacrawler.MetadataRetrievalStrategy;
import schemacrawler.schemacrawler.SchemaInfoMetadataRetrievalStrategy;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;
import schemacrawler.schemacrawler.SchemaRetrievalOptionsBuilder;
import us.fatehi.test.utility.TestObjectUtility;
import us.fatehi.utility.datasource.DatabaseServerType;

public class SchemaRetrievalOptionsBuilderTest {

  @Test
  public void connectionInitializer() {
    final Connection connection = TestObjectUtility.mockConnection();

    final SchemaRetrievalOptionsBuilder builder = SchemaRetrievalOptionsBuilder.builder();

    final SchemaRetrievalOptions defaultSchemaRetrievalOptions = builder.toOptions();
    defaultSchemaRetrievalOptions.getConnectionInitializer().accept(connection);
    verifyNoInteractions(connection);

    builder.withConnectionInitializer(
        conn -> {
          throw new RuntimeException("Test forced exception");
        });
    final SchemaRetrievalOptions throwingOptions = builder.toOptions();
    final RuntimeException runtimeException1 =
        assertThrows(
            RuntimeException.class,
            () -> throwingOptions.getConnectionInitializer().accept(connection));
    assertThat(runtimeException1.getMessage(), is("Test forced exception"));

    builder.withConnectionInitializer(null);
    final SchemaRetrievalOptions resetOptions = builder.toOptions();
    resetOptions.getConnectionInitializer().accept(connection);
    verifyNoInteractions(connection);
  }

  @Test
  public void dbMetaData() throws SQLException {

    final DatabaseMetaData dbMetaData = TestObjectUtility.mockDatabaseMetaData();
    when(dbMetaData.supportsCatalogsInTableDefinitions()).thenReturn(false);
    when(dbMetaData.supportsSchemasInTableDefinitions()).thenReturn(true);
    when(dbMetaData.getIdentifierQuoteString()).thenReturn("@");

    final Connection connection = TestObjectUtility.mockConnection();
    when(connection.getMetaData()).thenReturn(dbMetaData);

    SchemaRetrievalOptionsBuilder builder;

    builder = SchemaRetrievalOptionsBuilder.builder();
    final SchemaRetrievalOptions defaultSchemaRetrievalOptions = builder.toOptions();
    assertThat(defaultSchemaRetrievalOptions.isSupportsCatalogs(), is(true));
    assertThat(defaultSchemaRetrievalOptions.isSupportsSchemas(), is(true));
    assertThat(defaultSchemaRetrievalOptions.getTypeMap(), is(aMapWithSize(39)));
    assertThat(defaultSchemaRetrievalOptions.getIdentifierQuoteString(), is(""));

    builder.fromConnnection(connection);
    final SchemaRetrievalOptions schemaRetrievalOptions = builder.toOptions();
    assertThat(schemaRetrievalOptions.isSupportsCatalogs(), is(false));
    assertThat(schemaRetrievalOptions.isSupportsSchemas(), is(true));
    assertThat(schemaRetrievalOptions.getTypeMap(), is(aMapWithSize(39)));
    assertThat(schemaRetrievalOptions.getIdentifierQuoteString(), is("@"));
  }

  @Test
  public void dbMetaData_none() throws SQLException {

    SchemaRetrievalOptionsBuilder builder;

    builder = SchemaRetrievalOptionsBuilder.builder();
    SchemaRetrievalOptions schemaRetrievalOptions = builder.toOptions();
    assertThat(schemaRetrievalOptions.isSupportsCatalogs(), is(true));
    assertThat(schemaRetrievalOptions.isSupportsSchemas(), is(true));
    assertThat(schemaRetrievalOptions.getTypeMap(), is(aMapWithSize(39)));
    builder.fromConnnection(null);
    schemaRetrievalOptions = builder.toOptions();
    assertThat(schemaRetrievalOptions.isSupportsCatalogs(), is(true));
    assertThat(schemaRetrievalOptions.isSupportsSchemas(), is(true));
    assertThat(schemaRetrievalOptions.getTypeMap(), is(aMapWithSize(39)));

    final Connection connection = TestObjectUtility.mockConnection();
    when(connection.getMetaData()).thenThrow(SQLException.class);

    builder = SchemaRetrievalOptionsBuilder.builder();
    builder.fromConnnection(connection);
    schemaRetrievalOptions = builder.toOptions();
    assertThat(schemaRetrievalOptions.isSupportsCatalogs(), is(true));
    assertThat(schemaRetrievalOptions.isSupportsSchemas(), is(true));
    assertThat(schemaRetrievalOptions.getTypeMap(), is(aMapWithSize(39)));
    assertThat(schemaRetrievalOptions.getIdentifierQuoteString(), is("\""));
  }

  @Test
  public void dbMetaData_overrides() throws SQLException {

    final DatabaseMetaData dbMetaData = TestObjectUtility.mockDatabaseMetaData();
    when(dbMetaData.supportsCatalogsInTableDefinitions()).thenReturn(false);
    when(dbMetaData.supportsSchemasInTableDefinitions()).thenReturn(true);
    when(dbMetaData.getIdentifierQuoteString()).thenReturn("#");

    final Connection connection = TestObjectUtility.mockConnection();
    when(connection.getMetaData()).thenReturn(dbMetaData);

    SchemaRetrievalOptionsBuilder builder;

    builder = SchemaRetrievalOptionsBuilder.builder();
    builder.withSupportsCatalogs();
    builder.withSupportsSchemas();
    builder.withIdentifierQuoteString("@");

    SchemaRetrievalOptions schemaRetrievalOptions = builder.toOptions();
    assertThat(schemaRetrievalOptions.isSupportsCatalogs(), is(true));
    assertThat(schemaRetrievalOptions.isSupportsSchemas(), is(true));
    assertThat(schemaRetrievalOptions.getIdentifierQuoteString(), is("@"));
    builder.fromConnnection(connection);
    schemaRetrievalOptions = builder.toOptions();
    assertThat(schemaRetrievalOptions.isSupportsCatalogs(), is(true));
    assertThat(schemaRetrievalOptions.isSupportsSchemas(), is(true));
    assertThat(schemaRetrievalOptions.getTypeMap(), is(aMapWithSize(39)));
    assertThat(schemaRetrievalOptions.getIdentifierQuoteString(), is("@"));

    builder = SchemaRetrievalOptionsBuilder.builder();
    builder.withTypeMap(new HashMap<>());
    schemaRetrievalOptions = builder.toOptions();
    assertThat(schemaRetrievalOptions.getTypeMap(), is(aMapWithSize(0)));
    builder.fromConnnection(connection);
    schemaRetrievalOptions = builder.toOptions();
    assertThat(schemaRetrievalOptions.getTypeMap(), is(aMapWithSize(0)));

    when(dbMetaData.getIdentifierQuoteString()).thenReturn("\t");
    builder = SchemaRetrievalOptionsBuilder.builder();
    builder.fromConnnection(connection);
    schemaRetrievalOptions = builder.toOptions();
    assertThat(schemaRetrievalOptions.getIdentifierQuoteString(), is(""));
  }

  @Test
  public void dbServerType() {
    final SchemaRetrievalOptionsBuilder builder = SchemaRetrievalOptionsBuilder.builder();

    SchemaRetrievalOptions schemaRetrievalOptions = builder.toOptions();
    assertThat(schemaRetrievalOptions.getDatabaseServerType(), is(DatabaseServerType.UNKNOWN));

    builder.withDatabaseServerType(new DatabaseServerType("newdb", "New Database"));
    schemaRetrievalOptions = builder.toOptions();
    assertThat(
        schemaRetrievalOptions.getDatabaseServerType().getDatabaseSystemIdentifier(), is("newdb"));

    builder.withDatabaseServerType(null);
    schemaRetrievalOptions = builder.toOptions();
    assertThat(schemaRetrievalOptions.getDatabaseServerType(), is(DatabaseServerType.UNKNOWN));
  }

  @Test
  public void enumDataTypeHelper() {
    final SchemaRetrievalOptionsBuilder builder = SchemaRetrievalOptionsBuilder.builder();

    assertThat(
        builder.toOptions().getEnumDataTypeHelper().getEnumDataTypeInfo(null, null, null).getType(),
        is(EnumDataTypeInfo.EnumDataTypeTypes.not_enumerated));

    builder.withEnumDataTypeHelper(
        (column, columnDataType, connection) ->
            new EnumDataTypeInfo(EnumDataTypeTypes.enumerated_column, emptyList()));
    assertThat(
        builder.toOptions().getEnumDataTypeHelper().getEnumDataTypeInfo(null, null, null).getType(),
        is(EnumDataTypeInfo.EnumDataTypeTypes.enumerated_column));

    builder.withEnumDataTypeHelper(null);
    assertThat(
        builder.toOptions().getEnumDataTypeHelper().getEnumDataTypeInfo(null, null, null).getType(),
        is(EnumDataTypeInfo.EnumDataTypeTypes.not_enumerated));
  }

  @Test
  public void fromOptions() {
    final SchemaRetrievalOptions options =
        SchemaRetrievalOptionsBuilder.newSchemaRetrievalOptions();
    final SchemaRetrievalOptionsBuilder builder = SchemaRetrievalOptionsBuilder.builder(options);
    final SchemaRetrievalOptions schemaRetrievalOptions = builder.toOptions();
    assertThat(schemaRetrievalOptions.isSupportsCatalogs(), is(true));
    assertThat(schemaRetrievalOptions.isSupportsSchemas(), is(true));
    assertThat(schemaRetrievalOptions.getTypeMap(), is(aMapWithSize(39)));
  }

  @Test
  public void fromOptions_null() {
    final SchemaRetrievalOptionsBuilder builder =
        SchemaRetrievalOptionsBuilder.builder().fromOptions(null);
    final SchemaRetrievalOptions schemaRetrievalOptions = builder.toOptions();
    assertThat(schemaRetrievalOptions.isSupportsCatalogs(), is(true));
    assertThat(schemaRetrievalOptions.isSupportsSchemas(), is(true));
    assertThat(schemaRetrievalOptions.getTypeMap(), is(aMapWithSize(39)));
  }

  @Test
  public void identifierQuoteString() {
    final SchemaRetrievalOptionsBuilder builder = SchemaRetrievalOptionsBuilder.builder();

    SchemaRetrievalOptions schemaRetrievalOptions = builder.toOptions();
    assertThat(schemaRetrievalOptions.getIdentifierQuoteString(), is(""));

    builder.withIdentifierQuoteString("@");
    schemaRetrievalOptions = builder.toOptions();
    assertThat(schemaRetrievalOptions.getIdentifierQuoteString(), is("@"));

    builder.withoutIdentifierQuoteString();
    schemaRetrievalOptions = builder.toOptions();
    assertThat(schemaRetrievalOptions.getIdentifierQuoteString(), is(""));

    builder.withIdentifierQuoteString(null);
    schemaRetrievalOptions = builder.toOptions();
    assertThat(schemaRetrievalOptions.getIdentifierQuoteString(), is(""));

    builder.withIdentifierQuoteString("\t");
    schemaRetrievalOptions = builder.toOptions();
    assertThat(schemaRetrievalOptions.getIdentifierQuoteString(), is(""));
  }

  @Test
  public void informationSchemaViews() {

    final InformationSchemaViews informationSchemaViews =
        InformationSchemaViewsBuilder.builder()
            .withSql(InformationSchemaKey.ADDITIONAL_COLUMN_ATTRIBUTES, "SELECT * FROM DUAL")
            .toOptions();

    final SchemaRetrievalOptionsBuilder builder = SchemaRetrievalOptionsBuilder.builder();

    SchemaRetrievalOptions schemaRetrievalOptions = builder.toOptions();
    assertThat(schemaRetrievalOptions.getInformationSchemaViews().isEmpty(), is(true));

    builder.withInformationSchemaViews(informationSchemaViews);
    schemaRetrievalOptions = builder.toOptions();
    assertThat(schemaRetrievalOptions.getInformationSchemaViews().isEmpty(), is(false));
    assertThat(
        schemaRetrievalOptions
            .getInformationSchemaViews()
            .hasQuery(InformationSchemaKey.ADDITIONAL_COLUMN_ATTRIBUTES),
        is(true));

    builder.withInformationSchemaViews(null);
    schemaRetrievalOptions = builder.toOptions();
    assertThat(schemaRetrievalOptions.getInformationSchemaViews().isEmpty(), is(true));
  }

  @Test
  public void metadataRetrievalStrategy() {
    final SchemaRetrievalOptionsBuilder builder = SchemaRetrievalOptionsBuilder.builder();

    MetadataRetrievalStrategy metadataRetrievalStrategy;

    metadataRetrievalStrategy =
        builder.toOptions().get(SchemaInfoMetadataRetrievalStrategy.foreignKeysRetrievalStrategy);
    assertThat(metadataRetrievalStrategy, is(MetadataRetrievalStrategy.metadata));

    builder.with(
        SchemaInfoMetadataRetrievalStrategy.foreignKeysRetrievalStrategy,
        MetadataRetrievalStrategy.data_dictionary_all);
    metadataRetrievalStrategy =
        builder.toOptions().get(SchemaInfoMetadataRetrievalStrategy.foreignKeysRetrievalStrategy);
    assertThat(metadataRetrievalStrategy, is(MetadataRetrievalStrategy.data_dictionary_all));

    assertThrows(NullPointerException.class, () -> builder.toOptions().get(null));

    // -- Set with variations of null arguments

    // 1.
    // Setup
    builder.with(
        SchemaInfoMetadataRetrievalStrategy.foreignKeysRetrievalStrategy,
        MetadataRetrievalStrategy.data_dictionary_all);
    metadataRetrievalStrategy =
        builder.toOptions().get(SchemaInfoMetadataRetrievalStrategy.foreignKeysRetrievalStrategy);
    assertThat(metadataRetrievalStrategy, is(MetadataRetrievalStrategy.data_dictionary_all));
    // Test
    builder.with(SchemaInfoMetadataRetrievalStrategy.foreignKeysRetrievalStrategy, null);
    metadataRetrievalStrategy =
        builder.toOptions().get(SchemaInfoMetadataRetrievalStrategy.foreignKeysRetrievalStrategy);
    assertThat(metadataRetrievalStrategy, is(MetadataRetrievalStrategy.metadata));

    // 2.
    // Setup
    builder.with(
        SchemaInfoMetadataRetrievalStrategy.foreignKeysRetrievalStrategy,
        MetadataRetrievalStrategy.data_dictionary_all);
    metadataRetrievalStrategy =
        builder.toOptions().get(SchemaInfoMetadataRetrievalStrategy.foreignKeysRetrievalStrategy);
    assertThat(metadataRetrievalStrategy, is(MetadataRetrievalStrategy.data_dictionary_all));
    // Test
    builder.with(null, MetadataRetrievalStrategy.metadata);
    metadataRetrievalStrategy =
        builder.toOptions().get(SchemaInfoMetadataRetrievalStrategy.foreignKeysRetrievalStrategy);
    assertThat(metadataRetrievalStrategy, is(MetadataRetrievalStrategy.data_dictionary_all));

    // 3.
    // Setup
    builder.with(
        SchemaInfoMetadataRetrievalStrategy.foreignKeysRetrievalStrategy,
        MetadataRetrievalStrategy.data_dictionary_all);
    metadataRetrievalStrategy =
        builder.toOptions().get(SchemaInfoMetadataRetrievalStrategy.foreignKeysRetrievalStrategy);
    assertThat(metadataRetrievalStrategy, is(MetadataRetrievalStrategy.data_dictionary_all));
    // Test
    builder.with(null, null);
    metadataRetrievalStrategy =
        builder.toOptions().get(SchemaInfoMetadataRetrievalStrategy.foreignKeysRetrievalStrategy);
    assertThat(metadataRetrievalStrategy, is(MetadataRetrievalStrategy.data_dictionary_all));
  }

  @Test
  public void override_catalog_schema() throws SQLException {
    final DatabaseMetaData dbMetaData = TestObjectUtility.mockDatabaseMetaData();
    when(dbMetaData.supportsCatalogsInTableDefinitions()).thenReturn(false);
    when(dbMetaData.supportsSchemasInTableDefinitions()).thenReturn(true);

    final Connection connection = TestObjectUtility.mockConnection();
    when(connection.getMetaData()).thenReturn(dbMetaData);

    final SchemaRetrievalOptionsBuilder builder = SchemaRetrievalOptionsBuilder.builder();
    builder.fromConnnection(connection);

    SchemaRetrievalOptions schemaRetrievalOptions = builder.toOptions();
    assertThat(schemaRetrievalOptions.isSupportsCatalogs(), is(false));
    assertThat(schemaRetrievalOptions.isSupportsSchemas(), is(true));

    builder.withSupportsCatalogs();
    schemaRetrievalOptions = builder.toOptions();
    assertThat(schemaRetrievalOptions.isSupportsCatalogs(), is(true));

    builder.withoutSupportsCatalogs();
    schemaRetrievalOptions = builder.toOptions();
    assertThat(schemaRetrievalOptions.isSupportsCatalogs(), is(false));

    builder.withDoesNotSupportCatalogs();
    schemaRetrievalOptions = builder.toOptions();
    assertThat(schemaRetrievalOptions.isSupportsCatalogs(), is(false));

    builder.withSupportsSchemas();
    schemaRetrievalOptions = builder.toOptions();
    assertThat(schemaRetrievalOptions.isSupportsSchemas(), is(true));

    builder.withoutSupportsSchemas();
    schemaRetrievalOptions = builder.toOptions();
    assertThat(schemaRetrievalOptions.isSupportsSchemas(), is(true));

    builder.withDoesNotSupportSchemas();
    schemaRetrievalOptions = builder.toOptions();
    assertThat(schemaRetrievalOptions.isSupportsSchemas(), is(false));
  }

  @Test
  public void toOptions() {
    final SchemaRetrievalOptionsBuilder builder = SchemaRetrievalOptionsBuilder.builder();
    final SchemaRetrievalOptions schemaRetrievalOptions = builder.toOptions();
    assertThat(
        schemaRetrievalOptions.toString(),
        containsString("\"@object\": \"" + schemaRetrievalOptions.getClass().getName() + "\""));
  }

  @Test
  public void typeMap() {
    final SchemaRetrievalOptionsBuilder builder = SchemaRetrievalOptionsBuilder.builder();

    assertThat(builder.toOptions().getTypeMap(), is(aMapWithSize(39)));

    final Map<String, Class<?>> typeMap = new HashMap<>();
    typeMap.put(String.class.getSimpleName(), String.class);
    builder.withTypeMap(typeMap);
    assertThat(builder.toOptions().getTypeMap(), is(aMapWithSize(1)));
    assertThat(builder.toOptions().getTypeMap(), hasKey("String"));

    builder.withTypeMap(null);
    assertThat(builder.toOptions().getTypeMap(), is(aMapWithSize(39)));
  }
}
