package gms.shared.signalenhancementconfiguration.config;

import com.google.common.collect.ImmutableList;
import gms.shared.frameworks.configuration.RetryConfig;
import gms.shared.frameworks.configuration.repository.FileConfigurationRepository;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.signalenhancementconfiguration.coi.filter.CascadeFilterDescription;
import gms.shared.signalenhancementconfiguration.coi.filter.FilterDefinition;
import gms.shared.signalenhancementconfiguration.coi.filter.FilterDescription;
import gms.shared.signalenhancementconfiguration.coi.filter.FilterList;
import gms.shared.signalenhancementconfiguration.coi.filter.FilterListDefinition;
import gms.shared.signalenhancementconfiguration.coi.filter.LinearFilterDescription;
import gms.shared.signalenhancementconfiguration.coi.types.CascadeFilterName;
import gms.shared.signalenhancementconfiguration.coi.types.FilterDefinitionUsage;
import gms.shared.signalenhancementconfiguration.coi.types.FilterDescriptionName;
import gms.shared.signalenhancementconfiguration.coi.types.FilterListName;
import gms.shared.signalenhancementconfiguration.coi.types.FilterName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SignalEnhancementConfigurationTest {

  private static final String STATION_NAME_SELECTOR = "station";
  private static final String CHANNEL_GROUP_NAME_SELECTOR = "channelGroup";
  private static final String CHANNEL_BAND_NAME_SELECTOR = "channelBand";
  private static final String CHANNEL_INSTRUMENT_NAME_SELECTOR = "channelInstrument";
  private static final String CHANNEL_ORIENTATION_NAME_SELECTOR = "channelOrientation";
  private static final String PHASE_NAME_SELECTOR = "phase";
  private static final String DISTANCE_NAME_SELECTOR = "distance";
  private static final String FILTER_NAME_SELECTOR = "filterName";

  private static final String WILD_CARD = "*";

  private ConfigurationConsumerUtility configurationConsumerUtility;

  SignalEnhancementConfiguration signalEnhancementConfiguration;

  @BeforeAll
  void init() {
    var configurationRoot = checkNotNull(
      Thread.currentThread().getContextClassLoader().getResource("configuration-base")
    ).getPath();

    configurationConsumerUtility = ConfigurationConsumerUtility
      .builder(FileConfigurationRepository.create(new File(configurationRoot).toPath()))
      .retryConfiguration(RetryConfig.create(1, 2, ChronoUnit.SECONDS, 1))
      .build();
  }

  @BeforeEach
  void setUp() {
    signalEnhancementConfiguration = new SignalEnhancementConfiguration(configurationConsumerUtility);
    signalEnhancementConfiguration.filterDefinitionConfig = "global.filter-definition";
    signalEnhancementConfiguration.filterDescriptionConfig = "global.filter-description";
    signalEnhancementConfiguration.cascadeFilterConfig = "global.filter-cascade";
    signalEnhancementConfiguration.filterListConfig = "global.filter-list";
    signalEnhancementConfiguration.filterListDefinitionConfig = "global.filter-list-definition";
    signalEnhancementConfiguration.filterMetadataConfig = "global.filter-metadata";
  }

  @Test
  void testResolveFilterDefinition() {
    List<FilterDefinition> filterDefinitionList = signalEnhancementConfiguration.filterDefinitionList();

    List<LinearFilterDescription> actualFilterDescriptions = filterDefinitionList
      .stream()
      .map(t -> (LinearFilterDescription) t.getFilterDescription())
      .collect(Collectors.toList());
    actualFilterDescriptions.sort(Comparator.comparing(LinearFilterDescription::getComments,
      Comparator.comparing(Optional::get)));

    List<LinearFilterDescription> expectedFilterDescriptions = signalEnhancementConfiguration
      .filterDescriptionList(FilterDescriptionName.values());
    expectedFilterDescriptions.sort(Comparator.comparing(LinearFilterDescription::getComments,
      Comparator.comparing(Optional::get)));

    Assertions.assertEquals(expectedFilterDescriptions, actualFilterDescriptions);
  }

  @Test
  void testResolveCascadeFilter() {
    Map<String, FilterDefinition> cascadeFilterDefinitionMap = signalEnhancementConfiguration.cascadeFilterMap();

    ImmutableList<FilterDescription> actualCascadedFilterDescriptions = ((CascadeFilterDescription)
      cascadeFilterDefinitionMap.get(CascadeFilterName.CASCADE_FILTER_2.getFilterName())
        .getFilterDescription()).getFilterDescriptions();

    List<LinearFilterDescription> expectedCascadedFilterDescriptions = signalEnhancementConfiguration
      .filterDescriptionList(FilterDescriptionName.HAM_FIR_BP_1_50_3_50_HZ_DESCRIPTION,
        FilterDescriptionName.HAM_FIR_BP_2_00_4_00_HZ_DESCRIPTION);

    Assertions.assertEquals(expectedCascadedFilterDescriptions, actualCascadedFilterDescriptions);
  }

  @Test
  void testResolveFilterList() {
    Map<String, FilterList> filterListMap = signalEnhancementConfiguration.filterListMap();

    List<FilterDefinition> actualCascadedFilter = filterListMap.get(FilterListName.SEISMIC.getFilterName())
      .getFilters()
      .stream()
      .filter(t -> t.getFilterDefinition().isPresent())
      .map(f -> f.getFilterDefinition().get())
      .filter(r -> r.getName().contains("Cascade"))
      .collect(Collectors.toUnmodifiableList());

    List<FilterDefinition> expectedCascadedFilter = signalEnhancementConfiguration.cascadeFilterMap().values()
      .stream()
      .collect(Collectors.toUnmodifiableList());

    Assertions.assertEquals(expectedCascadedFilter, actualCascadedFilter);
  }

  @Test
  void testResolveFilterListDefinition() {
    FilterListDefinition filterListDefinition = signalEnhancementConfiguration.filterListDefinition();

    List<FilterList> actualFilterList = filterListDefinition.getFilterLists()
      .stream()
      .collect(Collectors.toUnmodifiableList());

    List<FilterList> expectedFilterList = signalEnhancementConfiguration.filterListMap().values()
      .stream()
      .collect(Collectors.toUnmodifiableList());

    Assertions.assertEquals(expectedFilterList, actualFilterList);
  }

  @Test
  void testFiltersByMetadata_defaultFilter() {
    var actualFilterDefinitions = signalEnhancementConfiguration.filterMetadata();

    var expectedFilterDefinitions = getFilterDefinition(FilterName
      .HAM_FIR_BP_0_40_3_50_HZ.getFilter());

    Assertions.assertEquals(expectedFilterDefinitions, actualFilterDefinitions);
  }

  @Test
  void testFiltersByMetadata_defaultDetectionFilter() {
    Properties properties = getCriterionProperties(WILD_CARD, WILD_CARD,WILD_CARD, WILD_CARD, WILD_CARD, WILD_CARD,
      WILD_CARD, FilterDefinitionUsage.DETECTION.getName());

    // TODO: Move this method to the tests
    var actualFilterDefinitions = signalEnhancementConfiguration.getFiltersByMetadata(properties);

    var expectedFilterDefinitions = getFilterDefinition(FilterName
      .HAM_FIR_BP_0_50_4_00_HZ.getFilter());

    Assertions.assertEquals(expectedFilterDefinitions, actualFilterDefinitions);
  }

  @Test
  void testFiltersByMetadata_defaultLocalDistanceFilter() {
    Properties properties = getCriterionProperties(WILD_CARD, WILD_CARD,WILD_CARD, WILD_CARD, WILD_CARD,
      WILD_CARD, "1.0", WILD_CARD);

    var actualFilterDefinitions = signalEnhancementConfiguration.getFiltersByMetadata(properties);

    var expectedFilterDefinitions = getFilterDefinition(FilterName
      .HAM_FIR_BP_2_00_5_00_HZ.getFilter());

    Assertions.assertEquals(expectedFilterDefinitions, actualFilterDefinitions);
  }

  @Test
  void testFiltersByMetadata_defaultPhaseFilter() {
    Properties properties = getCriterionProperties(WILD_CARD,WILD_CARD, WILD_CARD, WILD_CARD, WILD_CARD, "S",
      WILD_CARD, WILD_CARD);

    var actualFilterDefinitions = signalEnhancementConfiguration.getFiltersByMetadata(properties);

    var expectedFilterDefinitions = getFilterDefinition(FilterName
      .HAM_FIR_BP_1_50_3_00_HZ.getFilter());

    Assertions.assertEquals(expectedFilterDefinitions, actualFilterDefinitions);
  }

  @Test
  void testFiltersByMetadata_defaultStationFilter() {
    Properties properties = getCriterionProperties("ASAR", WILD_CARD,WILD_CARD, WILD_CARD, WILD_CARD,
      WILD_CARD, WILD_CARD, WILD_CARD);

    var actualFilterDefinitions = signalEnhancementConfiguration.getFiltersByMetadata(properties);

    var expectedFilterDefinitions = getFilterDefinition(FilterName
      .HAM_FIR_BP_0_50_4_00_HZ_NON_CAUSAL.getFilter());

    Assertions.assertEquals(expectedFilterDefinitions, actualFilterDefinitions);
  }

  @Test
  void testFiltersByMetadata_ASAR_BHZ_Filter() {
    Properties properties = getCriterionProperties("ASAR", WILD_CARD,"B", "H",
      "Z", WILD_CARD, WILD_CARD, WILD_CARD);

    var actualFilterDefinitions = signalEnhancementConfiguration.getFiltersByMetadata(properties);

    var expectedFilterDefinitions = getFilterDefinition(FilterName
      .HAM_FIR_LP_4_20_HZ.getFilter());

    Assertions.assertEquals(expectedFilterDefinitions, actualFilterDefinitions);
  }

  @Test
  void testFiltersByMetadata_ASAR_BHN_BHE_Filter() {
    Properties properties = getCriterionProperties("ASAR", "AS31","B", "H",
      "N", WILD_CARD, WILD_CARD, WILD_CARD);

    var actualFilterDefinitions = signalEnhancementConfiguration.getFiltersByMetadata(properties);

    var expectedFilterDefinitions = getFilterDefinition(FilterName
      .HAM_FIR_BP_0_50_1_50_HZ.getFilter());

    Assertions.assertEquals(expectedFilterDefinitions, actualFilterDefinitions);
  }

  @Test
  void testFiltersByMetadata_ASAR_BHE_Filter() {
    Properties properties = getCriterionProperties("ASAR", "AS31","B", "H",
      "E", WILD_CARD, WILD_CARD, WILD_CARD);

    var actualFilterDefinitions = signalEnhancementConfiguration.getFiltersByMetadata(properties);

    var expectedFilterDefinitions = getFilterDefinition(FilterName
      .HAM_FIR_BP_0_50_1_50_HZ.getFilter());

    Assertions.assertEquals(expectedFilterDefinitions, actualFilterDefinitions);
  }

  @Test
  void testFiltersByMetadata_ASAR_SH_Detect_Filter() {
    Properties properties = getCriterionProperties("ASAR", WILD_CARD,"S", "H",
      WILD_CARD, WILD_CARD, WILD_CARD, FilterDefinitionUsage.DETECTION.getName());

    var actualFilterDefinitions = signalEnhancementConfiguration.getFiltersByMetadata(properties);

    var expectedFilterDefinitions = getFilterDefinition(FilterName
      .HAM_FIR_BP_0_70_2_00_HZ.getFilter());

    Assertions.assertEquals(expectedFilterDefinitions, actualFilterDefinitions);
  }

  @Test
  void testFiltersByMetadata_ASAR_SH_Onset_Filter() {
    Properties properties = getCriterionProperties("ASAR", WILD_CARD,"S", "H",
      WILD_CARD, WILD_CARD, WILD_CARD, FilterDefinitionUsage.ONSET.getName());

    var actualFilterDefinitions = signalEnhancementConfiguration.getFiltersByMetadata(properties);

    var expectedFilterDefinitions = getFilterDefinition(FilterName
      .HAM_FIR_BP_0_70_2_00_HZ.getFilter());

    Assertions.assertEquals(expectedFilterDefinitions, actualFilterDefinitions);
  }

  @Test
  void testFiltersByMetadata_ASAR_SH_close_distance_Onset_Filter() {
    Properties properties = getCriterionProperties("ASAR", WILD_CARD,"S", "H",
      WILD_CARD, WILD_CARD, "2.0", FilterDefinitionUsage.ONSET.getName());

    var actualFilterDefinitions = signalEnhancementConfiguration.getFiltersByMetadata(properties);

    var expectedFilterDefinitions = getFilterDefinition(FilterName
      .HAM_FIR_BP_4_00_8_00_HZ.getFilter());

    Assertions.assertEquals(expectedFilterDefinitions, actualFilterDefinitions);
  }

  @Test
  void testFiltersByMetadata_ASAR_SH_close_distance_Detect_Filter() {
    Properties properties = getCriterionProperties("ASAR", WILD_CARD,"S", "H",
      WILD_CARD, WILD_CARD, "4.5", FilterDefinitionUsage.DETECTION.getName());

    var actualFilterDefinitions = signalEnhancementConfiguration.getFiltersByMetadata(properties);

    var expectedFilterDefinitions = getFilterDefinition(FilterName
      .HAM_FIR_BP_4_00_8_00_HZ.getFilter());

    Assertions.assertEquals(expectedFilterDefinitions, actualFilterDefinitions);
  }

  @Test
  void testFiltersByMetadata_AS01_P_phase_full_distance_Filter() {
    Properties properties = getCriterionProperties("ASAR", "AS01","S", "H",
      "Z", "P", "45.5", FilterDefinitionUsage.DETECTION.getName());

    var actualFilterDefinitions = signalEnhancementConfiguration.getFiltersByMetadata(properties);

    var expectedFilterDefinitions = getFilterDefinition(FilterName
      .HAM_FIR_BP_1_00_3_00_HZ.getFilter());

    Assertions.assertEquals(expectedFilterDefinitions, actualFilterDefinitions);
  }

  @Test
  void testFiltersByMetadata_LPAZ_BHZ_10_deg_Filter() {
    Properties properties = getCriterionProperties("LPAZ", WILD_CARD,"B", "H",
      "Z", "P", "5.5", FilterDefinitionUsage.DETECTION.getName());

    var actualFilterDefinitions = signalEnhancementConfiguration.getFiltersByMetadata(properties);

    var expectedFilterDefinitions = getFilterDefinition(FilterName
      .HAM_FIR_BP_2_00_4_00_HZ.getFilter());

    Assertions.assertEquals(expectedFilterDefinitions, actualFilterDefinitions);
  }

  @Test
  void testFiltersByMetadata_PDAR_BH_tele_distance_Filter() {
    Properties properties = getCriterionProperties("PDAR", WILD_CARD,"B", "H",
      WILD_CARD, WILD_CARD, "25.5", FilterDefinitionUsage.FK.getName());

    var actualFilterDefinitions = signalEnhancementConfiguration.getFiltersByMetadata(properties);

    var expectedFilterDefinitions = getFilterDefinition(FilterName
      .HAM_FIR_BP_2_00_4_00_HZ.getFilter());

    Assertions.assertEquals(expectedFilterDefinitions, actualFilterDefinitions);
  }

  @Test
  void testFiltersByMetadata_PDAR_SH_tele_distance_Filter() {
    Properties properties = getCriterionProperties("PDAR", WILD_CARD,"S", "H",
      WILD_CARD, WILD_CARD, "75.5", FilterDefinitionUsage.ONSET.getName());

    var actualFilterDefinitions = signalEnhancementConfiguration.getFiltersByMetadata(properties);

    var expectedFilterDefinitions = getFilterDefinition(FilterName
      .HAM_FIR_BP_2_00_4_00_HZ.getFilter());

    Assertions.assertEquals(expectedFilterDefinitions, actualFilterDefinitions);
  }

  @Test
  void testFiltersByMetadata_TXAR_BP_mid_distance_Filter() {
    Properties properties = getCriterionProperties("TXAR", WILD_CARD,"B", WILD_CARD, WILD_CARD,
      "P", "75.5", WILD_CARD);

    var actualFilterDefinitions = signalEnhancementConfiguration.getFiltersByMetadata(properties);

    var expectedFilterDefinitions = getFilterDefinition(FilterName
      .HAM_FIR_HP_0_30_HZ.getFilter());

    Assertions.assertEquals(expectedFilterDefinitions, actualFilterDefinitions);
  }

  @Test
  void testFiltersByMetadata_defaultFilter_allWildCards() {
    Properties properties = getCriterionProperties(WILD_CARD, WILD_CARD,WILD_CARD, WILD_CARD, WILD_CARD, WILD_CARD,
      WILD_CARD, WILD_CARD);

    var actualFilterDefinitions = signalEnhancementConfiguration.getFiltersByMetadata(properties);

    var expectedFilterDefinitions = getFilterDefinition(FilterName
      .HAM_FIR_BP_0_40_3_50_HZ.getFilter());

    Assertions.assertEquals(expectedFilterDefinitions, actualFilterDefinitions);
  }

  private Properties getCriterionProperties(String station, String channelGroup, String channelBand,
    String channelInstrument, String channelOrientation, String phase, String distance, String filterName) {
    Properties properties = new Properties();

    properties.setProperty(STATION_NAME_SELECTOR, station);
    properties.setProperty(CHANNEL_GROUP_NAME_SELECTOR, channelGroup);
    properties.setProperty(CHANNEL_BAND_NAME_SELECTOR, channelBand);
    properties.setProperty(CHANNEL_INSTRUMENT_NAME_SELECTOR, channelInstrument);
    properties.setProperty(CHANNEL_ORIENTATION_NAME_SELECTOR, channelOrientation);
    properties.setProperty(PHASE_NAME_SELECTOR, phase);
    properties.setProperty(DISTANCE_NAME_SELECTOR, distance);
    properties.setProperty(FILTER_NAME_SELECTOR, filterName);

    return properties;
  }

  private List<FilterDefinition> getFilterDefinition(String filterName) {
    List<FilterDefinition> filterDefinitionList = signalEnhancementConfiguration.filterDefinitionList();

    return filterDefinitionList.stream().filter(f -> f.getName().equals(filterName))
        .collect(Collectors.toUnmodifiableList());
  }
}
