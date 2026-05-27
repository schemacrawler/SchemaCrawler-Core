/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.property;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class PropertyNameUtilityTest {

  @Test
  public void tableOfWithTitleAndItems() {
    final List<PropertyName> names =
        List.of(new PropertyName("alpha", "First item"), new PropertyName("beta", "Second item"));

    final String table = PropertyNameUtility.tableOf("My Title", names);
    assertThat(table, is(not(nullValue())));
    assertThat(table, containsString("My Title"));
    assertThat(table, containsString("alpha"));
    assertThat(table, containsString("First item"));
    assertThat(table, containsString("beta"));
    assertThat(table, containsString("Second item"));
  }

  @Test
  public void tableOfWithoutTitle() {
    final List<PropertyName> names = List.of(new PropertyName("gamma", "Third item"));

    final String table = PropertyNameUtility.tableOf(null, names);
    assertThat(table, is(not(nullValue())));
    assertThat(table, containsString("gamma"));
    assertThat(table, containsString("Third item"));
  }

  @Test
  public void tableOfWithBlankTitle() {
    final List<PropertyName> names = List.of(new PropertyName("delta", "Fourth item"));

    final String table = PropertyNameUtility.tableOf("   ", names);
    assertThat(table, is(not(nullValue())));
    assertThat(table, containsString("delta"));
  }

  @Test
  public void tableOfWithEmptyList() {
    final String table = PropertyNameUtility.tableOf("Empty", Collections.emptyList());
    assertThat(table, is(not(nullValue())));
    assertThat(table, containsString("Empty"));
  }

  @Test
  public void tableOfWithNullList() {
    final String table = PropertyNameUtility.tableOf("NullList", null);
    assertThat(table, is(not(nullValue())));
  }
}
