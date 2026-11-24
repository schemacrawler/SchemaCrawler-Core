/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import us.fatehi.utility.Inflection;

public class InflectionTest {

  @ParameterizedTest
  @CsvSource({
    "orders, order",
    "customers, customer",
    "categories, category",
    "addresses, address",
    "statuses, status",
    "classes, class",
    "processes, process",
    "buses, bus",
    "analyses, analysis",
    "indices, index",
    "ids, id"
  })
  @DisplayName("Regular and common plurals are singularized")
  public void singularizePlurals(final String plural, final String singular) {
    assertThat(Inflection.singularize(plural), is(singular));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "status",
        "address",
        "class",
        "process",
        "bus",
        "access",
        "business",
        "glass",
        "bonus",
        "campus",
        "virus",
        "census",
        "canvas",
        "atlas",
        "chassis",
        "analysis",
        "basis",
        "lens",
        "gas",
        "alias",
        "bias",
        "plus",
        "surplus",
        "nexus",
        "genus",
        "locus",
        "nucleus",
        "apparatus",
        "thesaurus",
        "axis",
        "iris",
        "metropolis",
        "pancreas"
      })
  @DisplayName("Singular words that end in s are left alone")
  public void singularizeSingularWordsEndingInS(final String word) {
    assertThat(Inflection.singularize(word), is(word));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "previous",
        "various",
        "anonymous",
        "continuous",
        "numerous",
        "obvious",
        "ambiguous",
        "nutritious"
      })
  @DisplayName("Adjectives that end in ous are left alone")
  public void singularizeAdjectivesEndingInOus(final String word) {
    assertThat(Inflection.singularize(word), is(word));
  }

  @ParameterizedTest
  @CsvSource({
    "aliases, alias",
    "biases, bias",
    "surpluses, surplus",
    "nexuses, nexus",
    "axes, axis",
    "buses, bus",
    "bonuses, bonus",
    "campuses, campus",
    "viruses, virus",
    "gases, gas",
    "lenses, lens",
    "pluses, plus",
    "BONUSES, BONUS",
    "uses, use",
    "houses, house",
    "causes, cause",
    "bases, base",
    "databases, database"
  })
  @DisplayName("Plurals of words that end in s are singularized")
  public void singularizePluralsOfWordsEndingInS(final String plural, final String singular) {
    assertThat(Inflection.singularize(plural), is(singular));
  }

  @ParameterizedTest
  @ValueSource(strings = {"series", "news", "information", "equipment", "species"})
  @DisplayName("Uncountable words are left alone")
  public void singularizeUncountable(final String word) {
    assertThat(Inflection.singularize(word), is(word));
  }

  @Test
  @DisplayName("Singularizing is case-insensitive")
  public void singularizeCaseInsensitive() {
    assertThat(Inflection.singularize("ORDERS"), is("ORDER"));
    assertThat(Inflection.singularize("Status"), is("Status"));
    assertThat(Inflection.singularize("ADDRESS"), is("ADDRESS"));
  }

  @Test
  @DisplayName("Irregular plurals are singularized")
  public void singularizeIrregular() {
    assertThat(Inflection.singularize("people"), is("person"));
    assertThat(Inflection.singularize("children"), is("child"));
    assertThat(Inflection.singularize("men"), is("man"));
  }

  @Test
  @DisplayName("Words that are not words, such as identifiers, are handled without error")
  public void singularizeIdentifiers() {
    assertThat(Inflection.singularize("id"), is("id"));
    assertThat(Inflection.singularize("address1"), is("address1"));
    assertThat(Inflection.singularize(""), is(""));
  }

  @Test
  @DisplayName("Singularizing is stable when applied to its own output")
  public void singularizeIdempotent() {
    for (final String word :
        new String[] {
          "orders", "status", "address", "class", "process", "bus", "categories", "statuses"
        }) {
      final String once = Inflection.singularize(word);
      assertThat(word, Inflection.singularize(once), is(once));
    }
  }
}
