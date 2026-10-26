/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.crawl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import schemacrawler.schema.DataTypeType;
import schemacrawler.schema.Routine;
import schemacrawler.schema.Schema;
import schemacrawler.schema.TableType;
import schemacrawler.schemacrawler.SchemaReference;

public class NaturalTypeComparisonTest {

  @Test
  public void columnDataTypeUsesNaturalDataTypeOrderBeforeName() {
    final Schema schema = new SchemaReference("catalog", "BOOKS");
    final MutableColumnDataType systemType =
        new MutableColumnDataType(schema, "Z_SYSTEM", DataTypeType.system);
    final MutableColumnDataType userType =
        new MutableColumnDataType(schema, "A_USER", DataTypeType.user_defined);

    assertThat(systemType.compareTo(userType), lessThan(0));
    assertThat(userType.compareTo(systemType), greaterThan(0));
  }

  @Test
  public void columnDataTypeUsesSchemaOrderBeforeDataTypeOrder() {
    final MutableColumnDataType systemType =
        new MutableColumnDataType(
            new SchemaReference("catalog", "SYSTEM_LOBS"), "A_SYSTEM", DataTypeType.system);
    final MutableColumnDataType userType =
        new MutableColumnDataType(
            new SchemaReference("catalog", "BOOKS"), "Z_USER", DataTypeType.user_defined);

    assertThat(userType.compareTo(systemType), lessThan(0));
    assertThat(systemType.compareTo(userType), greaterThan(0));
  }

  @Test
  public void columnPositionPrecedesColumnDataType() {
    final Schema schema = new SchemaReference("catalog", "schema");
    final MutableTable table = new MutableTable(schema, "TABLE");
    final MutableColumn firstColumn = new MutableColumn(table, "Z_COLUMN");
    final MutableColumn secondColumn = new MutableColumn(table, "A_COLUMN");
    firstColumn.setOrdinalPosition(1);
    secondColumn.setOrdinalPosition(2);
    firstColumn.setColumnDataType(new MutableColumnDataType(schema, "Z_TYPE", DataTypeType.system));
    secondColumn.setColumnDataType(
        new MutableColumnDataType(schema, "A_TYPE", DataTypeType.system));

    assertThat(firstColumn.compareTo(secondColumn), lessThan(0));
    assertThat(secondColumn.compareTo(firstColumn), greaterThan(0));
  }

  @Test
  public void routineSpecificNamePrecedesParameterDataTypes() {
    final Schema schema = new SchemaReference("catalog", "schema");
    final MutableProcedure earlierSpecific = new MutableProcedure(schema, "ROUTINE", "A_SPECIFIC");
    final MutableProcedure laterSpecific = new MutableProcedure(schema, "ROUTINE", "Z_SPECIFIC");
    final MutableProcedureParameter laterType =
        new MutableProcedureParameter(earlierSpecific, "PARAMETER");
    final MutableProcedureParameter earlierType =
        new MutableProcedureParameter(laterSpecific, "PARAMETER");
    laterType.setOrdinalPosition(1);
    earlierType.setOrdinalPosition(1);
    laterType.setColumnDataType(new MutableColumnDataType(schema, "Z_TYPE", DataTypeType.system));
    earlierType.setColumnDataType(new MutableColumnDataType(schema, "A_TYPE", DataTypeType.system));
    earlierSpecific.addParameter(laterType);
    laterSpecific.addParameter(earlierType);

    assertThat(earlierSpecific.compareTo(laterSpecific), lessThan(0));
    assertThat(laterSpecific.compareTo(earlierSpecific), greaterThan(0));
  }

  @Test
  public void routineUsesNaturalRoutineTypeBeforeSpecificName() {
    final Schema schema = new SchemaReference("catalog", "schema");
    final Routine procedure = new MutableProcedure(schema, "ROUTINE", "Z_PROCEDURE");
    final Routine function = new MutableFunction(schema, "ROUTINE", "A_FUNCTION");

    assertThat(procedure.compareTo(function), lessThan(0));
    assertThat(function.compareTo(procedure), greaterThan(0));
  }

  @Test
  public void routineParameterComparisonDoesNotReenterParentRoutine() {
    final Schema schema = new SchemaReference("catalog", "schema");
    final MutableProcedure firstRoutine = new MutableProcedure(schema, "ROUTINE", "A_SPECIFIC");
    final MutableProcedure secondRoutine = new MutableProcedure(schema, "ROUTINE", "Z_SPECIFIC");
    final MutableProcedureParameter firstParameter =
        new MutableProcedureParameter(firstRoutine, "PARAMETER");
    final MutableProcedureParameter secondParameter =
        new MutableProcedureParameter(secondRoutine, "PARAMETER");
    firstParameter.setOrdinalPosition(2);
    secondParameter.setOrdinalPosition(1);
    firstRoutine.addParameter(firstParameter);
    secondRoutine.addParameter(secondParameter);

    assertThat(firstParameter.compareTo(firstParameter), is(0));
    assertThat(firstParameter.compareTo(secondParameter), lessThan(0));
    assertThat(secondParameter.compareTo(firstParameter), greaterThan(0));
    assertThat(firstRoutine.compareTo(firstRoutine), is(0));
  }

  @Test
  public void routineComparisonDoesNotReadParameters() {
    final Schema schema = new SchemaReference("catalog", "schema");
    final MutableProcedure earlierSpecific =
        spy(new MutableProcedure(schema, "ROUTINE", "A_SPECIFIC"));
    final MutableProcedure laterSpecific =
        spy(new MutableProcedure(schema, "ROUTINE", "Z_SPECIFIC"));

    assertThat(earlierSpecific.compareTo(laterSpecific), lessThan(0));
    assertThat(laterSpecific.compareTo(earlierSpecific), greaterThan(0));
    verify(earlierSpecific, never()).getParameters();
    verify(laterSpecific, never()).getParameters();
  }

  @Test
  public void routineWithoutSpecificNameIgnoresParameterCount() {
    final Schema schema = new SchemaReference("catalog", "schema");
    final MutableProcedure twoParameters = new MutableProcedure(schema, "ROUTINE", null);
    final MutableProcedure oneParameter = new MutableProcedure(schema, "ROUTINE", null);
    final MutableProcedureParameter firstParameter =
        new MutableProcedureParameter(twoParameters, "Z_PARAMETER");
    final MutableProcedureParameter secondParameter =
        new MutableProcedureParameter(twoParameters, "Y_PARAMETER");
    final MutableProcedureParameter earlierParameter =
        new MutableProcedureParameter(oneParameter, "A_PARAMETER");
    firstParameter.setOrdinalPosition(9);
    secondParameter.setOrdinalPosition(10);
    earlierParameter.setOrdinalPosition(1);
    twoParameters.addParameter(firstParameter);
    twoParameters.addParameter(secondParameter);
    oneParameter.addParameter(earlierParameter);

    assertThat(twoParameters.getSpecificName(), is("ROUTINE"));
    assertThat(twoParameters.compareTo(oneParameter), is(0));
    assertThat(oneParameter.compareTo(twoParameters), is(0));
  }

  @Test
  public void tableUsesNaturalTableTypeBeforeName() {
    final Schema schema = new SchemaReference("catalog", "schema");
    final MutableTable table = new MutableTable(schema, "Z_TABLE");
    table.setTableType(new TableType("TABLE"));
    final MutableTable view = new MutableTable(schema, "A_VIEW");
    view.setTableType(new TableType("VIEW"));

    assertThat(table.compareTo(view), lessThan(0));
    assertThat(view.compareTo(table), greaterThan(0));
  }

  @Test
  public void tableConstraintUsesNaturalConstraintTypeBeforeName() {
    final MutableTable table = new MutableTable(new SchemaReference("catalog", "schema"), "TABLE");
    final MutablePrimaryKey primaryKey = MutablePrimaryKey.newPrimaryKey(table, "Z_PRIMARY");
    final MutablePrimaryKey alternateKey = MutablePrimaryKey.newAlternateKey(table, "A_ALTERNATE");

    assertThat(primaryKey.compareTo(alternateKey), lessThan(0));
    assertThat(alternateKey.compareTo(primaryKey), greaterThan(0));
  }
}
