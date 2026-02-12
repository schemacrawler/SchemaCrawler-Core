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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import schemacrawler.schema.Column;
import schemacrawler.test.utility.crawl.LightTable;

public class ProposedWeakAssociationTest {

  @Test
  public void proposedWeakAssociation() {

    final LightTable table1 = new LightTable("Table1");
    final Column col1 = table1.addColumn("Id");
    final Column col2 = table1.addColumn("ColA");

    assertThrows(NullPointerException.class, () -> new WeakColumnReference(null, col2));
    assertThrows(NullPointerException.class, () -> new WeakColumnReference(col1, null));
    assertThrows(NullPointerException.class, () -> new WeakColumnReference(null, null));

    final WeakColumnReference proposedWeakAssociation = new WeakColumnReference(col1, col2);
    assertThat(proposedWeakAssociation.compareTo(null), is(-1));
    assertThat(proposedWeakAssociation.getKeySequence(), is(1));

    assertThat(proposedWeakAssociation.getPrimaryKeyColumn(), is(col2));
    assertThat(proposedWeakAssociation.getForeignKeyColumn(), is(col1));
    assertThat(proposedWeakAssociation.toString(), is("Table1.Id ~~> Table1.ColA"));
    assertThat(proposedWeakAssociation.isValid(), is(true));

    assertThat(new WeakColumnReference(col1, col1).isValid(), is(false));
  }
}
