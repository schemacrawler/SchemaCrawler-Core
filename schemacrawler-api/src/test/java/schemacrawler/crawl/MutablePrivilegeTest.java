/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.crawl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

import org.junit.jupiter.api.Test;
import schemacrawler.schema.Grant;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaReference;

public class MutablePrivilegeTest {

  private final Table table = new MutableTable(new SchemaReference("catalog", "schema"), "table");

  @Test
  public void grantComparisonHandlesNullComponents() {
    final MutablePrivilege<Table> privilege = privilege("privilege");
    final Grant<Table> nullGrantor = grant(privilege, null, "grantee", false);
    final Grant<Table> nonNullGrantor = grant(privilege, "grantor", "grantee", false);
    final Grant<Table> nullGrantee = grant(privilege, "grantor", null, false);
    final Grant<Table> nonNullGrantee = grant(privilege, "grantor", "grantee", false);

    assertThat(nullGrantor.compareTo(nullGrantor), is(0));
    assertThat(nullGrantor.compareTo(grant(privilege, null, "grantee", false)), is(0));
    assertThat(nullGrantor.compareTo(nonNullGrantor), greaterThan(0));
    assertThat(nonNullGrantor.compareTo(nullGrantor), lessThan(0));
    assertThat(nullGrantee.compareTo(nonNullGrantee), greaterThan(0));
    assertThat(nonNullGrantee.compareTo(nullGrantee), lessThan(0));
    assertThat(nullGrantor.compareTo(null), lessThan(0));
  }

  @Test
  public void grantComparisonUsesParentKeyThenGrantability() {
    final MutablePrivilege<Table> firstPrivilege = privilege("privilege-a");
    final MutablePrivilege<Table> secondPrivilege = privilege("privilege-b");
    final Grant<Table> firstParentGrant = grant(firstPrivilege, "grantor", "grantee", false);
    final Grant<Table> secondParentGrant = grant(secondPrivilege, "grantor", "grantee", false);
    final Grant<Table> nonGrantable = grant(firstPrivilege, "grantor", "grantee", false);
    final Grant<Table> grantable = grant(firstPrivilege, "grantor", "grantee", true);

    assertThat(firstParentGrant.compareTo(secondParentGrant), lessThan(0));
    assertThat(secondParentGrant.compareTo(firstParentGrant), greaterThan(0));
    assertThat(nonGrantable.compareTo(grantable), lessThan(0));
    assertThat(grantable.compareTo(nonGrantable), greaterThan(0));
  }

  private Grant<Table> grant(
      final MutablePrivilege<Table> privilege,
      final String grantor,
      final String grantee,
      final boolean grantable) {
    return privilege.new PrivilegeGrant(grantor, grantee, grantable);
  }

  private MutablePrivilege<Table> privilege(final String name) {
    return new MutablePrivilege<>(new TablePointer(table), name);
  }
}
