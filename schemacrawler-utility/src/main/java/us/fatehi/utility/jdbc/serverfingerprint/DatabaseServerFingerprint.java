/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.jdbc.serverfingerprint;

import static java.util.Objects.requireNonNull;
import static us.fatehi.utility.Utility.isBlank;
import static us.fatehi.utility.Utility.trimToEmpty;

import java.io.Serializable;

/** Database server fingerprint and confidence. */
public record DatabaseServerFingerprint(
    String databaseSystemIdentifier,
    HostClassification hostClassification,
    String fingerprint,
    FingerprintConfidence confidence)
    implements Serializable {

  public DatabaseServerFingerprint {
    databaseSystemIdentifier = trimToEmpty(databaseSystemIdentifier);
    fingerprint = trimToEmpty(fingerprint);
    if (isBlank(databaseSystemIdentifier) || isBlank(fingerprint)) {
      confidence = FingerprintConfidence.LOW;
    }
    requireNonNull(confidence, "No confidence provided");
  }

  public DatabaseServerFingerprint() {
    this(null, HostClassification.UNKNOWN, null, FingerprintConfidence.LOW);
  }

  public boolean isEmpty() {
    return isBlank(databaseSystemIdentifier);
  }

  @Override
  public String toString() {
    if (isEmpty()) {
      return "database {unknown}";
    }
    return "database {type=%s, host classification=%s, fingerprint=%s[%s]}"
        .formatted(databaseSystemIdentifier, hostClassification, fingerprint, confidence);
  }
}
