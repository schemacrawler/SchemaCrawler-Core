/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility;

import static us.fatehi.utility.Utility.isBlank;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Locale;
import java.util.regex.Pattern;

public final class HostClassifier {

  private static final Pattern IPV4_PATTERN =
      Pattern.compile(
          "^(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}$");
  private final String host;

  public HostClassifier(final String host) {
    this.host = normalize(host);
  }

  public HostLocation getHostLocation() {
    return new HostLocation(getHostType(), getCloudProvider(), getCloudRegion());
  }

  public boolean isHostName() {
    return !isBlank(host) && !isIpV4() && !isIpV6() && !isInternalDomain();
  }

  public boolean isInternalDomain() {
    if (isBlank(host)) {
      return false;
    }
    if (isLocalhost()) {
      return true;
    }
    final String value = host.toLowerCase(Locale.ROOT);
    return value.endsWith(".internal")
        || value.endsWith(".corp")
        || value.endsWith(".local")
        || value.endsWith(".lan")
        || value.endsWith(".example.com");
  }

  public boolean isIpV4() {
    if (isBlank(host)) {
      return false;
    }
    return IPV4_PATTERN.matcher(host).matches();
  }

  public boolean isIpV6() {
    if (isBlank(host) || !host.contains(":")) {
      return false;
    }
    try {
      return InetAddress.getByName(host) instanceof Inet6Address;
    } catch (final Exception e) {
      return false;
    }
  }

  public boolean isLocalhost() {
    if (isBlank(host)) {
      return false;
    }
    final String lower = host.toLowerCase(Locale.ROOT);

    if ("localhost".equals(lower)
        || "localhost.localdomain".equals(lower)
        || lower.endsWith(".localhost")) {
      return true;
    }

    if (isIpV4() && host.startsWith("127.")) {
      return true;
    }

    if (isIpV6()) {
      try {
        return InetAddress.getByName(host).isLoopbackAddress();
      } catch (final Exception e) {
      }
    }

    return false;
  }

  private CloudProvider getCloudProvider() {
    return CloudProvider.fromHost(host);
  }

  private String getCloudRegion() {
    if (isBlank(host)) {
      return null;
    }
    final CloudProvider cloudProvider = getCloudProvider();
    final String[] hostParts = host.toLowerCase(Locale.ROOT).split("\\.");
    if (cloudProvider == CloudProvider.AWS) {
      for (final String hostPart : hostParts) {
        if (hostPart.matches("[a-z]{2}-[a-z]+-\\d+")) {
          return hostPart;
        }
      }
    }
    if (cloudProvider == CloudProvider.OCI) {
      for (final String hostPart : hostParts) {
        if (hostPart.matches("[a-z]+-[a-z]+-\\d+")) {
          return hostPart;
        }
      }
    }
    if (cloudProvider == CloudProvider.AZURE) {
      return "global";
    }
    return null;
  }

  private HostType getHostType() {
    if (isLocalhost()) {
      return HostType.localhost;
    }
    if (isInternalDomain() || isInternalIpRange()) {
      return HostType.on_premises;
    }
    if (getCloudProvider() != CloudProvider.UNKNOWN || isHostName()) {
      return HostType.remote_host;
    }
    return HostType.unknown;
  }

  private boolean isInternalIpRange() {
    if (isBlank(host)) {
      return false;
    }

    if (isIpV4()) {
      return host.startsWith("10.")
          || host.startsWith("192.168.")
          || host.matches("^172\\.(1[6-9]|2\\d|3[0-1])\\..*");
    }

    if (isIpV6()) {
      try {
        final InetAddress ipV6Address = InetAddress.getByName(host);
        return ipV6Address.isSiteLocalAddress() || ipV6Address.isLinkLocalAddress();
      } catch (final Exception e) {
      }
    }

    return false;
  }

  private String normalize(final String host) {
    if (isBlank(host)) {
      return null;
    }
    String value = host.trim();
    if (value.startsWith("[") && value.endsWith("]") && value.length() > 2) {
      value = value.substring(1, value.length() - 1);
    }
    return value;
  }
}
