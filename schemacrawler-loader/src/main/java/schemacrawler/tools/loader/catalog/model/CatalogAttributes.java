/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.loader.catalog.model;

import java.beans.ConstructorProperties;
import java.io.Serial;
import java.util.List;
import java.util.Map;

public final class CatalogAttributes extends ObjectAttributes {

  @Serial private static final long serialVersionUID = 1436642683972751860L;

  private final List<TableAttributes> tables;
  private final List<WeakAssociationAttributes> weakAssociations;

  @ConstructorProperties({"name", "remarks", "attributes", "tables", "weak-associations"})
  public CatalogAttributes(
      final String name,
      final List<String> remarks,
      final Map<String, String> attributes,
      final List<TableAttributes> tables,
      final List<WeakAssociationAttributes> weakAssociations) {
    super(name, remarks, attributes);
    if (tables == null) {
      this.tables = List.of();
    } else {
      this.tables = List.copyOf(tables);
    }
    if (weakAssociations == null) {
      this.weakAssociations = List.of();
    } else {
      this.weakAssociations = List.copyOf(weakAssociations);
    }
  }

  public List<TableAttributes> getTables() {
    return tables;
  }

  public List<WeakAssociationAttributes> getWeakAssociations() {
    return weakAssociations;
  }
}
