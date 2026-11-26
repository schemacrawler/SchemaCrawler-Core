/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility.crawl;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Synonym;
import schemacrawler.schemacrawler.SchemaReference;

public class LightSynonym extends AbstractLightDatabaseObject implements Synonym {

  @Serial private static final long serialVersionUID = 1L;

  public LightSynonym(final Schema schema, final String name) {
    super(schema, name);
  }

  public LightSynonym(final String name) {
    this(new SchemaReference(), name);
  }

  @Override
  public DatabaseObject getReferencedObject() {
    return null;
  }

  @Override
  public Collection<? extends DatabaseObject> getReferencedObjects() {
    return List.of();
  }

  @Override
  public boolean hasReferencedObject() {
    return false;
  }
}
