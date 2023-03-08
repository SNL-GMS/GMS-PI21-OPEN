package gms.shared.featureprediction.framework;

import com.google.common.base.Functions;
import gms.shared.event.coi.EventLocation;
import gms.shared.event.coi.LocationSolution;
import gms.shared.event.coi.featureprediction.FeaturePrediction;
import gms.shared.event.coi.featureprediction.FeaturePredictionContainer;
import gms.shared.event.coi.featureprediction.FeaturePredictionCorrectionDefinition;
import gms.shared.event.coi.featureprediction.type.FeaturePredictionType;
import gms.shared.featureprediction.configuration.FeaturePredictorConfiguration;
import gms.shared.featureprediction.plugin.api.FeaturePredictorPlugin;
import gms.shared.plugin.Plugin;
import gms.shared.plugin.PluginRegistry;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility class for calculating feature predictions.
 */
@Component
@ComponentScan(
  basePackages = "gms.shared.featureprediction.plugin.featurepredictorplugin",
  includeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
      FeaturePredictorPlugin.class
    })
  }
)
public class FeaturePredictor {

  private static final Logger logger = LoggerFactory.getLogger(FeaturePredictor.class);
  @Autowired
  private PluginRegistry registry;

  @Autowired
  private FeaturePredictorConfiguration configuration;

  /**
   * Initializes the feature predictor plugins.
   */
  @PostConstruct
  public void init() {
    //call init on all configured plugins

    //probably should log something if not present

    var fpConfig = configuration.getCurrentFeaturePredictorDefinition();

    fpConfig
      .getPluginByPredictionTypeMap().getPluginNames().stream()
      .map(name -> registry.getPlugin(name, FeaturePredictorPlugin.class))
      .forEach(plugin -> plugin.ifPresent(Plugin::initialize));
  }

  public String getName() {
    return "FeaturePredictorUtility";
  }

  /**
   * Calculate some predictions. This version of the nethod takes pure location data.
   *
   * @param predictionTypes What types of predictions to calculate
   * @param sourceLocation The source location, or location of the event
   * @param receiverLocations Set of receiver locations
   * @param phaseTypes Which phases to predict for
   * @param earthModel Which model to use
   * @return A set of feature prediction, wrapped in a FeaturePredictionContainer.
   */
  public FeaturePredictionContainer predict(
    List<FeaturePredictionType<?>> predictionTypes, EventLocation sourceLocation,
    List<Location> receiverLocations, List<PhaseType> phaseTypes, String earthModel,
    List<FeaturePredictionCorrectionDefinition> featurePredictionCorrectionDefinitions) {

    logger.info("FeaturePredictor predict endpoint for PredictForLocationRequest starting");

    // Doing this the "non-streamy" way, because the "streamy" way creates a
    // List<FeaturePrediction<? capture of....  which will not work with
    // FeturePedictorContainer.create
    List<FeaturePrediction<?>> featurePredictionList = new ArrayList<>();

    receiverLocations.stream()
      .map(receiverLocation ->
        predictionTypes.stream()
          .map(predictionType -> phaseTypes.stream()
            .map(
              phaseType -> registry.getPlugin(configuration.getCurrentFeaturePredictorDefinition()
                  .getPluginNameByType(predictionType), FeaturePredictorPlugin.class)
                .orElseThrow(() -> new IllegalArgumentException("No plugin configured for " + predictionType))
                .predict(
                  predictionType,
                  sourceLocation,
                  receiverLocation,
                  phaseType,
                  earthModel,
                  featurePredictionCorrectionDefinitions
                )
            )))
      .flatMap(Functions.identity())
      .flatMap(Functions.identity())
      .forEach(featurePredictionList::add);

    logger.info("FeaturePredictor predict endpoint for PredictForLocationRequest complete");

    return FeaturePredictionContainer.create(featurePredictionList);
  }

  /**
   * Calculates some feature predictions, that places them inside the provided LocationSolution (that is, makes a copy
   * of the LocationSolution with the new feature predictions added.)
   * <p>
   * If the provided LocationSolution already contains a FeaturePrediction where the prediction type matches one of the
   * provided prediction types AND the channel matches one of the provided channels, no prediction is calculated for
   * that type/channel combination. In otherwords,this will only calculate predictions for prediction type/channel pairs
   * that dont exist in the location solution.
   *
   * @param predictionTypes What type of predictions to calculate
   * @param sourceLocationSolution The LocationSolution to update with new predictions. Contains the event location
   * @param receivingChannels The channels whose location to calculate the prediction for
   * @param phaseTypes Which phases to predict for
   * @param earthModel Which model to use
   * @return a copy of the sourceLocationSolution, updated with the new feature predictions.
   */
  public LocationSolution predict(List<FeaturePredictionType<?>> predictionTypes,
    LocationSolution sourceLocationSolution, List<Channel> receivingChannels,
    List<PhaseType> phaseTypes, String earthModel,
    List<FeaturePredictionCorrectionDefinition> featurePredictionCorrectionDefinitions) {

    logger.info(
      "FeaturePredictor predict endpoint for PredictForLocationSolutionAndChannelRequest starting");
    //
    // Can't do anything if there is no data object in the LocationSolution, because it has the event location.
    //
    var data = sourceLocationSolution.getData().orElseThrow(
      () -> new IllegalArgumentException("The source location solution has no data object!")
    );

    var oldFeaturePredictionContainer = data.getFeaturePredictions();

    var newFeaturePredictionContainer = FeaturePredictionContainer.create(predictionTypes.stream()
      //
      // Create a stream of Map.entry(predictionType, channel). Map.entry is just used for conveniene here, no map will
      // be created.
      //
      .map(predictionType -> receivingChannels.stream().map(channel -> Map.entry(predictionType, channel)))
      // The above actually created a stream of streams, so flatmap it
      .flatMap(Functions.identity())
      //
      // If the source location solution already has a FeaturePrediction with this predictionType/Channel pair, filter
      // the pair out so that we dont create a new feature prediction.
      //
      .filter(entry -> !oldFeaturePredictionContainer.anyMatch(
        featurePrediction -> entry.getValue().equals(featurePrediction.getChannel().orElse(null))
          && entry.getKey() == featurePrediction.getPredictionType()
      ))
      //
      // For ech phasetype, perform the actual feature prediction calculation.
      //
      .map(entry -> phaseTypes.stream().map(phaseType -> {
        var predictionType = entry.getKey();
        var receiverLocation = entry.getValue().getLocation();
        return registry.getPlugin(
            configuration.getCurrentFeaturePredictorDefinition()
              .getPluginNameByType(predictionType), FeaturePredictorPlugin.class)
          .orElseThrow(() -> new IllegalArgumentException("No plugin configured for " + predictionType)).predict(
            predictionType,
            sourceLocationSolution.getData().get().getLocation(),
            receiverLocation,
            phaseType,
            earthModel,
            featurePredictionCorrectionDefinitions
          ).toBuilder().setChannel(Optional.of(entry.getValue())).build();
      }))
      // Flatmap one more time, because by now we have combined three seperate streams: predictionTypes, channels, phasetypes.
      .flatMap(Functions.identity()).collect(Collectors.toList()));

    var featurePredictionContainer = oldFeaturePredictionContainer.union(newFeaturePredictionContainer);

    var newData = LocationSolution.Data.builder()
      .setFeaturePredictions(featurePredictionContainer)
      .setLocation(data.getLocation())
      .setLocationRestraint(data.getLocationRestraint())
      .setLocationUncertainty(data.getLocationUncertainty().orElse(null))
      .setNetworkMagnitudeSolutions(data.getNetworkMagnitudeSolutions())
      .setLocationBehaviors(data.getLocationBehaviors())
      .build();

    var newLocationSolution = LocationSolution.builder()
      .setId(sourceLocationSolution.getId())
      .setData(newData)
      .build();

    logger.info(
      "FeaturePredictor predict endpoint for PredictForLocationSolutionAndChannelRequest complete");

    return newLocationSolution;
  }

}
