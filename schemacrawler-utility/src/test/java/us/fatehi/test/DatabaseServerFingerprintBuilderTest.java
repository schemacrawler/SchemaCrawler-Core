/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static us.fatehi.test.utility.TestUtility.NOT_BLANK;

import org.junit.jupiter.api.Test;
import us.fatehi.utility.database.DatabaseInformation;
import us.fatehi.utility.jdbc.serverfingerprint.DatabaseServerFingerprint;
import us.fatehi.utility.jdbc.serverfingerprint.DatabaseServerFingerprintBuilder;
import us.fatehi.utility.jdbc.serverfingerprint.FingerprintConfidence;

public class DatabaseServerFingerprintBuilderTest {

  @Test
  public void buildFingerprintFromParsedJdbcUrl() {
    final DatabaseInformation databaseInformation =
        new DatabaseInformation("PostgreSQL", "16.2", "sa");

    final DatabaseServerFingerprint fingerprint =
        DatabaseServerFingerprintBuilder.builder("jdbc:postgresql://db.example.com:5432/appdb")
            .withDatabaseProductVersion(databaseInformation)
            .build();

    assertThat(fingerprint, is(notNullValue()));
    assertThat(fingerprint.fingerprint(), matchesPattern(NOT_BLANK));
    assertThat(fingerprint.confidence(), is(FingerprintConfidence.HIGH));
  }

  @Test
  public void buildFingerprintFromSparseJdbcUrlIsLowerConfidence() {
    final DatabaseInformation databaseInformation =
        new DatabaseInformation("SQLite", "3.45.0", "sa");

    final DatabaseServerFingerprint fingerprint =
        DatabaseServerFingerprintBuilder.builder("jdbc:sqlite::memory:")
            .withDatabaseProductVersion(databaseInformation)
            .build();

    assertThat(fingerprint, is(notNullValue()));
    assertThat(fingerprint.confidence(), is(FingerprintConfidence.MEDIUM));
  }

  @Test
  public void buildFingerprintFromPrivateIpJdbcUrlIsNotHighConfidence() {
    final DatabaseInformation databaseInformation =
        new DatabaseInformation("PostgreSQL", "16.2", "sa");

    final DatabaseServerFingerprint fingerprint =
        DatabaseServerFingerprintBuilder.builder("jdbc:postgresql://10.0.0.7:5432/appdb")
            .withDatabaseProductVersion(databaseInformation)
            .build();

    assertThat(fingerprint, is(notNullValue()));
    assertThat(fingerprint.confidence(), is(FingerprintConfidence.MEDIUM));
  }

  @Test
  public void buildFingerprintIsStableForEquivalentJdbcUrls() {
    final DatabaseInformation databaseInformation =
        new DatabaseInformation("PostgreSQL", "16.2", "sa");

    final DatabaseServerFingerprint first =
        DatabaseServerFingerprintBuilder.builder("jdbc:postgresql://DB.EXAMPLE.COM:5432/AppDB")
            .withDatabaseProductVersion(databaseInformation)
            .build();
    final DatabaseServerFingerprint second =
        DatabaseServerFingerprintBuilder.builder("jdbc:postgresql://db.example.com:5432/appdb")
            .withDatabaseProductVersion(databaseInformation)
            .build();

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
        DatabaseServerFingerprintBuilder.builder("jdbc:postgresql://db.example.com:5432/appdb")
            .withDatabaseProductVersion(databaseInformation)
            .build();
    final DatabaseServerFingerprint second =
        DatabaseServerFingerprintBuilder.builder("jdbc:postgresql://db.example.com:5432/otherdb")
            .withDatabaseProductVersion(databaseInformation)
            .build();

    assertThat(first.fingerprint(), is(not(second.fingerprint())));
  }

  @Test
  public void buildFingerprintFromLocalhostJdbcUrlIsMediumConfidence() {
    final DatabaseInformation databaseInformation =
        new DatabaseInformation("PostgreSQL", "16.2", "sa");

    final DatabaseServerFingerprint fingerprint =
        DatabaseServerFingerprintBuilder.builder("jdbc:postgresql://localhost:5432/appdb")
            .withDatabaseProductVersion(databaseInformation)
            .build();

    assertThat(fingerprint, is(notNullValue()));
    assertThat(fingerprint.confidence(), is(FingerprintConfidence.MEDIUM));
  }
}
