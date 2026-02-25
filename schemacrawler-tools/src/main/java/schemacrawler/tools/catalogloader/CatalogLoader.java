/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.catalogloader;

import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;
import schemacrawler.tools.executable.Command;
import schemacrawler.tools.executable.CommandOptions;

public interface CatalogLoader<P extends CommandOptions>
    extends Command<P>, Comparable<CatalogLoader<P>> {

  int getPriority();

  SchemaCrawlerOptions getSchemaCrawlerOptions();

  SchemaRetrievalOptions getSchemaRetrievalOptions();

  void setSchemaCrawlerOptions(SchemaCrawlerOptions schemaCrawlerOptions);

  void setSchemaRetrievalOptions(SchemaRetrievalOptions schemaRetrievalOptions);
}
