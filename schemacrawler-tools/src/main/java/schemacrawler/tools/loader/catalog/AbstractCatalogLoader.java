/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.loader.catalog;

import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;
import schemacrawler.schemacrawler.SchemaRetrievalOptionsBuilder;
import schemacrawler.tools.command.AbstractCommand;
import schemacrawler.tools.command.CommandOptions;
import us.fatehi.utility.property.PropertyName;

public abstract class AbstractCatalogLoader<P extends CommandOptions> extends AbstractCommand<P>
    implements CatalogLoader<P> {

  private final int priority;
  private SchemaRetrievalOptions schemaRetrievalOptions;
  private SchemaCrawlerOptions schemaCrawlerOptions;

  protected AbstractCatalogLoader(final PropertyName catalogLoaderName, final int priority) {
    super(catalogLoaderName);
    this.priority = priority;
  }

  @Override
  public final int getPriority() {
    return priority;
  }

  @Override
  public final SchemaCrawlerOptions getSchemaCrawlerOptions() {
    if (schemaCrawlerOptions == null) {
      return SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions();
    }
    return schemaCrawlerOptions;
  }

  @Override
  public final SchemaRetrievalOptions getSchemaRetrievalOptions() {
    if (schemaRetrievalOptions == null) {
      return SchemaRetrievalOptionsBuilder.newSchemaRetrievalOptions();
    }
    return schemaRetrievalOptions;
  }

  @Override
  public final void setSchemaCrawlerOptions(final SchemaCrawlerOptions schemaCrawlerOptions) {
    this.schemaCrawlerOptions = schemaCrawlerOptions;
  }

  @Override
  public final void setSchemaRetrievalOptions(final SchemaRetrievalOptions schemaRetrievalOptions) {
    this.schemaRetrievalOptions = schemaRetrievalOptions;
  }

  @Override
  public String toString() {
    return getCommandName().toString();
  }
}
