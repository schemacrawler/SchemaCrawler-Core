/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.jdbc.serverfingerprint;

import static us.fatehi.utility.Utility.isBlank;
import static us.fatehi.utility.Utility.trimToEmpty;

/**
 * The structured result of tokenizing a JDBC connection URL, as produced by {@link
 * JdbcUrlTokenizer#tokenize(String)}.
 *
 * <p>All string components are normalized to lower case (and blank/{@code null} input is normalized
 * to {@code ""}) by the compact constructor, so callers never need to null-check or case-normalize
 * these fields themselves.
 *
 * @param databaseSystemIdentifier the database system/driver identifier (e.g. {@code "mysql"},
 *     {@code "postgresql"}), taken from the {@code jdbc:<identifier>:...} prefix. Blank if the
 *     input was not a well-formed {@code jdbc:} URL.
 * @param host the primary host, as reported for fingerprinting purposes. This is either: (a) a real
 *     hostname/address, masked to a fixed placeholder such as {@code "<localhost>"} or {@code
 *     "<internal>"} when {@code hostClassification} is not {@code PUBLIC} (so that no
 *     privately-scoped hostname or IP address leaks into the tokenized output), or (b) a literal
 *     embedded-database mode token (e.g. {@code "mem"}, {@code "file"}, {@code "res"}, or SQLite's
 *     {@code ":memory:"}), reported verbatim and never masked, even though such tokens usually
 *     carry a {@code LOCALHOST} or {@code INTERNAL} classification. Blank if no host or mode token
 *     was present.
 * @param databaseName the database/schema name, or the best available fallback (a
 *     semicolon-property value, a mode token's remainder, or an unparsed local path). Blank if none
 *     was present.
 * @param hostClassification the network reachability classification of {@code host}; see {@link
 *     HostClassification}. Never {@code null} - defaults to {@code UNKNOWN} when no host
 *     information is available at all.
 * @param port the primary host's port number, if one was present and parsed as a valid integer;
 *     {@code null} otherwise (no port specified, a non-numeric port segment, or a host form - such
 *     as an embedded-database mode token - that has no notion of a port). This field is
 *     deliberately <b>not</b> used by {@link DatabaseServerFingerprintBuilder} for hashing or
 *     confidence scoring: two connections to the same logical server on different ports are still
 *     considered the same server identity for fingerprinting purposes.
 */
record JdbcUrlTokens(
    String databaseSystemIdentifier,
    String host,
    Integer port,
    String databaseName,
    HostClassification hostClassification) {

  JdbcUrlTokens {
    databaseSystemIdentifier = normalizeToken(databaseSystemIdentifier);
    host = normalizeToken(host);
    databaseName = normalizeToken(databaseName);
    if (hostClassification == null) {
      hostClassification = HostClassification.UNKNOWN;
    }
  }

  JdbcUrlTokens() {
    this(null, null, null, null, null);
  }

  boolean hasDatabaseName() {
    return !isBlank(databaseName);
  }

  boolean hasDatabaseSystemIdentifier() {
    return !isBlank(databaseSystemIdentifier);
  }

  boolean hasPort() {
    return port != null;
  }

  boolean hasHost() {
    return !isBlank(host);
  }

  boolean hasPublicHost() {
    return hasHost() && hostClassification == HostClassification.PUBLIC;
  }

  private static String normalizeToken(final String value) {
    return trimToEmpty(value).toLowerCase();
  }
}
