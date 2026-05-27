/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.property;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;

public class JvmArchitectureInfoTest {

  @Test
  public void jvmArchitectureInfoNotNull() {
    final JvmArchitectureInfo info = JvmArchitectureInfo.jvmArchitectureInfo();
    assertThat(info, is(not(nullValue())));
  }

  @Test
  public void jvmArchitectureInfoName() {
    final JvmArchitectureInfo info = JvmArchitectureInfo.jvmArchitectureInfo();
    assertThat(info.getName(), is("JVM Architecture"));
  }

  @Test
  public void jvmArchitectureInfoVersion() {
    final JvmArchitectureInfo info = JvmArchitectureInfo.jvmArchitectureInfo();
    // Product version contains the OS arch property in parentheses
    final String arch = System.getProperty("os.arch");
    assertThat(info.getProductVersion(), containsString(arch));
  }

  @Test
  public void jvmArchitectureInfoIsSingleton() {
    final JvmArchitectureInfo info1 = JvmArchitectureInfo.jvmArchitectureInfo();
    final JvmArchitectureInfo info2 = JvmArchitectureInfo.jvmArchitectureInfo();
    assertThat(info1, is(info2));
  }
}
