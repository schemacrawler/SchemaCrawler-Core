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
import java.util.Locale;

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

  public NamedObjectKey normalized() {
    final String[] normalizedKey = new String[key.length];
    for (int i = 0; i < key.length; i++) {
      normalizedKey[i] = normalizeComponent(key[i]);
    }
    return new NamedObjectKey(normalizedKey);
  }

  private static String normalizeComponent(final String component) {
    if (component == null || component.length() < 2) {
      return component;
    }

    final char first = component.charAt(0);
    final char last = component.charAt(component.length() - 1);
    if ((first == '"' && last == '"')
        || (first == '`' && last == '`')
        || (first == '[' && last == ']')) {
      return component.substring(1, component.length() - 1).toLowerCase(Locale.ROOT);
    }
    return component.toLowerCase(Locale.ROOT);
  }
}
