/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.crawl;

import static java.util.Objects.requireNonNull;
import static schemacrawler.schemacrawler.QueryUtility.executeAgainstSchema;
import static us.fatehi.utility.EnumUtility.enumValue;
import static us.fatehi.utility.EnumUtility.enumValueFromId;
import static us.fatehi.utility.Utility.isBlank;
import static us.fatehi.utility.Utility.isIntegral;
import static us.fatehi.utility.Utility.requireNotBlank;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.schema.ResultsColumn;
import schemacrawler.schema.ResultsColumns;
import schemacrawler.schemacrawler.Query;
import us.fatehi.utility.IdentifiedEnum;
import us.fatehi.utility.database.AbstractDataResultSet;
import us.fatehi.utility.string.StringFormat;

/**
 * A wrapper around a JDBC ResultSet obtained from a database metadata call. This allows type-safe
 * methods to obtain boolean, integer and string data, while abstracting away the quirks of the JDBC
 * metadata API.
 */
final class MetadataResultSet extends AbstractDataResultSet {

  private static final Logger LOGGER = Logger.getLogger(MetadataResultSet.class.getName());

  private final ResultSet results;
  private final ResultsColumns resultsColumns;
  private final String description;
  private Set<ResultsColumn> readColumns;

  MetadataResultSet(
      final Query query, final Statement statement, final Map<String, String> limitMap)
      throws SQLException {
    this(executeAgainstSchema(query, statement, limitMap), query.name());
  }

  MetadataResultSet(final ResultSet resultSet, final String description) throws SQLException {
    super(resultSet);
    setReadLargeData(true);

    results = getResults();
    this.description = requireNotBlank(description, "No result-set description provided");

    resultsColumns = new ResultsCrawler(results).crawl();
    readColumns = new HashSet<>();
  }

  /**
   * Gets unread (and therefore unmapped) columns from the database metadata result-set, and makes
   * them available as additional attributes.
   *
   * @return Map of additional attributes to the database object
   */
  public Map<String, Object> getAttributes() {
    final Map<String, Object> attributes = new HashMap<>();
    for (final ResultsColumn resultsColumn : resultsColumns) {
      if (!readColumns.contains(resultsColumn)) {
        try {
          final String key = resultsColumn.getLabel().toUpperCase();
          final Object value = getColumnData(resultsColumn);
          attributes.put(key, value);
        } catch (final SQLException | ArrayIndexOutOfBoundsException e) {
          /*
           * MySQL connector is broken and can cause ArrayIndexOutOfBoundsExceptions for no good
           * reason (tested with connector 5.1.26 and server version 5.0.95). Ignoring the
           * exception, we can still get some useful data out of the database.
           */
          LOGGER.log(
              Level.WARNING,
              e,
              new StringFormat("Could not read value for column <%s>", resultsColumn));
        }
      }
    }
    return attributes;
  }

  public BigInteger getBigInteger(final String columnName) {
    String stringBigInteger = getString(columnName);
    if (isBlank(stringBigInteger)) {
      return null;
    }
    stringBigInteger = stringBigInteger.replaceAll("[, ]", stringBigInteger);
    BigInteger value;
    try {
      value = new BigInteger(stringBigInteger);
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING, "Could not get big integer value", e);
      return null;
    }
    return value;
  }

  /**
   * Checks if the value of a column from the result set evaluates to true.
   *
   * @param columnName Column name to check
   * @return Whether the string evaluates to true
   */
  public boolean getBoolean(final String columnName) {
    if (useColumn(columnName)) {
      try {
        final Object booleanValue = results.getObject(columnName);
        final String stringBooleanValue;
        if (results.wasNull() || booleanValue == null) {
          LOGGER.log(
              Level.FINER,
              new StringFormat("NULL value for column <%s>, so evaluating to 'false'", columnName));
          return false;
        }
        stringBooleanValue = String.valueOf(booleanValue).strip();

        if (isIntegral(stringBooleanValue)) {
          return !"0".equals(stringBooleanValue);
        }
        return "yes".equalsIgnoreCase(stringBooleanValue)
            || "true".equalsIgnoreCase(stringBooleanValue);
      } catch (final SQLException e) {
        LOGGER.log(
            Level.WARNING,
            e,
            new StringFormat("Could not read boolean value for column <%s>", columnName));
      }
    }
    return false;
  }

  /**
   * Reads the value of a column from the result set as an enum.
   *
   * @param columnName Column name
   * @param defaultValue Default enum value to return
   * @return Enum value of the column, or the default if not available
   */
  public <E extends Enum<E>> E getEnum(final String columnName, final E defaultValue) {
    requireNotBlank(columnName, "No column name provided");
    requireNonNull(defaultValue, "No default value provided");
    final String value = getString(columnName);
    if (isBlank(value)) {
      return defaultValue;
    }
    return enumValue(value.toLowerCase(Locale.ENGLISH), defaultValue);
  }

  /**
   * Reads the value of a column from the result set as an enum.
   *
   * @param columnName Column name
   * @param defaultValue Default enum value to return
   * @return Enum value of the column, or the default if not available
   */
  public <E extends Enum<E> & IdentifiedEnum> E getEnumFromId(
      final String columnName, final E defaultValue) {
    requireNonNull(defaultValue, "No default value provided");
    final int value = getInt(columnName, defaultValue.id());
    return enumValueFromId(value, defaultValue);
  }

  /**
   * Reads the value of a column from the result set as an enum.
   *
   * @param columnName Column name
   * @param defaultValue Default enum value to return
   * @return Enum value of the column, or the default if not available
   */
  public <E extends Enum<E> & IdentifiedEnum> E getEnumFromShortId(
      final String columnName, final E defaultValue) {
    requireNonNull(defaultValue, "No default value provided");
    final int value = getShort(columnName, (short) defaultValue.id());
    return enumValueFromId(value, defaultValue);
  }

  /**
   * Reads the value of a column from the result set as an integer. If the value was null, returns
   * the default.
   *
   * @param columnName Column name
   * @param defaultValue Default value
   * @return Integer value of the column, or the default if not available
   */
  public int getInt(final String columnName, final int defaultValue) {
    int value = defaultValue;
    if (useColumn(columnName)) {
      try {
        value = results.getInt(columnName);
        if (results.wasNull()) {
          LOGGER.log(
              Level.FINER,
              new StringFormat(
                  "NULL int value for column <%s>, so using default %d", columnName, defaultValue));
          value = defaultValue;
        }
      } catch (final SQLException e) {
        LOGGER.log(
            Level.WARNING,
            e,
            new StringFormat("Could not read integer value for column <%s>", columnName));
      }
    }
    return value;
  }

  /**
   * Reads the value of a column from the result set as a long. If the value was null, returns the
   * default.
   *
   * @param columnName Column name
   * @param defaultValue Default value
   * @return Long value of the column, or the default if not available
   */
  public long getLong(final String columnName, final long defaultValue) {
    long value = defaultValue;
    if (useColumn(columnName)) {
      try {
        value = results.getLong(columnName);
        if (results.wasNull()) {
          LOGGER.log(
              Level.FINER,
              new StringFormat(
                  "NULL long value for column <%s>, so using default %d",
                  columnName, defaultValue));
          value = defaultValue;
        }
      } catch (final SQLException e) {
        LOGGER.log(
            Level.WARNING,
            e,
            new StringFormat("Could not read long value for column <%s>", columnName));
      }
    }
    return value;
  }

  /**
   * Reads the value of a column from the result set as a short. If the value was null, returns the
   * default.
   *
   * @param columnName Column name
   * @param defaultValue Default value
   * @return Short value of the column, or the default if not available
   */
  public short getShort(final String columnName, final short defaultValue) {
    short value = defaultValue;
    if (useColumn(columnName)) {
      try {
        value = results.getShort(columnName);
        if (results.wasNull()) {
          LOGGER.log(
              Level.FINER,
              new StringFormat(
                  "NULL short value for column <%s>, so using default %d",
                  columnName, defaultValue));
          value = defaultValue;
        }
      } catch (final SQLException e) {
        LOGGER.log(
            Level.WARNING,
            e,
            new StringFormat("Could not read short value for column <%s>", columnName));
      }
    }
    return value;
  }

  /**
   * Reads the value of a column from the result set as a string.
   *
   * @param columnName Column name
   * @return String value of the column, or null if not available
   */
  public String getString(final String columnName) {
    String value = null;
    if (useColumn(columnName)) {
      try {
        value = results.getString(columnName);
        if (results.wasNull()) {
          value = null;
        }

        if (value != null) {
          value = value.strip();
        }
      } catch (final SQLException e) {
        LOGGER.log(
            Level.WARNING,
            e,
            new StringFormat("Could not read string value for column <%s>", columnName));
      }
    }
    return value;
  }

  /**
   * Moves the cursor down one row from its current position. A <code>ResultSet</code> cursor is
   * initially positioned before the first row; the first call to the method <code>next</code> makes
   * the first row the current row; the second call makes the second row the current row, and so on.
   *
   * @return <code>true</code> if the new current row is valid; <code>false</code> if there are no
   *     more rows
   * @throws SQLException On a database access error
   */
  public boolean next() throws SQLException {
    readColumns = new HashSet<>();

    return advanceNext();
  }

  private Object getColumnData(final ResultsColumn resultsColumn) throws SQLException {
    final int ordinalPosition = resultsColumn.getOrdinalPosition();

    return readColumnData(ordinalPosition);
  }

  private boolean useColumn(final String columnName) {
    final Optional<ResultsColumn> optionalResultsColumn = resultsColumns.lookupColumn(columnName);
    optionalResultsColumn.ifPresent(readColumns::add);
    return optionalResultsColumn.isPresent();
  }
}
