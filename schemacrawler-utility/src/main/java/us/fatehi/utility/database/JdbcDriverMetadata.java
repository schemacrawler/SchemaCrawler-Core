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

/** Complete JDBC driver metadata including available connection properties. */
public record JdbcDriverMetadata(
    JdbcDriver jdbcDriver, Collection<JdbcDriverPropertyInfo> properties) {

  public JdbcDriverMetadata {
    jdbcDriver = requireNonNull(jdbcDriver, "Driver required");
    properties = properties == null ? List.of() : List.copyOf(new ArrayList<>(properties));
  }

  public JdbcDriverMetadata() {
    this(new JdbcDriver(), null);
  }
}
