/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.test.integration.utility;

import org.testcontainers.utility.DockerImageName;

public class HiveTestUtility {

  @SuppressWarnings("resource")
  public static HiveContainer newHiveContainer() {
    return new HiveContainer(DockerImageName.parse("apache/hive").withTag("4.2.0"));
  }
}
