package io.github.aytronn.jackson.jts;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.exc.InvalidDefinitionException;

public class GeometrySerializer extends ValueSerializer<Geometry> {
  static final int DEFAULT_DECIMAL_PLACES = 8;

  private final NumberFormat decimalFormat;
  private final IncludeBoundingBox includeBoundingBox;

  public GeometrySerializer() {
    this(null, DEFAULT_DECIMAL_PLACES);
  }

  @Override
  public void serialize(Geometry geometry, JsonGenerator generator, SerializationContext provider)
      throws JacksonException {
    if (geometry == null) {
      generator.writeNull();
    } else if (geometry instanceof Polygon p) {
      serialize(p, generator, provider);
    } else if (geometry instanceof Point p) {
      serialize(p, generator, provider);
    } else if (geometry instanceof MultiPoint mp) {
      serialize(mp, generator, provider);
    } else if (geometry instanceof MultiPolygon mp) {
      serialize(mp, generator, provider);
    } else if (geometry instanceof LineString ls) {
      serialize(ls, generator, provider);
    } else if (geometry instanceof MultiLineString mls) {
      serialize(mls, generator, provider);
    } else if (geometry instanceof GeometryCollection gc) {
      serialize(gc, generator, provider);
    } else {
      throw InvalidDefinitionException.from(generator, String.format("Geometry type %s is not supported.", geometry.getClass().getName()));
    }
  }

  public GeometrySerializer(IncludeBoundingBox includeBoundingBox) {
    this(includeBoundingBox, DEFAULT_DECIMAL_PLACES);
  }

  public GeometrySerializer(IncludeBoundingBox includeBoundingBox, int decimalPlaces) {
    this.includeBoundingBox = Optional.ofNullable(includeBoundingBox).orElseGet(IncludeBoundingBox::never);
    if (decimalPlaces < 0) throw new IllegalArgumentException("decimalPlaces < 0");
    this.decimalFormat = createNumberFormat(decimalPlaces);
  }

  private NumberFormat createNumberFormat(int decimalPlaces) {
    var format = DecimalFormat.getInstance(Locale.ROOT);
    format.setRoundingMode(RoundingMode.HALF_UP);
    format.setMinimumFractionDigits(0);
    format.setMaximumFractionDigits(decimalPlaces);
    format.setGroupingUsed(false);
    return format;
  }

  @Override
  public Class<Geometry> handledType() {
    return Geometry.class;
  }

  private void serialize(GeometryCollection value, JsonGenerator generator, SerializationContext provider) {
    generator.writeStartObject();
    serializeTypeAndBoundingBox(GeometryType.GEOMETRY_COLLECTION, value, generator);

    generator.writeArrayPropertyStart(Field.GEOMETRIES);
    for (int i = 0; i != value.getNumGeometries(); ++i) {
      serialize(value.getGeometryN(i), generator, provider);
    }
    generator.writeEndArray();
    generator.writeEndObject();
  }

  private void serialize(MultiPoint value, JsonGenerator generator, SerializationContext provider) {
    generator.writeStartObject();
    serializeTypeAndBoundingBox(GeometryType.MULTI_POINT, value, generator);

    generator.writeArrayPropertyStart(Field.COORDINATES);
    for (int i = 0; i < value.getNumGeometries(); ++i) {
      serializeCoordinate((Point) value.getGeometryN(i), generator, provider);
    }
    generator.writeEndArray();

    generator.writeEndObject();
  }

  private void serialize(MultiLineString value, JsonGenerator generator, SerializationContext provider) {
    generator.writeStartObject();
    serializeTypeAndBoundingBox(GeometryType.MULTI_LINE_STRING, value, generator);

    generator.writeArrayPropertyStart(Field.COORDINATES);
    for (int i = 0; i < value.getNumGeometries(); ++i) {
      serializeCoordinates((LineString) value.getGeometryN(i), generator, provider);
    }
    generator.writeEndArray();

    generator.writeEndObject();
  }

  private void serialize(MultiPolygon value, JsonGenerator generator, SerializationContext provider) {
    generator.writeStartObject();
    serializeTypeAndBoundingBox(GeometryType.MULTI_POLYGON, value, generator);

    generator.writeArrayPropertyStart(Field.COORDINATES);
    for (int i = 0; i < value.getNumGeometries(); ++i) {
      serializeCoordinates((Polygon) value.getGeometryN(i), generator, provider);
    }
    generator.writeEndArray();

    generator.writeEndObject();
  }

  private void serialize(Polygon value, JsonGenerator generator, SerializationContext provider) throws JacksonException {
    generator.writeStartObject();
    serializeTypeAndBoundingBox(GeometryType.POLYGON, value, generator);
    generator.writeName(Field.COORDINATES);
    serializeCoordinates(value, generator, provider);
    generator.writeEndObject();
  }

  private void serialize(LineString value, JsonGenerator generator, SerializationContext provider) throws JacksonException {
    generator.writeStartObject();
    serializeTypeAndBoundingBox(GeometryType.LINE_STRING, value, generator);
    generator.writeName(Field.COORDINATES);
    serializeCoordinates(value, generator, provider);
    generator.writeEndObject();
  }

  private void serialize(Point value, JsonGenerator generator, SerializationContext provider) throws JacksonException {
    generator.writeStartObject();
    serializeTypeAndBoundingBox(GeometryType.POINT, value, generator);
    generator.writeName(Field.COORDINATES);
    serializeCoordinate(value, generator, provider);
    generator.writeEndObject();
  }

  private void serializeTypeAndBoundingBox(GeometryType type, Geometry geometry, JsonGenerator generator) {
    generator.writeStringProperty(Field.TYPE, type.toString());

    if (this.includeBoundingBox.shouldIncludeBoundingBoxFor(type) && !geometry.isEmpty()) {
      Envelope envelope = geometry.getEnvelopeInternal();
      generator.writeArrayPropertyStart(Field.BOUNDING_BOX);
      generator.writeNumber(envelope.getMinX());
      generator.writeNumber(envelope.getMinY());
      generator.writeNumber(envelope.getMaxX());
      generator.writeNumber(envelope.getMaxY());
      generator.writeEndArray();
    }
  }

  private void serializeCoordinates(Polygon value, JsonGenerator generator, SerializationContext provider) {
    generator.writeStartArray();
    if (!value.isEmpty()) {
      serializeCoordinates(value.getExteriorRing(), generator, provider);
      for (int i = 0; i < value.getNumInteriorRing(); ++i) {
        serializeCoordinates(value.getInteriorRingN(i), generator, provider);
      }
    }
    generator.writeEndArray();
  }

  private void serializeCoordinates(LineString value, JsonGenerator generator, SerializationContext provider) {
    serializeCoordinates(value.getCoordinateSequence(), generator, provider);
  }

  private void serializeCoordinates(CoordinateSequence value, JsonGenerator generator, SerializationContext provider) {
    generator.writeStartArray();
    for (int i = 0; i < value.size(); ++i) {
      serializeCoordinate(value.getCoordinate(i), generator, provider);
    }
    generator.writeEndArray();
  }

  private void serializeCoordinate(Point value, JsonGenerator generator, SerializationContext provider) {
    serializeCoordinate(value.getCoordinate(), generator, provider);
  }

  private void serializeCoordinate(Coordinate value, JsonGenerator generator, SerializationContext provider) {
    generator.writeStartArray();
    generator.writeNumber(formatAsNumber(value.getX()));
    generator.writeNumber(formatAsNumber(value.getY()));
    if (!Double.isNaN(value.getZ()) && Double.isFinite(value.getZ())) {
      generator.writeNumber(formatAsNumber(value.getZ()));
    }
    generator.writeEndArray();
  }

  private double formatAsNumber(double value) {
    return Double.parseDouble(decimalFormat.format(value));
  }
}
