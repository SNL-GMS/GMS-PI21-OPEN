package gms.shared.featureprediction.plugin.correction.ellipticity;

import com.fasterxml.jackson.core.JsonProcessingException;
import gms.shared.utilities.test.TestUtilities;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DziewonskiGilbertLookupTableDefinitionTest {

  @Test
  void testSerialization() throws JsonProcessingException {
    var table = DziewonskiGilbertLookupTableDefinition.create(Map.of("Ak135", "location/one", "Iaspei", "location/two"));
      TestUtilities.assertSerializes(table, DziewonskiGilbertLookupTableDefinition.class);
  }
}
