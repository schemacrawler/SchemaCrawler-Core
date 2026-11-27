/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility.crawl;

import java.io.Serial;
import java.math.BigInteger;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Sequence;
import schemacrawler.schemacrawler.SchemaReference;

public class LightSequence extends AbstractLightDatabaseObject implements Sequence {

  @Serial private static final long serialVersionUID = 1L;

  public LightSequence(final Schema schema, final String name) {
    super(schema, name);
  }

  public LightSequence(final String name) {
    this(new SchemaReference(), name);
  }

  @Override
  public long getIncrement() {
    return 1;
  }

  @Override
  public BigInteger getMaximumValue() {
    return null;
  }

  @Override
  public BigInteger getMinimumValue() {
    return null;
  }

  @Override
  public BigInteger getStartValue() {
    return null;
  }

  @Override
  public boolean isCycle() {
    return false;
  }
}
