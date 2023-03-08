package gms.shared.utilities.bridge.database.converter;

import javax.persistence.AttributeConverter;

/**
 * Double converter for checking double columns for nulls
 */
public class DoubleConverter implements AttributeConverter<Double, Double> {
  private static final Double NA_VALUE = -1.0;

  @Override
  public Double convertToDatabaseColumn(Double value) {
    return value;
  }

  @Override
  public Double convertToEntityAttribute(Double value) {
    return value == null ? NA_VALUE : value;
  }
}
