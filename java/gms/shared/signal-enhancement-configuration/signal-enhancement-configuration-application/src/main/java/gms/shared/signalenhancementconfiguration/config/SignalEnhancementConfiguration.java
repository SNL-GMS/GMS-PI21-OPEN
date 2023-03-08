package gms.shared.signalenhancementconfiguration.config;

import gms.shared.frameworks.configuration.Selector;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.signalenhancementconfiguration.coi.filter.CascadeFilterDescription;
import gms.shared.signalenhancementconfiguration.coi.filter.FilterConfiguration;
import gms.shared.signalenhancementconfiguration.coi.filter.FilterDefinition;
import gms.shared.signalenhancementconfiguration.coi.filter.FilterList;
import gms.shared.signalenhancementconfiguration.coi.filter.FilterListDefinition;
import gms.shared.signalenhancementconfiguration.coi.filter.LinearFilterDescription;
import gms.shared.signalenhancementconfiguration.coi.types.CascadeFilterName;
import gms.shared.signalenhancementconfiguration.coi.types.FilterDescriptionName;
import gms.shared.signalenhancementconfiguration.coi.types.FilterListName;
import gms.shared.signalenhancementconfiguration.coi.types.FilterName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@ComponentScan(basePackages = {"gms.shared.spring"})
public class SignalEnhancementConfiguration {

  private static final String STATION_NAME_SELECTOR = "station";
  private static final String CHANNEL_GROUP_NAME_SELECTOR = "channelGroup";
  private static final String CHANNEL_BAND_NAME_SELECTOR = "channelBand";
  private static final String CHANNEL_INSTRUMENT_NAME_SELECTOR = "channelInstrument";
  private static final String CHANNEL_ORIENTATION_NAME_SELECTOR = "channelOrientation";
  private static final String PHASE_NAME_SELECTOR = "phase";
  private static final String DISTANCE_NAME_SELECTOR = "distance";
  private static final String FILTER_NAME_SELECTOR = "filterName";
  private static final String NAME_SELECTOR = "name";

  @Value("${filterDefinitionConfig}")
  String filterDefinitionConfig;

  @Value("${filterDescriptionConfig}")
  String filterDescriptionConfig;

  @Value("${cascadeFilterConfig}")
  String cascadeFilterConfig;

  @Value("${filterListConfig}")
  String filterListConfig;

  @Value("${filterListDefinitionConfig}")
  String filterListDefinitionConfig;

  @Value("${filterMetadataConfig}")
  String filterMetadataConfig;

  private final ConfigurationConsumerUtility configurationConsumerUtility;

  @Autowired
  public SignalEnhancementConfiguration(ConfigurationConsumerUtility configurationConsumerUtility) {
    this.configurationConsumerUtility = configurationConsumerUtility;
  }

  @Bean
  public List<FilterDefinition> filterDefinitionList() {
    return Arrays.stream(FilterName.values())
      .map(filterName -> configurationConsumerUtility.resolve(filterDefinitionConfig,
      List.of(Selector.from(NAME_SELECTOR, filterName.getFilter())), FilterDefinition.class)).collect(Collectors.toList());
  }

  @Bean
  List<LinearFilterDescription> filterDescriptionList(FilterDescriptionName... filterDescriptionNames) {
    return Arrays.stream(filterDescriptionNames)
      .map(filterName -> configurationConsumerUtility.resolve(filterDescriptionConfig,
        List.of(Selector.from(NAME_SELECTOR, filterName.getFilterDescription())),
        LinearFilterDescription.class))
      .collect(Collectors.toList());
  }

  @Bean
  public Map<String, FilterDefinition> cascadeFilterMap() {
    List<CascadeFilterDescription> cascadeFilterDescriptions = Arrays.stream(CascadeFilterName.values())
      .map(cascadeFilterName -> configurationConsumerUtility.resolve(cascadeFilterConfig,
        List.of(Selector.from(NAME_SELECTOR, cascadeFilterName.getFilterName())),
        CascadeFilterDescription.class))
      .collect(Collectors.toList());

    return cascadeFilterDescriptions.stream()
      .map(cascadeFilterDescription -> FilterDefinition.from(
        cascadeFilterDescription.getComments()
          .map(x -> x.replace(" comments", ""))
          .orElse("name"),
        cascadeFilterDescription.getComments(),
        cascadeFilterDescription))
      .collect(Collectors.toMap(FilterDefinition::getName, Function.identity()));
  }

  @Bean
  public Map<String, FilterList> filterListMap() {
    return Arrays.stream(FilterListName.values())
      .map(filterListName -> configurationConsumerUtility.resolve(filterListConfig,
        List.of(Selector.from(NAME_SELECTOR, filterListName.getFilterName())), FilterList.class))
      .collect(Collectors.toMap(FilterList::getName, Function.identity()));
  }

  @Bean
  public FilterListDefinition filterListDefinition() {
    return configurationConsumerUtility
      .resolve(filterListDefinitionConfig, List.of(), FilterListDefinition.class);
  }

  @Bean
  public List<FilterDefinition> filterMetadata() {
    return configurationConsumerUtility.resolve(filterMetadataConfig,
      List.of(), FilterConfiguration.class).getFilterDefinitions();
  }

  public List<FilterDefinition> getFiltersByMetadata(Properties criterionProperties) {
    Objects.requireNonNull(criterionProperties,
      "Cannot resolve Configuration to null criterion properties");

    var stationNameSelector = getSelector(criterionProperties, STATION_NAME_SELECTOR);
    var channelGroupNameSelector = getSelector(criterionProperties, CHANNEL_GROUP_NAME_SELECTOR);
    var channelBandNameSelector = getSelector(criterionProperties, CHANNEL_BAND_NAME_SELECTOR);
    var channelInstrumentNameSelector = getSelector(criterionProperties, CHANNEL_INSTRUMENT_NAME_SELECTOR);
    var channelOrientationNameSelector = getSelector(criterionProperties, CHANNEL_ORIENTATION_NAME_SELECTOR);
    var phaseNameSelector = getSelector(criterionProperties, PHASE_NAME_SELECTOR);
    var distanceNameSelector = getSelector(criterionProperties, DISTANCE_NAME_SELECTOR);
    var filterNameSelector = getSelector(criterionProperties, FILTER_NAME_SELECTOR);

    return configurationConsumerUtility.resolve(filterMetadataConfig,
      List.of(stationNameSelector, channelGroupNameSelector, channelBandNameSelector, channelInstrumentNameSelector,
        channelOrientationNameSelector, phaseNameSelector, distanceNameSelector, filterNameSelector),
      FilterConfiguration.class).getFilterDefinitions();
  }

  private Selector<?> getSelector(Properties criterionProperties, String criterion) {
    Objects.requireNonNull(criterionProperties.getProperty(criterion),
      "Cannot resolve Configuration to missing " + criterion + " criterion");

    if (criterion.equals(DISTANCE_NAME_SELECTOR)) {
      if (isDistanceWildCard(criterionProperties))
        criterionProperties.setProperty(DISTANCE_NAME_SELECTOR, "-99.0");

      return  Selector.from(DISTANCE_NAME_SELECTOR,
        Double.parseDouble(criterionProperties.getProperty(DISTANCE_NAME_SELECTOR)));
    } else {
      return Selector.from(criterion, criterionProperties.getProperty(criterion));
    }
  }

  private boolean isDistanceWildCard(Properties criterionProperties) {
    return "*".equals(criterionProperties.getProperty(DISTANCE_NAME_SELECTOR));
  }
}
