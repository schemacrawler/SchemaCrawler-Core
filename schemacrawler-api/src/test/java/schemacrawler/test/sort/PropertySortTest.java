/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.sort;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.comparesEqualTo;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static schemacrawler.crawl.SortTestUtility.newDatabaseProperty;
import static schemacrawler.crawl.SortTestUtility.newServerInfoProperty;

import org.junit.jupiter.api.Test;
import us.fatehi.utility.property.Property;

public class PropertySortTest {

  @Test
  public void compare() {
    final Property serverInfoProperty = newServerInfoProperty("name", null, null);
    final Property serverInfoProperty1 = newServerInfoProperty("name1", "value1", "desc");

    assertThat(serverInfoProperty, greaterThan(null));
    assertThat(serverInfoProperty1, greaterThan(null));

    assertThat(serverInfoProperty, comparesEqualTo(newDatabaseProperty("name", null)));
    assertThat(serverInfoProperty, comparesEqualTo(newDatabaseProperty("NAME", null)));

    assertThat(serverInfoProperty, lessThan(serverInfoProperty1));
    assertThat(serverInfoProperty1, greaterThan(serverInfoProperty));
  }
}
