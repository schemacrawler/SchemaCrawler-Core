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

import java.io.Serial;
import java.util.List;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;
import schemacrawler.namedobject.AbstractNamedObjectWithAttributes;
import schemacrawler.namedobject.ConcurrentNamedObjectIndex;
import schemacrawler.namedobject.NamedObjectIndex;

public class NamedObjectSupportExtensionTest {

  private static final class TestNamedObject extends AbstractNamedObjectWithAttributes {

    @Serial private static final long serialVersionUID = 1L;

    TestNamedObject(final String name) {
      super(name);
    }
  }

  @Test
  public void publicBasesAndIndexesAreUsableOutsideSupportPackage() {
    final TestNamedObject namedObject = new TestNamedObject("named-object");
    namedObject.setAttribute("attribute", "value");
    final ConcurrentNamedObjectIndex<TestNamedObject> mutableIndex =
        new ConcurrentNamedObjectIndex<>();
    final NamedObjectIndex<TestNamedObject> index = mutableIndex;

    assertTrue(mutableIndex.add(namedObject));
    assertSame(namedObject, index.lookup(namedObject.key()).orElseThrow());
    assertEquals("value", namedObject.getAttribute("attribute"));
    assertEquals(List.of(namedObject), index.getObjects());

    final TestNamedObject clonedNamedObject = SerializationUtils.clone(namedObject);
    assertEquals(namedObject.key(), clonedNamedObject.key());
    assertEquals("value", clonedNamedObject.getAttribute("attribute"));
  }
}
