/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import schemacrawler.schemacrawler.SchemaReference;

public class SchemaReferenceSortTest {

  @Test
  public void nullSchemasAndNullableComponentsSortLast() {
    final SchemaReference absentCatalog = new SchemaReference(null, "schema");
    final SchemaReference absentSchema = new SchemaReference("catalog", null);
    final SchemaReference named = new SchemaReference("catalog", "schema");
    final SchemaReference empty = new SchemaReference("catalog", "");
    assertTrue(named.compareTo(null) < 0);
    assertTrue(named.compareTo(absentCatalog) < 0);
    assertTrue(absentCatalog.compareTo(named) > 0);
    assertTrue(named.compareTo(absentSchema) < 0);
    assertTrue(absentSchema.compareTo(named) > 0);
    assertTrue(empty.compareTo(absentSchema) < 0);
    assertEquals(0, absentSchema.compareTo(new SchemaReference("catalog", null)));
  }
}
