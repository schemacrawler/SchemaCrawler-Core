/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import schemacrawler.namedobject.NamedObjectSort;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.test.utility.TestNamedObject;
import schemacrawler.test.utility.TestNamedObjectNullName;

public class NamedObjectSortTest {

  @Test
  public void alphabeticalNullNamesAndTieBreakingKeysSortLast() {
    final NamedObject named = mock(NamedObject.class);
    final NamedObject unnamed = mock(NamedObject.class);
    final NamedObject missingKey = mock(NamedObject.class);
    when(named.getFullName()).thenReturn("NAME");
    when(named.key()).thenReturn(new NamedObjectKey("NAME"));
    when(unnamed.key()).thenReturn(new NamedObjectKey());
    when(missingKey.getFullName()).thenReturn("name");
    final NamedObjectSort comparator = NamedObjectSort.alphabetical;
    assertTrue(comparator.compare(named, unnamed) < 0);
    assertTrue(comparator.compare(unnamed, named) > 0);
    assertTrue(comparator.compare(named, missingKey) == 0);
    assertTrue(comparator.compare(missingKey, named) == 0);
    for (final NamedObjectSort sort : NamedObjectSort.values()) {
      assertTrue(sort.compare(named, null) < 0);
      assertTrue(sort.compare(null, named) > 0);
      assertEquals(0, sort.compare(null, null));
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false, true, false, false, true})
  public void namedObjectSort(final boolean sortAlphabetically) throws Exception {

    // Set up
    final List<NamedObject> expected = createExpectedList(sortAlphabetically);

    final List<NamedObject> objects = new ArrayList<>(expected);
    Collections.shuffle(objects);
    assertThat("Test named object list is not shuffled", objects, is(not(expected)));

    // Finally - do the sort
    Collections.sort(objects, NamedObjectSort.getNamedObjectSort(sortAlphabetically));

    // Compare
    assertThat("Test named object list is not sorted", objects, is(expected));
  }

  private List<NamedObject> createExpectedList(final boolean sortAlphabetically) {

    final String[] names = {
      "ID",
      "FIRSTNAME",
      "LASTNAME",
      "ADDRESS1",
      "ADDRESS2",
      "CITY",
      "STATE",
      "POSTALCODE",
      "COUNTRY",
    };
    if (sortAlphabetically) {
      Arrays.sort(names);
    }

    final int length = names.length;

    final TestNamedObjectNullName nullNamedObject = new TestNamedObjectNullName();
    final List<NamedObject> expected = new ArrayList<>();
    for (int i = 0; i < length; i++) {
      expected.add(new TestNamedObject(names[i], i));
    }
    expected.add(nullNamedObject);
    expected.add(null);

    return expected;
  }
}
