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

public final class TestNamedObjectComparisonFailing implements NamedObject {

  @Serial private static final long serialVersionUID = 1L;

  private final NamedObjectKey key;

  TestNamedObjectComparisonFailing(final NamedObjectKey key) {
    this.key = key;
  }

  @Override
  public int compareTo(final NamedObject other) {
    throw new AssertionError("Snapshot must not compare objects");
  }

  @Override
  public String getFullName() {
    return key.join();
  }

  @Override
  public String getName() {
    return key.join();
  }

  @Override
  public NamedObjectKey key() {
    return key;
  }
}
