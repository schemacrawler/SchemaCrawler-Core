/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.associations;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static schemacrawler.ermodel.associations.ImplicitAssociationsUtility.normalizeMatchKeyName;

import org.junit.jupiter.api.Test;

public class ImplicitAssociationsUtilityTest {

  @Test
  public void normalizeMatchKeyName_blank() {
    assertThat(normalizeMatchKeyName(null), is(""));
    assertThat(normalizeMatchKeyName(""), is(""));
    assertThat(normalizeMatchKeyName("   "), is(""));
  }

  @Test
  public void normalizeMatchKeyName_styles() {
    assertThat(normalizeMatchKeyName("CustomerOrderItem"), is("customer_order_item"));
    assertThat(normalizeMatchKeyName("customerOrderItem"), is("customer_order_item"));
    assertThat(normalizeMatchKeyName("CustomerID"), is("customer_id"));
    assertThat(normalizeMatchKeyName("URLId"), is("url_id"));
    assertThat(normalizeMatchKeyName("Order Details"), is("order_details"));
    assertThat(normalizeMatchKeyName("kebab-name"), is("kebab_name"));
    assertThat(normalizeMatchKeyName("CUSTOMER_ORDER"), is("customer_order"));
    assertThat(normalizeMatchKeyName("customer_order"), is("customer_order"));
  }

  @Test
  public void normalizeMatchKeyName_unicode() {
    assertThat(normalizeMatchKeyName("ŻółtyKot"), is("żółty_kot"));
    assertThat(normalizeMatchKeyName("kotŻółty"), is("kot_żółty"));
    assertThat(normalizeMatchKeyName("CustomerÉtatId"), is("customer_état_id"));
  }

  @Test
  public void normalizeMatchKeyName_unicodeSpaces() {
    assertThat(normalizeMatchKeyName("Order　Details"), is("order_details"));
    assertThat(normalizeMatchKeyName("Order Details"), is("order_details"));
    assertThat(normalizeMatchKeyName("Order　Details"), is(normalizeMatchKeyName("Order Details")));
  }
}
