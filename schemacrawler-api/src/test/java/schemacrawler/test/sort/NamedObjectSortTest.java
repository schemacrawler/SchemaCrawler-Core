/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.sort;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static schemacrawler.crawl.SortTestUtility.newColumn;
import static schemacrawler.crawl.SortTestUtility.newTable;
import static schemacrawler.crawl.SortTestUtility.setOrdinalPosition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import schemacrawler.namedobject.NamedObjectSort;
import schemacrawler.schema.Column;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaReference;

public class NamedObjectSortTest {

  @Test
  public void alphabeticalNullNamesAndObjectsSortLast() {
    final NamedObject named = mock(NamedObject.class);
    final NamedObject unnamed = mock(NamedObject.class);
    final NamedObject missingKey = mock(NamedObject.class);
    when(named.getFullName()).thenReturn("NAME");
    when(named.key()).thenReturn(new NamedObjectKey("NAME"));
    when(unnamed.key()).thenReturn(new NamedObjectKey());
    when(missingKey.getFullName()).thenReturn("name");
    final NamedObjectSort comparator = NamedObjectSort.alphabetical;
    assertThat(comparator.compare(named, unnamed), lessThan(0));
    assertThat(comparator.compare(unnamed, named), greaterThan(0));
    assertThat(comparator.compare(named, missingKey), lessThan(0));
    assertThat(comparator.compare(missingKey, named), greaterThan(0));
    for (final NamedObjectSort sort : NamedObjectSort.values()) {
      assertThat(sort.compare(named, null), lessThan(0));
      assertThat(sort.compare(null, named), greaterThan(0));
      assertThat(sort.compare(null, null), is(0));
    }
  }

  @Test
  public void alphabeticalNormalizedNameCollisionsUseExactKeys() {
    final NamedObject hyphenated = namedObject("a-b", new NamedObjectKey("schema", "a-b"));
    final NamedObject spaced = namedObject("a b", new NamedObjectKey("schema", "a b"));
    final NamedObject plain = namedObject("ab", new NamedObjectKey("schema", "ab"));
    final List<NamedObject> expected = List.of(spaced, hyphenated, plain);
    final List<List<NamedObject>> permutations =
        List.of(
            List.of(hyphenated, spaced, plain),
            List.of(hyphenated, plain, spaced),
            List.of(spaced, hyphenated, plain),
            List.of(spaced, plain, hyphenated),
            List.of(plain, hyphenated, spaced),
            List.of(plain, spaced, hyphenated));

    for (final List<NamedObject> permutation : permutations) {
      final SortedSet<NamedObject> sorted = new TreeSet<>(NamedObjectSort.alphabetical);
      sorted.addAll(permutation);
      assertThat(sorted.size(), is(3));
      assertThat(List.copyOf(sorted), is(expected));
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void namedObjectSort(final boolean sortAlphabetically) {
    final List<NamedObject> expected = createExpectedList(sortAlphabetically);
    final List<NamedObject> objects = new ArrayList<>(expected);
    Collections.shuffle(objects, new Random(42));
    Collections.sort(objects, NamedObjectSort.getNamedObjectSort(sortAlphabetically));
    assertThat("Test named object list is not sorted", objects, is(expected));
  }

  private List<NamedObject> createExpectedList(final boolean sortAlphabetically) {

    final List<String> names =
        new ArrayList<>(
            List.of(
                "ID",
                "FIRSTNAME",
                "LASTNAME",
                "ADDRESS1",
                "ADDRESS2",
                "CITY",
                "STATE",
                "POSTALCODE",
                "COUNTRY"));
    final Table table = newTable(new SchemaReference("CATALOG", "SCHEMA"), "TABLE");
    final List<NamedObject> expected = new ArrayList<>();
    for (int i = 0; i < names.size(); i++) {
      final Column column = newColumn(table, names.get(i));
      setOrdinalPosition(column, i + 1);
      expected.add(column);
    }
    if (sortAlphabetically) {
      expected.sort(Comparator.comparing(NamedObject::getName));
    }
    expected.add(null);

    return expected;
  }

  private NamedObject namedObject(final String fullName, final NamedObjectKey key) {
    final NamedObject namedObject = mock(NamedObject.class);
    when(namedObject.getFullName()).thenReturn(fullName);
    when(namedObject.key()).thenReturn(key);
    return namedObject;
  }
}
