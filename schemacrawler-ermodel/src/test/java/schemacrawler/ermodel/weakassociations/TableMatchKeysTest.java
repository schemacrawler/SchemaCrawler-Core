/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.weakassociations;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import schemacrawler.schema.Table;
import schemacrawler.test.utility.crawl.LightTable;

public class TableMatchKeysTest {

  @Test
  public void tableMatchKeys_boundaries() {
    TableMatchKeys matchkeys;
    List<String> withoutPrefix;

    // 1.
    assertThrows(NullPointerException.class, () -> new TableMatchKeys(null));

    // 2.
    matchkeys = new TableMatchKeys(Collections.emptyList());
    assertThat(matchkeys.toString(), is("{}"));

    withoutPrefix = matchkeys.get(new LightTable("table0"));
    assertThat(withoutPrefix, is(nullValue()));

    // 3.
    matchkeys = new TableMatchKeys(tables("table1"));
    assertThat(matchkeys.toString(), is("{table1=[table1]}"));

    withoutPrefix = matchkeys.get(new LightTable("table0"));
    assertThat(withoutPrefix, is(nullValue()));

    withoutPrefix = matchkeys.get(new LightTable("table1"));
    assertThat(withoutPrefix, containsInAnyOrder("table1"));

    withoutPrefix = matchkeys.get((Table) null);
    assertThat(withoutPrefix, is(nullValue()));
  }

  @Test
  public void tableMatchKeys_mixed_prefixes() {
    List<String> withoutPrefix;

    final TableMatchKeys matchkeys =
        new TableMatchKeys(tables("xyz_old_table1", "xyz_old_table2", "xyz_table3"));

    withoutPrefix = matchkeys.get(new LightTable("table0"));
    assertThat(withoutPrefix, is(nullValue()));

    withoutPrefix = matchkeys.get(new LightTable("xyz_old_table1"));
    assertThat(withoutPrefix, containsInAnyOrder("table1", "old_table1", "xyz_old_table1"));

    withoutPrefix = matchkeys.get(new LightTable("xyz_old_table2"));
    assertThat(withoutPrefix, containsInAnyOrder("table2", "old_table2", "xyz_old_table2"));

    withoutPrefix = matchkeys.get(new LightTable("xyz_table3"));
    assertThat(withoutPrefix, containsInAnyOrder("table3", "xyz_table3"));
  }

  private List<Table> tables(final String... tableNames) {
    requireNonNull(tableNames);

    final List<Table> tables = new ArrayList<>();
    for (final String tableName : tableNames) {
      tables.add(new LightTable(tableName));
    }
    return tables;
  }
}
