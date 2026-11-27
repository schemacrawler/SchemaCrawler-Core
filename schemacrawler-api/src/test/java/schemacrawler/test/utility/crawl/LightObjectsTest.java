/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility.crawl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

import org.junit.jupiter.api.Test;
import schemacrawler.schema.Column;
import schemacrawler.schema.Function;
import schemacrawler.schema.Index;
import schemacrawler.schema.RoutineType;
import schemacrawler.schema.Trigger;
import schemacrawler.schemacrawler.SchemaReference;

/** Tests the light objects that are used in place of mocks. */
public class LightObjectsTest {

  private final LightTable table =
      new LightTable(new SchemaReference("catalog", "schema"), "customer");

  @Test
  public void columnsSortByKey() {
    final Column first = table.addColumn("alpha_id");
    final Column second = table.addColumn("beta_id");

    assertThat(first.compareTo(second), is(lessThan(0)));
    assertThat(second.compareTo(first), is(greaterThan(0)));
    assertThat(first.compareTo(first), is(0));
  }

  @Test
  public void triggersSortByKey() {
    final Trigger first = new LightTrigger(table, "alpha_trigger");
    final Trigger second = new LightTrigger(table, "beta_trigger");

    assertThat(first.compareTo(second), is(lessThan(0)));
    assertThat(second.compareTo(first), is(greaterThan(0)));
  }

  @Test
  public void procedureParametersSortByKey() {
    final LightProcedure procedure = new LightProcedure("refresh");
    final LightProcedureParameter first = new LightProcedureParameter(procedure, "alpha");
    final LightProcedureParameter second = new LightProcedureParameter(procedure, "beta");

    assertThat(first.compareTo(second), is(lessThan(0)));
    assertThat(second.compareTo(first), is(greaterThan(0)));
  }

  @Test
  public void indexHasNameKeyAndTable() {
    final Index index = new LightIndex(table, "idx_customer");

    assertThat(index.getName(), is("idx_customer"));
    assertThat(index.getParent(), is(table));
    assertThat(index.key(), is(table.key().with("idx_customer")));
    assertThat(index.getSchema(), is(table.getSchema()));
    assertThat(index.getColumns().isEmpty(), is(true));
    assertThat(index.iterator().hasNext(), is(false));
  }

  @Test
  public void indexesSortByKey() {
    final Index first = new LightIndex(table, "idx_alpha");
    final Index second = new LightIndex(table, "idx_beta");

    assertThat(first.compareTo(second), is(lessThan(0)));
    assertThat(second.compareTo(first), is(greaterThan(0)));
  }

  @Test
  public void functionHasNameKeyAndSpecificName() {
    final Function function = new LightFunction(new SchemaReference("catalog", "schema"), "calc");

    assertThat(function.getName(), is("calc"));
    assertThat(function.getSpecificName(), is("calc_specific"));
    assertThat(function.getType(), is(RoutineType.function));
  }

  @Test
  public void overloadedFunctionsHaveDifferentKeys() {
    final SchemaReference schema = new SchemaReference("catalog", "schema");
    final Function first = new LightFunction(schema, "calc", "calc_1");
    final Function second = new LightFunction(schema, "calc", "calc_2");

    assertThat(first.key().equals(second.key()), is(false));
    assertThat(first.compareTo(second), is(lessThan(0)));
  }
}
