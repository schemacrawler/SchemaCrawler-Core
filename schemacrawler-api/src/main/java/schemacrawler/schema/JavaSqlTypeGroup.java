/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.schema;

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

public enum JavaSqlTypeGroup {
  unknown,
  binary,
  bit,
  character,
  id,
  integer,
  real,
  reference,
  temporal,
  url,
  xml,
  large_object,
  object;

  private static final Map<Integer, JavaSqlTypeGroup> TYPE_MAP = new HashMap<>();

  static {
    register(object, ARRAY, DISTINCT, JAVA_OBJECT, OTHER, STRUCT);
    register(binary, BINARY, LONGVARBINARY, VARBINARY);
    register(bit, BIT, BOOLEAN);
    register(character, CHAR, LONGNVARCHAR, LONGVARCHAR, NCHAR, NVARCHAR, VARCHAR);
    register(id, ROWID);
    register(integer, BIGINT, INTEGER, SMALLINT, TINYINT);
    register(large_object, BLOB, CLOB, NCLOB);
    register(real, DECIMAL, DOUBLE, FLOAT, NUMERIC, REAL);
    register(reference, REF, REF_CURSOR);
    register(temporal, DATE, TIME, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE, TIME_WITH_TIMEZONE);
    register(url, DATALINK);
    register(xml, SQLXML);
  }

  private static void register(final JavaSqlTypeGroup group, final int... types) {
    for (final int type : types) {
      TYPE_MAP.put(type, group);
    }
  }

  public static JavaSqlTypeGroup valueOf(final int type) {
    return TYPE_MAP.getOrDefault(type, unknown);
  }
}
