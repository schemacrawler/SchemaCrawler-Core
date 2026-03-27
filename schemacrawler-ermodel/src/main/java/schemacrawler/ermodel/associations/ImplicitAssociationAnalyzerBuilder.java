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

public class ImplicitAssociationAnalyzerBuilder implements Builder<ImplicitAssociationAnalyzer> {

  public static ImplicitAssociationAnalyzerBuilder builder(final Collection<Table> allTables) {
    requireNonNull(allTables, "No tables provided");
    final List<Table> tables = new ArrayList<>(allTables);
    Collections.sort(tables);
    return new ImplicitAssociationAnalyzerBuilder(List.copyOf(tables));
  }

  private final TableMatchKeys tableMatchKeys;
  private Predicate<ColumnReference> implicitAssociationsRule;

  private ImplicitAssociationAnalyzerBuilder(final List<Table> allTables) {
    tableMatchKeys = new TableMatchKeys(allTables);
    implicitAssociationsRule = colRef -> false;
  }

  @Override
  public ImplicitAssociationAnalyzer build() {
    return new ImplicitAssociationAnalyzer(tableMatchKeys, implicitAssociationsRule);
  }

  public ImplicitAssociationAnalyzerBuilder withExtensionTableMatcher() {
    implicitAssociationsRule =
        implicitAssociationsRule.or(new ExtensionTableMatcher(tableMatchKeys));
    return this;
  }

  public ImplicitAssociationAnalyzerBuilder withIdMatcher() {
    implicitAssociationsRule = implicitAssociationsRule.or(new IdMatcher());
    return this;
  }
}
