package gms.shared.event.coi.type.converter;

import gms.shared.event.coi.type.DepthMethod;
import gms.shared.utilities.bridge.database.converter.EnumToStringConverter;

/**
 * An {@link EnumToStringConverter} for {@link DepthMethod}
 */
public class DepthMethodConverter extends EnumToStringConverter<DepthMethod> {
  public DepthMethodConverter() {
    super(DepthMethod.class, DepthMethod::getLabel);
  }
}