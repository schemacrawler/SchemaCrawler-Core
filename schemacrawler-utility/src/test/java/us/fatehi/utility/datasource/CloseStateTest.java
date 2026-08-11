/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.datasource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

class CloseStateTest {

  @Test
  void closeCanTransitionOnlyOnce() {
    final CloseState closeState = new CloseState();

    assertThat(closeState.tryClose(), is(true));
    assertThat(closeState.tryClose(), is(false));
    assertThat(closeState.isClosed(), is(true));
  }

  @Test
  void newCloseStateIsOpen() {
    final CloseState closeState = new CloseState();

    assertThat(closeState.isClosed(), is(false));
  }
}
