/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.sort;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

import java.sql.JDBCType;
import org.junit.jupiter.api.Test;
import schemacrawler.schema.JavaSqlType;
import schemacrawler.schema.JavaSqlTypeGroup;

public class JavaSqlTypeSortTest {

  @Test
  public void javaSqlType_compareTo() {

    final JavaSqlType javaSqlType =
        new JavaSqlType(JDBCType.INTEGER, Integer.class, JavaSqlTypeGroup.integer);

    assertThat(javaSqlType.compareTo(null), is(greaterThan(0)));
    assertThat(JavaSqlType.UNKNOWN.compareTo(null), is(greaterThan(0)));

    assertThat(javaSqlType.compareTo(javaSqlType), is(0));
    assertThat(JavaSqlType.UNKNOWN.compareTo(JavaSqlType.UNKNOWN), is(0));

    assertThat(javaSqlType.compareTo(JavaSqlType.UNKNOWN), is(lessThan(0)));
    assertThat(JavaSqlType.UNKNOWN.compareTo(javaSqlType), is(greaterThan(0)));
  }
}
