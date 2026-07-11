/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.utility;

import static java.util.stream.Collectors.toList;
import static schemacrawler.ermodel.model.RelationshipCardinality.many_many;
import static schemacrawler.ermodel.model.RelationshipCardinality.one_many;
import static schemacrawler.ermodel.model.RelationshipCardinality.zero_many;
import static schemacrawler.utility.MetaDataUtility.getSimpleTypeName;
import static schemacrawler.utility.MetaDataUtility.isPartial;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static us.fatehi.utility.Utility.trimToEmpty;

import schemacrawler.ermodel.model.ERModel;
import schemacrawler.ermodel.model.Entity;
import schemacrawler.ermodel.model.Relationship;
import schemacrawler.ermodel.model.RelationshipCardinality;
import schemacrawler.ermodel.utility.ERModelUtility;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.DescribedObject;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.IdentifierQuotingStrategy;
import schemacrawler.schema.Identifiers;
import schemacrawler.schema.IdentifiersBuilder;
import schemacrawler.schema.Index;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.PrimaryKey;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableReference;
import schemacrawler.tools.state.AbstractExecutionState;
import schemacrawler.utility.MetaDataUtility;
import schemacrawler.utility.MetaDataUtility.SimpleDatabaseObjectType;
import us.fatehi.utility.string.StringFormat;

/**
 * Shared base for classes that format catalog and ER-model objects as text. Provides null-safe
 * helpers for names, columns, cardinality symbols, and entity-relationship data on top of the
 * catalog and ER model carried by {@link AbstractExecutionState}. Concrete subclasses (for example,
 * the scripting and Scribe support classes) reuse these helpers without duplicating them.
 */
public abstract class AbstractTextSupport extends AbstractExecutionState {

  private static final Logger LOGGER = Logger.getLogger(AbstractTextSupport.class.getName());

  private final Identifiers quotedIdentifiers;

  /** Creates the shared text-support base, using fully-quoted identifiers for column listings. */
  protected AbstractTextSupport() {
    quotedIdentifiers =
        IdentifiersBuilder.builder()
            .withIdentifierQuotingStrategy(IdentifierQuotingStrategy.quote_all)
            .toOptions();
  }

  /**
   * Infers the relationship cardinality of a foreign key.
   *
   * @param fk Foreign key
   * @return Relationship cardinality
   */
  public RelationshipCardinality cardinality(final TableReference fk) {
    return ERModelUtility.inferCardinality(fk);
  }

  /**
   * Show cardinality symbol, from PK to FK column.
   *
   * @param rel Foreign key
   * @return Cardinality symbol, from PK to FK column
   */
  public String cardinalitySymbol(final Relationship rel) {
    final RelationshipCardinality cardinality;
    if (rel == null) {
      cardinality = RelationshipCardinality.unknown;
    } else {
      cardinality = rel.getType();
    }
    return cardinalitySymbol(cardinality);
  }

  /**
   * Show cardinality symbol, from PK to FK column.
   *
   * @param fk Foreign key
   * @return Cardinality symbol, from PK to FK column
   */
  public String cardinalitySymbol(final TableReference fk) {
    final RelationshipCardinality cardinality = ERModelUtility.inferCardinality(fk);
    return cardinalitySymbol(cardinality);
  }

  /**
   * Gets the full name of a named object without quotes.
   *
   * @param namedObject Named object
   * @return Unquoted full name, or an empty string when the object is {@code null}
   */
  public String cleanFullName(final NamedObject namedObject) {
    if (namedObject == null) {
      return "";
    }
    return namedObject.getFullName().replace("\"", "");
  }

  /**
   * Gets the name of a named object without quotes.
   *
   * @param namedObject Named object
   * @return Unquoted name, or an empty string when the object is {@code null}
   */
  public String cleanName(final NamedObject namedObject) {
    if (namedObject == null) {
      return "";
    }
    return trimToEmpty(namedObject.getName()).replace("\"", "");
  }

  /**
   * Gets the columns of an index as a quoted, comma-separated string.
   *
   * @param index Index
   * @return Column list string, or an empty string when the index is {@code null}
   */
  public String columns(final Index index) {
    if (index == null) {
      return "";
    }
    return MetaDataUtility.getColumnsListAsString(index, quotedIdentifiers);
  }

  /**
   * Gets the columns of a primary key as a quoted, comma-separated string.
   *
   * @param primaryKey Primary key
   * @return Column list string, or an empty string when the primary key is {@code null}
   */
  public String columns(final PrimaryKey primaryKey) {
    if (primaryKey == null) {
      return "";
    }
    return MetaDataUtility.getColumnsListAsString(primaryKey, quotedIdentifiers);
  }

  /**
   * Gets the data-type name of a column.
   *
   * @param column Column
   * @return Data-type name, or an empty string when unavailable
   */
  public String columnType(final Column column) {
    if (column == null || column.getColumnDataType() == null) {
      return "";
    }
    return column.getColumnDataType().getName();
  }

  /**
   * Gets the entities in the ER model, including those inferred for otherwise unmodeled tables.
   *
   * @return Entities, or an empty collection when no ER model is available
   */
  public Collection<Entity> entities() {
    if (!hasERModel()) {
      return List.of();
    }
    final ERModel erModel = getERModel();
    final List<Entity> allEntities = new ArrayList<>(erModel.getEntities());
    for (final Table table : erModel.getUnmodeledTables()) {
      if (isPartial(table) || getSimpleTypeName(table) == SimpleDatabaseObjectType.view) {
        LOGGER.log(Level.FINE, new StringFormat("Excluding table <%s>", table));
        continue;
      }
      final Optional<Entity> optionalEntity = erModel.lookupEntity(table);
      if (optionalEntity.isEmpty()) {
        LOGGER.log(Level.FINE, new StringFormat("Entity not found for table <%s>", table));
        continue;
      }
      final Entity entity = optionalEntity.get();
      allEntities.add(entity);
    }
    return List.copyOf(allEntities);
  }

  /**
   * Gets the foreign-key (constrained) columns as a quoted, comma-separated string.
   *
   * @param foreignKey Foreign key
   * @return Column list string, or an empty string when the foreign key is {@code null}
   */
  public String fkColumns(final ForeignKey foreignKey) {
    if (foreignKey == null) {
      return "";
    }
    return MetaDataUtility.joinColumns(
        foreignKey.getConstrainedColumns(), false, quotedIdentifiers);
  }

  /**
   * Indicates whether a database object has a name that is not system-generated.
   *
   * @param dbObject Database object
   * @return {@code true} when the object has a user-visible name
   */
  public boolean hasUserDefinedName(final DatabaseObject dbObject) {
    return !MetaDataUtility.hasSystemGeneratedName(dbObject);
  }

  /**
   * Indents each line of the given text by the given number of spaces.
   *
   * @param text Text to indent
   * @param indent Number of spaces
   * @return Indented text, or an empty string when the text is {@code null}
   */
  public String indent(final String text, final int indent) {
    if (text == null) {
      return "";
    }
    return text.indent(indent);
  }

  /**
   * Indicates whether the foreign key points to a "many" side.
   *
   * @param fk Foreign key
   * @return {@code true} for zero-or-many, one-or-many, or many-to-many cardinality
   */
  public boolean isToMany(final TableReference fk) {
    final RelationshipCardinality cardinality = ERModelUtility.inferCardinality(fk);
    return EnumSet.of(many_many, one_many, zero_many).contains(cardinality);
  }

  /**
   * Gets the indexes of a table, excluding any index equivalent to the primary key.
   *
   * @param table Table
   * @return Non-primary-key indexes, or an empty list when none are available
   */
  public List<Index> nonPrimaryIndexes(final Table table) {
    final List<Index> indexes = new ArrayList<>();
    if (table == null || table.getIndexes() == null || table.getIndexes().isEmpty()) {
      return indexes;
    }
    for (final Index index : table.getIndexes()) {
      if (!isPrimaryKeyEquivalentIndex(table, index)) {
        indexes.add(index);
      }
    }
    return indexes;
  }

  /**
   * Gets the referenced primary-key columns of a foreign key as a quoted, comma-separated string.
   *
   * @param foreignKey Foreign key
   * @return Column list string, or an empty string when the foreign key is {@code null}
   */
  public String pkColumns(final ForeignKey foreignKey) {
    if (foreignKey == null) {
      return "";
    }
    final List<Column> pkColumns =
        foreignKey.getColumnReferences().stream()
            .map(ColumnReference::getPrimaryKeyColumn)
            .collect(toList());
    return MetaDataUtility.joinColumns(pkColumns, false, quotedIdentifiers);
  }

  /**
   * Gets the simple type name of a database object (for example, {@code TABLE} or {@code VIEW}).
   *
   * @param dbObject Database object
   * @return Simple type name
   */
  public String simpleTypeName(final DatabaseObject dbObject) {
    return MetaDataUtility.getSimpleTypeName(dbObject).toString();
  }

  /**
   * Puts remarks on a single line, normalizing line breaks to spaces and replacing double quotes
   * with single quotes.
   *
   * @param describedObject Object with remarks
   * @return Remarks on a single line
   */
  public String singleLineRemarks(final DescribedObject describedObject) {
    if (describedObject == null || !describedObject.hasRemarks()) {
      return "";
    }
    return describedObject.getRemarks().replaceAll("\\R", " ").replace('"', '\'').strip();
  }

  /**
   * Strips characters that are not word characters or hyphens from an object's name.
   *
   * @param namedObject Named object
   * @return Stripped name, or an empty string when the object is {@code null}
   */
  public String stripName(final NamedObject namedObject) {
    if (namedObject == null) {
      return "";
    }
    return namedObject.getName().replaceAll("(?U)[^\\d\\w\\-]", "");
  }

  /**
   * Gets the foreign key that a column participates in, from the perspective of the foreign-key
   * column.
   *
   * @param column Column
   * @return Foreign key, or {@code null} when the column is not part of a foreign key
   */
  public TableReference tableReference(final Column column) {
    if (MetaDataUtility.isPartial(column) || !column.isPartOfForeignKey()) {
      return null;
    }
    final Table table = column.getParent();
    for (final ForeignKey foreignKey : table.getImportedForeignKeys()) {
      for (final ColumnReference columnReference : foreignKey) {
        if (column.equals(columnReference.getForeignKeyColumn())) {
          return foreignKey;
        }
      }
    }
    return null;
  }

  /**
   * Gets the type name of a database object (for example, {@code TABLE} or {@code VIEW}).
   *
   * @param dbObject Database object
   * @return Simple type name
   */
  public String typeName(final DatabaseObject dbObject) {
    return MetaDataUtility.getTypeName(dbObject).toString();
  }

  /**
   * This cardinality symbol syntax is used by Mermaid. When the graph is generated, the foreign key
   * is on the left and the primary key is on the right.
   *
   * @param cardinality Relationship cardinality
   * @return Mermaid cardinality symbol from foreign key to primary key
   */
  private String cardinalitySymbol(final RelationshipCardinality cardinality) {
    return switch (cardinality) {
      case zero_one -> "|o--||";
      case zero_many -> "}o--||";
      case one_one -> "||--||";
      case one_many -> "}|--||";
      case many_many -> "}o--o{"; // bridge table implied
      default -> "}o--||";
    };
  }

  private boolean isPrimaryKeyEquivalentIndex(final Table table, final Index index) {
    if (table == null || index == null || !table.hasPrimaryKey()) {
      return false;
    }
    return Objects.equals(columns(table.getPrimaryKey()), columns(index));
  }
}
