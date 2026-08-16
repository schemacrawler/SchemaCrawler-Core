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

import org.junit.jupiter.api.Test;
import schemacrawler.schemacrawler.SchemaReference;
import schemacrawler.test.utility.crawl.LightColumn;
import schemacrawler.test.utility.crawl.LightPrimaryKey;
import schemacrawler.test.utility.crawl.LightTable;
import schemacrawler.tools.utility.EntityModelType;

public class EntityModelTypeTest {

  @Test
  public void returnsUnknownForNullTable() {
    assertThat(EntityModelType.from(null), is(EntityModelType.unknown));
  }

  @Test
  public void returnsStrongEntityForTableWithSelfSufficientPrimaryKey() {
    final LightTable table = new LightTable(new SchemaReference("PUBLIC", "BOOKS"), "AUTHORS");
    final LightColumn id = table.addColumn("ID");
    table.setPrimaryKey(new LightPrimaryKey(id));

    assertThat(EntityModelType.from(table), is(EntityModelType.strong_entity));
  }
}
