/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Multimap<K, V> implements Map<K, List<V>>, Serializable {

  @Serial private static final long serialVersionUID = 1470713639458689002L;

  private final ConcurrentHashMap<K, List<V>> multimap;

  public Multimap() {
    multimap = new ConcurrentHashMap<>();
  }

  public V add(final K key, final V value) {
    if (key == null) {
      return null;
    }
    final CopyOnWriteArrayList<V> values =
        (CopyOnWriteArrayList<V>) multimap.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());
    values.addIfAbsent(value);
    return value;
  }

  @Override
  public void clear() {
    multimap.clear();
  }

  @Override
  public boolean containsKey(final Object key) {
    return multimap.containsKey(key);
  }

  @Override
  public boolean containsValue(final Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<Entry<K, List<V>>> entrySet() {
    return multimap.entrySet();
  }

  @Override
  public List<V> get(final Object key) {
    if (key == null || !multimap.containsKey(key)) {
      return null;
    }
    return List.copyOf(multimap.get(key));
  }

  @Override
  public boolean isEmpty() {
    return multimap.isEmpty();
  }

  @Override
  public Set<K> keySet() {
    return multimap.keySet();
  }

  @Override
  public List<V> put(final K key, final List<V> value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void putAll(final Map<? extends K, ? extends List<V>> m) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<V> remove(final Object key) {
    return multimap.remove(key);
  }

  @Override
  public int size() {
    return multimap.size();
  }

  @Override
  public String toString() {
    return multimap.toString();
  }

  @Override
  public Collection<List<V>> values() {
    return multimap.values();
  }
}
