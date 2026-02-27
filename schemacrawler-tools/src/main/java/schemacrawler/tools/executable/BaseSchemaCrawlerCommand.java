/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.executable;

import static java.util.Objects.requireNonNull;

import schemacrawler.ermodel.model.ERModel;
import schemacrawler.ermodel.utility.EntityModelUtility;
import schemacrawler.schema.Identifiers;
import schemacrawler.schemacrawler.InformationSchemaViews;
import schemacrawler.schemacrawler.InformationSchemaViewsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.tools.options.OutputOptions;
import schemacrawler.tools.options.OutputOptionsBuilder;
import us.fatehi.utility.property.PropertyName;

/** A SchemaCrawler tools executable unit. */
public abstract class BaseSchemaCrawlerCommand<P extends CommandOptions>
    extends BaseCommand<P, Void> implements SchemaCrawlerCommand<P> {

  protected ERModel erModel;
  protected Identifiers identifiers;
  protected InformationSchemaViews informationSchemaViews;
  protected OutputOptions outputOptions;
  protected SchemaCrawlerOptions schemaCrawlerOptions;

  protected BaseSchemaCrawlerCommand(final PropertyName command) {
    super(command);

    schemaCrawlerOptions = SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions();
    outputOptions = OutputOptionsBuilder.newOutputOptions();
  }

  @Override
  public final Void call() {
    execute();
    return null;
  }

  /** Runtime exceptions will be thrown if the command is not available. */
  @Override
  public abstract void checkAvailability() throws RuntimeException;

  @Override
  public ERModel getERModel() {
    return erModel;
  }

  /** {@inheritDoc} */
  @Override
  public Identifiers getIdentifiers() {
    return identifiers;
  }

  /** {@inheritDoc} */
  @Override
  public final InformationSchemaViews getInformationSchemaViews() {
    if (informationSchemaViews == null) {
      return InformationSchemaViewsBuilder.newInformationSchemaViews();
    }
    return informationSchemaViews;
  }

  /** {@inheritDoc} */
  @Override
  public final OutputOptions getOutputOptions() {
    return outputOptions;
  }

  /** {@inheritDoc} */
  @Override
  public final SchemaCrawlerOptions getSchemaCrawlerOptions() {
    return schemaCrawlerOptions;
  }

  /** {@inheritDoc} */
  @Override
  public void initialize() {
    super.initialize();
    checkOptions();
    if (erModel == null) {
      erModel = EntityModelUtility.buildEmptyERModel();
    }
  }

  @Override
  public void setERModel(final ERModel erModel) {
    this.erModel = requireNonNull(erModel, "No ER model provided");
  }

  /** {@inheritDoc} */
  @Override
  public void setIdentifiers(final Identifiers identifiers) {
    this.identifiers = identifiers;
  }

  /** {@inheritDoc} */
  @Override
  public final void setInformationSchemaViews(final InformationSchemaViews informationSchemaViews) {
    this.informationSchemaViews = informationSchemaViews;
  }

  /** {@inheritDoc} */
  @Override
  public final void setOutputOptions(final OutputOptions outputOptions) {
    if (outputOptions != null) {
      this.outputOptions = outputOptions;
    } else {
      this.outputOptions = OutputOptionsBuilder.newOutputOptions();
    }
  }

  /** {@inheritDoc} */
  @Override
  public final void setSchemaCrawlerOptions(final SchemaCrawlerOptions schemaCrawlerOptions) {
    if (schemaCrawlerOptions != null) {
      this.schemaCrawlerOptions = schemaCrawlerOptions;
    } else {
      this.schemaCrawlerOptions = SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions();
    }
  }

  protected void checkCatalog() {
    requireNonNull(catalog, "No database catalog provided");
    if (usesConnection()) {
      requireNonNull(getConnectionSource(), "No database connection source provided");
    }
  }

  private void checkOptions() {
    requireNonNull(schemaCrawlerOptions, "No SchemaCrawler options provided");
    requireNonNull(commandOptions, "No command options provided");
    requireNonNull(outputOptions, "No output options provided");
    requireNonNull(identifiers, "No database identifiers provided");
  }
}
