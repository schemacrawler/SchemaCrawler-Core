/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility;

import static us.fatehi.utility.Utility.isBlank;
import static us.fatehi.utility.Utility.trimToEmpty;

import java.io.Serial;
import java.io.Serializable;

public record HostLocation(HostType hostType, CloudProvider cloudProvider, String cloudRegion)
    implements Serializable {

  @Serial private static final long serialVersionUID = -4347254509135801162L;

  public static HostLocation unknown() {
    return new HostLocation(HostType.unknown, CloudProvider.UNKNOWN, "");
  }

  public HostLocation {
    hostType = hostType == null ? HostType.unknown : hostType;
    cloudProvider = cloudProvider == null ? CloudProvider.UNKNOWN : cloudProvider;
    cloudRegion = trimToEmpty(cloudRegion);
  }

  public String getDescription() {
    switch (hostType) {
      case remote_host:
        {
          switch (cloudProvider) {
            case UNKNOWN:
              return hostType.getDescription();
            default:
              {
                String description = cloudProvider.getDescription();
                if (!isBlank(cloudRegion)) {
                  return description = "%s (%s)".formatted(description, cloudRegion);
                }
                return description;
              }
          }
        }
      default:
        return hostType.getDescription();
    }
  }
}
