/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.jdbc.serverfingerprint;

/**
 * Classifies the host in a JDBC URL by its network accessibility. Used to determine whether a
 * hostname should be hashed, masked, or treated as a public identifier in security-sensitive
 * contexts such as fingerprinting.
 */
public enum HostClassification {
  UNKNOWN,

  /** The host is localhost or a loopback/link-local address. */
  LOCALHOST,

  /** The host is an internal or private network address (private IP range, corporate domain). */
  INTERNAL,

  /** The host is a publicly routable hostname or IP address. */
  PUBLIC
}
