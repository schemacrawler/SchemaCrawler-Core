/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import us.fatehi.utility.Multimap;

public class MultimapTest {

  @Test
  public void add() {
    final Multimap<String, Integer> multimap = new Multimap<>();
    multimap.add("foo", 1);
    multimap.add("bar", 2);
    multimap.add("foo", 3);
    assertThat(multimap.get("foo"), containsInAnyOrder(1, 3));
    assertThat(multimap.get("bar"), containsInAnyOrder(2));
  }

  @Test
  public void addDoesNotAddDuplicateValues() {
    final Multimap<String, Integer> multimap = new Multimap<>();

    assertThat(multimap.add("foo", 1), is(1));
    assertThat(multimap.add("foo", 1), is(1));

    assertThat(multimap.get("foo"), containsInAnyOrder(1));
  }

  @Test
  public void addNullKey() {
    final Multimap<String, Integer> multimap = new Multimap<>();

    assertThat(multimap.add(null, 1), is(nullValue()));
    assertThat(multimap.isEmpty(), is(true));
    assertThat(multimap.get(null), is(nullValue()));
  }

  @Test
  public void getReturnsSnapshot() {
    final Multimap<String, Integer> multimap = new Multimap<>();
    multimap.add("foo", 1);

    final List<Integer> values = multimap.get("foo");
    assertThat(values, containsInAnyOrder(1));
    assertThrows(UnsupportedOperationException.class, () -> values.add(2));

    multimap.add("foo", 2);
    assertThat(values, containsInAnyOrder(1));
    assertThat(multimap.get("foo"), containsInAnyOrder(1, 2));
  }

  @Test
  public void mapOperations() {
    final Multimap<String, Integer> multimap = new Multimap<>();
    multimap.add("foo", 1);
    multimap.add("bar", 2);

    assertThat(multimap.containsKey("foo"), is(true));
    assertThat(multimap.containsKey("missing"), is(false));
    assertThat(multimap.size(), is(2));
    assertThat(multimap.keySet(), containsInAnyOrder("foo", "bar"));
    assertThat(multimap.entrySet(), hasSize(2));
    assertThat(multimap, hasKey("foo"));

    multimap.clear();
    assertThat(multimap.isEmpty(), is(true));
    assertThat(multimap.get("foo"), is(nullValue()));
  }

  @Test
  public void remove() {
    final Multimap<String, Integer> multimap = new Multimap<>();
    multimap.add("foo", 1);

    assertThat(multimap.containsKey("foo"), is(true));
    multimap.remove("foo");
    assertThat(multimap.containsKey("foo"), is(false));
  }

  @Test
  public void unsupportedMapOperations() {
    final Multimap<String, Integer> multimap = new Multimap<>();
    final Map<String, List<Integer>> values = Map.of("foo", List.of(1));

    assertThrows(UnsupportedOperationException.class, () -> multimap.containsValue(List.of(1)));
    assertThrows(UnsupportedOperationException.class, () -> multimap.put("foo", List.of(1)));
    assertThrows(UnsupportedOperationException.class, () -> multimap.putAll(values));
  }

  @Test
  public void values() {
    final Multimap<String, Integer> multimap = new Multimap<>();
    multimap.add("foo", 1);

    assertThat(multimap.values(), containsInAnyOrder(List.of(1)));
  }
}
