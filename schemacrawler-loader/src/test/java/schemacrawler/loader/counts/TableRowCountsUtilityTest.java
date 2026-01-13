/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.counts;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static schemacrawler.loader.utility.TableRowCountsUtility.TABLE_ROW_COUNT_KEY;

import org.junit.jupiter.api.Test;
import schemacrawler.loader.utility.TableRowCountsUtility;
import schemacrawler.schema.Table;
import schemacrawler.test.utility.crawl.LightTable;

public class TableRowCountsUtilityTest {

  @Test
  public void add() {
    final Table table = new LightTable("table1");

    addRowCountToTable(null, 0);
    assertThat(TableRowCountsUtility.hasRowCount(null), is(false));

    addRowCountToTable(table, 1);
    assertThat(TableRowCountsUtility.hasRowCount(table), is(true));
    assertThat(TableRowCountsUtility.getRowCount(table), is(1L));

    addRowCountToTable(table, 0);
    assertThat(TableRowCountsUtility.hasRowCount(table), is(true));
    assertThat(TableRowCountsUtility.getRowCount(table), is(0L));

    addRowCountToTable(table, -1);
    assertThat(TableRowCountsUtility.hasRowCount(table), is(false));
    assertThat(TableRowCountsUtility.getRowCount(table), is(-1L));
  }

  @Test
  public void message() {
    final Table table = new LightTable("table1");

    final NullPointerException nullPointerException =
        assertThrows(
            NullPointerException.class,
            () -> TableRowCountsUtility.getRowCountMessage((Number) null));
    assertThat(nullPointerException.getMessage(), is("No number provided"));

    assertThat(TableRowCountsUtility.getRowCountMessage(-1), is("empty"));
    assertThat(TableRowCountsUtility.getRowCountMessage(0), is("empty"));
    assertThat(TableRowCountsUtility.getRowCountMessage(1), is("1 rows"));

    assertThat(TableRowCountsUtility.getRowCountMessage((Table) null), is("empty"));
    assertThat(TableRowCountsUtility.getRowCountMessage(table), is("empty"));
    addRowCountToTable(table, 1);
    assertThat(TableRowCountsUtility.getRowCountMessage(table), is("1 rows"));
  }

  private void addRowCountToTable(final Table table, final long rowCount) {
    if (table != null) {
      if (rowCount >= 0) {
        table.setAttribute(TABLE_ROW_COUNT_KEY, rowCount);
      } else {
        table.removeAttribute(TABLE_ROW_COUNT_KEY);
      }
    }
  }
}
