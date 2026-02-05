/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.model;

import java.util.List;
import schemacrawler.schema.Column;
import schemacrawler.schema.TypedObject;

/** Conceptual entity backed by a SchemaCrawler column. */
public interface EntityAttribute
    extends DatabaseObjectBacked<Column>, TypedObject<EntityAttributeType> {

  default Column getColumn() {
    return getDatabaseObject();
  }

  /**
   * Get list of enum values if the data type is enumerated.
   *
   * @return List of enum values
   */
  List<String> getEnumValues();

  /**
   * Gets the default data value for the column.
   *
   * @return Default data value for the column
   */
  String getDefaultValue();

  /**
   * Whether the attribute is optional.
   *
   * @return Whether the attribute is optional
   */
  boolean isOptional();
}
