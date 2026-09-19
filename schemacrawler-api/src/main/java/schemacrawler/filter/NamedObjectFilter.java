/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import java.util.function.Predicate;
import schemacrawler.schema.NamedObject;

/** A filter for named objects. */
@FunctionalInterface
public interface NamedObjectFilter<N extends NamedObject> extends Predicate<N> {

  /** {@inheritDoc} */
  @Override
  boolean test(N namedObject);
}
