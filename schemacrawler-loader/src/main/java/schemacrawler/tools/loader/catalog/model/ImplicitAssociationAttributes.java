/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.loader.catalog.model;

import static java.util.Objects.requireNonNull;

import java.beans.ConstructorProperties;
import java.io.Serial;
import java.util.List;
import java.util.Map;

public class ImplicitAssociationAttributes extends ObjectAttributes {

  @Serial private static final long serialVersionUID = 8305929253225133307L;

  private final TableAttributes dependentTable;
  private final TableAttributes referencedTable;
  private final Map<String, String> columnReferences;

  @ConstructorProperties({
    "name",
    "remarks",
    "attributes",
    "referenced-table",
    "referencing-table",
    "column-references"
  })
  public ImplicitAssociationAttributes(
      final String name,
      final List<String> remarks,
      final Map<String, String> attributes,
      final TableAttributes referencedTable,
      final TableAttributes dependentTable,
      final Map<String, String> columnReferences) {
    super(name, remarks, attributes);
    this.referencedTable = requireNonNull(referencedTable, "No referenced table provided");
    this.dependentTable = requireNonNull(dependentTable, "No referencing table provided");
    if (columnReferences == null || columnReferences.isEmpty()) {
      throw new IllegalArgumentException("No column references provided");
    }
    // This is an ordered map read from a file, so do not make a copy
    this.columnReferences = columnReferences;
  }

  public Map<String, String> getColumnReferences() {
    // This is an ordered map read from a file, so do not make a copy
    return columnReferences;
  }

  public TableAttributes getDependentTable() {
    return dependentTable;
  }

  public TableAttributes getReferencedTable() {
    return referencedTable;
  }
}
