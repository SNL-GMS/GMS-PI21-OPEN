package gms.shared.event.manager.config;

import gms.shared.frameworks.configuration.Selector;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.stationdefinition.coi.channel.ChannelBandType;
import gms.shared.stationdefinition.coi.channel.ChannelDataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Provides configuration utilities for the
 * {@link gms.shared.event.manager.EventManager}
 */
@ComponentScan(basePackages = {"gms.shared.system.events"})
@Configuration
public class EventManagerConfiguration {

  @Value("${featurePredictorService.hostname}")
  private String featurePredictionServiceHostname;

  @Value("${featurePredictorService.contextPath}")
  private String featurePredictionContextPath;
  @Value("${featurePredictorService.port:8080}")
  private long featurePredictionServicePort;

  @Value("${featurePredictorService.urlPaths.predictForLocation}")
  private String predictForLocationUrlPath;

  @Value("${featurePredictorService.urlPaths.predictForLocationSolutionAndChannel}")
  private String predictForLocationSolutionAndChannelUrlPath;

  private final ConfigurationConsumerUtility configurationConsumerUtility;

  private static final String PREDICTIONS_FOR_LOCATION_SOLUTION_DEFINITION = "event-manager.predictions-for-location-solution-definition";
  private static final String PREDICT_FEATURES_FOR_LOCATION_DEFINITION = "event-manager.predict-features-for-location-definition";

  @Autowired
  public EventManagerConfiguration(ConfigurationConsumerUtility configurationConsumerUtility) {
    this.configurationConsumerUtility = configurationConsumerUtility;
  }

  public URI predictForLocationUrl() {
    var predictForLocationUrlString = String.format("http://%s:%d%s%s", featurePredictionServiceHostname,
      featurePredictionServicePort, featurePredictionContextPath, predictForLocationUrlPath);
    try {
      return new URL(predictForLocationUrlString).toURI();
    } catch (MalformedURLException | URISyntaxException e) {
      throw new IllegalStateException(String.format("Configured URL %s is malformed", predictForLocationUrlString), e);
    }
  }

  public URI predictForLocationSolutionAndChannelUrl() {
    var predictForLocationSolutionAndChannelUrlString = String.format("http://%s:%d%s%s", featurePredictionServiceHostname,
      featurePredictionServicePort, featurePredictionContextPath, predictForLocationSolutionAndChannelUrlPath);
    try {
      return new URL(predictForLocationSolutionAndChannelUrlString).toURI();
    } catch (MalformedURLException | URISyntaxException e) {
      throw new IllegalStateException(String.format("Configured URL %s is malformed", predictForLocationSolutionAndChannelUrlString), e);
    }
  }

  public List<FeaturePredictionsDefinitions> getPredictionDefinitions() {

    return configurationConsumerUtility.resolve(
      PREDICTIONS_FOR_LOCATION_SOLUTION_DEFINITION,
      List.of(),
      FeaturePredictionDefinitionConfigurationOption.class
    ).getPredictionsForLocationSolutionDefinitions();
  }

  public List<FeaturePredictionsDefinitions> getPredictionDefinitions(String stationName, String channelName,
    PhaseType phaseType, double distance) {
    var stationNameSelector = Selector.from("stationName", stationName);
    var channelNameSelector = Selector.from("channelName", channelName);
    var phaseTypeSelector = Selector.from("phaseType", phaseType.toString());
    var distanceSelector = Selector.from("distance", distance);

    return configurationConsumerUtility.resolve(
      PREDICTIONS_FOR_LOCATION_SOLUTION_DEFINITION,
      List.of(stationNameSelector, channelNameSelector, phaseTypeSelector, distanceSelector),
      FeaturePredictionDefinitionConfigurationOption.class
    ).getPredictionsForLocationSolutionDefinitions();
  }

  public List<FeaturePredictionsDefinitions> getPredictionDefinitions(PhaseType phaseType, double distance, 
    Optional<ChannelDataType> receiverDataType, Optional<ChannelBandType> receiverBandType) {
    
    // Code smell for missing type parameter not fixable because configurationConsumerUtility.resolve() 
    // requires no type parameter on selectors
    var selectors = new ArrayList<Selector>();

    selectors.add(Selector.from("phaseType", phaseType.toString()));
    selectors.add(Selector.from("distance", distance));
    
    receiverDataType.ifPresent(dt -> selectors.add(Selector.from("receiverDataType", dt.toString())));
    receiverBandType.ifPresent(bt -> selectors.add(Selector.from("receiverBandType", bt.toString())));

    return configurationConsumerUtility.resolve(
      PREDICT_FEATURES_FOR_LOCATION_DEFINITION,
      selectors,
      FeaturePredictionDefinitionConfigurationOption.class
    ).getPredictionsForLocationSolutionDefinitions();

  }
}
