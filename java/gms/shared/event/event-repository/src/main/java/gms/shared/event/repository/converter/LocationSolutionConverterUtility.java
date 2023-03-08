package gms.shared.event.repository.converter;

import gms.shared.event.coi.EventLocation;
import gms.shared.event.coi.LocationRestraint;
import gms.shared.event.coi.LocationSolution;
import gms.shared.event.coi.featureprediction.FeaturePredictionContainer;
import gms.shared.event.repository.BridgedEhInformation;
import gms.shared.event.repository.BridgedSdhInformation;
import gms.shared.utilities.bridge.database.converter.InstantToDoubleConverterNegativeNa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

/**
 * Utility class to help build {@link LocationSolution}s
 */
public class LocationSolutionConverterUtility {
  private static final InstantToDoubleConverterNegativeNa instantToDoubleConverter = new InstantToDoubleConverterNegativeNa();

  //private constructor for sonarqube
  private LocationSolutionConverterUtility() {

  }

  /**
   * Provided with {@link BridgedEhInformation} and {@link BridgedSdhInformation}, builds a {@link LocationSolution}
   * @param ehInfo relevant BridgedEhInformation
   * @param sdhInfo relevant BridgedSdhInformation
   * @return A LocationSolution constructed using the provided bridged info
   */
  public static LocationSolution fromLegacyToLocationSolution(BridgedEhInformation ehInfo,
    Collection<BridgedSdhInformation> sdhInfo) {
    var dataBuilder = LocationSolution.Data.builder();

    dataBuilder.setLocationUncertainty(LocationUncertaintyConverterUtility
      .fromLegacyToLocationUncertainty(ehInfo));

    var originLocation = ehInfo.getOriginDao().getLatLonDepthTimeKey();
    var eventLocation = EventLocation.from(originLocation.getLatitude(), originLocation.getLongitude(),
      originLocation.getDepth(), instantToDoubleConverter.convertToEntityAttribute(originLocation.getTime()));
    dataBuilder.setLocation(eventLocation);

    if (sdhInfo.isEmpty()) {

      dataBuilder.setFeaturePredictions(FeaturePredictionContainer.of());
      dataBuilder.setLocationBehaviors(new HashSet<>());
      dataBuilder.setNetworkMagnitudeSolutions(new ArrayList<>());
      dataBuilder.setLocationRestraint(LocationRestraint.free());
    } else {

      var behaviorsAndPredictions = sdhInfo.stream()
        .filter(info -> info.getSignalDetectionHypothesis().isPresent())
        .map(info -> PredictionsAndBehaviorsConverterUtility.fromLegacyToPredictionsAndBehaviors(info, eventLocation))
        .reduce(PredictionsAndBehaviors.empty(), PredictionsAndBehaviors::union);
      dataBuilder.setFeaturePredictions(behaviorsAndPredictions.getFeaturePredictions());
      dataBuilder.setLocationBehaviors(behaviorsAndPredictions.getLocationBehaviors());

      var netMags = ehInfo.netMagDaos()
        .flatMap(netMagDao -> NetworkMagnitudeSolutionConverterUtility
          .fromLegacyToNetworkMagnitudeSolutions(ehInfo, sdhInfo)
          .stream())
        .collect(toList());
      dataBuilder.setNetworkMagnitudeSolutions(netMags);

      var locationRestraint = LocationRestraintConverterUtility.fromLegacyToLocationRestraint(ehInfo, sdhInfo);
      dataBuilder.setLocationRestraint(locationRestraint);
    }

    return LocationSolution.builder()
      .setData(dataBuilder.build())
      .setId(UUID.randomUUID())
      .build();
  }
}
