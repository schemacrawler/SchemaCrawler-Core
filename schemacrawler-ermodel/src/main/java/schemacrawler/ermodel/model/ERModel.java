/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.model;

import java.util.Collection;
import java.util.Optional;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;

public interface ERModel {

  <R extends Relationship> Collection<R> getRelationships();

  <E extends Entity> Collection<E> getStrongEntities();

  <E extends EntitySubtype> Collection<E> getSubtypes();

  <E extends EntitySubtype> Collection<E> getSubtypesOf(Entity supertype);

  <T extends Table> Collection<T> getTables();

  <E extends Entity> Collection<E> getWeakEntities();

  <R extends Relationship> Optional<R> lookupByBridgeTable(Schema schema, String tableName);

  <R extends Relationship> Optional<R> lookupByBridgeTable(Table table);

  <E extends Entity> Optional<E> lookupEntity(Schema schema, String tableName);

  <E extends Entity> Optional<E> lookupEntity(String entityName);

  <E extends Entity> Optional<E> lookupEntity(Table table);
}
