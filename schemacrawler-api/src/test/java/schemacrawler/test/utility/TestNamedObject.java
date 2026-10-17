/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility;

import java.io.Serial;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;

public class TestNamedObject implements NamedObject {

  @Serial private static final long serialVersionUID = 6242517200200079638L;

  private final String name;
  private final int value;

  public TestNamedObject(final String name, final int value) {
    this.name = name;
    this.value = value;
  }

  @Override
  public int compareTo(final NamedObject o) {
    if (o == null) {
      return -1;
    }
    if (o instanceof final TestNamedObject object) {
      return Integer.compare(value, object.value);
    }
    return -1;
  }

  @Override
  public String getFullName() {
    return name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public NamedObjectKey key() {
    return new NamedObjectKey(name);
  }
}
