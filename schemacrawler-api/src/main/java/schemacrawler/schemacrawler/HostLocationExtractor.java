/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.schemacrawler;

import java.sql.Connection;
import us.fatehi.utility.HostLocation;

@FunctionalInterface
public interface HostLocationExtractor {

  HostLocation extract(Connection connection);
}
