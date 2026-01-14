/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility;

import java.util.Optional;

public enum OptionalBoolean {
  unknown,
  true_value,
  false_value;

  public static OptionalBoolean fromBoolean(final boolean value) {
    if (value) {
      return OptionalBoolean.true_value;
    }
    return OptionalBoolean.false_value;
  }

  public static OptionalBoolean fromOptional(final Optional<Boolean> value) {
    if (value == null || value.isEmpty()) {
      return OptionalBoolean.unknown;
    }
    return fromBoolean(value.get());
  }

  public Optional<Boolean> toOptional() {
    return switch (this) {
      case true_value -> Optional.of(true);
      case false_value -> Optional.of(false);
      default -> Optional.empty();
    };
  }
}
