/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.implementation;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.ermodel.associations.ImplicitAssociation;
import schemacrawler.ermodel.associations.ImplicitAssociationsAnalyzer;
import schemacrawler.ermodel.associations.ImplicitAssociationsAnalyzerBuilder;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.ermodel.model.Entity;
import schemacrawler.ermodel.model.RelationshipCardinality;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.Table;
import us.fatehi.utility.Builder;
import us.fatehi.utility.string.StringFormat;

/** Builder for adding implicit associations to an ER model. */
public final class ImplicitAssociationBuilder implements Builder<ERModel> {

  private static final Logger LOGGER = Logger.getLogger(ImplicitAssociationBuilder.class.getName());

  /**
   * Creates a new builder for adding implicit associations to the given ER model.
   *
   * @param erModel Mutable ER model to enrich
   * @return New builder instance
   */
  public static ImplicitAssociationBuilder builder(final ERModel erModel) {
    requireNonNull(erModel, "No ER model provided");
    if (!(erModel instanceof final MutableERModel mutableERModel)) {
      throw new IllegalArgumentException("Cannot add implicit associations to ER model");
    }
    return new ImplicitAssociationBuilder(mutableERModel);
  }

  private final MutableERModel erModel;
  private ImplicitAssociationsAnalyzer implicitAssociationsAnalyzer;

  private ImplicitAssociationBuilder(final MutableERModel erModel) {
    this.erModel = erModel;
    implicitAssociationsAnalyzer = buildDefaultImplicitAssociationAnalyzer();
  }

  @Override
  public ERModel build() {
    final Collection<ColumnReference> implicitColumnReferences =
        implicitAssociationsAnalyzer.analyzeTables();
    if (implicitColumnReferences == null || implicitColumnReferences.isEmpty()) {
      return erModel;
    }

    for (final ColumnReference implicitColumnReference : implicitColumnReferences) {
      LOGGER.log(
          Level.INFO,
          new StringFormat(
              "Adding implicit association <%s> to ER model", implicitColumnReference));
      addImplicitColumnReference(implicitColumnReference);
    }

    return erModel;
  }

  public ImplicitAssociationBuilder withImplicitAssociationsAnalyzer(
      final ImplicitAssociationsAnalyzer implicitAssociationsAnalyzer) {
    if (implicitAssociationsAnalyzer != null) {
      this.implicitAssociationsAnalyzer = implicitAssociationsAnalyzer;
    }
    return this;
  }

  /**
   * Adds an implicit association derived from the given column reference to the ER model.
   *
   * <p>The association is skipped if either the FK or PK table entity is not found in the model, or
   * if the FK table is inferred to be a bridge table.
   *
   * @param columnReference Implicit column reference describing the association
   */
  private void addImplicitColumnReference(final ColumnReference columnReference) {
    if (columnReference == null) {
      return;
    }

    final ImplicitAssociation implicitAssociation = new ImplicitAssociation(columnReference);

    final Table leftTable = implicitAssociation.getForeignKeyTable();
    final TableEntityModelInferrer modelInferrer = new TableEntityModelInferrer(leftTable);
    if (modelInferrer.inferBridgeTable()) {
      LOGGER.log(
          Level.FINE,
          new StringFormat(
              "Implicit association <%s> not built from bridge table <%s>",
              implicitAssociation, leftTable));
      return;
    }

    final Table rightTable = implicitAssociation.getPrimaryKeyTable();
    final Entity leftEntity = erModel.lookupEntity(leftTable).orElse(null);
    final Entity rightEntity = erModel.lookupEntity(rightTable).orElse(null);

    if (leftEntity == null || rightEntity == null) {
      LOGGER.log(
          Level.FINE,
          new StringFormat(
              "Implicit association <%s> cannot be built from <%s> -> <%s>",
              implicitAssociation, leftTable, rightTable));
      return;
    }

    final MutableTableReferenceRelationship rel =
        new MutableTableReferenceRelationship(implicitAssociation);
    final RelationshipCardinality cardinality = modelInferrer.inferCardinality(implicitAssociation);
    rel.setCardinality(cardinality);
    rel.setEntities(leftEntity, rightEntity);

    erModel.addImplicitRelationship(rel);
  }

  private ImplicitAssociationsAnalyzer buildDefaultImplicitAssociationAnalyzer() {
    return ImplicitAssociationsAnalyzerBuilder.builder(erModel.getTables())
        .withIdMatcher()
        .withExtensionTableMatcher()
        .build();
  }
}
