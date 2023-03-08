package gms.shared.featureprediction.plugin.prediction;

import gms.shared.event.coi.EventLocation;
import gms.shared.event.coi.featureprediction.ElevationCorrectionDefinition;
import gms.shared.event.coi.featureprediction.EllipticityCorrectionDefinition;
import gms.shared.event.coi.featureprediction.FeaturePrediction;
import gms.shared.event.coi.featureprediction.FeaturePredictionComponent;
import gms.shared.event.coi.featureprediction.FeaturePredictionComponentType;
import gms.shared.event.coi.featureprediction.FeaturePredictionCorrectionDefinition;
import gms.shared.event.coi.featureprediction.type.FeaturePredictionType;
import gms.shared.event.coi.featureprediction.value.ArrivalTimeFeaturePredictionValue;
import gms.shared.event.coi.featureprediction.value.FeaturePredictionValue;
import gms.shared.featureprediction.plugin.api.FeaturePredictorPlugin;
import gms.shared.featureprediction.plugin.api.correction.ellipticity.EllipticityCorrectorPlugin;
import gms.shared.featureprediction.plugin.api.lookuptable.TravelTimeDepthDistanceLookupTablePlugin;
import gms.shared.featureprediction.plugin.correction.elevation.ElevationCorrector;
import gms.shared.featureprediction.utilities.math.EarthModelUtility;
import gms.shared.featureprediction.utilities.math.GeoMath;
import gms.shared.plugin.PluginRegistry;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.signaldetection.coi.values.ArrivalTimeMeasurementValue;
import gms.shared.signaldetection.coi.values.DurationValue;
import gms.shared.signaldetection.coi.values.InstantValue;
import gms.shared.stationdefinition.coi.channel.Location;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation of FeaturePredictorPlugin that uses bicubic spline interpolation to calculate travel times.
 */
@Service
@ComponentScan(
  basePackages = {
    "gms.shared.featureprediction.plugin.lookuptable",
    "gms.shared.featureprediction.utilities.elevationcorrector",
    "gms.shared.featureprediction.plugin.correction.ellipticity'"
  }
)
public class BicubicSplineFeaturePredictor implements FeaturePredictorPlugin {

  private static final Logger logger = LoggerFactory.getLogger(BicubicSplineFeaturePredictor.class);

  @Autowired
  private BicubicSplineFeaturePredictorConfiguration configuration;

  @Autowired
  private ElevationCorrector elevationCorrector;

  private PluginRegistry registry;

  @Autowired
  @Lazy
  @Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  void setPluginRegistry(PluginRegistry pluginRegistry) {
    this.registry = pluginRegistry;
  }

  /**
   * Initialize our travel time lookup tables.
   */
  @Override
  public void initialize() {
    logger.info("Initializing the BicubicSplineFeaturePredictor plugin");

    configuration.getCurrentBicubicSplineFeaturePredictorDefinition()
      .getTravelTimeDepthDistanceLookupTablePluginNameByEarthModel()
      .values()
      .forEach(pluginName ->
        registry.getPlugin(pluginName, TravelTimeDepthDistanceLookupTablePlugin.class)
          .ifPresent(TravelTimeDepthDistanceLookupTablePlugin::initialize));

    configuration.getCurrentBicubicSplineFeaturePredictorDefinition()
      .getEllipticityCorrectorPluginNameByEllipticityCorrectionPluginType()
      .values()
      .forEach(pluginName ->
        registry.getPlugin(pluginName, EllipticityCorrectorPlugin.class)
          .ifPresent(EllipticityCorrectorPlugin::initialize));
  }

  /**
   * Calculate a feature prediction.Uses bicubic spline interpolation on the data tables.
   *
   * @param predictionType Which type of prediction to calculate.
   * @param sourceLocation The event location
   * @param receiverLocation The receiver location.
   * @param phaseType The phase to predict for.
   * @param earthModel The earth model to use.
   * @param correctionDefinitions List of corrections to use; each definition contains
   * correction-specific parameters.
   * @param <T> Class that will hold the feature prediction information. Must extend FeaturePredictionValue
   * @return A new feature prediction.
   */
  @Override
  public <T extends FeaturePredictionValue<?, ?, ?>> FeaturePrediction<T> predict(
    FeaturePredictionType<T> predictionType,
    EventLocation sourceLocation, Location receiverLocation, PhaseType phaseType,
    String earthModel,
    List<FeaturePredictionCorrectionDefinition> correctionDefinitions) {

    logger.info("BicubicSplineFeaturePredictor predict starting");

    var pluginName = configuration
      .getCurrentBicubicSplineFeaturePredictorDefinition()
      .getTravelTimeDepthDistanceLookupTablePluginNameByEarthModel()
      .get(earthModel);

    var travelTimePlugin = registry.getPlugin(pluginName, TravelTimeDepthDistanceLookupTablePlugin.class)
      .orElseThrow(() -> new IllegalStateException(
        "There is no FeaturePredictor travelTimePlugin for earth model " +
          earthModel + " (tried travelTimePlugin name " + pluginName + ")"
      ));

    var travelTimesAsDoubles = Arrays.stream(travelTimePlugin.getValues(phaseType).copyOf())
      .map(durationArray -> Arrays.stream(durationArray).mapToDouble(
        duration -> Optional.ofNullable(duration).map(d -> d.toNanos() / 1_000_000_000.0).orElse(Double.NaN)
      ).toArray()).toArray(double[][]::new);

    var utility = new EarthModelUtility(
      travelTimePlugin.getDepthsKmForData(phaseType).toArray(),
      travelTimePlugin.getDistancesDegForData(phaseType).toArray(),
      travelTimesAsDoubles,
      this.configuration.getCurrentBicubicSplineFeaturePredictorDefinition().getExtrapolate()
    );

    var travelTimeAndDerivatives  = utility.interpolateEarthModel(
      sourceLocation.getDepthKm(),
      GeoMath.greatCircleAngularSeparation(
        sourceLocation.getLatitudeDegrees(), sourceLocation.getLongitudeDegrees(),
        receiverLocation.getLatitudeDegrees(), receiverLocation.getLongitudeDegrees()
      )
    );

    var basePredictedTravelTime = travelTimeAndDerivatives[0];

    //
    // If this is an ARRIVAL_TIME feature prediction, then use the travel time calculated above to predict an
    // arrival time, by adding the travel time to the date in the sourceLocation object.
    //
    if (predictionType == FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE) {

      var basePredictedTravelDuration = Duration.ofSeconds((long) Math.floor(basePredictedTravelTime))
        .plusNanos((long) ((basePredictedTravelTime - Math.floor(basePredictedTravelTime)) * 1_000_000_000L));

      var componentSet = getFeaturePredictionComponents(
        sourceLocation,
        receiverLocation,
        phaseType,
        earthModel,
        travelTimeAndDerivatives,
        correctionDefinitions
      );

      var correctedTravelDuration = basePredictedTravelDuration.plus(
        componentSet.stream().map(component -> component.getValue().getValue())
          .reduce(Duration.ZERO, Duration::plus)
      );

      componentSet.add(
        FeaturePredictionComponent.from(
          DurationValue.from(
            basePredictedTravelDuration,
            null
          ),
          utility.wasExtrapolated(),
          FeaturePredictionComponentType.BASELINE_PREDICTION
        )
      );

      var predictedValue = ArrivalTimeFeaturePredictionValue.create(
        ArrivalTimeMeasurementValue.from(
          InstantValue.from(
            sourceLocation.getTime().plus(correctedTravelDuration),
            Duration.ZERO
          ),
          Optional.of(DurationValue.from(
            correctedTravelDuration, Duration.ZERO
          ))
        ),
        Map.of(),
        componentSet
      );

      if (predictedValue.getClass() == predictionType.getTypeValueClass()) {
        return FeaturePrediction.<T>builder()
          .setPredictionType(predictionType)
          .setChannel(Optional.empty())
          .setExtrapolated(utility.wasExtrapolated())
          .setPhase(phaseType)
          .setPredictionChannelSegment(Optional.empty())
          .setPredictionValue(predictionType.getTypeValueClass().cast(predictedValue))
          .setReceiverLocation(receiverLocation)
          .setSourceLocation(sourceLocation)
          .build();
      } else {
        // NOTE: Type matching may prevent this from actually being thrown.
        throw new IllegalStateException("Got a FeaturePredictionType of "
          + predictedValue.getClass() + " expected "
          + predictionType.getTypeValueClass());
      }
    }

    // Only arrival time has been implemented so far.
    throw new NotImplementedException("BicubicSplineFeaturePredictor is not implemented for " + predictionType);
  }

  private Set<FeaturePredictionComponent<DurationValue>> getFeaturePredictionComponents(
    EventLocation sourceLocation,
    Location receiverLocation,
    PhaseType phaseType,
    String earthModel,
    double[] travelTimeAndDerivatives,
    List<FeaturePredictionCorrectionDefinition> correctionDefinitions
  ) {

    return correctionDefinitions.stream().map(definition -> {

      switch (definition.getCorrectionType()) {

        case ELEVATION_CORRECTION:
          return elevationCorrector.correct(
            ((ElevationCorrectionDefinition) definition).getMediumVelocityEarthModel(),
            receiverLocation,
            travelTimeAndDerivatives[3], // The third element is df/dy, or 
                                         // travel time wrt distance
            phaseType
          );

        case ELLIPTICITY_CORRECTION:
          return registry.getPlugin(
              this.configuration.getCurrentBicubicSplineFeaturePredictorDefinition()
                .getEllipticityCorrectorPluginNameByEllipticityCorrectionPluginType()
                .get(((EllipticityCorrectionDefinition) definition).getEllipticityCorrectionType()),
              EllipticityCorrectorPlugin.class)
            .map(plugin -> plugin.correct(
              earthModel, sourceLocation, receiverLocation, phaseType
            )).flatMap(Function.identity());

        default:
          logger.info(
            "A correction is being asked for that is not implemented: {}",
            definition.getCorrectionType()
          );
          return Optional.<FeaturePredictionComponent<DurationValue>>empty();
      }

    }).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet());
  }

}
