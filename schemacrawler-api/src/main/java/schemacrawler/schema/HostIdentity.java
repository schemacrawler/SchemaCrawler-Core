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
import us.fatehi.utility.HostType;

public record HostIdentity(HostType hostType, CloudProvider cloudProvider, String region)
    implements Serializable {

  @Serial private static final long serialVersionUID = -4347254509135801162L;

  public static HostIdentity unknown() {
    return new HostIdentity(HostType.unknown, CloudProvider.UNKNOWN, "");
  }

  public HostIdentity {
    hostType = hostType == null ? HostType.unknown : hostType;
    cloudProvider = cloudProvider == null ? CloudProvider.UNKNOWN : cloudProvider;
    region = isBlank(region) ? "unknown" : region;
  }

  @Override
  public String toString() {
    return "%s @ %s / %s"
        .formatted(hostType.name(), cloudProvider.name().toLowerCase(Locale.ROOT), region);
  }
}
