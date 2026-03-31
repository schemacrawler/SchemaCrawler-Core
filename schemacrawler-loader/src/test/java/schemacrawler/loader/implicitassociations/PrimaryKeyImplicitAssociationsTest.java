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
public class PrimaryKeyImplicitAssociationsTest {

  @Test
  @WithTestDatabase(script = "/pk_test_1.sql")
  public void implicitAssociations1(
      final TestContext testContext, final DatabaseConnectionSource connectionSource)
      throws Exception {
    implicitAssociations(testContext, connectionSource, false);
  }

  @Test
  @WithTestDatabase(script = "/pk_test_1.sql")
  public void implicitAssociations1a(
      final TestContext testContext, final DatabaseConnectionSource connectionSource)
      throws Exception {
    implicitAssociations(testContext, connectionSource, true);
  }

  @Test
  @WithTestDatabase(script = "/pk_test_2.sql")
  public void implicitAssociations2(
      final TestContext testContext, final DatabaseConnectionSource connectionSource)
      throws Exception {
    implicitAssociations(testContext, connectionSource, false);
  }

  @Test
  @WithTestDatabase(script = "/pk_test_2.sql")
  public void implicitAssociations2a(
      final TestContext testContext, final DatabaseConnectionSource connectionSource)
      throws Exception {
    implicitAssociations(testContext, connectionSource, true);
  }

  @Test
  @WithTestDatabase(script = "/pk_test_3.sql")
  public void implicitAssociations3(
      final TestContext testContext, final DatabaseConnectionSource connectionSource)
      throws Exception {
    implicitAssociations(testContext, connectionSource, false);
  }

  @Test
  @WithTestDatabase(script = "/pk_test_3.sql")
  public void implicitAssociations3a(
      final TestContext testContext, final DatabaseConnectionSource connectionSource)
      throws Exception {
    implicitAssociations(testContext, connectionSource, true);
  }
}
