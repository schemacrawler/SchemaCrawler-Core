/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.implementation;

import static java.util.Objects.requireNonNull;

import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.crawl.ImplicitAssociationBuilder;
import schemacrawler.crawl.ImplicitAssociationBuilder.ImplicitAssociationColumn;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.ermodel.model.Entity;
import schemacrawler.ermodel.model.RelationshipCardinality;
import schemacrawler.ermodel.model.TableReferenceRelationship;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableReference;
import us.fatehi.utility.Builder;
import us.fatehi.utility.string.StringFormat;

public final class ImplicitRelationshipBuilder implements Builder<TableReferenceRelationship> {

  private static final Logger LOGGER =
      Logger.getLogger(ImplicitRelationshipBuilder.class.getName());

  public static ImplicitRelationshipBuilder builder(final Catalog catalog, final ERModel erModel) {
    return new ImplicitRelationshipBuilder(catalog, erModel);
  }

  private final ImplicitAssociationBuilder implicitAssociationBuilder;
  private final MutableERModel erModel;

  private ImplicitRelationshipBuilder(final Catalog catalog, final ERModel erModel) {
    implicitAssociationBuilder = ImplicitAssociationBuilder.builder(catalog);
    requireNonNull(erModel, "No ER model provided");
    this.erModel = (MutableERModel) erModel;
  }

  public ImplicitRelationshipBuilder addColumnReference(
      final Column fkColumn, final Column pkColumn) {
    implicitAssociationBuilder.addColumnReference(fkColumn, pkColumn);
    return this;
  }

  public ImplicitRelationshipBuilder addColumnReference(
      final ImplicitAssociationColumn referencingColumn,
      final ImplicitAssociationColumn referencedColumn) {
    implicitAssociationBuilder.addColumnReference(referencingColumn, referencedColumn);
    return this;
  }

  @Override
  public TableReferenceRelationship build() {
    final TableReference implicitAssociation = implicitAssociationBuilder.build();
    if (implicitAssociation == null) {
      return null;
    }

    final TableReferenceRelationship implicitRelationship =
        addTableReferenceRelationship(implicitAssociation);
    return implicitRelationship;
  }

  public ImplicitRelationshipBuilder withName(final String weakAssociationName) {
    implicitAssociationBuilder.withName(weakAssociationName);
    return this;
  }

  /**
   * Adds an implicit association derived from the given column reference to the ER model.
   *
   * <p>The association is skipped if either the FK or PK table entity is not found in the model, or
   * if the FK table is inferred to be a bridge table.
   *
   * @param implicitAssociation Implicit column association describing the association
   */
  private TableReferenceRelationship addTableReferenceRelationship(
      final TableReference implicitAssociation) {
    if (implicitAssociation == null) {
      return null;
    }

    final Table leftTable = implicitAssociation.getForeignKeyTable();
    final Table rightTable = implicitAssociation.getPrimaryKeyTable();
    final Entity leftEntity = erModel.lookupEntity(leftTable).orElse(null);
    final Entity rightEntity = erModel.lookupEntity(rightTable).orElse(null);

    if (implicitAssociation instanceof ForeignKey) {
      LOGGER.log(
          Level.FINE,
          new StringFormat(
              "Implicit association <%s> not modeled from foreign key <%s> -> <%s>",
              implicitAssociation, leftTable, rightTable));
      final MutableTableReferenceRelationship rel =
          new MutableTableReferenceRelationship(implicitAssociation);
      return rel;
    }

    final TableEntityModelInferrer modelInferrer = new TableEntityModelInferrer(leftTable);
    if (modelInferrer.inferBridgeTable()) {
      LOGGER.log(
          Level.FINE,
          new StringFormat(
              "Implicit association <%s> not built from bridge table <%s>",
              implicitAssociation, leftTable));
      return addUnmodeled(implicitAssociation);
    }

    if (leftEntity == null || rightEntity == null) {
      LOGGER.log(
          Level.FINE,
          new StringFormat(
              "Implicit association <%s> cannot be modeled from <%s> -> <%s>",
              implicitAssociation, leftTable, rightTable));
      return addUnmodeled(implicitAssociation);
    }

    final MutableTableReferenceRelationship rel =
        new MutableTableReferenceRelationship(implicitAssociation);
    final RelationshipCardinality cardinality = modelInferrer.inferCardinality(implicitAssociation);
    rel.setCardinality(cardinality);
    rel.setEntities(leftEntity, rightEntity);

    erModel.addImplicitRelationship(rel);
    return rel;
  }

  private TableReferenceRelationship addUnmodeled(final TableReference implicitAssociation) {
    final MutableTableReferenceRelationship rel =
        new MutableTableReferenceRelationship(implicitAssociation);
    erModel.addUnmodeledTableReference(implicitAssociation);
    return rel;
  }
}
