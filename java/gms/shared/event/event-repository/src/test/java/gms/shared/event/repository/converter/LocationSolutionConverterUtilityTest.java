package gms.shared.event.repository.converter;

import gms.shared.event.coi.LocationRestraint;
import gms.shared.event.coi.featureprediction.type.FeaturePredictionType;
import gms.shared.event.repository.BridgeTestFixtures;
import gms.shared.event.repository.BridgedSdhInformation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class LocationSolutionConverterUtilityTest {

  @Test
  void testFromLegacyToLocationSolution() {
    var ehInfo = BridgeTestFixtures.DEFAULT_BRIDGED_EH_INFORMATION;
    var sdhInfo = BridgeTestFixtures.DEFAULT_BRIDGED_SDH_INFORMATION;
    var actualLocationSolution = LocationSolutionConverterUtility.fromLegacyToLocationSolution(ehInfo,
      singleton(sdhInfo));

    assertThat(actualLocationSolution).isNotNull();
    assertThat(actualLocationSolution.getData()).isPresent();
    var locationSolutionData = actualLocationSolution.getData().get();
    assertThat(locationSolutionData.getFeaturePredictions().getFeaturePredictionsForType(FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE)).isNotEmpty();
    assertThat(locationSolutionData.getLocationBehaviors()).isNotEmpty();
    assertThat(locationSolutionData.getLocationUncertainty()).isPresent();
    assertThat(locationSolutionData.getNetworkMagnitudeSolutions()).isNotEmpty();
  }

  @Test
  void testFromLegacyToLocationSolution_EmptySdh() {
    var ehInfo = BridgeTestFixtures.DEFAULT_BRIDGED_EH_INFORMATION;
    var sdhInfoWithEmptySdh = BridgedSdhInformation.builder()
      .setArInfoDao(BridgeTestFixtures.DEFAULT_BRIDGED_SDH_INFORMATION.getArInfoDao().orElseThrow())
      .setAssocDao(BridgeTestFixtures.DEFAULT_BRIDGED_SDH_INFORMATION.getAssocDao())
      .setStaMagDaos(BridgeTestFixtures.DEFAULT_BRIDGED_SDH_INFORMATION.getStaMagDaos())
      .build();
    var actualLocationSolution = LocationSolutionConverterUtility.fromLegacyToLocationSolution(ehInfo,
      singleton(sdhInfoWithEmptySdh));

    assertThat(actualLocationSolution).isNotNull();
    assertThat(actualLocationSolution.getData()).isPresent();
    var locationSolutionData = actualLocationSolution.getData().get();
    assertThat(locationSolutionData.getFeaturePredictions().getFeaturePredictionsForType(FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE)).isEmpty();
    assertThat(locationSolutionData.getLocationBehaviors()).isEmpty();
    assertThat(locationSolutionData.getLocationUncertainty()).isPresent();
    assertThat(locationSolutionData.getNetworkMagnitudeSolutions()).isNotEmpty();
  }

  @Test
  void testFromLegacyToLocationSolution_NoSdhInfo() {

    var ehInfo = BridgeTestFixtures.DEFAULT_BRIDGED_EH_INFORMATION;
    var actualLocationSolution = LocationSolutionConverterUtility.fromLegacyToLocationSolution(ehInfo,
      new HashSet<>());

    assertThat(actualLocationSolution).isNotNull();
    assertEquals(LocationRestraint.free(), actualLocationSolution.getData().orElseThrow().getLocationRestraint());
    assertTrue(actualLocationSolution.getData().isPresent());
    assertTrue(actualLocationSolution.getData().orElseThrow().getLocationBehaviors().isEmpty());
    assertThat(actualLocationSolution.getData().orElseThrow().getFeaturePredictions().getFeaturePredictionsForType(FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE)).isEmpty();
    assertTrue(actualLocationSolution.getData().orElseThrow().getNetworkMagnitudeSolutions().isEmpty());
  }

}
