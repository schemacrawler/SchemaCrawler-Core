/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.implicitassociations;

import static schemacrawler.test.utility.ProposedImplicitAssociationsTestUtility.implicitAssociations;

import org.junit.jupiter.api.Test;
import schemacrawler.test.utility.DisableLogging;
import schemacrawler.test.utility.WithTestDatabase;
import us.fatehi.test.utility.extensions.ResolveTestContext;
import us.fatehi.test.utility.extensions.TestContext;
import us.fatehi.utility.datasource.DatabaseConnectionSource;

@DisableLogging
@ResolveTestContext
public class SimpleImplicitAssociationsTest {

  @Test
  @WithTestDatabase(script = "/simple_implicit_association_with_ids.sql")
  public void simpleImplicitAssociationWithIds(
      final TestContext testContext, final DatabaseConnectionSource connectionSource)
      throws Exception {
    implicitAssociations(testContext, connectionSource, false);
    implicitAssociations(testContext, connectionSource, true);
  }

  @Test
  @WithTestDatabase(script = "/simple_implicit_association_with_plurals.sql")
  public void simpleImplicitAssociationWithPlurals(
      final TestContext testContext, final DatabaseConnectionSource connectionSource)
      throws Exception {
    implicitAssociations(testContext, connectionSource, false);
    implicitAssociations(testContext, connectionSource, true);
  }
}
