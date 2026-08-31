/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.jdbc.serverfingerprint;

import static java.util.Objects.requireNonNull;
import static us.fatehi.utility.Utility.requireNotBlank;

import java.util.LinkedHashMap;
import java.util.Map;
import us.fatehi.utility.Builder;
import us.fatehi.utility.Nullable;
import us.fatehi.utility.Utility;
import us.fatehi.utility.property.ProductVersion;

/**
 * Builds a {@link DatabaseServerFingerprint}, which reports two distinct and intentionally
 * independent signals:
 *
 * <ul>
 *   <li><b>{@code fingerprint}</b> (a hash) - answers "has anything about this server changed since
 *       we last saw it?" It is derived from the JDBC URL plus the reported database product
 *       version, so the hash value changes across a version or patch upgrade of the same physical
 *       server. This lets consumers detect drift between crawls.
 *   <li><b>{@code confidence}</b> - answers "how certain are we that this fingerprint identifies
 *       one specific, distinguishable server?" It is derived purely from JDBC URL and network
 *       structure (database type, host, database name, and whether the host is public), and
 *       deliberately does not take the product version into account. A version or patch upgrade
 *       does not change which server is being identified, so it should not affect confidence - only
 *       the fingerprint hash should move in that case.
 * </ul>
 */
public final class DatabaseServerFingerprintBuilder implements Builder<DatabaseServerFingerprint> {

  public static DatabaseServerFingerprintBuilder builder(final String connectionUrl) {
    requireNonNull(connectionUrl, "No JDBC connection URL provided");
    return new DatabaseServerFingerprintBuilder(connectionUrl);
  }

  @Nullable private ProductVersion databaseInformation;
  private final String connectionUrl;

  private DatabaseServerFingerprintBuilder(final String connectionUrl) {
    this.connectionUrl = requireNotBlank(connectionUrl, "No database connection JDBC URL provided");
    databaseInformation = null;
  }

  @Override
  public DatabaseServerFingerprint build() {

    final JdbcUrlTokens jdbcUrl = JdbcUrlTokenizer.tokenize(connectionUrl);
    final Map<String, String> canonical = canonicalMap(jdbcUrl);
    final String fingerprint = Utility.hash(canonical);
    final FingerprintConfidence confidence = confidence(jdbcUrl);
    return new DatabaseServerFingerprint(
        jdbcUrl.databaseSystemIdentifier(), jdbcUrl.hostClassification(), fingerprint, confidence);
  }

  public DatabaseServerFingerprintBuilder withDatabaseProductVersion(
      final ProductVersion databaseInformation) {
    this.databaseInformation = databaseInformation; // nulls are allowed
    return this;
  }

  private Map<String, String> canonicalMap(final JdbcUrlTokens jdbcUrl) {
    final Map<String, String> canonical = new LinkedHashMap<>();
    canonical.put("type", jdbcUrl.databaseSystemIdentifier());
    canonical.put("host", canonicalHost(jdbcUrl));
    canonical.put("database", jdbcUrl.databaseName().toLowerCase());
    // The product version is included in the hash on purpose, so that the
    // fingerprint value changes when a server is upgraded or patched,
    // even though the server's identity (and hence  its confidence level)
    // has not changed. This gives consumers a way to detect drift between
    // crawls of what is otherwise the same server.
    if (databaseInformation != null) {
      canonical.put("version", databaseInformation.getProductVersion());
    }
    return canonical;
  }

  /**
   * Derives the host value used as hash input, normalized according to {@code hostClassification} -
   * this is the single place in the codebase where host masking and case-normalization policy is
   * applied, deliberately kept separate from {@link JdbcUrlTokenizer}, which reports {@code host}
   * exactly as it appears in the URL.
   *
   * <ul>
   *   <li>{@code PUBLIC} - lower-cased, but never masked, since DNS hostnames are case-insensitive
   *       and a publicly routable host is not sensitive information.
   *   <li>{@code LOCALHOST}/{@code INTERNAL} - masked to a fixed, lower-case placeholder such as
   *       {@code "<localhost>"} or {@code "<internal>"}, so that no privately-scoped hostname or IP
   *       address leaks into the fingerprint hash input.
   *   <li>{@code UNKNOWN} - treated as blank, since there is no host information to normalize.
   * </ul>
   */
  private static String canonicalHost(final JdbcUrlTokens jdbcUrl) {
    final HostClassification classification = jdbcUrl.hostClassification();
    final String host = jdbcUrl.host();
    switch (classification) {
      case PUBLIC:
        return host.strip().toLowerCase();
      case LOCALHOST:
      case INTERNAL:
        return String.format("<%s>", classification).toLowerCase();
      case UNKNOWN:
      default:
        return "";
    }
  }

  /**
   * NOTE: Confidence is based purely on JDBC URL and network structure, and intentionally does not
   * consider the database product version (databaseInformation is unused here). A version or patch
   * upgrade does not change which server this is, so it should not raise or lower how confident we
   * are in the server's identity - only the fingerprint hash reflects that change.
   */
  private FingerprintConfidence confidence(final JdbcUrlTokens jdbcUrl) {
    final boolean hasType = jdbcUrl.hasDatabaseSystemIdentifier();
    final boolean hasHost = jdbcUrl.hasHost();
    final boolean hasDatabaseName = jdbcUrl.hasDatabaseName();
    final boolean hasPublicHost = jdbcUrl.hasPublicHost();

    // High confidence means the URL and server metadata point to a specific
    // server identity rather than a shared or heavily normalized endpoint.
    if (hasType && hasDatabaseName && hasPublicHost) {
      return FingerprintConfidence.HIGH;
    }
    // Medium confidence means we have enough structured signal to compare runs,
    // but not enough to treat the fingerprint as strongly identifying.
    if (hasType && hasHost) {
      return FingerprintConfidence.MEDIUM;
    }
    // Low confidence is the fallback for sparse or ambiguous inputs.
    return FingerprintConfidence.LOW;
  }
}
