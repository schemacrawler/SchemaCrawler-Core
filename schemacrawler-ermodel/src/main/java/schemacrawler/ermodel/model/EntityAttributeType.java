/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.model;

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

import java.util.HashMap;
import java.util.Map;
import schemacrawler.schema.ColumnDataType;

/** Represents the entity attribute type, corresponding to the data type of the backing column. */
public enum EntityAttributeType {
  unknown,
  binary,
  bool,
  date,
  decimal,
  enumerated,
  integer,
  other,
  string,
  time,
  timestamp,
  ;

  private static final Map<Integer, EntityAttributeType> TYPE_MAP = new HashMap<>();

  static {
    register(
        other,
        ARRAY,
        DISTINCT,
        JAVA_OBJECT,
        OTHER,
        STRUCT,
        ROWID,
        REF,
        REF_CURSOR,
        DATALINK,
        SQLXML);
    register(binary, BINARY, LONGVARBINARY, VARBINARY, BLOB);
    register(bool, BIT, BOOLEAN);
    register(string, CHAR, LONGNVARCHAR, LONGVARCHAR, NCHAR, NVARCHAR, VARCHAR, CLOB, NCLOB);
    register(integer, BIGINT, INTEGER, SMALLINT, TINYINT);
    register(decimal, DECIMAL, DOUBLE, FLOAT, NUMERIC, REAL);
    register(date, DATE);
    register(time, TIME, TIME_WITH_TIMEZONE);
    register(timestamp, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE);
  }

  private static void register(final EntityAttributeType type, final int... sqlTypes) {
    for (final int sqlType : sqlTypes) {
      TYPE_MAP.put(sqlType, type);
    }
  }

  public static EntityAttributeType from(final ColumnDataType columnDataType) {
    if (columnDataType == null) {
      return unknown;
    }
    if (columnDataType.isEnumerated()) {
      return enumerated;
    }
    return TYPE_MAP.getOrDefault(columnDataType.getJavaSqlType().getVendorTypeNumber(), unknown);
  }
}
