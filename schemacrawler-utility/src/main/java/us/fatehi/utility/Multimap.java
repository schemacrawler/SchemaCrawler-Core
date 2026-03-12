/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility;

import java.io.Serial;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Multimap<K, V> extends ConcurrentHashMap<K, List<V>> {

  @Serial private static final long serialVersionUID = 1470713639458689002L;

  public V add(final K key, final V value) {
    if (key == null) {
      return null;
    }
    final List<V> values = computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());
    values.add(value);
    return value;
  }

  @Override
  public List<V> get(final Object key) {
    if (key == null) {
      return null;
    }
    return super.get(key);
  }
}
