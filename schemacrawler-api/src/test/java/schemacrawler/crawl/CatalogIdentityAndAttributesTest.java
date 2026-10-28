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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;
import schemacrawler.filter.CatalogProjectionBuilder;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.DataTypeType;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableType;
import schemacrawler.schemacrawler.LimitOptions;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaReference;
import schemacrawler.utility.SerializedCatalogUtility;
import us.fatehi.utility.database.ConnectionInfoBuilder;

public class CatalogIdentityAndAttributesTest {

  @Test
  public void readOperationsStayEquivalentAcrossCatalogImplementations() throws Exception {
    final MutableCatalog source = newCatalog();
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final Schema otherSchema = new SchemaReference("CATALOG", "OTHER");
    source.addSchema(schema);
    source.addSchema(otherSchema);
    final MutableTable table = new MutableTable(schema, "TABLE");
    table.setTableType(new TableType("TABLE"));
    final MutableColumn column = new MutableColumn(table, "COLUMN");
    table.addColumn(column);
    source.addTable(table);
    final MutableProcedure later = new MutableProcedure(schema, "run", "z");
    final MutableProcedure earlier = new MutableProcedure(schema, "run", "a");
    source.addRoutine(later);
    source.addRoutine(earlier);
    final MutableSequence sequence = new MutableSequence(schema, "SEQUENCE");
    source.addSequence(sequence);
    final MutableSynonym synonym = new MutableSynonym(schema, "SYNONYM");
    source.addSynonym(synonym);
    final MutableColumnDataType dataType =
        new MutableColumnDataType(schema, "TYPE", DataTypeType.user_defined);
    final MutableColumnDataType systemType =
        new MutableColumnDataType(new SchemaReference(), "SYSTEM", DataTypeType.system);
    source.addColumnDataType(dataType);
    source.addColumnDataType(systemType);
    final ImmutableDatabaseUser user = new ImmutableDatabaseUser("USER");
    source.addDatabaseUser(user);

    final Optional<MutableTable> mutableTable = source.lookupTable(schema, "TABLE");
    final Optional<MutableRoutine> mutableRoutine = source.lookupRoutine(schema, "run");
    final Optional<MutableColumnDataType> mutableType = source.lookupColumnDataType(schema, "TYPE");
    final Optional<MutableColumnDataType> mutableSystemType =
        source.lookupSystemColumnDataType("SYSTEM");
    final Optional<MutableSequence> mutableSequence = source.lookupSequence(schema, "SEQUENCE");
    final Optional<MutableSynonym> mutableSynonym = source.lookupSynonym(schema, "SYNONYM");
    assertThat(mutableTable.orElseThrow(), sameInstance(table));
    assertThat(mutableRoutine.orElseThrow(), sameInstance(earlier));
    assertThat(mutableType.orElseThrow(), sameInstance(dataType));
    assertThat(mutableSystemType.orElseThrow(), sameInstance(systemType));
    assertThat(mutableSequence.orElseThrow(), sameInstance(sequence));
    assertThat(mutableSynonym.orElseThrow(), sameInstance(synonym));

    final LimitOptions limitOptions =
        LimitOptionsBuilder.builder()
            .includeAllRoutines()
            .includeAllSequences()
            .includeAllSynonyms()
            .build();
    final SchemaCrawlerOptions options =
        SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions().withLimitOptions(limitOptions);
    final Catalog projection =
        CatalogProjectionBuilder.builder(source).withOptions(options).build();

    for (final Catalog catalog : List.of(source, projection)) {
      final Optional<? extends Table> publicTable = catalog.lookupTable(schema, "TABLE");
      final Optional<? extends ColumnDataType> publicType =
          catalog.lookupColumnDataType(schema, "TYPE");
      assertThat(publicTable.orElseThrow(), sameInstance(table));
      assertThat(publicType.orElseThrow(), sameInstance(dataType));
      assertThat(catalog.getSchemas(), contains(otherSchema, schema));
      assertThat(catalog.getTables(), contains(sameInstance(table)));
      assertThat(catalog.getTables(schema), contains(sameInstance(table)));
      assertThat(catalog.getRoutines(), contains(earlier, later));
      assertThat(catalog.getRoutines(schema), contains(earlier, later));
      assertThat(catalog.getRoutines(schema, "run"), contains(earlier, later));
      assertThat(catalog.getSequences(), contains(sameInstance(sequence)));
      assertThat(catalog.getSequences(schema), contains(sameInstance(sequence)));
      assertThat(catalog.getSynonyms(), contains(sameInstance(synonym)));
      assertThat(catalog.getSynonyms(schema), contains(sameInstance(synonym)));
      assertThat(catalog.getColumnDataTypes(schema), contains(sameInstance(dataType)));
      assertThat(catalog.getSystemColumnDataTypes(), contains(sameInstance(systemType)));
      assertThat(
          List.copyOf(catalog.getColumnDataTypes()), is(List.copyOf(source.getColumnDataTypes())));
      assertThat(catalog.getDatabaseUsers(), contains(sameInstance(user)));
      assertThat(catalog.getCrawlInfo(), sameInstance(source.getCrawlInfo()));
      assertThat(catalog.getDatabaseInfo(), sameInstance(source.getDatabaseInfo()));
      assertThat(catalog.getJdbcDriverInfo(), sameInstance(source.getJdbcDriverInfo()));
      assertThat(catalog.lookupSchema(schema.getFullName()).orElseThrow(), sameInstance(schema));
      assertThat(catalog.lookupTable(schema, "TABLE").orElseThrow(), sameInstance(table));
      assertThat(
          catalog.lookupColumn(schema, "TABLE", "COLUMN").orElseThrow(), sameInstance(column));
      assertThat(
          catalog.lookupColumnDataType(schema, "TYPE").orElseThrow(), sameInstance(dataType));
      assertThat(
          catalog.lookupSystemColumnDataType("SYSTEM").orElseThrow(), sameInstance(systemType));
      assertThat(catalog.lookupRoutine(schema, "run").orElseThrow(), sameInstance(earlier));
      assertThat(catalog.lookupSequence(schema, "SEQUENCE").orElseThrow(), sameInstance(sequence));
      assertThat(catalog.lookupSynonym(schema, "SYNONYM").orElseThrow(), sameInstance(synonym));

      for (final Collection<?> values :
          List.of(
              catalog.getSchemas(),
              catalog.getTables(),
              catalog.getTables(schema),
              catalog.getRoutines(),
              catalog.getRoutines(schema),
              catalog.getRoutines(schema, "run"),
              catalog.getSequences(),
              catalog.getSequences(schema),
              catalog.getSynonyms(),
              catalog.getSynonyms(schema),
              catalog.getColumnDataTypes(),
              catalog.getColumnDataTypes(schema),
              catalog.getSystemColumnDataTypes(),
              catalog.getDatabaseUsers())) {
        assertThrows(UnsupportedOperationException.class, values::clear);
      }
      for (final Schema absentSchema : new Schema[] {null, otherSchema}) {
        assertThat(catalog.getTables(absentSchema), is(empty()));
        assertThat(catalog.getRoutines(absentSchema), is(empty()));
        assertThat(catalog.getSequences(absentSchema), is(empty()));
        assertThat(catalog.getSynonyms(absentSchema), is(empty()));
        assertThat(catalog.getColumnDataTypes(absentSchema), is(empty()));
        assertThat(catalog.lookupTable(absentSchema, "TABLE"), is(Optional.empty()));
        assertThat(catalog.lookupRoutine(absentSchema, "run"), is(Optional.empty()));
        assertThat(catalog.lookupSequence(absentSchema, "SEQUENCE"), is(Optional.empty()));
        assertThat(catalog.lookupSynonym(absentSchema, "SYNONYM"), is(Optional.empty()));
        assertThat(catalog.lookupColumnDataType(absentSchema, "TYPE"), is(Optional.empty()));
        assertThat(catalog.lookupColumn(absentSchema, "TABLE", "COLUMN"), is(Optional.empty()));
      }
      for (final String absentName : new String[] {null, "", " \t ", "MISSING"}) {
        assertThat(catalog.lookupSchema(absentName), is(Optional.empty()));
        assertThat(catalog.lookupTable(schema, absentName), is(Optional.empty()));
        assertThat(catalog.lookupRoutine(schema, absentName), is(Optional.empty()));
        assertThat(catalog.getRoutines(schema, absentName), is(empty()));
        assertThat(catalog.lookupSequence(schema, absentName), is(Optional.empty()));
        assertThat(catalog.lookupSynonym(schema, absentName), is(Optional.empty()));
        assertThat(catalog.lookupColumnDataType(schema, absentName), is(Optional.empty()));
        assertThat(catalog.lookupSystemColumnDataType(absentName), is(Optional.empty()));
        assertThat(catalog.lookupColumn(schema, "TABLE", absentName), is(Optional.empty()));
        assertThat(catalog.lookupColumn(schema, absentName, "COLUMN"), is(Optional.empty()));
      }
    }
    for (final Catalog original : List.of(source, projection)) {
      final Catalog copy = roundTrip(original);
      assertThat(copy.getSystemColumnDataTypes().size(), is(1));
      assertThat(
          copy.lookupSystemColumnDataType("SYSTEM").orElseThrow(),
          sameInstance(copy.getSystemColumnDataTypes().iterator().next()));
      assertThat(copy.lookupSystemColumnDataType("TYPE"), is(Optional.empty()));
    }
    final Collection<Table> previous = source.getTables();
    final MutableTable added = new MutableTable(schema, "ADDED");
    added.setTableType(new TableType("table"));
    source.addTable(added);
    assertThat(previous, contains(sameInstance(table)));
    assertThat(source.getTables(), contains(added, table));
    assertThat(projection.getTables(), contains(sameInstance(table)));
  }

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
