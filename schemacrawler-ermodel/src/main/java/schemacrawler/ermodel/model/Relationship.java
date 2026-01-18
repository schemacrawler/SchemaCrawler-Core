/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.model;

/** Base relationship abstraction. */
public interface Relationship {

  Entity getLeftEntity();

  Entity getRightEntity();

  RelationshipCardinality getCardinality();

  boolean isBasedOnForeignKeys();
}
