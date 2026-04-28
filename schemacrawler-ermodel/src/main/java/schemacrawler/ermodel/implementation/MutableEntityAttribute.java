package schemacrawler.ermodel.implementation;

import static java.util.Objects.requireNonNull;

import java.io.Serial;
import java.util.List;
import schemacrawler.ermodel.model.EntityAttribute;
import schemacrawler.ermodel.model.EntityAttributeType;
import schemacrawler.ermodel.model.TableBacked;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.PartialDatabaseObject;

final class MutableEntityAttribute extends AbstractDatabaseObjectBacked<Column>
    implements EntityAttribute {

  @Serial private static final long serialVersionUID = 7349443487412594755L;

  private final TableBacked parent;
  private final boolean isPartial;
  private final EntityAttributeType type;
  private final List<String> enumValues;

  MutableEntityAttribute(final TableBacked parent, final Column column) {
    super(column);

    this.parent = requireNonNull(parent, "No parent provided");

    final ColumnDataType columnDataType = column.getColumnDataType();
    type = EntityAttributeType.from(columnDataType);
    enumValues = columnDataType.getEnumValues();
    isPartial = column instanceof PartialDatabaseObject;
  }

  @Override
  public String getDefaultValue() {
    if (isPartial) {
      return null;
    }
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
    return getDefaultValue() != null;
  }

  @Override
  public boolean isRequired() {
    if (isPartial) {
      return false;
    }
    return !getDatabaseObject().isNullable();
  }
}
