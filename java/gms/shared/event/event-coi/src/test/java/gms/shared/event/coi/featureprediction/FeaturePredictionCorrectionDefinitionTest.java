package gms.shared.event.coi.featureprediction;

import com.fasterxml.jackson.core.type.TypeReference;
import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Test;

import java.util.List;

class FeaturePredictionCorrectionDefinitionTest {

  @Test
  void testSerialization() {
    var list = List.of(
      ElevationCorrectionDefinition.from("MyAmazingModel"),
      EllipticityCorrectionDefinition.from(EllipticityCorrectionType.DZIEWONSKI_GILBERT)
    );

    TestUtilities.assertSerializes(list,
      new TypeReference<List<FeaturePredictionCorrectionDefinition>>() {
      });
  }
}
