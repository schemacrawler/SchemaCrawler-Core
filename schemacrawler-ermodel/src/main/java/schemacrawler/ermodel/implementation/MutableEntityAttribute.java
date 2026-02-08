package schemacrawler.ermodel.implementation;

import static java.util.Objects.requireNonNull;

import java.io.Serial;
import java.util.List;
import schemacrawler.ermodel.model.EntityAttribute;
import schemacrawler.ermodel.model.EntityAttributeType;
import schemacrawler.ermodel.model.TableBacked;
import schemacrawler.ermodel.utility.EntityModelUtility;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnDataType;

class MutableEntityAttribute extends AbstractDatabaseObjectBacked<Column>
    implements EntityAttribute {

  @Serial private static final long serialVersionUID = 7349443487412594755L;

  private final TableBacked parent;
  private final EntityAttributeType type;
  private final List<String> enumValues;

  MutableEntityAttribute(final TableBacked parent, final Column column) {
    super(column);

    this.parent = requireNonNull(parent, "No parent provided");

    final ColumnDataType columnDataType = column.getColumnDataType();
    type = EntityModelUtility.inferEntityAttributeType(columnDataType);
    enumValues = columnDataType.getEnumValues();
  }

  @Override
  public String getDefaultValue() {
    return getDatabaseObject().getDefaultValue();
  }

  @Override
  public List<String> getEnumValues() {
    return List.copyOf(enumValues);
  }

  @Override
  public TableBacked getParent() {
    return parent;
  }

  @Override
  public EntityAttributeType getType() {
    return type;
  }

  @Override
  public boolean hasDefaultValue() {
    return getDatabaseObject().hasDefaultValue();
  }

  @Override
  public boolean isRequired() {
    return !getDatabaseObject().isNullable();
  }
}
