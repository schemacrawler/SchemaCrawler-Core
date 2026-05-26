/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.scheduler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;

public class RunnerExceptionTest {

  @Test
  public void runnerExceptionWithCause() {
    final RuntimeException cause = new RuntimeException("original error");
    final RunnerException exception = new RunnerException(cause);
    assertThat(exception, is(not(nullValue())));
    assertThat(exception.getCause(), is(cause));
  }
}
