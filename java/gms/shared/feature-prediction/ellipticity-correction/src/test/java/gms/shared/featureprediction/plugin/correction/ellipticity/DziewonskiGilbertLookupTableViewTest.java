package gms.shared.featureprediction.plugin.correction.ellipticity;

import com.fasterxml.jackson.core.JsonProcessingException;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.utilities.javautilities.objectmapper.ObjectMapperFactory;
import gms.shared.utilities.test.TestUtilities;
import java.util.List;
import org.junit.jupiter.api.Test;

class DziewonskiGilbertLookupTableViewTest {

  @Test
  void testSerialization() throws JsonProcessingException {
    var lookupTableView = DziewonskiGilbertLookupTableView.builder()
      .setModel("Brad Pitt")
      .setPhase(PhaseType.I)
      .setDepthUnits("kilometers")
      .setDistanceUnits("degrees")
      .setDepths(List.of(1D, 2D, 3D))
      .setDistances(List.of(1D, 2D, 3D))
      .setTau0(List.of(List.of(1D, 2D, 3D)))
      .setTau1(List.of(List.of(1D, 2D, 3D)))
      .setTau2(List.of(List.of(1D, 2D, 3D)))
      .build();
    System.out.println(ObjectMapperFactory.getJsonObjectMapper().writeValueAsString(lookupTableView));
    TestUtilities.assertSerializes(lookupTableView, DziewonskiGilbertLookupTableView.class);
  }
}
