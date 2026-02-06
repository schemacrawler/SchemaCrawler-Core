/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.utility;

import static java.sql.Types.ARRAY;
import static java.sql.Types.BIGINT;
import static java.sql.Types.BINARY;
import static java.sql.Types.BIT;
import static java.sql.Types.BLOB;
import static java.sql.Types.BOOLEAN;
import static java.sql.Types.CHAR;
import static java.sql.Types.CLOB;
import static java.sql.Types.DATALINK;
import static java.sql.Types.DATE;
import static java.sql.Types.DECIMAL;
import static java.sql.Types.DISTINCT;
import static java.sql.Types.DOUBLE;
import static java.sql.Types.FLOAT;
import static java.sql.Types.INTEGER;
import static java.sql.Types.JAVA_OBJECT;
import static java.sql.Types.LONGNVARCHAR;
import static java.sql.Types.LONGVARBINARY;
import static java.sql.Types.LONGVARCHAR;
import static java.sql.Types.NCHAR;
import static java.sql.Types.NCLOB;
import static java.sql.Types.NUMERIC;
import static java.sql.Types.NVARCHAR;
import static java.sql.Types.OTHER;
import static java.sql.Types.REAL;
import static java.sql.Types.REF;
import static java.sql.Types.REF_CURSOR;
import static java.sql.Types.ROWID;
import static java.sql.Types.SMALLINT;
import static java.sql.Types.SQLXML;
import static java.sql.Types.STRUCT;
import static java.sql.Types.TIME;
import static java.sql.Types.TIMESTAMP;
import static java.sql.Types.TIMESTAMP_WITH_TIMEZONE;
import static java.sql.Types.TIME_WITH_TIMEZONE;
import static java.sql.Types.TINYINT;
import static java.sql.Types.VARBINARY;
import static java.sql.Types.VARCHAR;
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

    if (columnDataType.isEnumerated()) {
      return EntityAttributeType.enumerated;
    }

    final EntityAttributeType attributeType =
        switch (columnDataType.getJavaSqlType().getVendorTypeNumber()) {
          case ARRAY,
              DISTINCT,
              JAVA_OBJECT,
              OTHER,
              STRUCT,
              ROWID,
              REF,
              REF_CURSOR,
              DATALINK,
              SQLXML ->
              EntityAttributeType.other;
          case BINARY, LONGVARBINARY, VARBINARY, BLOB -> EntityAttributeType.binary;
          case BIT, BOOLEAN -> EntityAttributeType.bool;
          case CHAR, LONGNVARCHAR, LONGVARCHAR, NCHAR, NVARCHAR, VARCHAR, CLOB, NCLOB ->
              EntityAttributeType.string;
          case BIGINT, INTEGER, SMALLINT, TINYINT -> EntityAttributeType.integer;
          case DECIMAL, DOUBLE, FLOAT, NUMERIC, REAL -> EntityAttributeType.decimal;
          case DATE -> EntityAttributeType.date;
          case TIME, TIME_WITH_TIMEZONE -> EntityAttributeType.time;
          case TIMESTAMP, TIMESTAMP_WITH_TIMEZONE -> EntityAttributeType.timestamp;
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
