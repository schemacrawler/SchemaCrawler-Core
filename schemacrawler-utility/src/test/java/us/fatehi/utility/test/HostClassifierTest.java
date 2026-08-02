/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;
import us.fatehi.utility.CloudProvider;
import us.fatehi.utility.HostClassifier;

public class HostClassifierTest {

  @Test
  public void ipV4Matching() {
    assertThat(new HostClassifier("127.0.0.1").isIpV4(), is(true));
    assertThat(new HostClassifier("10.20.30.40").isIpV4(), is(true));
    assertThat(new HostClassifier("256.1.1.1").isIpV4(), is(false));
    assertThat(new HostClassifier("db.example.com").isIpV4(), is(false));
  }

  @Test
  public void ipV6Matching() {
    assertThat(new HostClassifier("::1").isIpV6(), is(true));
    assertThat(new HostClassifier("2001:db8::8a2e:370:7334").isIpV6(), is(true));
    assertThat(new HostClassifier("127.0.0.1").isIpV6(), is(false));
    assertThat(new HostClassifier("db.example.com").isIpV6(), is(false));
  }

  @Test
  public void internalDomainMatching() {
    assertThat(new HostClassifier("app.internal").isInternalDomain(), is(true));
    assertThat(new HostClassifier("db.corp").isInternalDomain(), is(true));
    assertThat(new HostClassifier("dev.local").isInternalDomain(), is(true));
    assertThat(new HostClassifier("cache.lan").isInternalDomain(), is(true));
    assertThat(new HostClassifier("example.com").isInternalDomain(), is(false));
  }

  @Test
  public void localhostMatching() {
    assertThat(new HostClassifier("localhost").isLocalhost(), is(true));
    assertThat(new HostClassifier("LOCALHOST").isLocalhost(), is(true));
    assertThat(new HostClassifier("localhost.localdomain").isLocalhost(), is(true));
    assertThat(new HostClassifier("api.localhost").isLocalhost(), is(true));
    assertThat(new HostClassifier("127.0.0.1").isLocalhost(), is(true));
    assertThat(new HostClassifier("127.12.34.56").isLocalhost(), is(true));
    assertThat(new HostClassifier("::1").isLocalhost(), is(true));
    assertThat(new HostClassifier("0:0:0:0:0:0:0:1").isLocalhost(), is(true));
    assertThat(new HostClassifier("10.0.0.1").isLocalhost(), is(false));
    assertThat(new HostClassifier("example.com").isLocalhost(), is(false));
  }

  @Test
  public void nonPublicPredicate() {
    assertThat(new HostClassifier("10.0.0.1").isNotHostName(), is(true));
    assertThat(new HostClassifier("2001:db8::1").isNotHostName(), is(true));
    assertThat(new HostClassifier("db.internal").isNotHostName(), is(true));
    assertThat(new HostClassifier("localhost").isNotHostName(), is(false));
    assertThat(new HostClassifier("db.example.com").isNotHostName(), is(false));
  }

  @Test
  public void blankInput() {
    assertThat(new HostClassifier(null).isNotHostName(), is(false));
    assertThat(new HostClassifier("  ").isNotHostName(), is(false));
    assertThat(new HostClassifier(null).isLocalhost(), is(false));
    assertThat(new HostClassifier("").isLocalhost(), is(false));
  }

  @Test
  public void cloudProviderDetection() {
    assertThat(
        new HostClassifier("mydb.us-east-1.rds.amazonaws.com").getCloudProvider(),
        is(CloudProvider.AWS));
    assertThat(new HostClassifier("localhost").getCloudProvider(), is(CloudProvider.LOCAL));
    assertThat(new HostClassifier("db.example.com").getCloudProvider(), is(CloudProvider.UNKNOWN));
  }

  @Test
  public void cloudRegionDetection() {
    assertThat(
        new HostClassifier("mydb.us-east-1.rds.amazonaws.com").getCloudRegion(), is("us-east-1"));
    assertThat(new HostClassifier("mydb.database.windows.net").getCloudRegion(), is("global"));
    assertThat(new HostClassifier("localhost").getCloudRegion(), is("local"));
    assertThat(new HostClassifier("db.example.com").getCloudRegion(), is((String) null));
  }

  @Test
  public void sanitizedHostName() {
    assertThat(new HostClassifier("localhost").getSanitizedHostName(), is("localhost"));
    assertThat(new HostClassifier("10.0.0.1").getSanitizedHostName(), is("masked-instance"));
    assertThat(new HostClassifier("db.internal").getSanitizedHostName(), is("masked-instance"));
    assertThat(new HostClassifier("db.example.com").getSanitizedHostName(), is("db.example.com"));
    assertThat(new HostClassifier(null).getSanitizedHostName(), is((String) null));
  }
}
