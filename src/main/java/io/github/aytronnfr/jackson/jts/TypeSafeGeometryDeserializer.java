package io.github.aytronnfr.jackson.jts;

import java.util.Objects;
import org.locationtech.jts.geom.Geometry;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.exc.InvalidDefinitionException;
import tools.jackson.databind.type.TypeFactory;

public class TypeSafeGeometryDeserializer<T extends Geometry> extends ValueDeserializer<T> {

  private final ValueDeserializer<Geometry> delegate;
  private final JavaType type;

  public TypeSafeGeometryDeserializer(Class<T> clazz, ValueDeserializer<Geometry> delegate) {
    this.delegate = Objects.requireNonNull(delegate);
    this.type = TypeFactory.createDefaultInstance().constructType(Objects.requireNonNull(clazz));
  }

  @Override
  public T deserialize(JsonParser p, DeserializationContext context)
      throws DatabindException {
    Object obj = delegate.deserialize(p, context);
    if (obj == null) {
      return null;
    } else if (type.isTypeOrSuperTypeOf(obj.getClass())) {
      return (T) obj;
    } else {
      throw InvalidDefinitionException.from(context, String.format("Invalid type for %s: %s", type, obj.getClass()));
    }
  }
}
