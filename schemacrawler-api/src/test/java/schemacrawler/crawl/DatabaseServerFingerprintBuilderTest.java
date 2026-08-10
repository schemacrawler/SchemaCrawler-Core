/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.crawl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;
import schemacrawler.schema.DatabaseServerFingerprint;
import schemacrawler.schema.FingerprintConfidence;
import us.fatehi.utility.database.DatabaseInformation;

public class DatabaseServerFingerprintBuilderTest {

  @Test
  public void buildFingerprintFromParsedJdbcUrl() {
    final DatabaseInformation databaseInformation =
        new DatabaseInformation("PostgreSQL", "16.2", "sa");

    final DatabaseServerFingerprint fingerprint =
        DatabaseServerFingerprintBuilder.build(
            databaseInformation, "jdbc:postgresql://db.example.com:5432/appdb");

    assertThat(fingerprint, is(notNullValue()));
    assertThat(fingerprint.fingerprint(), containsString("sha-256:"));
    assertThat(fingerprint.confidence(), is(FingerprintConfidence.HIGH));
  }

  @Test
  public void buildFingerprintFromSparseJdbcUrlIsLowerConfidence() {
    final DatabaseInformation databaseInformation =
        new DatabaseInformation("SQLite", "3.45.0", "sa");

    final DatabaseServerFingerprint fingerprint =
        DatabaseServerFingerprintBuilder.build(databaseInformation, "jdbc:sqlite::memory:");

    assertThat(fingerprint, is(notNullValue()));
    assertThat(fingerprint.confidence(), is(FingerprintConfidence.LOW));
  }

  @Test
  public void buildFingerprintFromPrivateIpJdbcUrlIsNotHighConfidence() {
    final DatabaseInformation databaseInformation =
        new DatabaseInformation("PostgreSQL", "16.2", "sa");

    final DatabaseServerFingerprint fingerprint =
        DatabaseServerFingerprintBuilder.build(
            databaseInformation, "jdbc:postgresql://10.0.0.7:5432/appdb");

    assertThat(fingerprint, is(notNullValue()));
    assertThat(fingerprint.confidence(), is(FingerprintConfidence.MEDIUM));
  }

  @Test
  public void buildFingerprintIsStableForEquivalentJdbcUrls() {
    final DatabaseInformation databaseInformation =
        new DatabaseInformation("PostgreSQL", "16.2", "sa");

    final DatabaseServerFingerprint first =
        DatabaseServerFingerprintBuilder.build(
            databaseInformation, "jdbc:postgresql://DB.EXAMPLE.COM:5432/AppDB");
    final DatabaseServerFingerprint second =
        DatabaseServerFingerprintBuilder.build(
            databaseInformation, "jdbc:postgresql://db.example.com:5432/appdb");

    assertThat(first, is(notNullValue()));
    assertThat(second, is(notNullValue()));
    assertThat(first.fingerprint(), is(second.fingerprint()));
    assertThat(first.confidence(), is(second.confidence()));
  }

  @Test
  public void buildFingerprintChangesWhenDatabaseNameChanges() {
    final DatabaseInformation databaseInformation =
        new DatabaseInformation("PostgreSQL", "16.2", "sa");

    final DatabaseServerFingerprint first =
        DatabaseServerFingerprintBuilder.build(
            databaseInformation, "jdbc:postgresql://db.example.com:5432/appdb");
    final DatabaseServerFingerprint second =
        DatabaseServerFingerprintBuilder.build(
            databaseInformation, "jdbc:postgresql://db.example.com:5432/otherdb");

    assertThat(first.fingerprint(), is(not(second.fingerprint())));
  }

  @Test
  public void buildFingerprintFromLocalhostJdbcUrlIsMediumConfidence() {
    final DatabaseInformation databaseInformation =
        new DatabaseInformation("PostgreSQL", "16.2", "sa");

    final DatabaseServerFingerprint fingerprint =
        DatabaseServerFingerprintBuilder.build(
            databaseInformation, "jdbc:postgresql://localhost:5432/appdb");

    assertThat(fingerprint, is(notNullValue()));
    assertThat(fingerprint.confidence(), is(FingerprintConfidence.MEDIUM));
  }
}
