/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.datasource;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

final class CloseState {

  private static final VarHandle CLOSED;

  static {
    try {
      CLOSED = MethodHandles.lookup().findVarHandle(CloseState.class, "closed", boolean.class);
    } catch (final ReflectiveOperationException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  // Backing field for CLOSED VarHandle. All reads/writes go through the
  // VarHandle to enforce explicit access modes (volatile read/write and CAS - compare and set).
  private volatile boolean closed;

  boolean isClosed() {
    return (boolean) CLOSED.getVolatile(this);
  }

  boolean tryClose() {
    // Atomically transition from open(false) to closed(true).
    // Returns true only for the first caller that wins the transition.
    // Subsequent callers see false and should treat close as already done.
    return CLOSED.compareAndSet(this, false, true);
  }
}
