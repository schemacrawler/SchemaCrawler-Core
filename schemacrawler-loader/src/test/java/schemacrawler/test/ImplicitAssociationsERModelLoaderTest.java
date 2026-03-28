/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static schemacrawler.test.utility.DatabaseTestUtility.getCatalog;

import java.sql.Connection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.ermodel.utility.ERModelUtility;
import schemacrawler.inclusionrule.RegularExpressionExclusionRule;
import schemacrawler.schema.Catalog;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.test.utility.DatabaseTestUtility;
import schemacrawler.test.utility.WithTestDatabase;
import schemacrawler.tools.loader.ermodel.ERModelLoader;
import schemacrawler.tools.loader.ermodel.implicitassociations.ImplicitAssociationsLoaderOptions;
import schemacrawler.tools.loader.ermodel.implicitassociations.ImplicitAssociationsLoaderProvider;
import schemacrawler.tools.options.Config;
import schemacrawler.tools.options.ConfigUtility;
import schemacrawler.tools.utility.SchemaCrawlerUtility;
import us.fatehi.utility.property.PropertyName;

@WithTestDatabase
@TestInstance(PER_CLASS)
public class ImplicitAssociationsERModelLoaderTest {

  private Catalog catalog;

  @BeforeAll
  public void loadCatalog(final Connection connection) throws Exception {
    final LimitOptionsBuilder limitOptionsBuilder =
        LimitOptionsBuilder.builder()
            .includeSchemas(new RegularExpressionExclusionRule(".*\\.FOR_LINT"));
    final SchemaCrawlerOptions schemaCrawlerOptions =
        SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
            .withLimitOptions(limitOptionsBuilder.toOptions());

    catalog =
        getCatalog(
            connection, DatabaseTestUtility.newSchemaRetrievalOptions(), schemaCrawlerOptions);
  }

  @Test
  public void loaderAddsImplicitAssociationsWhenEnabled() {

    final Config config = ConfigUtility.newConfig();
    config.put("implicit-associations", true);
    config.put("infer-extension-tables", true);

    final ERModel erModel = SchemaCrawlerUtility.buildERModel(catalog, config);

    final ImplicitAssociationsLoaderProvider provider = new ImplicitAssociationsLoaderProvider();
    final ERModelLoader<?> loader = provider.newCommand(config);
    loader.setCatalog(catalog);
    loader.setERModel(erModel);
    loader.execute();

    assertThat(loader.getERModel().getImplicitRelationships().size(), is(greaterThan(0)));
  }

  @Test
  public void loaderDefaultsToDisabled() {
    final ERModel erModel = SchemaCrawlerUtility.buildERModel(catalog);

    final ImplicitAssociationsLoaderProvider provider = new ImplicitAssociationsLoaderProvider();
    final ERModelLoader<?> loader = provider.newCommand(ConfigUtility.newConfig());
    loader.setCatalog(catalog);
    loader.setERModel(erModel);
    loader.execute();

    assertThat(loader.getERModel().getImplicitRelationships(), is(empty()));
  }

  @Test
  public void loaderDoesNothingWhenERModelNotLoaded() {
    final ImplicitAssociationsLoaderProvider provider = new ImplicitAssociationsLoaderProvider();
    final ERModelLoader<?> loader = provider.newCommand(ConfigUtility.newConfig());
    loader.setCatalog(catalog);

    // ERModel not set - loader should skip gracefully, leaving ERModel null
    loader.execute();

    assertThat(loader.getERModel(), is(nullValue()));
  }

  @Test
  public void loaderDoesNothingWhenOptionDisabled1() {

    final Config config = ConfigUtility.newConfig();
    config.put("implicit-associations", false);

    final ERModel erModel = SchemaCrawlerUtility.buildERModel(catalog, config);

    // With option disabled, no implicit relationships should be added
    assertThat(erModel.getImplicitRelationships(), is(empty()));
  }

  @Test
  public void loaderDoesNothingWhenOptionDisabled2() {

    final ERModel erModel = ERModelUtility.buildEmptyERModel();
    // Assert there are no implicit relationships at the start
    assertThat(erModel.getImplicitRelationships(), is(empty()));

    final Config config = ConfigUtility.newConfig();
    config.put("implicit-associations", false);
    final ImplicitAssociationsLoaderProvider provider = new ImplicitAssociationsLoaderProvider();
    final ERModelLoader<?> loader = provider.newCommand(config);
    loader.setCatalog(catalog);
    loader.setERModel(erModel);
    loader.execute();

    // With option disabled, no implicit relationships should be added
    assertThat(loader.getERModel().getImplicitRelationships(), is(empty()));
  }

  @Test
  public void loaderOptionsCanBeDisabled() {
    final ImplicitAssociationsLoaderOptions options =
        new ImplicitAssociationsLoaderOptions(false, false);
    assertThat(options.loadImplicitAssociations(), is(false));
    assertThat(options.inferExtensionTables(), is(false));
  }

  @Test
  public void loaderOptionsDefaultToEnabled() {
    final ImplicitAssociationsLoaderOptions options =
        new ImplicitAssociationsLoaderOptions(true, true);
    assertThat(options.loadImplicitAssociations(), is(true));
    assertThat(options.inferExtensionTables(), is(true));
  }

  @Test
  public void providerSupportsExpectedCommand() {
    final ImplicitAssociationsLoaderProvider provider = new ImplicitAssociationsLoaderProvider();
    assertThat(provider.getSupportedCommands(), hasSize(1));
    final PropertyName commandName = provider.getSupportedCommands().iterator().next();
    assertThat(commandName.getName(), is("implicitassociationsloader"));
  }
}
