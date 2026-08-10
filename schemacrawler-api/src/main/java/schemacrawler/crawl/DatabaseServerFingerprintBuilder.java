/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.crawl;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import schemacrawler.schema.DatabaseServerFingerprint;
import schemacrawler.schema.FingerprintConfidence;
import us.fatehi.utility.datasource.JdbcUrl;
import us.fatehi.utility.datasource.JdbcUrlParser;
import us.fatehi.utility.property.ProductVersion;

final class DatabaseServerFingerprintBuilder {

  static DatabaseServerFingerprint build(
      final ProductVersion databaseInformation, final String connectionUrl) {
    requireNonNull(databaseInformation, "No database information provided");
    requireNonNull(connectionUrl, "No JDBC connection URL provided");

    final JdbcUrl jdbcUrl = JdbcUrlParser.parse(connectionUrl);
    final Map<String, String> canonical = canonicalMap(databaseInformation, jdbcUrl);
    final String fingerprint = hash(canonical);
    final FingerprintConfidence confidence = confidence(databaseInformation, jdbcUrl);
    return new DatabaseServerFingerprint(fingerprint, confidence);
  }

  private static Map<String, String> canonicalMap(
      final ProductVersion databaseInformation, final JdbcUrl jdbcUrl) {
    final Map<String, String> canonical = new LinkedHashMap<>();
    canonical.put("type", jdbcUrl.databaseServerType());
    canonical.put("host", jdbcUrl.hostHash());
    canonical.put("database", jdbcUrl.databaseName());
    canonical.put("database_product_version", databaseInformation.getProductVersion());
    return canonical;
  }

  private static String hash(final Map<String, String> value) {
    try {
      final MessageDigest digest = MessageDigest.getInstance("SHA-256");
      final byte[] hashed = digest.digest(value.toString().getBytes(UTF_8));
      return "sha-256:" + HexFormat.of().formatHex(hashed);
    } catch (final NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  private static FingerprintConfidence confidence(
      final ProductVersion databaseInformation, final JdbcUrl jdbcUrl) {
    final boolean hasType = jdbcUrl.hasDatabaseServerType();
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

  private DatabaseServerFingerprintBuilder() {
    // Prevent instantiation
  }
}
