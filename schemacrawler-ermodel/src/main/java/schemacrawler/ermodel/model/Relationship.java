/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.model;

import schemacrawler.schema.AttributedObject;
import schemacrawler.schema.DescribedObject;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.TypedObject;

/** Base relationship abstraction. */
public interface Relationship
    extends NamedObject, AttributedObject, DescribedObject, TypedObject<RelationshipCardinality> {

  Entity getLeftEntity();

  Entity getRightEntity();
}
