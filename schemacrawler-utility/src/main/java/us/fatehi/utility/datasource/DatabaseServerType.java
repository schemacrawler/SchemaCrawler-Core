/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.datasource;

import static java.util.Objects.requireNonNull;
import static us.fatehi.utility.Utility.requireNotBlank;

import java.io.Serial;
import java.util.Comparator;
import us.fatehi.utility.property.PropertyName;

/**
 * Class that represents an id for SchemaCrawler plugin that allows for crawl customizations for a
 * particular database. The "server" id is used on the SchemaCrawler command-line. It also allows
 * for customizations for the behavior of a particular database driver.
 */
public record DatabaseServerType(PropertyName propertyName)
    implements Comparable<DatabaseServerType> {

  @Serial private static final long serialVersionUID = 2160456864554076419L;

  public static final DatabaseServerType UNKNOWN = new DatabaseServerType("unknown", "Unknown");

  private static final Comparator<DatabaseServerType> SERVER_TYPE_COMPARATOR =
      Comparator.nullsFirst(
          Comparator.comparing(
                  DatabaseServerType::isUnknownDatabaseSystem, Comparator.reverseOrder())
              .thenComparing(DatabaseServerType::propertyName));

  public DatabaseServerType(
      final String databaseSystemIdentifier, final String databaseSystemName) {
    this(new PropertyName(databaseSystemIdentifier, databaseSystemName));
  }

  public DatabaseServerType {
    propertyName = requireNonNull(propertyName, "No property name provided");
    requireNotBlank(propertyName.getDescription(), "No database system name provided");
  }

  /** {@inheritDoc} */
  @Override
  public int compareTo(final DatabaseServerType other) {
    return SERVER_TYPE_COMPARATOR.compare(this, other);
  }

  public String getDatabaseSystemIdentifier() {
    if (isUnknownDatabaseSystem()) {
      return null;
    }
    return propertyName.getName();
  }

  public String getDatabaseSystemName() {
    if (isUnknownDatabaseSystem()) {
      return null;
    }
    return propertyName.getDescription();
  }

  public boolean isUnknownDatabaseSystem() {
    return equals(UNKNOWN);
  }

  @Override
  public String toString() {
    if (isUnknownDatabaseSystem()) {
      return "";
    }
    return propertyName.toString();
  }
}
