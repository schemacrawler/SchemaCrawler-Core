/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.model;

/** Represents the entity attribute type, corresponding to the data type of the backing column. */
public enum EntityAttributeType {
  unknown,
  binary,
  bool,
  date,
  decimal,
  enumerated,
  integer,
  other,
  string,
  time,
  timestamp,
}
