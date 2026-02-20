package io.github.aytronnfr.jackson.jts;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

public class JtsModule extends SimpleModule {
  private static final long serialVersionUID = 1L;

  private final GeometryFactory geometryFactory;
  private final IncludeBoundingBox includeBoundingBox;
  private final int decimalPlaces;

  public JtsModule() {
    this(null, null, GeometrySerializer.DEFAULT_DECIMAL_PLACES);
  }

  public JtsModule(int decimalPlaces) {
    this(null, null, decimalPlaces);
  }

  public JtsModule(GeometryFactory geometryFactory) {
    this(geometryFactory, null, GeometrySerializer.DEFAULT_DECIMAL_PLACES);
  }

  public JtsModule(GeometryFactory geometryFactory, int decimalPlaces) {
    this(geometryFactory, null, decimalPlaces);
  }

  public JtsModule(IncludeBoundingBox includeBoundingBox) {
    this(null, includeBoundingBox, GeometrySerializer.DEFAULT_DECIMAL_PLACES);
  }

  public JtsModule(IncludeBoundingBox includeBoundingBox, int decimalPlaces) {
    this(null, includeBoundingBox, decimalPlaces);
  }

  public JtsModule(GeometryFactory geometryFactory, IncludeBoundingBox includeBoundingBox) {
    this(geometryFactory, includeBoundingBox, GeometrySerializer.DEFAULT_DECIMAL_PLACES);
  }

  public JtsModule(GeometryFactory geometryFactory, IncludeBoundingBox includeBoundingBox, int decimalPlaces) {
    super(); // Jackson 3: pas besoin de VersionInfo, optionnel
    this.geometryFactory = geometryFactory;
    this.includeBoundingBox = includeBoundingBox;
    if (decimalPlaces < 0) throw new IllegalArgumentException("decimalPlaces < 0");
    this.decimalPlaces = decimalPlaces;
  }

  @Override
  public void setupModule(SetupContext context) {
    var deserializer = getDeserializer();

    addSerializer(Geometry.class, getSerializer());
    addDeserializer(Geometry.class, deserializer);

    addDeserializer(Point.class, new TypeSafeGeometryDeserializer<>(Point.class, deserializer));
    addDeserializer(LineString.class, new TypeSafeGeometryDeserializer<>(LineString.class, deserializer));
    addDeserializer(Polygon.class, new TypeSafeGeometryDeserializer<>(Polygon.class, deserializer));
    addDeserializer(MultiPoint.class, new TypeSafeGeometryDeserializer<>(MultiPoint.class, deserializer));
    addDeserializer(MultiLineString.class, new TypeSafeGeometryDeserializer<>(MultiLineString.class, deserializer));
    addDeserializer(MultiPolygon.class, new TypeSafeGeometryDeserializer<>(MultiPolygon.class, deserializer));
    addDeserializer(GeometryCollection.class, new TypeSafeGeometryDeserializer<>(GeometryCollection.class, deserializer));

    super.setupModule(context);
  }

  private ValueSerializer<Geometry> getSerializer() {
    return new GeometrySerializer(this.includeBoundingBox, this.decimalPlaces);
  }

  private ValueDeserializer<Geometry> getDeserializer() {
    return new GeometryDeserializer(this.geometryFactory);
  }
}
