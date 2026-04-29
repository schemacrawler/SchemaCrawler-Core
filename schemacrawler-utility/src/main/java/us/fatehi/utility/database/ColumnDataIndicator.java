/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.database;

/**
 * Special placeholder values for column data that could not be returned as-is from the database.
 * These values are returned by result-set wrappers in place of actual column data when the data is
 * binary/LOB (and therefore omitted) or when a read error occurred.
 */
public enum ColumnDataIndicator {

  /** Column data was binary or LOB and was not read from the database. */
  BINARY_DATA("<binary>"),

  /** Column data could not be read due to a JDBC error. */
  ERROR_DATA("<error>");

  private final String description;

  ColumnDataIndicator(final String description) {
    this.description = description;
  }

  @Override
  public String toString() {
    return description;
  }
}
