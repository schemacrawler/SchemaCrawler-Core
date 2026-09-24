/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import java.util.stream.Stream;
import schemacrawler.inclusionrule.InclusionRule;
import schemacrawler.schema.Column;
import schemacrawler.schema.Table;
import schemacrawler.schema.Trigger;
import schemacrawler.schemacrawler.GrepOptions;

class TableGrepFilter extends AbstractGrepFilter<Table> {

  TableGrepFilter(final GrepOptions options) {
    super(options);
  }

  @Override
  protected Stream<String> definitionTexts(final Table table) {
    final Stream.Builder<String> definitionTexts = Stream.builder();
    definitionTexts.add(table.getRemarks());
    definitionTexts.add(table.getDefinition());
    table.getColumns().stream().map(Column::getRemarks).forEach(definitionTexts::add);
    table.getTriggers().stream().map(Trigger::getActionStatement).forEach(definitionTexts::add);
    return definitionTexts.build();
  }

  @Override
  protected boolean checkIncludeForMembers(final Table table) {
    return checkIncludeForColumns(table);
  }

  @Override
  protected boolean checkIncludeForNamedObjects(final Table table) {
    return checkIncludeForTables(table);
  }

  @Override
  protected boolean isGrepForMembers() {
    return options.isGrepColumns();
  }

  @Override
  protected boolean isGrepForNamedObjects() {
    return options.isGrepTables();
  }

  private boolean checkIncludeForColumns(final Table table) {
    final InclusionRule rule = options.grepColumnInclusionRule();
    return checkIncludeForMembers(table.getColumns(), rule);
  }

  private boolean checkIncludeForTables(final Table table) {
    return checkIncludeForNamedObject(table, options.grepTableInclusionRule());
  }
}
