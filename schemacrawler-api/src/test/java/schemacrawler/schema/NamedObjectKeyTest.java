/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.schema;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

public class NamedObjectKeyTest {

  @Test
  public void normalizedKeyPreservesComponents() {
    final NamedObjectKey key = new NamedObjectKey("\"Catalog\"", "`Schema`", "[A.B]");

    assertThat(key.normalized(), equalTo(new NamedObjectKey("catalog", "schema", "a.b")));
    assertThat(key, equalTo(new NamedObjectKey("\"Catalog\"", "`Schema`", "[A.B]")));
    assertThat(key.equals(key.normalized()), is(false));
  }

  @Test
  public void normalizedKeyPreservesInteriorWhitespaceAndPunctuation() {
    final NamedObjectKey key = new NamedObjectKey("\"A B\"", "\"A-B\"");

    assertThat(key.normalized(), equalTo(new NamedObjectKey("a b", "a-b")));
  }
}
