/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.associations;

import static java.util.Objects.requireNonNull;
import static schemacrawler.utility.MetaDataUtility.isPartial;
import static us.fatehi.utility.Utility.isBlank;
import static us.fatehi.utility.Utility.trimToEmpty;

import java.io.Serial;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.Identifiers;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableConstraintColumn;
import schemacrawler.schema.TableConstraintType;
import schemacrawler.schema.TableReference;
import us.fatehi.utility.CollectionsUtility;

/** Represents a weak association between two tables. */
public final class ImplicitAssociation implements TableReference {

  @Serial private static final long serialVersionUID = -246830743604473724L;

  private static final String REMARKS_ATTRIBUTE = "REMARKS";

  private final String name;
  private transient String fullName;
  private final Schema schema;
  private transient NamedObjectKey key;

  private final Table fkTable;
  private final Table pkTable;
  private final ImplicitColumnReference columnReference;
  private final TableConstraintColumn tableConstraintColumn;
  private final boolean isSelfReferencing;
  private final boolean isOptional;

  private final Map<String, Object> attributeMap;

  public ImplicitAssociation(final ImplicitColumnReference columnReference) {
    this.columnReference = requireNonNull(columnReference, "No column reference provided");
    final Column fkColumn = columnReference.getForeignKeyColumn();

    fkTable = fkColumn.getParent();
    pkTable = columnReference.getPrimaryKeyColumn().getParent();

    name =
        "SCHCRWLR_%1$08X_%2$08X"
            .formatted(
                fkColumn.getFullName().hashCode(),
                columnReference.getPrimaryKeyColumn().getFullName().hashCode());
    schema = fkTable.getSchema();

    tableConstraintColumn = TableConstraintColumnWrapper.createConstrainedColumn(fkColumn, this);
    isSelfReferencing = getParent().equals(pkTable);
    isOptional = !isPartial(fkColumn) && fkColumn.isNullable();

    attributeMap = new ConcurrentHashMap<>();
  }

  /**
   * {@inheritDoc}
   *
   * <p>NOTE: compareTo is not compatible with equals. equals compares the full name of a database
   * object, but compareTo uses more fields to define a "natural" sorting order. compareTo may
   * return incorrect results until the object is fully built by SchemaCrawler.
   *
   * <p>Since foreign keys are not always explicitly named in databases, the sorting routine orders
   * the foreign keys by the names of the columns in the foreign keys.
   */
  @Override
  public int compareTo(final NamedObject obj) {
    if (obj == null) {
      return -1;
    }

    if (obj instanceof final TableReference other) {
      final List<ColumnReference> thisColumnReferences = getColumnReferences();
      final List<ColumnReference> otherColumnReferences = other.getColumnReferences();

      return CollectionsUtility.compareLists(thisColumnReferences, otherColumnReferences);
    }

    if (obj instanceof NamedObject) {
      final NamedObject other = obj;
      return key().compareTo(other.key());
    }

    return -1;
  }

  /** {@inheritDoc} */
  @Override
  public <T> T getAttribute(final String name) {
    return getAttribute(name, null);
  }

  /** {@inheritDoc} */
  @Override
  public <T> T getAttribute(final String name, final T defaultValue) throws ClassCastException {
    return (T) attributeMap.getOrDefault(name, defaultValue);
  }

  /** {@inheritDoc} */
  @Override
  public Map<String, Object> getAttributes() {
    return Map.copyOf(attributeMap);
  }

  @Override
  public List<ColumnReference> getColumnReferences() {
    return List.of(columnReference);
  }

  @Override
  public List<TableConstraintColumn> getConstrainedColumns() {
    return List.of(tableConstraintColumn);
  }

  @Override
  public String getDefinition() {
    return "";
  }

  @Override
  public Table getForeignKeyTable() {
    return fkTable;
  }

  /** {@inheritDoc} */
  @Override
  public String getFullName() {
    buildFullName();
    return fullName;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Table getParent() {
    return getForeignKeyTable();
  }

  @Override
  public Table getPrimaryKeyTable() {
    return pkTable;
  }

  @Override
  public String getRemarks() {
    final Object remarks = attributeMap.get(REMARKS_ATTRIBUTE);
    if (remarks == null) {
      return "";
    }
    return String.valueOf(remarks);
  }

  @Override
  public Schema getSchema() {
    return schema;
  }

  @Override
  public String getShortName() {
    return getName();
  }

  @Override
  public TableConstraintType getType() {
    return TableConstraintType.unknown;
  }

  /** {@inheritDoc} */
  @Override
  public boolean hasAttribute(final String name) {
    return attributeMap.containsKey(name);
  }

  @Override
  public boolean hasDefinition() {
    return false;
  }

  @Override
  public int hashCode() {
    return columnReference.hashCode();
  }

  /** {@inheritDoc} */
  @Override
  public boolean hasRemarks() {
    return hasAttribute(REMARKS_ATTRIBUTE) && !isBlank(getRemarks());
  }

  @Override
  public boolean isDeferrable() {
    return false;
  }

  @Override
  public boolean isInitiallyDeferred() {
    return false;
  }

  @Override
  public boolean isOptional() {
    return isOptional;
  }

  @Override
  public boolean isParentPartial() {
    return isPartial(fkTable);
  }

  @Override
  public boolean isSelfReferencing() {
    return isSelfReferencing;
  }

  @Override
  public Iterator<ColumnReference> iterator() {
    return getColumnReferences().iterator();
  }

  @Override
  public NamedObjectKey key() {
    buildKey();
    return key;
  }

  /** {@inheritDoc} */
  @Override
  public <T> Optional<T> lookupAttribute(final String name) {
    if (name == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(getAttribute(name));
  }

  /** {@inheritDoc} */
  @Override
  public void removeAttribute(final String name) {
    if (!isBlank(name)) {
      attributeMap.remove(name);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setAttribute(final String name, final Object value) {
    if (!isBlank(name)) {
      if (value == null) {
        attributeMap.remove(name);
      } else {
        attributeMap.put(name, value);
      }
    }
  }

  @Override
  public void setRemarks(final String remarks) {
    setAttribute(REMARKS_ATTRIBUTE, trimToEmpty(remarks));
  }

  @Override
  public String toString() {
    return getFullName();
  }

  @Override
  public void withQuoting(final Identifiers identifiers) {
    if (identifiers == null) {
      return;
    }
    fullName = identifiers.quoteFullName(this);
  }

  private void buildFullName() {
    if (fullName != null) {
      return;
    }
    fullName = Identifiers.STANDARD.quoteFullName(this);
  }

  private void buildKey() {
    if (key != null) {
      return;
    }
    key = schema.key().with(name);
  }
}
