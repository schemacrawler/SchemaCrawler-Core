/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.associations;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.Table;
import us.fatehi.utility.Builder;

public class ImplicitAssociationsAnalyzerBuilder implements Builder<ImplicitAssociationsAnalyzer> {

  public static ImplicitAssociationsAnalyzerBuilder builder(final Collection<Table> allTables) {
    requireNonNull(allTables, "No tables provided");
    final List<Table> tables = new ArrayList<>(allTables);
    Collections.sort(tables);
    return new ImplicitAssociationsAnalyzerBuilder(List.copyOf(tables));
  }

  private final TableMatchKeys tableMatchKeys;
  private Predicate<ColumnReference> weakAssociationsRule;

  private ImplicitAssociationsAnalyzerBuilder(final List<Table> allTables) {
    tableMatchKeys = new TableMatchKeys(allTables);
    weakAssociationsRule = colRef -> false;
  }

  @Override
  public ImplicitAssociationsAnalyzer build() {
    return new ImplicitAssociationsAnalyzer(tableMatchKeys, weakAssociationsRule);
  }

  public ImplicitAssociationsAnalyzerBuilder withExtensionTableMatcher() {
    weakAssociationsRule = weakAssociationsRule.or(new ExtensionTableMatcher(tableMatchKeys));
    return this;
  }

  public ImplicitAssociationsAnalyzerBuilder withIdMatcher() {
    weakAssociationsRule = weakAssociationsRule.or(new IdMatcher());
    return this;
  }
}
