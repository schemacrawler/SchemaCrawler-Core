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

import org.junit.jupiter.api.Test;

public class NamedObjectKeyTest {

  @Test
  public void joinCombinesAllNonBlankPartsWithDot() {
    final NamedObjectKey key = new NamedObjectKey("catalog", "schema", "table");

    assertThat(key.join(), equalTo("catalog.schema.table"));
  }

  @Test
  public void joinSkipsNullAndEmptyParts() {
    assertThat(new NamedObjectKey("catalog", null, "table").join(), equalTo("catalog.table"));
    assertThat(new NamedObjectKey("catalog", "", "table").join(), equalTo("catalog.table"));
    assertThat(new NamedObjectKey(null, null, "table").join(), equalTo("table"));
  }

  @Test
  public void joinOnEmptyKeyReturnsEmptyString() {
    assertThat(new NamedObjectKey().join(), equalTo(""));
    assertThat(new NamedObjectKey((String[]) null).join(), equalTo(""));
    assertThat(new NamedObjectKey(null, "", null).join(), equalTo(""));
  }

  @Test
  public void joinPreservesLiteralDotsWithinIndividualParts() {
    // Each part was captured separately (never split from one concatenated string), so a
    // literal dot embedded inside a single part is preserved as content, distinct from the
    // separator dot added between parts.
    final NamedObjectKey key = new NamedObjectKey("my.schema", "my.table");

    assertThat(key.join(), equalTo("my.schema.my.table"));
  }

  @Test
  public void withoutLastDropsOnlyTheFinalPart() {
    final NamedObjectKey key = new NamedObjectKey("catalog", "schema", "name", "specificName");

    assertThat(key.withoutLast().join(), equalTo("catalog.schema.name"));
  }

  @Test
  public void withoutLastOnSinglePartKeyReturnsEmptyKey() {
    final NamedObjectKey key = new NamedObjectKey("onlyPart");

    assertThat(key.withoutLast(), equalTo(new NamedObjectKey()));
    assertThat(key.withoutLast().join(), equalTo(""));
  }

  @Test
  public void withoutLastOnEmptyKeyIsANoOp() {
    final NamedObjectKey key = new NamedObjectKey();

    assertThat(key.withoutLast().join(), equalTo(""));
  }
}
