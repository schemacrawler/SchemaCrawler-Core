/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.loader.ermodel.implicitassociations;

import schemacrawler.tools.command.CommandOptions;

/**
 * Options for the implicit associations ER model loader.
 *
 * @param loadImplicitAssociations Whether to load implicit associations into the ER model
 */
public record ImplicitAssociationsERModelLoaderOptions(boolean loadImplicitAssociations)
    implements CommandOptions {}
