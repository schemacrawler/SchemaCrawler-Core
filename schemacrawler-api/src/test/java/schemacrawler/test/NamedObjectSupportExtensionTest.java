/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;
import schemacrawler.namedobject.ConcurrentDatabaseObjectIndex;
import schemacrawler.namedobject.NamedObjectIndex;
import schemacrawler.test.utility.crawl.LightTable;

public class NamedObjectSupportExtensionTest {

  @Test
  public void publicBasesAndIndexesAreUsableOutsideSupportPackage() {
    final LightTable namedObject = new LightTable("table");
    namedObject.setAttribute("attribute", "value");
    final ConcurrentDatabaseObjectIndex<LightTable> mutableIndex =
        new ConcurrentDatabaseObjectIndex<>();
    final NamedObjectIndex<LightTable> index = mutableIndex;

    assertTrue(mutableIndex.add(namedObject));
    assertSame(namedObject, index.lookup(namedObject.key()).orElseThrow());
    assertEquals("value", namedObject.getAttribute("attribute"));
    assertEquals(List.of(namedObject), index.getObjects());

    final LightTable clonedNamedObject = SerializationUtils.clone(namedObject);
    assertEquals(namedObject.key(), clonedNamedObject.key());
    assertEquals("value", clonedNamedObject.getAttribute("attribute"));
  }
}
