/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.catalogloader;

import static java.util.Comparator.comparingInt;
import static java.util.Comparator.nullsLast;
import static java.util.Objects.compare;

import java.util.Comparator;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;
import schemacrawler.schemacrawler.SchemaRetrievalOptionsBuilder;
import schemacrawler.tools.executable.BaseCommand;
import schemacrawler.tools.executable.CommandOptions;
import schemacrawler.tools.executable.commandline.PluginCommand;
import us.fatehi.utility.property.PropertyName;

public abstract class BaseCatalogLoader<P extends CommandOptions> extends BaseCommand<P>
    implements CatalogLoader<P> {

  public static Comparator<CatalogLoader<?>> comparator =
      nullsLast(comparingInt(CatalogLoader<?>::getPriority))
          .thenComparing(loader -> loader.getCommandName().getName());

  private final int priority;
  private SchemaRetrievalOptions schemaRetrievalOptions;
  private SchemaCrawlerOptions schemaCrawlerOptions;

  protected BaseCatalogLoader(final PropertyName catalogLoaderName, final int priority) {
    super(catalogLoaderName);
    this.priority = priority;
  }

  @Override
  public final int compareTo(final CatalogLoader<P> otherCatalogLoader) {
    return compare(this, otherCatalogLoader, comparator);
  }

  @Override
  public PluginCommand getCommandLineCommand() {
    return PluginCommand.empty();
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

  protected final boolean isDatabaseSystemIdentifier(final String databaseSystemIdentifier) {
    final String actualDatabaseSystemIdentifier =
        getSchemaRetrievalOptions().getDatabaseServerType().getDatabaseSystemIdentifier();
    if (actualDatabaseSystemIdentifier == null && databaseSystemIdentifier == null) {
      return true;
    }
    if (actualDatabaseSystemIdentifier != null) {
      return actualDatabaseSystemIdentifier.equals(databaseSystemIdentifier);
    }
    return false;
  }

  protected final boolean isLoaded() {
    return catalog != null;
  }

  protected void setCommandOptions(P commandOptions) {
    this.commandOptions = commandOptions;
  }
}
