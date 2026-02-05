/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.utility;

import static java.util.Objects.requireNonNull;
import static schemacrawler.utility.MetaDataUtility.isPartial;

import schemacrawler.ermodel.implementation.ERModelBuilder;
import schemacrawler.ermodel.implementation.TableEntityModelInferrer;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.ermodel.model.EntityAttributeType;
import schemacrawler.ermodel.model.EntityType;
import schemacrawler.ermodel.model.RelationshipCardinality;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableReference;
import us.fatehi.utility.OptionalBoolean;
import us.fatehi.utility.UtilityMarker;

/** Utility for inferring entity model information from tables and foreign keys. */
@UtilityMarker
public class EntityModelUtility {

  public static ERModel buildERModel(final Catalog catalog) {
    requireNonNull(catalog, "No catalog provided");
    return new ERModelBuilder(catalog).build();
  }

  /**
   * Checks if a foreign key is covered by an index.
   *
   * @param fk Foreign key
   * @return Whether the foreign key is covered by an index
   */
  public static OptionalBoolean coveredByIndex(final TableReference fk) {
    if (fk == null) {
      return OptionalBoolean.unknown;
    }

    final Table table = fk.getForeignKeyTable();
    if (table == null || isPartial(table)) {
      return OptionalBoolean.unknown;
    }

    final TableEntityModelInferrer tableEntityModel = new TableEntityModelInferrer(table);
    final OptionalBoolean coveredByIndex = tableEntityModel.coveredByIndex(fk);
    return coveredByIndex;
  }

  /**
   * Checks if a foreign key is covered by a unique index.
   *
   * @param fk Foreign key
   * @return Whether the foreign key is covered by a unique index
   */
  public static OptionalBoolean coveredByUniqueIndex(final TableReference fk) {
    if (fk == null) {
      return OptionalBoolean.unknown;
    }

    final Table table = fk.getForeignKeyTable();
    if (table == null || isPartial(table)) {
      return OptionalBoolean.unknown;
    }

    final TableEntityModelInferrer tableEntityModel = new TableEntityModelInferrer(table);
    final OptionalBoolean coveredByIndex = tableEntityModel.coveredByUniqueIndex(fk);
    return coveredByIndex;
  }

  /**
   * Infers if the table is a bridge table.
   *
   * @param table Table
   * @return Inferred bridge table type
   */
  public static OptionalBoolean inferBridgeTable(final Table table) {
    if (table == null || isPartial(table)) {
      return OptionalBoolean.unknown;
    }

    final TableEntityModelInferrer tableEntityModel = new TableEntityModelInferrer(table);
    final boolean isBridgeTable = tableEntityModel.inferBridgeTable();
    return OptionalBoolean.fromBoolean(isBridgeTable);
  }

  /**
   * Infers the cardinality of a foreign key.
   *
   * @param fk Foreign key
   * @return Inferred cardinality
   */
  public static RelationshipCardinality inferCardinality(final TableReference fk) {
    if (fk == null) {
      return RelationshipCardinality.unknown;
    }

    final Table table = fk.getForeignKeyTable();
    if (table == null || isPartial(table)) {
      return RelationshipCardinality.unknown;
    }

    final TableEntityModelInferrer tableEntityModel = new TableEntityModelInferrer(table);
    final RelationshipCardinality fkCardinality = tableEntityModel.inferCardinality(fk);
    return fkCardinality;
  }

  public static EntityAttributeType inferEntityAttributeType(final ColumnDataType columnDataType) {
    if (columnDataType == null) {
      return EntityAttributeType.unknown;
    }

    final EntityAttributeType attributeType =
        switch (columnDataType.getJavaSqlType().getVendorTypeNumber()) {
          case java.sql.Types.ARRAY,
              java.sql.Types.DISTINCT,
              java.sql.Types.JAVA_OBJECT,
              java.sql.Types.OTHER,
              java.sql.Types.STRUCT,
              java.sql.Types.ROWID,
              java.sql.Types.REF,
              java.sql.Types.REF_CURSOR,
              java.sql.Types.DATALINK,
              java.sql.Types.SQLXML ->
              EntityAttributeType.other;
          case java.sql.Types.BINARY,
              java.sql.Types.LONGVARBINARY,
              java.sql.Types.VARBINARY,
              java.sql.Types.BLOB ->
              EntityAttributeType.binary;
          case java.sql.Types.BIT, java.sql.Types.BOOLEAN -> EntityAttributeType.bool;
          case java.sql.Types.CHAR,
              java.sql.Types.LONGNVARCHAR,
              java.sql.Types.LONGVARCHAR,
              java.sql.Types.NCHAR,
              java.sql.Types.NVARCHAR,
              java.sql.Types.VARCHAR,
              java.sql.Types.CLOB,
              java.sql.Types.NCLOB ->
              EntityAttributeType.string;
          case java.sql.Types.BIGINT,
              java.sql.Types.INTEGER,
              java.sql.Types.SMALLINT,
              java.sql.Types.TINYINT ->
              EntityAttributeType.integer;
          case java.sql.Types.DECIMAL,
              java.sql.Types.DOUBLE,
              java.sql.Types.FLOAT,
              java.sql.Types.NUMERIC,
              java.sql.Types.REAL ->
              EntityAttributeType.decimal;
          case java.sql.Types.DATE -> EntityAttributeType.date;
          case java.sql.Types.TIME, java.sql.Types.TIME_WITH_TIMEZONE -> EntityAttributeType.time;
          case java.sql.Types.TIMESTAMP, java.sql.Types.TIMESTAMP_WITH_TIMEZONE ->
              EntityAttributeType.timestamp;
          default -> EntityAttributeType.unknown;
        };
    return attributeType;
  }

  /**
   * Infers the entity type of a table.
   *
   * @param table Table
   * @return Inferred entity type
   */
  public static EntityType inferEntityType(final Table table) {
    if (table == null || isPartial(table)) {
      return EntityType.unknown;
    }

    final TableEntityModelInferrer tableEntityModel = new TableEntityModelInferrer(table);
    final EntityType entityType = tableEntityModel.inferEntityType();
    return entityType;
  }

  private EntityModelUtility() {
    // Prevent instantiation
  }
}
