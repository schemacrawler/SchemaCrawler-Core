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

public class TestNamedObjectNullName implements NamedObject {

  @Serial private static final long serialVersionUID = -5606558254139597791L;

  @Override
  public int compareTo(final NamedObject o) {
    if (o == this) {
      return 0;
    }
    return o == null ? -1 : 1;
  }

  @Override
  public String getFullName() {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public NamedObjectKey key() {
    return new NamedObjectKey();
  }
}
