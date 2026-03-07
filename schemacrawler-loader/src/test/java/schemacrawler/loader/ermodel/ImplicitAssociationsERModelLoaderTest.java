/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.ermodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static schemacrawler.test.utility.DatabaseTestUtility.getCatalog;
import static schemacrawler.test.utility.DatabaseTestUtility.schemaCrawlerOptionsWithMaximumSchemaInfoLevel;

import java.sql.Connection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import schemacrawler.ermodel.implementation.ERModelBuilder;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.ermodel.utility.EntityModelUtility;
import schemacrawler.inclusionrule.RegularExpressionExclusionRule;
import schemacrawler.schema.Catalog;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.test.utility.DatabaseTestUtility;
import schemacrawler.test.utility.WithTestDatabase;
import schemacrawler.tools.loader.ermodel.implicitassociations.ImplicitAssociationsERModelLoader;
import schemacrawler.tools.loader.ermodel.implicitassociations.ImplicitAssociationsERModelLoaderOptions;
import schemacrawler.tools.loader.ermodel.implicitassociations.ImplicitAssociationsERModelLoaderProvider;
import schemacrawler.tools.options.Config;
import schemacrawler.tools.options.ConfigUtility;
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
            connection,
            DatabaseTestUtility.newSchemaRetrievalOptions(),
            schemaCrawlerOptions);
  }

  @Test
  public void loaderDoesNothingWhenERModelNotLoaded() {
    final ImplicitAssociationsERModelLoaderProvider provider =
        new ImplicitAssociationsERModelLoaderProvider();
    final ImplicitAssociationsERModelLoader loader =
        provider.newCommand(ConfigUtility.newConfig());
    loader.setCatalog(catalog);

    // ERModel not set - loader should skip gracefully, leaving ERModel null
    loader.execute();

    assertThat(loader.getERModel(), is(org.hamcrest.core.IsNull.nullValue()));
  }

  @Test
  public void loaderDoesNothingWhenOptionDisabled() {
    final ERModel erModel = EntityModelUtility.buildERModel(catalog);

    final Config config = ConfigUtility.newConfig();
    config.put("implicit-associations", false);
    final ImplicitAssociationsERModelLoaderProvider provider =
        new ImplicitAssociationsERModelLoaderProvider();
    final ImplicitAssociationsERModelLoader loader = provider.newCommand(config);
    loader.setCatalog(catalog);
    loader.setERModel(erModel);
    loader.execute();

    // With option disabled, no implicit relationships should be added
    assertThat(loader.getERModel().getImplicitRelationships(), is(empty()));
  }

  @Test
  public void loaderAddsImplicitAssociationsWhenEnabled() {
    final ERModel erModel = EntityModelUtility.buildERModel(catalog);

    final Config config = ConfigUtility.newConfig();
    config.put("implicit-associations", true);
    final ImplicitAssociationsERModelLoaderProvider provider =
        new ImplicitAssociationsERModelLoaderProvider();
    final ImplicitAssociationsERModelLoader loader = provider.newCommand(config);
    loader.setCatalog(catalog);
    loader.setERModel(erModel);
    loader.execute();

    assertThat(
        loader.getERModel().getImplicitRelationships().size(), is(greaterThan(0)));
  }

  @Test
  public void loaderDefaultsToEnabled() {
    final ERModel erModel = EntityModelUtility.buildERModel(catalog);

    final ImplicitAssociationsERModelLoaderProvider provider =
        new ImplicitAssociationsERModelLoaderProvider();
    final ImplicitAssociationsERModelLoader loader =
        provider.newCommand(ConfigUtility.newConfig());
    loader.setCatalog(catalog);
    loader.setERModel(erModel);
    loader.execute();

    assertThat(loader.getERModel().getImplicitRelationships().size(), is(greaterThan(0)));
  }

  @Test
  public void providerSupportsExpectedCommand() {
    final ImplicitAssociationsERModelLoaderProvider provider =
        new ImplicitAssociationsERModelLoaderProvider();
    assertThat(provider.getSupportedCommands(), hasSize(1));
    final PropertyName commandName = provider.getSupportedCommands().iterator().next();
    assertThat(commandName.getName(), is("implicitassociationsmodelloader"));
  }

  @Test
  public void loaderOptionsDefaultToEnabled() {
    final ImplicitAssociationsERModelLoaderOptions options =
        new ImplicitAssociationsERModelLoaderOptions(true);
    assertThat(options.loadImplicitAssociations(), is(true));
  }

  @Test
  public void loaderOptionsCanBeDisabled() {
    final ImplicitAssociationsERModelLoaderOptions options =
        new ImplicitAssociationsERModelLoaderOptions(false);
    assertThat(options.loadImplicitAssociations(), is(false));
  }
}
