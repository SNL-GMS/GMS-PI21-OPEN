package gms.shared.frameworks.osd.coi.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FeaturePredictionCorrectionTests {

  ObjectMapper mapper;

  public FeaturePredictionCorrectionTests() {
    this.mapper = CoiObjectMapperFactory.getJsonObjectMapper();
  }

  @Test
  void testElevationCorrectionSerializationDeserializationIntoInterface() throws IOException {
    FeaturePredictionCorrection elevationCorrection = ElevationCorrection1dDefinition.create(false);
    String elevationCorrectionJson = this.mapper.writeValueAsString(elevationCorrection);

    FeaturePredictionCorrection deserializedElevationCorrection = this.mapper
      .readValue(elevationCorrectionJson, FeaturePredictionCorrection.class);

    assertEquals(elevationCorrection.getClass(), deserializedElevationCorrection.getClass());
    assertEquals(elevationCorrection.getCorrectionType(),
      deserializedElevationCorrection.getCorrectionType());
    assertEquals(((ElevationCorrection1dDefinition) elevationCorrection).isUsingGlobalVelocity(),
      ((ElevationCorrection1dDefinition) deserializedElevationCorrection)
        .isUsingGlobalVelocity());
  }

  @Test
  void testEllipticityCorrectionSerializationDeserializationIntoInterface() throws IOException {
    FeaturePredictionCorrection ellipticityCorrection = EllipticityCorrection1dDefinition.create();
    String ellipticityCorrectionJson = this.mapper.writeValueAsString(ellipticityCorrection);

    FeaturePredictionCorrection deserializedEllipticityCorrection = this.mapper
      .readValue(ellipticityCorrectionJson, FeaturePredictionCorrection.class);

    assertEquals(ellipticityCorrection.getClass(), deserializedEllipticityCorrection.getClass());
    assertEquals(ellipticityCorrection.getCorrectionType(),
      deserializedEllipticityCorrection.getCorrectionType());
  }

  @Test
  void testDeserializeElevationCorrectionIntoEllipticityCorrectionFails() throws IOException {
    FeaturePredictionCorrection elevationCorrection = ElevationCorrection1dDefinition.create(false);
    String elevationCorrectionJson = this.mapper.writeValueAsString(elevationCorrection);

    assertThrows(ClassCastException.class, () -> {
      EllipticityCorrection1dDefinition actual = this.mapper
        .readValue(elevationCorrectionJson, EllipticityCorrection1dDefinition.class);
    });
  }

  @Test
  void testDeserializeEllipticityCorrectionIntoElevationCorrectionFails() throws IOException {
    FeaturePredictionCorrection ellipticityCorrection = EllipticityCorrection1dDefinition.create();
    String ellipticityCorrectionJson = this.mapper.writeValueAsString(ellipticityCorrection);

    assertThrows(ClassCastException.class, () -> {
      ElevationCorrection1dDefinition actual = this.mapper
        .readValue(ellipticityCorrectionJson, ElevationCorrection1dDefinition.class);
    });
  }
}
