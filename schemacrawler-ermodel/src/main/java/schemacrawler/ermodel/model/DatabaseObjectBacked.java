/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.model;

import schemacrawler.schema.AttributedObject;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.DescribedObject;
import schemacrawler.schema.NamedObject;

public interface DatabaseObjectBacked<DO extends DatabaseObject>
    extends NamedObject, AttributedObject, DescribedObject {

  DO getDatabaseObject();
}
