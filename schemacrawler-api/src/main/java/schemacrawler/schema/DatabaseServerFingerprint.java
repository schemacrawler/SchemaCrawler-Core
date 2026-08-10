/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.schema;

import static java.util.Objects.requireNonNull;
import static us.fatehi.utility.Utility.isBlank;
import static us.fatehi.utility.Utility.trimToEmpty;

import java.io.Serializable;

/** Database server fingerprint and confidence. */
public record DatabaseServerFingerprint(String fingerprint, FingerprintConfidence confidence)
    implements Serializable {

  public DatabaseServerFingerprint {
    fingerprint = trimToEmpty(fingerprint);
    if (isBlank(fingerprint)) {
      confidence = FingerprintConfidence.LOW;
    }
    requireNonNull(confidence, "No confidence provided");
  }
}
