/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.namedobject;

import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

import java.util.Comparator;
import java.util.Objects;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.DatabaseUser;
import schemacrawler.schema.DependantObject;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Schema;
import schemacrawler.schema.TableReference;
import schemacrawler.schema.TypedObject;
import schemacrawler.utility.MetaDataUtility;
import us.fatehi.utility.CollectionsUtility;
import us.fatehi.utility.UtilityMarker;

/** Reusable comparison stages for named objects. */
@UtilityMarker
public final class NamedObjectComparators {

  /** Compares exact text case-sensitively, with nulls first. */
  public static final Comparator<String> EXACT_TEXT_COMPARATOR = nullsFirst(naturalOrder());

  /** Compares the existing lossy normalized text without allocating normalized strings. */
  public static final Comparator<String> NORMALIZED_TEXT_COMPARATOR =
      nullsFirst(NamedObjectComparators::compareNormalizedText);

  public static final Comparator<NamedObject> NORMALIZED_NAME_COMPARATOR =
      nullsFirst(comparing(NamedObject::getFullName, NORMALIZED_TEXT_COMPARATOR));

  public static final Comparator<NamedObject> EXACT_KEY_COMPARATOR =
      nullsFirst(comparing(NamedObject::key, nullsFirst(naturalOrder())));

  public static final Comparator<NamedObject> NAME_THEN_KEY_COMPARATOR =
      NORMALIZED_NAME_COMPARATOR.thenComparing(EXACT_KEY_COMPARATOR);

  public static final Comparator<NamedObject> TYPED_OBJECT_COMPARATOR =
      nullsFirst(NamedObjectComparators::typeComparator);

  /** Compares mapping sequence, then normalized foreign and primary column names; nulls first. */
  public static final Comparator<ColumnReference> COLUMN_REFERENCE_COMPARATOR =
      nullsFirst(
          comparingInt(ColumnReference::getKeySequence)
              .thenComparing(ColumnReference::getForeignKeyColumn, NORMALIZED_NAME_COMPARATOR)
              .thenComparing(ColumnReference::getPrimaryKeyColumn, NORMALIZED_NAME_COMPARATOR));

  /** Compares ordered mappings size-first, without relationship names or identity keys. */
  public static final Comparator<TableReference> TABLE_REFERENCE_MAPPING_COMPARATOR =
      nullsFirst(
          comparing(
              TableReference::getColumnReferences, nullsFirst(CollectionsUtility::compareLists)));

  public static final Comparator<NamedObject> DATABASE_OBJECT_ORDER_COMPARATOR =
      nullsFirst(
          comparingInt(NamedObjectComparators::namedObjectGroup)
              .thenComparing(
                  object ->
                      object instanceof final DatabaseObject databaseObject
                          ? databaseObject.getSchema()
                          : null,
                  Comparator.<Schema>nullsFirst(naturalOrder()))
              .thenComparingInt(NamedObjectComparators::databaseObjectOrder));

  private static final Comparator<NamedObject> DEPENDANT_OBJECT_ORDER_COMPARATOR =
      nullsFirst(NamedObjectComparators::dependantObjectOrder);

  public static final Comparator<NamedObject> DATABASE_OBJECT_SIMPLE_TYPE_COMPARATOR =
      DATABASE_OBJECT_ORDER_COMPARATOR.thenComparing(DEPENDANT_OBJECT_ORDER_COMPARATOR);

  /** Compares structural order, type, and normalized name without a terminal identity key. */
  public static final Comparator<NamedObject> DATABASE_OBJECT_FIELDS_COMPARATOR =
      nullsFirst(
          DATABASE_OBJECT_SIMPLE_TYPE_COMPARATOR
              .thenComparing(TYPED_OBJECT_COMPARATOR)
              .thenComparing(NORMALIZED_NAME_COMPARATOR));

  /** Compares database-object fields followed by the exact identity key. */
  public static final Comparator<NamedObject> DATABASE_OBJECT_COMPARATOR =
      nullsFirst(DATABASE_OBJECT_FIELDS_COMPARATOR.thenComparing(EXACT_KEY_COMPARATOR));

  private static int compareNormalizedText(final String text, final String otherText) {
    final String left = text;
    final String right = otherText;
    int leftOffset = 0;
    int rightOffset = 0;
    while (true) {
      leftOffset = nextRetainedOffset(left, leftOffset);
      rightOffset = nextRetainedOffset(right, rightOffset);
      if (leftOffset == left.length() || rightOffset == right.length()) {
        return Integer.compare(left.length() - leftOffset, right.length() - rightOffset);
      }
      final int leftCodePoint = left.codePointAt(leftOffset);
      final int rightCodePoint = right.codePointAt(rightOffset);
      final int lowerLeft = Character.toLowerCase(leftCodePoint);
      final int lowerRight = Character.toLowerCase(rightCodePoint);
      final char leftUnit =
          Character.isSupplementaryCodePoint(lowerLeft)
              ? Character.highSurrogate(lowerLeft)
              : (char) lowerLeft;
      final char rightUnit =
          Character.isSupplementaryCodePoint(lowerRight)
              ? Character.highSurrogate(lowerRight)
              : (char) lowerRight;
      final int comparison = Character.compare(leftUnit, rightUnit);
      if (comparison != 0) {
        return comparison;
      }
      if (Character.isSupplementaryCodePoint(lowerLeft)
          && Character.isSupplementaryCodePoint(lowerRight)) {
        final int trailingComparison =
            Character.compare(
                Character.lowSurrogate(lowerLeft), Character.lowSurrogate(lowerRight));
        if (trailingComparison != 0) {
          return trailingComparison;
        }
      }
      leftOffset += Character.charCount(leftCodePoint);
      rightOffset += Character.charCount(rightCodePoint);
    }
  }

  private static int databaseObjectOrder(final NamedObject object) {
    if (object instanceof Catalog) {
      return 0;
    }
    if (object instanceof Schema) {
      return 1;
    }
    if (object instanceof DependantObject<?>) {
      return 6;
    }
    if (object instanceof final DatabaseObject databaseObject) {
      return switch (MetaDataUtility.getSimpleTypeName(databaseObject)) {
        case table -> 2;
        case view -> 3;
        case procedure -> 4;
        case function -> 5;
        case sequence -> 7;
        case synonym -> 8;
        case unknown -> 9;
      };
    }
    if (object instanceof DatabaseUser) {
      return 10;
    }
    return 11;
  }

  private static int dependantObjectOrder(final NamedObject object, final NamedObject otherObject) {
    if (object == otherObject) {
      return 0;
    }
    if (object instanceof final DependantObject<?> dependantObject
        && otherObject instanceof final DependantObject<?> otherDependantObject) {
      final int parentComparison =
          NamedObjectSort.natural.compare(
              dependantObject.getParent(), otherDependantObject.getParent());
      if (parentComparison != 0) {
        return parentComparison;
      }
      return MetaDataUtility.getSimpleDependantObjectType(dependantObject)
          .compareTo(MetaDataUtility.getSimpleDependantObjectType(otherDependantObject));
    }
    return 0;
  }

  private static int namedObjectGroup(final NamedObject object) {
    if (object instanceof Catalog) {
      return 0;
    }
    if (object instanceof Schema) {
      return 1;
    }
    if (object instanceof DatabaseObject) {
      return 2;
    }
    if (object instanceof DatabaseUser) {
      return 3;
    }
    return 4;
  }

  private static int nextRetainedOffset(final String text, final int offset) {
    int nextOffset = offset;
    while (nextOffset < text.length()) {
      final int codePoint = text.codePointAt(nextOffset);
      if (Character.isLetterOrDigit(codePoint) || codePoint == '_' || codePoint == '.') {
        break;
      }
      nextOffset += Character.charCount(codePoint);
    }
    return nextOffset;
  }

  private static int typeComparator(final NamedObject object, final NamedObject otherObject) {
    if (object == otherObject) {
      return 0;
    }
    if (object instanceof final TypedObject typedObject
        && otherObject instanceof final TypedObject otherTypedObject) {
      try {
        final int typeCompareTo =
            Objects.compare(
                typedObject.getType(), otherTypedObject.getType(), nullsFirst(naturalOrder()));
        if (typeCompareTo != 0) {
          return typeCompareTo;
        }
      } catch (final Exception e) {
        // Ignore, since getType() may not be implemented by partial database objects
      }
    }
    return 0;
  }

  private NamedObjectComparators() {
    throw new UnsupportedOperationException();
  }
}
