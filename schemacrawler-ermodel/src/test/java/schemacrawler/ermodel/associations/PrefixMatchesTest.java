/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.associations;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class PrefixMatchesTest {

  @Test
  public void prefixMatches_boundaries() {
    PrefixMatches matchkeys;
    List<String> withoutPrefix;

    // 1.
    assertThrows(NullPointerException.class, () -> new PrefixMatches(null, "_"));

    // 2.
    matchkeys = new PrefixMatches(List.of(), "_");
    assertThat(matchkeys.toString(), is("{}"));

    withoutPrefix = matchkeys.get("key0");
    assertThat(withoutPrefix, containsInAnyOrder("key0"));

    // 3.
    matchkeys = new PrefixMatches(keys("key1"), "_");
    assertThat(matchkeys.toString(), is("{key1=[key1]}"));

    withoutPrefix = matchkeys.get("key0");
    assertThat(withoutPrefix, containsInAnyOrder("key0"));

    withoutPrefix = matchkeys.get("key1");
    assertThat(withoutPrefix, containsInAnyOrder("key1"));
  }

  @Test
  public void prefixMatches_mixed_prefixes() {
    List<String> withoutPrefix;

    final PrefixMatches matchkeys =
        new PrefixMatches(keys("pfx1_pfx2_key1", "pfx1_pfx2_key2", "pfx1_key3"), "_");

    withoutPrefix = matchkeys.get("key0");
    assertThat(withoutPrefix, containsInAnyOrder("key0"));

    withoutPrefix = matchkeys.get("pfx1_pfx2_key1");
    assertThat(withoutPrefix, containsInAnyOrder("key1", "pfx2_key1", "pfx1_pfx2_key1"));

    withoutPrefix = matchkeys.get("pfx1_pfx2_key2");
    assertThat(withoutPrefix, containsInAnyOrder("key2", "pfx2_key2", "pfx1_pfx2_key2"));

    withoutPrefix = matchkeys.get("pfx1_key3");
    assertThat(withoutPrefix, containsInAnyOrder("key3", "pfx1_key3"));
  }

  @Test
  public void prefixMatches_no_prefix() {
    List<String> withoutPrefix;

    final PrefixMatches matchkeys = new PrefixMatches(keys("key1", "key2"), "_");

    withoutPrefix = matchkeys.get("key0");
    assertThat(withoutPrefix, containsInAnyOrder("key0"));

    withoutPrefix = matchkeys.get("key1");
    assertThat(withoutPrefix, containsInAnyOrder("key1"));

    withoutPrefix = matchkeys.get("key2");
    assertThat(withoutPrefix, containsInAnyOrder("key2"));
  }

  /**
   * Characterization of current behavior with more than 12 distinct prefixes. Prefixes are sorted
   * ascending by key count, so the first 12 kept are the least widely shared, and the widely shared
   * prefix {@code z_} is dropped. This documents existing behavior and is not a statement that it
   * is correct.
   */
  @Test
  public void prefixMatches_more_than_12_prefixes() {
    final List<String> names = new ArrayList<>();
    for (int i = 1; i <= 12; i++) {
      final String number = String.format("%02d", i);
      names.add("p" + number + "_k" + number);
    }
    names.addAll(List.of("z_a", "z_b", "z_c"));

    final PrefixMatches matchkeys = new PrefixMatches(names, "_");

    // Least widely shared prefixes (first alphabetically among equal counts) are kept
    assertThat(matchkeys.get("p01_k01"), containsInAnyOrder("k01", "p01_k01"));
    assertThat(matchkeys.get("p06_k06"), containsInAnyOrder("k06", "p06_k06"));

    // Later prefixes are dropped, only the full key remains
    assertThat(matchkeys.get("p07_k07"), containsInAnyOrder("p07_k07"));
    assertThat(matchkeys.get("p12_k12"), containsInAnyOrder("p12_k12"));

    // The most widely shared prefix (3 keys) is dropped
    assertThat(matchkeys.get("z_a"), containsInAnyOrder("z_a"));
    assertThat(matchkeys.get("z_b"), containsInAnyOrder("z_b"));
  }

  @Test
  public void prefixMatches_pascal_case_prefixes() {
    final PrefixMatches matchkeys =
        new PrefixMatches(keys("AppCustomer", "AppOrder", "AppProduct"), "_");

    assertThat(matchkeys.get("AppCustomer"), containsInAnyOrder("customer", "app_customer"));
    assertThat(matchkeys.get("AppOrder"), containsInAnyOrder("order", "app_order"));
    assertThat(matchkeys.get("AppProduct"), containsInAnyOrder("product", "app_product"));
  }

  @Test
  public void prefixMatches_camel_case_prefixes() {
    final PrefixMatches matchkeys =
        new PrefixMatches(keys("appCustomer", "appOrder", "appProduct"), "_");

    assertThat(matchkeys.get("appCustomer"), containsInAnyOrder("customer", "app_customer"));
    assertThat(matchkeys.get("appOrder"), containsInAnyOrder("order", "app_order"));
  }

  @Test
  public void prefixMatches_mixed_snake_and_camel() {
    final PrefixMatches matchkeys =
        new PrefixMatches(keys("app_customer", "AppOrder", "appProduct"), "_");

    assertThat(matchkeys.get("app_customer"), containsInAnyOrder("customer", "app_customer"));
    assertThat(matchkeys.get("AppOrder"), containsInAnyOrder("order", "app_order"));
    assertThat(matchkeys.get("appProduct"), containsInAnyOrder("product", "app_product"));
  }

  @Test
  public void prefixMatches_spaced_names() {
    final PrefixMatches matchkeys =
        new PrefixMatches(keys("App Customer", "App Order", "App Product"), "_");

    assertThat(matchkeys.get("App Customer"), containsInAnyOrder("customer", "app_customer"));
    assertThat(matchkeys.get("App Order"), containsInAnyOrder("order", "app_order"));
  }

  @Test
  public void prefixMatches_unknown_mixed_case_name() {
    final PrefixMatches matchkeys = new PrefixMatches(keys("AppCustomer", "AppOrder"), "_");

    assertThat(matchkeys.get("SomeOtherName"), containsInAnyOrder("some_other_name"));
  }

  /** Snake_case, lowercase and ALL-CAPS names give the same keys as before this change. */
  @Test
  public void prefixMatches_unchanged_for_snake_lower_and_upper_case() {
    final PrefixMatches matchkeys =
        new PrefixMatches(
            keys("PFX1_PFX2_KEY1", "PFX1_PFX2_KEY2", "PFX1_KEY3", "lowercase", "UPPERCASE"), "_");

    assertThat(
        matchkeys.get("PFX1_PFX2_KEY1"), containsInAnyOrder("key1", "pfx2_key1", "pfx1_pfx2_key1"));
    assertThat(
        matchkeys.get("PFX1_PFX2_KEY2"), containsInAnyOrder("key2", "pfx2_key2", "pfx1_pfx2_key2"));
    assertThat(matchkeys.get("PFX1_KEY3"), containsInAnyOrder("key3", "pfx1_key3"));
    assertThat(matchkeys.get("lowercase"), containsInAnyOrder("lowercase"));
    assertThat(matchkeys.get("UPPERCASE"), containsInAnyOrder("uppercase"));
  }

  @Test
  public void prefixMatches_plural_keeps_key_as_written() {
    final PrefixMatches matchkeys = new PrefixMatches(keys("BOOK", "BOOKS", "Order Details"), "_");

    assertThat(matchkeys.get("BOOK"), containsInAnyOrder("book"));
    assertThat(matchkeys.get("BOOKS"), containsInAnyOrder("book", "books"));
    assertThat(
        matchkeys.get("Order Details"),
        containsInAnyOrder("order_detail", "order_details", "detail", "details"));
  }

  @Test
  public void prefixMatches_plurals() {
    List<String> withoutPrefix;

    final PrefixMatches matchkeys = new PrefixMatches(keys("cats", "buffaloes", "giraffes"), "_");

    withoutPrefix = matchkeys.get("key0");
    assertThat(withoutPrefix, containsInAnyOrder("key0"));

    withoutPrefix = matchkeys.get("cats");
    assertThat(withoutPrefix, containsInAnyOrder("cat", "cats"));

    withoutPrefix = matchkeys.get("buffaloes");
    assertThat(withoutPrefix, containsInAnyOrder("buffalo", "buffaloes"));

    withoutPrefix = matchkeys.get("giraffes");
    assertThat(withoutPrefix, containsInAnyOrder("giraffe", "giraffes"));
  }

  @Test
  public void prefixMatches_same_prefixes() {
    List<String> withoutPrefix;

    final PrefixMatches matchkeys =
        new PrefixMatches(keys("pfx1_pfx2_key1", "pfx1_pfx2_key2", "pfx1_pfx2_key3"), "_");

    withoutPrefix = matchkeys.get("key0");
    assertThat(withoutPrefix, containsInAnyOrder("key0"));

    withoutPrefix = matchkeys.get("pfx1_pfx2_key1");
    assertThat(withoutPrefix, containsInAnyOrder("key1", "pfx2_key1", "pfx1_pfx2_key1"));

    withoutPrefix = matchkeys.get("pfx1_pfx2_key2");
    assertThat(withoutPrefix, containsInAnyOrder("key2", "pfx2_key2", "pfx1_pfx2_key2"));

    withoutPrefix = matchkeys.get("pfx1_pfx2_key3");
    assertThat(withoutPrefix, containsInAnyOrder("key3", "pfx2_key3", "pfx1_pfx2_key3"));
  }

  private List<String> keys(final String... keyNames) {
    requireNonNull(keyNames);
    return Arrays.asList(keyNames);
  }
}
