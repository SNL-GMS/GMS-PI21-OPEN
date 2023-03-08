package gms.shared.signaldetection.coi.types;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import java.io.IOException;

public class FeatureMeasurementTypeDeserializer extends StdDeserializer<FeatureMeasurementType<?>> {

  public FeatureMeasurementTypeDeserializer() {
    this(null);
  }

  public FeatureMeasurementTypeDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public FeatureMeasurementType<?> deserialize(JsonParser p,
    DeserializationContext ctxt) throws IOException, JsonProcessingException {
    var featureMeasurementString = p.getValueAsString();
    return FeatureMeasurementTypes.getTypeInstance(featureMeasurementString)
      .orElseThrow(() -> MismatchedInputException.from(p, FeatureMeasurementType.class, "JSON does not map to any known FeatureMeasurementType"));
  }
}
