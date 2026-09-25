/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import schemacrawler.inclusionrule.InclusionRule;
import schemacrawler.schema.BaseColumn;
import schemacrawler.schema.NamedObject;
import schemacrawler.schemacrawler.GrepOptions;
import us.fatehi.utility.string.StringFormat;

abstract class AbstractGrepFilter<N extends NamedObject> implements NamedObjectFilter<N> {

  private final Logger logger = Logger.getLogger(getClass().getName());

  protected final GrepOptions options;

  AbstractGrepFilter(final GrepOptions options) {
    this.options = requireNonNull(options, "No grep options provided");
  }

  @Override
  public final boolean test(final N namedObject) {
    final boolean checkIncludeForNamedObjects = isGrepForNamedObjects();
    final boolean checkIncludeForMembers = isGrepForMembers();
    final boolean checkIncludeForDefinitions = options.isGrepDefinitions();

    if (!checkIncludeForNamedObjects && !checkIncludeForMembers && !checkIncludeForDefinitions) {
      if (options.isGrepInvertMatch()) {
        logger.log(
            Level.FINE,
            new StringFormat(
                "Ignoring the invert match setting for %s <%s>, "
                    + "since no inclusion rules are set",
                getClass().getSimpleName(), namedObject));
      }
      return true;
    }

    final boolean includeForNamedObjects =
        checkIncludeForNamedObjects && checkIncludeForNamedObjects(namedObject);
    final boolean includeForMembers = checkIncludeForMembers && checkIncludeForMembers(namedObject);
    final boolean includeForDefinitions =
        checkIncludeForDefinitions && checkIncludeForDefinitions(definitionTexts(namedObject));

    boolean include = includeForNamedObjects || includeForMembers || includeForDefinitions;
    if (options.isGrepInvertMatch()) {
      include = !include;
    }

    if (!include) {
      logger.log(
          Level.FINE,
          new StringFormat("Excluding %s <%s>", getClass().getSimpleName(), namedObject));
    }
    return include;
  }

  protected final boolean checkIncludeForDefinitions(final Stream<String> definitionTexts) {
    final InclusionRule rule = options.grepDefinitionInclusionRule();
    return definitionTexts.anyMatch(rule::test);
  }

  protected final boolean checkIncludeForMembers(
      final Collection<? extends BaseColumn<?>> members, final InclusionRule rule) {
    return members.stream().anyMatch(member -> rule.test(member.getFullName()));
  }

  protected abstract boolean checkIncludeForMembers(N namedObject);

  protected final boolean checkIncludeForNamedObject(
      final N namedObject, final InclusionRule rule) {
    return FullNameInclusionRuleFilters.<N>fullName(rule).test(namedObject);
  }

  protected abstract boolean checkIncludeForNamedObjects(N namedObject);

  protected abstract Stream<String> definitionTexts(N namedObject);

  protected abstract boolean isGrepForMembers();

  protected abstract boolean isGrepForNamedObjects();
}
