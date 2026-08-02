/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.schemacrawler;

import java.sql.Connection;
import schemacrawler.schema.ServerIdentity;

@FunctionalInterface
public interface ServerIdentityExtractor {

  ServerIdentity extract(Connection connection);
}
