# SchemaCrawler Core Change History

SchemaCrawler Core release notes.

<a name="v17.4.0"></a>
## Release 17.4.0 - 2026-01-16
- Infer 1..1 and 1..many cardinalities - fixes [issue #2237](https://github.com/schemacrawler/SchemaCrawler/issues/2237)
- Add methods to identify table self-references
  - `Table::isSelfReferencing()`
  - `Column::isPartOfSelfReferencingRelationship()`
  - `TableReference::isSelfReferencing()`
- Add a number of entity model inferences to the catalog model
  - `Column::isSignificant()`
  - `TableReference::isOptional()`
- Add `EntityModelUtility` with methods to obtain information useful in entity modeling


<a name="v17.3.0"></a>
## Release 17.3.0 - 2025-12-31
- Milestone release with modularity


<a name="v17.2.8"></a>
## Release 17.2.8 - 2025-12-30

- Allow reflection on schema model classes
- Clean up style issues


<a name="v17.2.7"></a>
## Release 17.2.7 - 2025-12-29

- Return immutable collections from the `Catalog` schema model
- Run tests using the module path rather than the classpath


<a name="v17.2.6"></a>
## Release 17.2.6 - 2025-12-27

- Split Core into a separate project and apply JPMS package permissions
- Split text and diagram modules into separate jars
