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

import org.junit.jupiter.api.Test;
import us.fatehi.utility.datasource.DatabaseServerType;

public class DatabaseServerTypeSortTest {

  @Test
  public void compareTo() {
    final DatabaseServerType databaseServerType0 = DatabaseServerType.UNKNOWN;
    final DatabaseServerType databaseServerType1 = new DatabaseServerType("newdb", "NewDB");

    assertThat(databaseServerType0, greaterThan(null));
    assertThat(databaseServerType1, greaterThan(null));

    assertThat(databaseServerType0, comparesEqualTo(databaseServerType0));
    assertThat(databaseServerType1, comparesEqualTo(databaseServerType1));

    assertThat(databaseServerType0, lessThan(databaseServerType1));
    assertThat(databaseServerType1, greaterThan(databaseServerType0));

    final DatabaseServerType otherKnown = new DatabaseServerType("otherdb", "OtherDB");
    final DatabaseServerType unknownCopy = new DatabaseServerType("unknown", "Unknown");
    assertThat(databaseServerType1, lessThan(otherKnown));
    assertThat(otherKnown, greaterThan(databaseServerType1));
    assertThat(databaseServerType0, comparesEqualTo(unknownCopy));
    assertThat(unknownCopy, comparesEqualTo(databaseServerType0));
  }
}
