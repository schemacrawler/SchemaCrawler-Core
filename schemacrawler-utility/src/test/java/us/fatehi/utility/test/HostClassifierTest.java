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
import us.fatehi.utility.HostType;

public class HostClassifierTest {

  @Test
  public void blankInput() {
    assertThat(new HostClassifier(null).isHostName(), is(false));
    assertThat(new HostClassifier("  ").isHostName(), is(false));
    assertThat(new HostClassifier(null).isLocalhost(), is(false));
    assertThat(new HostClassifier("").isLocalhost(), is(false));
  }

  @Test
  public void cloudProviderDetection() {
    assertThat(
        new HostClassifier("mydb.us-east-1.rds.amazonaws.com").getHostLocation().cloudProvider(),
        is(CloudProvider.AWS));
    assertThat(
        new HostClassifier("mydb-aurora-cluster").getHostLocation().cloudProvider(),
        is(CloudProvider.AWS));
    assertThat(
        new HostClassifier("mydb.cloudsql.google.internal").getHostLocation().cloudProvider(),
        is(CloudProvider.GCP));
    assertThat(
        new HostClassifier("adb.uk-london-1.oraclecloud.com").getHostLocation().cloudProvider(),
        is(CloudProvider.OCI));
    assertThat(
        new HostClassifier("localhost").getHostLocation().cloudProvider(),
        is(CloudProvider.UNKNOWN));
    assertThat(
        new HostClassifier("db.example.com").getHostLocation().cloudProvider(),
        is(CloudProvider.UNKNOWN));
  }

  @Test
  public void cloudRegionDetection() {
    assertThat(
        new HostClassifier("mydb.us-east-1.rds.amazonaws.com").getHostLocation().cloudRegion(),
        is("us-east-1"));
    assertThat(
        new HostClassifier("adb.uk-london-1.oraclecloud.com").getHostLocation().cloudRegion(),
        is("uk-london-1"));
    assertThat(
        new HostClassifier("mydb.database.windows.net").getHostLocation().cloudRegion(),
        is("global"));
    assertThat(new HostClassifier("localhost").getHostLocation().cloudRegion(), is(""));
    assertThat(new HostClassifier("127.2.3.4").getHostLocation().cloudRegion(), is(""));
    assertThat(new HostClassifier(null).getHostLocation().cloudRegion(), is(""));
    assertThat(new HostClassifier("db.example.com").getHostLocation().cloudRegion(), is(""));
  }

  @Test
  public void hostTypeDetection() {
    assertThat(
        new HostClassifier("localhost").getHostLocation().hostType(), is(HostType.localhost));
    assertThat(
        new HostClassifier("127.0.0.1").getHostLocation().hostType(), is(HostType.localhost));
    assertThat(
        new HostClassifier("db.internal").getHostLocation().hostType(), is(HostType.on_premises));
    assertThat(
        new HostClassifier("10.20.30.40").getHostLocation().hostType(), is(HostType.on_premises));
    assertThat(
        new HostClassifier("mydb.us-east-1.rds.amazonaws.com").getHostLocation().hostType(),
        is(HostType.remote_host));
    assertThat(
        new HostClassifier("prod.mycompany.com").getHostLocation().hostType(),
        is(HostType.remote_host));
    assertThat(
        new HostClassifier("2001:db8::8a2e:370:7334").getHostLocation().hostType(),
        is(HostType.unknown));
  }

  @Test
  public void internalDomainMatching() {
    assertThat(new HostClassifier("app.internal").isInternalDomain(), is(true));
    assertThat(new HostClassifier("db.corp").isInternalDomain(), is(true));
    assertThat(new HostClassifier("dev.local").isInternalDomain(), is(true));
    assertThat(new HostClassifier("cache.lan").isInternalDomain(), is(true));
    assertThat(new HostClassifier("db.EXAMPLE.com").isInternalDomain(), is(true));
    assertThat(new HostClassifier("LOCALHOST").isInternalDomain(), is(true));
    assertThat(new HostClassifier("example.com").isInternalDomain(), is(false));
  }

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
  public void localhostMatching() {
    assertThat(new HostClassifier("localhost").isLocalhost(), is(true));
    assertThat(new HostClassifier("LOCALHOST").isLocalhost(), is(true));
    assertThat(new HostClassifier("localhost.localdomain").isLocalhost(), is(true));
    assertThat(new HostClassifier("api.localhost").isLocalhost(), is(true));
    assertThat(new HostClassifier("127.0.0.1").isLocalhost(), is(true));
    assertThat(new HostClassifier("127.12.34.56").isLocalhost(), is(true));
    assertThat(new HostClassifier("::1").isLocalhost(), is(true));
    assertThat(new HostClassifier("[::1]").isLocalhost(), is(true));
    assertThat(new HostClassifier("0:0:0:0:0:0:0:1").isLocalhost(), is(true));
    assertThat(new HostClassifier("10.0.0.1").isLocalhost(), is(false));
    assertThat(new HostClassifier("example.com").isLocalhost(), is(false));
  }

  @Test
  public void hostNameMatching() {
    assertThat(new HostClassifier("prod.mycompany.com").isHostName(), is(true));
    assertThat(new HostClassifier("  prod.mycompany.com  ").isHostName(), is(true));
    assertThat(new HostClassifier("[2001:db8::1]").isHostName(), is(false));
    assertThat(new HostClassifier("10.0.0.1").isHostName(), is(false));
    assertThat(new HostClassifier("2001:db8::1").isHostName(), is(false));
    assertThat(new HostClassifier("db.internal").isHostName(), is(false));
    assertThat(new HostClassifier("localhost").isHostName(), is(false));
    assertThat(new HostClassifier("db.example.com").isHostName(), is(false));
  }
}
