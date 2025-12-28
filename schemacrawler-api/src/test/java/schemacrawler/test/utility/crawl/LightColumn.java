/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility.crawl;

import static java.util.Objects.requireNonNull;
import static schemacrawler.test.utility.crawl.LightColumnDataTypeFactory.columnDataType;
import static schemacrawler.test.utility.crawl.LightColumnDataTypeFactory.enumColumnDataType;
import static us.fatehi.utility.Utility.requireNotBlank;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.Identifiers;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Privilege;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;

final class LightColumn implements Column {

  @Serial private static final long serialVersionUID = -1931193814458050468L;

  public static LightColumn newColumn(final Table parent, final String name) {
    return new LightColumn(
        parent, name, columnDataType("INTEGER"), /* isHidden */ false, /* isGenerated */ false);
  }

  public static LightColumn newEnumeratedColumn(final Table parent, final String name) {
    return new LightColumn(
        parent, name, enumColumnDataType(), /* isHidden */ false, /* isGenerated */ false);
  }

  public static LightColumn newGeneratedColumn(final Table parent, final String name) {
    return new LightColumn(
        parent, name, columnDataType("DATA_TYPE"), /* isHidden */ false, /* isGenerated */ true);
  }

  public static LightColumn newHiddenColumn(final Table parent, final String name) {
    return new LightColumn(
        parent, name, columnDataType("DATA_TYPE"), /* isHidden */ true, /* isGenerated */ false);
  }

  private final Table parent;
  private final String name;
  private final ColumnDataType columnDataType;
  private final boolean isHidden;
  private final boolean isGenerated;

  private LightColumn(
      final Table parent,
      final String name,
      final ColumnDataType columnDataType,
      final boolean isHidden,
      final boolean isGenerated) {
    this.parent = requireNonNull(parent);
    this.name = requireNotBlank(name, "No name provided");
    this.columnDataType = requireNonNull(columnDataType, "No column data type provided");
    this.isHidden = isHidden;
    this.isGenerated = isGenerated;
  }

  @Override
  public int compareTo(final NamedObject o) {
    return 0;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if ((obj == null) || (getClass() != obj.getClass())) {
      return false;
    }
    final LightColumn other = (LightColumn) obj;
    return Objects.equals(name, other.name) && Objects.equals(parent, other.parent);
  }

  @Override
  public <T> T getAttribute(final String name) {
    return null;
  }

  @Override
  public <T> T getAttribute(final String name, final T defaultValue) throws ClassCastException {
    return null;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return new HashMap<>();
  }

  @Override
  public ColumnDataType getColumnDataType() {
    return columnDataType;
  }

  @Override
  public int getDecimalDigits() {
    return 0;
  }

  @Override
  public String getDefaultValue() {
    return null;
  }

  @Override
  public String getFullName() {
    final StringBuffer buffer = new StringBuffer();
    buffer.append(parent.getFullName()).append(".").append(name);
    return buffer.toString();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public int getOrdinalPosition() {
    return 0;
  }

  @Override
  public Table getParent() {
    return parent;
  }

  @Override
  public Collection<Privilege<Column>> getPrivileges() {
    return Collections.emptyList();
  }

  @Override
  public Column getReferencedColumn() {
    return null;
  }

  @Override
  public String getRemarks() {
    return "";
  }

  @Override
  public Schema getSchema() {
    return parent.getSchema();
  }

  @Override
  public String getShortName() {
    return name;
  }

  @Override
  public int getSize() {
    return 0;
  }

  @Override
  public ColumnDataType getType() {
    return getColumnDataType();
  }

  @Override
  public String getWidth() {
    return "";
  }

  @Override
  public boolean hasAttribute(final String name) {
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, parent);
  }

  @Override
  public boolean hasRemarks() {
    return false;
  }

  @Override
  public boolean isAutoIncremented() {
    return false;
  }

  @Override
  public boolean isColumnDataTypeKnown() {
    return true;
  }

  @Override
  public boolean isGenerated() {
    return isGenerated;
  }

  @Override
  public boolean isHidden() {
    return isHidden;
  }

  @Override
  public boolean isNullable() {
    return false;
  }

  @Override
  public boolean isParentPartial() {
    return false;
  }

  @Override
  public boolean isPartOfForeignKey() {
    return false;
  }

  @Override
  public boolean isPartOfIndex() {
    return false;
  }

  @Override
  public boolean isPartOfPrimaryKey() {
    return true;
  }

  @Override
  public boolean isPartOfUniqueIndex() {
    return true;
  }

  @Override
  public NamedObjectKey key() {
    return parent.key().with(name);
  }

  @Override
  public <T> Optional<T> lookupAttribute(final String name) {
    return Optional.empty();
  }

  @Override
  public <P extends Privilege<Column>> Optional<P> lookupPrivilege(final String name) {
    return Optional.empty();
  }

  @Override
  public void removeAttribute(final String name) {}

  @Override
  public <T> void setAttribute(final String name, final T value) {}

  @Override
  public void setRemarks(final String remarks) {}

  @Override
  public String toString() {
    return getFullName();
  }

  @Override
  public void withQuoting(final Identifiers identifiers) {
    // No-op
  }
}
