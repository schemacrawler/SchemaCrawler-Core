/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.schema;

import static us.fatehi.utility.Utility.isBlank;

import java.io.Serial;
import java.io.Serializable;
import java.util.Locale;
import us.fatehi.utility.CloudProvider;

public record ServerIdentity(String instanceName, CloudProvider cloudProvider, String region)
    implements Serializable {

  @Serial private static final long serialVersionUID = -4347254509135801162L;

  public static ServerIdentity unknown() {
    return new ServerIdentity("unknown-instance", CloudProvider.UNKNOWN, "unknown");
  }

  public ServerIdentity {
    instanceName = isBlank(instanceName) ? "unknown-instance" : instanceName;
    cloudProvider = cloudProvider == null ? CloudProvider.UNKNOWN : cloudProvider;
    region = isBlank(region) ? "unknown" : region;
  }

  @Override
  public String toString() {
    return "%s @ %s / %s"
        .formatted(instanceName, cloudProvider.name().toLowerCase(Locale.ROOT), region);
  }
}
