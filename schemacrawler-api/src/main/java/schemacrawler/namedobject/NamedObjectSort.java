/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.namedobject;

import static java.util.Comparator.naturalOrder;

import java.util.Comparator;
import schemacrawler.schema.NamedObject;

/**
 * Comparators for named objects, including case-insensitive alphabetical and natural order. Null
 * objects and nullable alphabetical names or tie-breaking keys sort first.
 */
public enum NamedObjectSort implements Comparator<NamedObject> {

  /** Alphabetical sort, case-insensitive. */
  alphabetical(NamedObjectComparators.NAME_THEN_KEY_COMPARATOR),

  /** Natural sort. */
  natural(naturalOrder());

  /**
   * Gets the named-object comparator for the requested alphabetical or natural ordering.
   *
   * @param alphabeticalSort whether to sort alphabetically
   * @return the corresponding comparator
   */
  public static NamedObjectSort getNamedObjectSort(final boolean alphabeticalSort) {
    if (alphabeticalSort) {
      return NamedObjectSort.alphabetical;
    }
    return NamedObjectSort.natural;
  }

  private final Comparator<NamedObject> comparator;

  NamedObjectSort(final Comparator<NamedObject> comparator) {
    this.comparator = comparator;
  }

  /** {@inheritDoc} */
  @Override
  public int compare(final NamedObject namedObject1, final NamedObject namedObject2) {
    final int nullComparison =
        NamedObjectComparators.NULL_COMPARATOR.compare(namedObject1, namedObject2);
    if (nullComparison != 0 || namedObject1 == namedObject2) {
      return nullComparison;
    }
    return comparator.compare(namedObject1, namedObject2);
  }
}
