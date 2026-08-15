/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */
package schemacrawler.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static schemacrawler.loader.utility.TableRowCountsUtility.TABLE_ROW_COUNT_KEY;

import org.junit.jupiter.api.Test;
import schemacrawler.schemacrawler.SchemaReference;
import schemacrawler.test.utility.crawl.LightTable;
import schemacrawler.test.utility.crawl.LightTrigger;
import schemacrawler.tools.utility.TableCounts;

public class TableCountsTest {

  // ---- constructor compaction ------------------------------------------------

  @Test
  public void defaultConstructorProducesAllNulls() {
    final TableCounts counts = new TableCounts();

    assertThat(counts.columnCount(), is(nullValue()));
    assertThat(counts.foreignKeyCount(), is(nullValue()));
    assertThat(counts.indexCount(), is(nullValue()));
    assertThat(counts.triggerCount(), is(nullValue()));
    assertThat(counts.rowCount(), is(nullValue()));
  }

  @Test
  public void negativeIntegerCountsAreCoercedToNull() {
    final TableCounts counts = new TableCounts(-1, -5, -99, -1, null);

    assertThat(counts.columnCount(), is(nullValue()));
    assertThat(counts.foreignKeyCount(), is(nullValue()));
    assertThat(counts.indexCount(), is(nullValue()));
    assertThat(counts.triggerCount(), is(nullValue()));
  }

  @Test
  public void zeroCountsAreRetained() {
    final TableCounts counts = new TableCounts(0, 0, 0, 0, null);

    assertThat(counts.columnCount(), is(0));
    assertThat(counts.foreignKeyCount(), is(0));
    assertThat(counts.indexCount(), is(0));
    assertThat(counts.triggerCount(), is(0));
  }

  @Test
  public void zeroRowCountIsCoercedToNull() {
    final TableCounts counts = new TableCounts(null, null, null, null, 0L);

    assertThat(counts.rowCount(), is(nullValue()));
  }

  @Test
  public void negativeRowCountIsCoercedToNull() {
    final TableCounts counts = new TableCounts(null, null, null, null, -1L);

    assertThat(counts.rowCount(), is(nullValue()));
  }

  @Test
  public void positiveValuesArePassedThrough() {
    final TableCounts counts = new TableCounts(3, 1, 2, 1, 42L);

    assertThat(counts.columnCount(), is(3));
    assertThat(counts.foreignKeyCount(), is(1));
    assertThat(counts.indexCount(), is(2));
    assertThat(counts.triggerCount(), is(1));
    assertThat(counts.rowCount(), is(42L));
  }

  // ---- from(Table) -----------------------------------------------------------

  @Test
  public void fromNullTableReturnsNull() {
    assertThat(TableCounts.from(null), is(nullValue()));
  }

  @Test
  public void fromTableWithoutRowCount() {
    final LightTable table = new LightTable(new SchemaReference("PUBLIC", "BOOKS"), "AUTHORS");
    table.addColumn("ID");
    table.addColumn("NAME");
    table.addTrigger(new LightTrigger(table, "TRG_AUTHORS"));

    final TableCounts counts = TableCounts.from(table);

    assertThat(counts.columnCount(), is(2));
    assertThat(counts.triggerCount(), is(1));
    assertThat(counts.rowCount(), is(nullValue()));
  }

  @Test
  public void fromTableWithRowCount() {
    final LightTable table = new LightTable(new SchemaReference("PUBLIC", "BOOKS"), "AUTHORS");
    table.addColumn("ID");
    table.setAttribute(TABLE_ROW_COUNT_KEY, 100L);

    final TableCounts counts = TableCounts.from(table);

    assertThat(counts.columnCount(), is(1));
    assertThat(counts.rowCount(), is(100L));
  }

  @Test
  public void fromTableWithZeroRowCountProducesNullRowCount() {
    final LightTable table = new LightTable(new SchemaReference("PUBLIC", "BOOKS"), "AUTHORS");
    table.setAttribute(TABLE_ROW_COUNT_KEY, 0L);

    final TableCounts counts = TableCounts.from(table);

    assertThat(counts.rowCount(), is(nullValue()));
  }
}
