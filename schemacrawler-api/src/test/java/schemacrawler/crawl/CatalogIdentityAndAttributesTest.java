/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.crawl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static us.fatehi.test.utility.TestObjectUtility.mockConnection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;
import schemacrawler.filter.CatalogProjectionBuilder;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableType;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaReference;
import schemacrawler.utility.SerializedCatalogUtility;
import us.fatehi.utility.database.ConnectionInfoBuilder;

public class CatalogIdentityAndAttributesTest {

  @Test
  public void attributeMappingsAreIndependentInBothConstructorPaths() throws Exception {
    final MutableCatalog source = newCatalog();
    source.addSchema(new SchemaReference("CATALOG", "PUBLIC"));
    source.setAttribute("baseline", "before");
    source.setAttribute("removed", "retained");
    source.setRemarks("source remarks");
    final List<Catalog> projections = projections(source);

    source.setAttribute("baseline", "source replacement");
    source.removeAttribute("removed");
    source.setAttribute("source-only", "value");
    source.setRemarks("new source remarks");

    for (final Catalog projection : projections) {
      assertThat(projection.getAttribute("baseline", ""), is("before"));
      assertThat(projection.getAttribute("removed", ""), is("retained"));
      assertThat(projection.hasAttribute("source-only"), is(false));
      assertThat(projection.getRemarks(), is("source remarks"));
      projection.setAttribute("baseline", "projection replacement");
      projection.removeAttribute("removed");
      projection.setAttribute("projection-only", "value");
      projection.setRemarks("projection remarks");
    }

    assertThat(source.getAttribute("baseline", ""), is("source replacement"));
    assertThat(source.hasAttribute("projection-only"), is(false));
    assertThat(source.getRemarks(), is("new source remarks"));
    projections.get(0).removeAttribute("baseline");
    assertThat(projections.get(1).getAttribute("baseline", ""), is("projection replacement"));
  }

  @Test
  public void attributesAndRemarksFollowSharedMutableSemantics() throws Exception {
    final MutableCatalog source = newCatalog();
    source.addSchema(new SchemaReference("CATALOG", "PUBLIC"));
    final List<Catalog> projections = projections(source);
    for (final Catalog catalog : List.of(source, projections.get(0), projections.get(1))) {
      catalog.setAttribute("z", "last");
      catalog.setAttribute("a", "first");
      assertThat(catalog.getAttribute("a", ""), is("first"));
      assertThat(catalog.lookupAttribute("a"), is(Optional.of("first")));
      assertThat(catalog.getAttribute("missing", "default"), is("default"));
      assertThat(catalog.lookupAttribute("missing"), is(Optional.empty()));
      assertThat(catalog.lookupAttribute(null), is(Optional.empty()));

      for (final String blank : new String[] {null, "", " \t "}) {
        catalog.setAttribute(blank, "ignored");
        catalog.removeAttribute(blank);
      }
      assertThat(new ArrayList<>(catalog.getAttributes().keySet()), contains("a", "z"));
      final Map<String, Object> detached = catalog.getAttributes();
      detached.put("a", "detached");
      detached.put("new", "detached");
      detached.remove("z");
      assertThat(catalog.getAttribute("a", ""), is("first"));
      assertThat(catalog.hasAttribute("new"), is(false));
      assertThat(catalog.hasAttribute("z"), is(true));

      catalog.setAttribute("a", null);
      catalog.removeAttribute("z");
      assertThat(catalog.getAttributes().isEmpty(), is(true));
      catalog.setRemarks(" \t remarks \n ");
      assertThat(catalog.getRemarks(), is("remarks"));
      assertThat(catalog.getAttribute("REMARKS", ""), is("remarks"));
      assertThat(catalog.hasRemarks(), is(true));
      catalog.setRemarks(null);
      assertThat(catalog.getRemarks(), is(""));
      assertThat(catalog.hasRemarks(), is(false));
      catalog.setRemarks(" \t ");
      assertThat(catalog.getRemarks(), is(""));
      catalog.setAttribute("REMARKS", 42);
      assertThat(catalog.getRemarks(), is("42"));
      catalog.removeAttribute("REMARKS");
      assertThat(catalog.hasRemarks(), is(false));
    }
  }

  @Test
  public void catalogsKeepReferenceIdentityThroughAttributeAndMembershipMutation()
      throws Exception {
    final MutableCatalog source = newCatalog();
    source.addSchema(new SchemaReference("CATALOG", "PUBLIC"));
    final MutableCatalog other =
        new MutableCatalog("", source.getDatabaseInfo(), source.getJdbcDriverInfo());
    final Catalog projection = CatalogProjectionBuilder.builder(source).build();
    final Catalog otherProjection = CatalogProjectionBuilder.builder(source).build();
    final List<Catalog> catalogs = List.of(source, other, projection, otherProjection);
    final Map<Catalog, Integer> entries = new HashMap<>();
    for (int index = 0; index < catalogs.size(); index++) {
      entries.put(catalogs.get(index), index);
    }
    assertThat(entries.size(), is(4));

    for (final Catalog catalog : catalogs) {
      final int hash = catalog.hashCode();
      assertThat(hash, is(System.identityHashCode(catalog)));
      assertThat(catalog.equals(catalog), is(true));
      assertThat(catalog.equals(null), is(false));
      assertThat(catalog.equals(new Object()), is(false));
      for (final Catalog candidate : catalogs) {
        assertThat(catalog.equals(candidate), is(catalog == candidate));
      }
      catalog.setAttribute("changed", "value");
      catalog.setRemarks("changed remarks");
      assertThat(catalog.hashCode(), is(hash));
    }
    source.addSchema(new SchemaReference("CATALOG", "ADDED"));
    for (int index = 0; index < catalogs.size(); index++) {
      final Catalog catalog = catalogs.get(index);
      assertThat(catalog.hashCode(), is(System.identityHashCode(catalog)));
      assertThat(entries.get(catalog), is(index));
    }
  }

  @Test
  public void namesComparisonAndRenderingStayBlankWithoutImplyingEquality() throws Exception {
    final MutableCatalog source = newCatalog();
    source.addSchema(new SchemaReference("CATALOG", "PUBLIC"));
    final List<Catalog> projections = projections(source);
    for (final Catalog catalog : List.of(source, projections.get(0), projections.get(1))) {
      assertThat(catalog.getName(), is(""));
      assertThat(catalog.getFullName(), is(""));
      assertThat(catalog.toString(), is(""));
      assertThat(catalog.key(), is(new NamedObjectKey("")));
      assertThat(catalog.compareTo(source), is(0));
      assertThat(source.compareTo(catalog), is(0));
      assertThat(catalog.equals(source), is(catalog == source));
    }
  }

  @Test
  public void projectionAttributesDoNotChangeMembershipOrSharedMemberReferences() throws Exception {
    final MutableCatalog source = newCatalog();
    final SchemaReference schema = new SchemaReference("CATALOG", "PUBLIC");
    source.addSchema(schema);
    final MutableTable table = new MutableTable(schema, "table");
    table.setTableType(new TableType("TABLE"));
    source.addTable(table);
    final List<Catalog> projections = projections(source);
    final Catalog projection = projections.get(0);
    final Catalog excludedProjection = projections.get(1);
    projection.setAttribute("attribute", "value");
    projection.setRemarks("remarks");
    excludedProjection.setAttribute("attribute", "value");
    source.addTable(new MutableTable(schema, "later"));
    source.addSchema(new SchemaReference("CATALOG", "LATER"));

    assertThat(projection.getSchemas(), contains(sameInstance(schema)));
    assertThat(projection.getTables(), contains(sameInstance(table)));
    assertThat(projection.lookupTable(schema, "table").orElseThrow(), sameInstance(table));
    assertThat(projection.lookupTable(schema, "later"), is(Optional.empty()));
    assertThat(projection.lookupSchema(schema.getFullName()).orElseThrow(), sameInstance(schema));
    assertThat(excludedProjection.getSchemas(), is(empty()));
    assertThat(excludedProjection.getTables(), is(empty()));
    assertThat(excludedProjection.lookupTable(schema, "table"), is(Optional.empty()));
    table.setRemarks("shared member remarks");
    assertThat(
        projection.lookupTable(schema, "table").orElseThrow().getRemarks(),
        is("shared member remarks"));
    assertThrows(UnsupportedOperationException.class, () -> projection.getTables().clear());
    assertThrows(UnsupportedOperationException.class, () -> projection.getSchemas().clear());
  }

  @Test
  public void serializationRetainsAttributesIdentityAndReferencesWithinTheGraph() throws Exception {
    final MutableCatalog source = newCatalog();
    final SchemaReference schema = new SchemaReference("CATALOG", "PUBLIC");
    source.addSchema(schema);
    final MutableTable table = new MutableTable(schema, "table");
    table.setTableType(new TableType("TABLE"));
    source.addTable(table);
    source.setAttribute("member", table);
    source.setAttribute("shared", new ArrayList<>(List.of("before")));
    source.setRemarks("source remarks");
    final Catalog projection = CatalogProjectionBuilder.builder(source).build();
    projection.setRemarks("projection remarks");

    final ArrayList<Catalog> graph =
        SerializationUtils.clone(new ArrayList<>(List.of(source, projection)));
    final Catalog sourceCopy = graph.get(0);
    final Catalog projectionCopy = graph.get(1);
    assertThat(sourceCopy.equals(projectionCopy), is(false));
    assertThat(projectionCopy.equals(sourceCopy), is(false));
    assertThat(
        projectionCopy.getAttribute("shared"), sameInstance(sourceCopy.getAttribute("shared")));
    final Table memberCopy = projectionCopy.lookupTable(schema, "table").orElseThrow();
    assertThat(memberCopy, sameInstance(sourceCopy.lookupTable(schema, "table").orElseThrow()));
    assertThat(projectionCopy.getAttribute("member"), sameInstance(memberCopy));
    assertThat(
        memberCopy.getSchema(),
        sameInstance(projectionCopy.lookupSchema(schema.getFullName()).orElseThrow()));
    sourceCopy.setAttribute("source-only", "value");
    assertThat(projectionCopy.hasAttribute("source-only"), is(false));

    final List<Catalog> projections = projections(source);
    for (final Catalog catalog : List.of(source, projection, projections.get(1))) {
      final Catalog copy = roundTrip(catalog);
      assertThat(copy.getAttributes(), is(catalog.getAttributes()));
      assertThat(copy.getRemarks(), is(catalog.getRemarks()));
      assertThat(copy.getTables().size(), is(catalog.getTables().size()));
      assertThat(copy.equals(catalog), is(false));
      assertThat(catalog.equals(copy), is(false));
      assertThat(copy.equals(copy), is(true));
      assertThat(copy.equals(null), is(false));
      final int hash = copy.hashCode();
      assertThat(hash, is(System.identityHashCode(copy)));
      final Map<Catalog, String> entries = new HashMap<>();
      entries.put(catalog, "original");
      entries.put(copy, "copy");
      assertThat(entries.size(), is(2));
      copy.setAttribute("post-read", "value");
      copy.setRemarks("post-read remarks");
      assertThat(copy.hashCode(), is(hash));
      assertThat(entries.get(copy), is("copy"));
      assertThat(catalog.hasAttribute("post-read"), is(false));
      if (!copy.getTables().isEmpty()) {
        final Table copyTable = copy.lookupTable(schema, "table").orElseThrow();
        assertThat(copy.getAttribute("member"), sameInstance(copyTable));
      }
    }
  }

  @Test
  public void sharedMutableAttributeValuesAreNotDeepCopied() throws Exception {
    final MutableCatalog source = newCatalog();
    source.addSchema(new SchemaReference("CATALOG", "PUBLIC"));
    final List<String> value = new ArrayList<>(List.of("initial"));
    source.setAttribute("shared", value);
    final List<Catalog> projections = projections(source);
    value.add("changed");
    for (final Catalog projection : projections) {
      assertThat(projection.getAttribute("shared"), sameInstance(value));
      assertThat(projection.<List<String>>getAttribute("shared"), contains("initial", "changed"));
      assertThat(projection.getAttributes().get("shared"), sameInstance(value));
    }
    projections.get(0).setAttribute("shared", List.of("replacement"));
    assertThat(source.getAttribute("shared"), sameInstance(value));
    assertThat(projections.get(1).getAttribute("shared"), sameInstance(value));
  }

  private static MutableCatalog newCatalog() throws Exception {
    final Connection connection = mockConnection();
    final ConnectionInfoBuilder connectionInfoBuilder = ConnectionInfoBuilder.builder(connection);
    final MutableDatabaseInfo databaseInfo =
        new MutableDatabaseInfo(connectionInfoBuilder.buildDatabaseInformation());
    final MutableJdbcDriverInfo jdbcDriverInfo =
        new MutableJdbcDriverInfo(connectionInfoBuilder.buildJdbcDriverInformation());
    return new MutableCatalog("", databaseInfo, jdbcDriverInfo);
  }

  private static List<Catalog> projections(final Catalog source) {
    return List.of(
        CatalogProjectionBuilder.builder(source).build(),
        CatalogProjectionBuilder.builder(source)
            .withOptions(
                SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
                    .withLimitOptions(
                        LimitOptionsBuilder.builder()
                            .includeSchemas(Pattern.compile("NEVER_MATCH"))
                            .toOptions()))
            .build());
  }

  private static Catalog roundTrip(final Catalog catalog) {
    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    SerializedCatalogUtility.saveCatalog(catalog, output);
    return SerializedCatalogUtility.readCatalog(new ByteArrayInputStream(output.toByteArray()));
  }
}
