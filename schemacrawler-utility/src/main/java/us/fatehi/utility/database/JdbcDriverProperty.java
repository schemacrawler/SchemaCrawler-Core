/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.database;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** JDBC driver property definition extracted from {@link java.sql.DriverPropertyInfo}. */
public record JdbcDriverProperty(
    String name,
    String description,
    boolean required,
    String defaultValue,
    Collection<String> choices) {

  public JdbcDriverProperty {
    name = requireNonNull(name, "Property name required");
    description = description == null ? "" : description;
    choices = choices == null ? List.of() : List.copyOf(new ArrayList<>(choices));
  }
}
