/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static schemacrawler.test.utility.DatabaseTestUtility.getCatalog;
import static schemacrawler.test.utility.DatabaseTestUtility.schemaCrawlerOptionsWithMaximumSchemaInfoLevel;
import static schemacrawler.test.utility.DatabaseTestUtility.validateSchema;
import static us.fatehi.test.utility.extensions.FileHasContent.classpathResource;
import static us.fatehi.test.utility.extensions.FileHasContent.hasSameContentAs;
import static us.fatehi.test.utility.extensions.FileHasContent.outputOf;

import java.sql.Connection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.ermodel.model.Entity;
import schemacrawler.ermodel.model.EntityAttribute;
import schemacrawler.ermodel.model.EntitySubtype;
import schemacrawler.ermodel.model.EntityType;
import schemacrawler.ermodel.model.ManyToManyRelationship;
import schemacrawler.ermodel.model.Relationship;
import schemacrawler.ermodel.utility.EntityModelUtility;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.test.utility.WithTestDatabase;
import us.fatehi.test.utility.TestWriter;
import us.fatehi.test.utility.extensions.ResolveTestContext;
import us.fatehi.test.utility.extensions.TestContext;

@WithTestDatabase
@ResolveTestContext
@TestInstance(Lifecycle.PER_CLASS)
public class ERModelTest {

  private Catalog catalog;
  private ERModel erModel;

  @Test
  public void attributes(final TestContext testContext) {
    final TestWriter testout = new TestWriter();
    try (final TestWriter out = testout) {
      out.println("# Entity attributes:");
      for (final Entity entity : erModel.getEntities()) {
        out.println("- %s".formatted(entity));
        for (EntityAttribute entityAttribute : entity.getEntityAttributes()) {
          out.println(
              "  - %s [%s]".formatted(entityAttribute.getName(), entityAttribute.getType()));
          out.println("    - optional: %b".formatted(entityAttribute.isOptional()));
          out.println("    - default: %s".formatted(entityAttribute.getDefaultValue()));
          out.println("    - enum: %s".formatted(entityAttribute.getEnumValues()));
        }
      }
    }
    assertThat(
        outputOf(testout),
        hasSameContentAs(classpathResource(testContext.testMethodFullName() + ".txt")));
  }

  @Test
  public void entities(final TestContext testContext) {
    final TestWriter testout = new TestWriter();
    try (final TestWriter out = testout) {
      out.println("# Entities:");
      for (final Entity entity : erModel.getEntities()) {
        out.println("- %s [%s]".formatted(entity, entity.getType()));
        if (entity instanceof final EntitySubtype subentity) {
          out.println("  - super-type: %s".formatted(subentity.getSupertype()));
        }
      }
    }
    assertThat(
        outputOf(testout),
        hasSameContentAs(classpathResource(testContext.testMethodFullName() + ".txt")));
  }

  @BeforeAll
  public void loadCatalog(final Connection connection) {
    final SchemaCrawlerOptions schemaCrawlerOptions =
        schemaCrawlerOptionsWithMaximumSchemaInfoLevel;
    try {
      catalog = getCatalog(connection, schemaCrawlerOptions);
    } catch (final Exception e) {
      fail("Catalog not loaded", e);
    }
    validateSchema(catalog);

    erModel = EntityModelUtility.buildERModel(catalog);
  }

  @Test
  public void nonEntities(final TestContext testContext) {
    final TestWriter testout = new TestWriter();
    try (final TestWriter out = testout) {
      out.println("# Non-entities:");
      for (final Entity entity : erModel.getEntitiesByType(EntityType.non_entity)) {
        out.println("- %s [%s]".formatted(entity, entity.getType()));
      }
    }
    assertThat(
        outputOf(testout),
        hasSameContentAs(classpathResource(testContext.testMethodFullName() + ".txt")));
  }

  @Test
  public void relationships(final TestContext testContext) {
    final TestWriter testout = new TestWriter();
    try (final TestWriter out = testout) {
      out.println("# Relationships:");
      for (final Relationship relationship : erModel.getRelationships()) {
        out.println("- %s [%s]".formatted(relationship, relationship.getType()));
        out.println("  - left: %s".formatted(relationship.getLeftEntity()));
        out.println("  - right: %s".formatted(relationship.getRightEntity()));
        if (relationship instanceof final ManyToManyRelationship mnRel) {
          out.println("  - bridge: %s".formatted(mnRel.getBridgeTable()));
        }
      }
    }
    assertThat(
        outputOf(testout),
        hasSameContentAs(classpathResource(testContext.testMethodFullName() + ".txt")));
  }

  @Test
  public void unknownEntities(final TestContext testContext) {
    final TestWriter testout = new TestWriter();
    try (final TestWriter out = testout) {
      out.println("# Unknown entities:");
      for (final Relationship relationship : erModel.getRelationships()) {
        out.println("- %s [%s]".formatted(relationship, relationship.getType()));
        out.println("  - left: %s".formatted(relationship.getLeftEntity()));
        out.println("  - right: %s".formatted(relationship.getRightEntity()));
      }
    }
    assertThat(
        outputOf(testout),
        hasSameContentAs(classpathResource(testContext.testMethodFullName() + ".txt")));
  }

  @Test
  public void unmodeled(final TestContext testContext) {
    final TestWriter testout = new TestWriter();
    try (final TestWriter out = testout) {
      out.println("# Unmodeled tables:");
      for (final Table table : erModel.getUnmodeledTables()) {
        out.println("- %s".formatted(table));
      }
    }
    assertThat(
        outputOf(testout),
        hasSameContentAs(classpathResource(testContext.testMethodFullName() + ".txt")));
  }

  @Test
  public void weakRelationships(final TestContext testContext) {
    final TestWriter testout = new TestWriter();
    try (final TestWriter out = testout) {
      out.println("# Weak relationships:");
      for (final Relationship relationship : erModel.getWeakRelationships()) {
        out.println("- %s [%s]".formatted(relationship, relationship.getType()));
        out.println("  - left: %s".formatted(relationship.getLeftEntity()));
        out.println("  - right: %s".formatted(relationship.getRightEntity()));
      }
    }
    assertThat(
        outputOf(testout),
        hasSameContentAs(classpathResource(testContext.testMethodFullName() + ".txt")));
  }
}
