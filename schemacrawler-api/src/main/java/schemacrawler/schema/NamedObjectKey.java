/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.schema;

import static us.fatehi.utility.Utility.convertForComparison;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;

public final class NamedObjectKey implements Serializable, Comparable<NamedObjectKey> {

  @Serial private static final long serialVersionUID = -5008609072012459037L;

  private final String[] key;

  public NamedObjectKey(final String... key) {
    if (key == null || key.length == 0) {
      this.key = new String[0];
    } else {
      this.key = Arrays.copyOf(key, key.length);
    }
  }

  @Override
  public int compareTo(final NamedObjectKey other) {
    if (other == null) {
      return 1;
    }
    return Arrays.compare(key, other.key);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof final NamedObjectKey other)) {
      return false;
    }
    return Arrays.equals(key, other.key);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(key);
  }

  /**
   * Joins the individual key parts with '.', skipping any parts that are null or empty. Since each
   * part was captured separately when the key was built up (never by splitting a single
   * concatenated name), a delimiter character embedded within an individual part (for example, a
   * literal dot inside a quoted identifier) is preserved as content and is never mistaken for a
   * separator between parts.
   *
   * @return Joined key parts, or an empty string if there are no non-blank parts
   */
  public String join() {
    final String delimiter = ".";
    final StringBuilder buffer = new StringBuilder();
    for (final String part : key) {
      if (part == null || part.isEmpty()) {
        continue;
      }
      if (!buffer.isEmpty()) {
        buffer.append(delimiter);
      }
      buffer.append(part);
    }
    return buffer.toString();
  }

  public String slug() {
    if (key.length == 0) {
      return "";
    }
    final String name = key[key.length - 1];
    return convertForComparison(name) + "_" + Integer.toHexString(hashCode());
  }

  @Override
  public String toString() {
    return "{\"key\": \"" + String.join("/", key) + "\"}";
  }

  public NamedObjectKey with(final String name) {
    final int currentLength = key.length;
    final String[] newKey = Arrays.copyOf(key, currentLength + 1);
    newKey[currentLength] = name;
    return new NamedObjectKey(newKey);
  }
}
