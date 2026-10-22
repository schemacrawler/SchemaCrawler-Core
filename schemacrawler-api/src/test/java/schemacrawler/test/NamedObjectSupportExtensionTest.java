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
import schemacrawler.namedobject.BuildableDatabaseObjectIndex;
import schemacrawler.namedobject.MapBackedNamedObjectIndex;
import schemacrawler.namedobject.NamedObjectIndex;
import schemacrawler.namedobject.NamedObjectIndexes;
import schemacrawler.test.utility.crawl.LightTable;

public class NamedObjectSupportExtensionTest {

  private static final class ExtendedNamedObject extends AbstractNamedObjectWithAttributes {

    @Serial private static final long serialVersionUID = 1L;

    ExtendedNamedObject(final String name) {
      super(name);
    }
  }

  @Test
  public void publicAttributeBaseSupportsExternalSubclassesAndGenericIndexes() {
    final ExtendedNamedObject object = new ExtendedNamedObject("object");
    object.setAttribute("attribute", "value");
    final MapBackedNamedObjectIndex<ExtendedNamedObject> mutable =
        NamedObjectIndexes.newBuildableNamedObjectIndex();
    mutable.add(object);
    final NamedObjectIndex<ExtendedNamedObject> index = mutable;
    assertSame(object, index.lookup(object.key()).orElseThrow());
    final ExtendedNamedObject copy = SerializationUtils.clone(object);
    assertEquals(object.key(), copy.key());
    assertEquals("value", copy.getAttribute("attribute"));
  }

  @Test
  public void databaseIndexesAreUsableOutsideSupportPackage() {
    final LightTable namedObject = new LightTable("table");
    namedObject.setAttribute("attribute", "value");
    final BuildableDatabaseObjectIndex<LightTable> mutableIndex =
        NamedObjectIndexes.newBuildableDatabaseObjectIndex();
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
