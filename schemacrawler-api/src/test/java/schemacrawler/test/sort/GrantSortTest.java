/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.sort;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static schemacrawler.crawl.SortTestUtility.newGrant;
import static schemacrawler.crawl.SortTestUtility.newTable;

import org.junit.jupiter.api.Test;
import schemacrawler.schema.Grant;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaReference;

public class GrantSortTest {

  private final Table table = newTable(new SchemaReference("catalog", "schema"), "table");

  @Test
  public void grantComparisonHandlesNullComponents() {
    final String privilege = "privilege";
    final Grant<Table> nullGrantor = newGrant(table, privilege, null, "grantee", false);
    final Grant<Table> nonNullGrantor = newGrant(table, privilege, "grantor", "grantee", false);
    final Grant<Table> nullGrantee = newGrant(table, privilege, "grantor", null, false);
    final Grant<Table> nonNullGrantee = newGrant(table, privilege, "grantor", "grantee", false);

    assertThat(nullGrantor.compareTo(nullGrantor), is(0));
    assertThat(nullGrantor.compareTo(newGrant(table, privilege, null, "grantee", false)), is(0));
    assertThat(nullGrantor.compareTo(nonNullGrantor), lessThan(0));
    assertThat(nonNullGrantor.compareTo(nullGrantor), greaterThan(0));
    assertThat(nullGrantee.compareTo(nonNullGrantee), lessThan(0));
    assertThat(nonNullGrantee.compareTo(nullGrantee), greaterThan(0));
    assertThat(nullGrantor.compareTo(null), greaterThan(0));
  }

  @Test
  public void grantComparisonNormalizesGrantorAndGrantee() {
    final Grant<Table> punctuated = newGrant(table, "privilege", "A-B", "C D", false);
    final Grant<Table> plain = newGrant(table, "privilege", "ab", "cd", false);

    assertThat(punctuated.compareTo(plain), is(0));
    assertThat(plain.compareTo(punctuated), is(0));

    final Grant<Table> earlierGrantee = newGrant(table, "privilege", "a b", "A_USER", false);
    final Grant<Table> laterGrantee = newGrant(table, "privilege", "AB", "Z_USER", false);

    assertThat(earlierGrantee.compareTo(laterGrantee), lessThan(0));
    assertThat(laterGrantee.compareTo(earlierGrantee), greaterThan(0));
  }

  @Test
  public void grantComparisonUsesParentKeyThenGrantability() {
    final String firstPrivilege = "privilege-a";
    final String secondPrivilege = "privilege-b";
    final Grant<Table> firstParentGrant =
        newGrant(table, firstPrivilege, "grantor", "grantee", false);
    final Grant<Table> secondParentGrant =
        newGrant(table, secondPrivilege, "grantor", "grantee", false);
    final Grant<Table> nonGrantable = newGrant(table, firstPrivilege, "grantor", "grantee", false);
    final Grant<Table> grantable = newGrant(table, firstPrivilege, "grantor", "grantee", true);

    assertThat(firstParentGrant.compareTo(secondParentGrant), lessThan(0));
    assertThat(secondParentGrant.compareTo(firstParentGrant), greaterThan(0));
    assertThat(nonGrantable.compareTo(grantable), lessThan(0));
    assertThat(grantable.compareTo(nonGrantable), greaterThan(0));
  }
}
