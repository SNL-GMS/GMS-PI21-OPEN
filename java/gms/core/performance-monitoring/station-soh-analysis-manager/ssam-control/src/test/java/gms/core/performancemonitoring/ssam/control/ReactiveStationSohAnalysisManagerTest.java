package gms.core.performancemonitoring.ssam.control;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import gms.core.performancemonitoring.soh.control.configuration.StationSohDefinition;
import gms.core.performancemonitoring.ssam.control.ReactiveStationSohAnalysisManager.DataContainer;
import gms.core.performancemonitoring.ssam.control.api.DecimationRequestParams;
import gms.core.performancemonitoring.ssam.control.config.StationSohMonitoringDefinition;
import gms.core.performancemonitoring.ssam.control.config.StationSohMonitoringDisplayParameters;
import gms.core.performancemonitoring.ssam.control.config.StationSohMonitoringUiClientParameters;
import gms.shared.frameworks.osd.api.OsdRepositoryInterface;
import gms.shared.frameworks.osd.api.performancemonitoring.CapabilitySohRollupRepositoryInterface;
import gms.shared.frameworks.osd.api.performancemonitoring.PerformanceMonitoringRepositoryInterface;
import gms.shared.frameworks.osd.api.util.HistoricalStationSohRequest;
import gms.shared.frameworks.osd.coi.soh.CapabilitySohRollup;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
import gms.shared.frameworks.osd.coi.soh.quieting.QuietedSohStatusChange;
import gms.shared.frameworks.osd.coi.soh.quieting.UnacknowledgedSohStatusChange;
import gms.shared.frameworks.osd.dto.soh.HistoricalSohMonitorValues;
import gms.shared.frameworks.osd.dto.soh.HistoricalStationSoh;
import gms.shared.frameworks.osd.dto.soh.PercentSohMonitorValues;
import gms.shared.frameworks.systemconfig.SystemConfig;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ReactiveStationSohAnalysisManagerTest {
  
  @Mock
  SystemConfig mockSystemConfig;
  
  @Mock
  ReactiveStationSohAnalysisManager reactiveStationSohAnalysisManager;
  
  @Mock
  StationSohAnalysisManagerConfiguration mockStationSohAnalysisManagerConfiguration;
  
  @Mock
  PerformanceMonitoringRepositoryInterface mockPMRI;

  @Mock
  CapabilitySohRollupRepositoryInterface mockCSRRI;
  
  @Mock
  StationSohMonitoringUiClientParameters mockStationSohMonitoringUiClientParameters;
  


  @BeforeEach
  void setUp() {
    Mockito.when(mockSystemConfig.getValue(ReactiveStationSohAnalysisManager.MAX_CACHE_INIT_RETRIES)).thenReturn("1");
    reactiveStationSohAnalysisManager = new ReactiveStationSohAnalysisManager(mockStationSohAnalysisManagerConfiguration, mockSystemConfig, mockPMRI, mockCSRRI);

  }


  @Test
  void testGetHistoricalStationSoh() {

    var osdRepositoryInterface = mock(OsdRepositoryInterface.class);

    var historicalSohMonitorValues1 = HistoricalSohMonitorValues
      .create("CHANNEL1", Map.of(SohMonitorType.MISSING,
        PercentSohMonitorValues.create(new double[]{2.43, 4.5,})));

    var historicalSohMonitorValues2 = HistoricalSohMonitorValues
      .create("CHANNEL2", Map.of(SohMonitorType.MISSING,
        PercentSohMonitorValues.create(new double[]{2.43, 4.63,})));

    Mockito.when(osdRepositoryInterface.retrieveHistoricalStationSoh(any(
        HistoricalStationSohRequest.class)))
      .thenReturn(HistoricalStationSoh.create("MY_COOL_STATION", new long[]{32323, 43424},
        List.of(historicalSohMonitorValues1, historicalSohMonitorValues2)));

    var endTime = Instant.now();
    var startTime = endTime.minus(30, ChronoUnit.HOURS);

    var decimationRequestParams = DecimationRequestParams
      .create(startTime, endTime, 1000, "MY_COOL_STATION",
        SohMonitorType.MISSING);
    
    var historicalStationSoh = ReactiveStationSohAnalysisManager.getHistoricalStationSoh(decimationRequestParams, osdRepositoryInterface);

    Assertions.assertEquals("MY_COOL_STATION", historicalStationSoh.getStationName());

    Assertions.assertEquals(2, historicalStationSoh.getMonitorValues().size());

    Assertions.assertEquals(2, historicalStationSoh.getCalculationTimes().length);

    Assertions.assertEquals(historicalSohMonitorValues1,
      historicalStationSoh.getMonitorValues().stream().filter(
        historicalSohMonitorValues ->
          "CHANNEL1".equals(historicalSohMonitorValues.getChannelName())).findFirst().orElse(null));

    Assertions.assertEquals(historicalSohMonitorValues2,
      historicalStationSoh.getMonitorValues().stream().filter(
        historicalSohMonitorValues ->
          "CHANNEL2".equals(historicalSohMonitorValues.getChannelName())).findFirst().orElse(null));
  }

  @Test
  void testSenderOptions() {
    var systemConfig = mock(SystemConfig.class);

    Mockito.when(systemConfig.getValue(ReactiveStationSohAnalysisManager.KAFKA_BOOTSTRAP_SERVERS))
      .thenReturn("kafka-broker-address");

    var senderOptions = ReactiveStationSohAnalysisManager.senderOptions(systemConfig);

    Assertions.assertEquals("kafka-broker-address", senderOptions.producerProperties()
      .get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
  }

  @Test
  void testInitializeFromOsd() {

    var mockStationSohMonitoringDefinition = mock(StationSohMonitoringDefinition.class);

    Mockito.when(mockStationSohMonitoringUiClientParameters.getStationSohControlConfiguration())
      .thenReturn(mockStationSohMonitoringDefinition);
    

    Mockito.when(mockStationSohAnalysisManagerConfiguration.resolveDisplayParameters())
      .thenReturn(mockStationSohMonitoringUiClientParameters);

    var osdRepositoryInterface = mock(OsdRepositoryInterface.class);

    Mockito.when(mockStationSohAnalysisManagerConfiguration.getSohRepositoryInterface())
      .thenReturn(osdRepositoryInterface);

    Mockito.when(mockStationSohMonitoringDefinition.getStationSohDefinitions()).thenReturn(
      Set.of(StationSohDefinition.create("Station1", Set.of(), Map.of(), Set.of(), Map.of()),
        StationSohDefinition.create("Station2", Set.of(), Map.of(), Set.of(), Map.of())));

    Mockito.when(mockStationSohMonitoringDefinition.getDisplayedStationGroups())
      .thenReturn(List.of("Group1", "Group2"));

    var stationSoh1 = mock(StationSoh.class);
    var stationSoh2 = mock(StationSoh.class);

    Mockito.when(stationSoh1.getStationName()).thenReturn("Station1");
    Mockito.when(stationSoh2.getStationName()).thenReturn("Station2");

    var capabilitySohRollup1 = mock(CapabilitySohRollup.class);
    var capabilitySohRollup2 = mock(CapabilitySohRollup.class);

    Mockito.when(capabilitySohRollup1.getForStationGroup()).thenReturn("Group1");
    Mockito.when(capabilitySohRollup2.getForStationGroup()).thenReturn("Group2");

    var quietedSohStatusChange = mock(QuietedSohStatusChange.class);
    var unacknowledgedSohStatusChange = mock(UnacknowledgedSohStatusChange.class);

    Mockito.when(mockPMRI.retrieveByStationId(any()))
      .thenReturn(List.of(stationSoh1, stationSoh2));
    Mockito.when(
        mockCSRRI.retrieveLatestCapabilitySohRollupByStationGroup(any()))
      .thenReturn(
        List.of(capabilitySohRollup1, capabilitySohRollup2));

    Mockito.when(osdRepositoryInterface.retrieveQuietedSohStatusChangesByTime(any()))
      .thenReturn(List.of(quietedSohStatusChange));
    Mockito
      .when(osdRepositoryInterface.retrieveUnacknowledgedSohStatusChanges(any()))
      .thenReturn(
        List.of(unacknowledgedSohStatusChange));


    var dataContainerMono = reactiveStationSohAnalysisManager.initializeCacheFromOsd();

    StepVerifier.create(dataContainerMono)
      .assertNext(dataContainer -> {
        Assertions
          .assertEquals(stationSoh1, dataContainer.latestStationSohByStation.get("Station1"));
        Assertions
          .assertEquals(stationSoh2, dataContainer.latestStationSohByStation.get("Station2"));
        Assertions.assertEquals(capabilitySohRollup1,
          dataContainer.latestCapabilitySohRollupByStationGroup.get("Group1"));
        Assertions.assertEquals(capabilitySohRollup2,
          dataContainer.latestCapabilitySohRollupByStationGroup.get("Group2"));
        Assertions.assertEquals(quietedSohStatusChange,
          dataContainer.quietedSohStatusChanges.stream().findFirst().orElse(null));
        Assertions.assertEquals(unacknowledgedSohStatusChange,
          dataContainer.unacknowledgedSohStatusChanges.stream().findFirst().orElse(null));
      })
      .verifyComplete();
  }

  @Test
  void testHasNonEmptyConfiguration() {

    var mockStationSohMonitoringDefinition = Mockito.mock(StationSohMonitoringDefinition.class);
    Set<StationSohDefinition> stationSohDefinitions = new HashSet<>();
    
    Mockito.when(mockStationSohAnalysisManagerConfiguration.resolveDisplayParameters()).thenReturn(mockStationSohMonitoringUiClientParameters);
    Mockito.when(mockStationSohMonitoringUiClientParameters.getStationSohControlConfiguration()).thenReturn(mockStationSohMonitoringDefinition);
    Mockito.when(mockStationSohMonitoringDefinition.getStationSohDefinitions()).thenReturn(stationSohDefinitions);

    Assertions.assertFalse(reactiveStationSohAnalysisManager.hasNonEmptyConfiguration());
  }

  @Test
  void testSetupReactiveProcessingPipeline() {

    Mockito.when(mockSystemConfig.getValue(any())).thenReturn("mocked value");

    var mockStationSohMonitoringDisplayParameters = Mockito.mock(StationSohMonitoringDisplayParameters.class);
    var mockStationSohMonitoringDefinition = Mockito.mock(StationSohMonitoringDefinition.class);
    when(mockStationSohMonitoringDisplayParameters.getAcknowledgementQuietDuration()).thenReturn(Duration.ofMillis(100));
    var stationSohMonitoringUiClientParameters = StationSohMonitoringUiClientParameters.from(mockStationSohMonitoringDefinition, mockStationSohMonitoringDisplayParameters);
    Mockito.when(mockStationSohAnalysisManagerConfiguration.resolveDisplayParameters()).thenReturn(stationSohMonitoringUiClientParameters);

    DataContainer dataContainer = new DataContainer(Map.of(), Map.of(), Set.of(), Set.of());

    // perhaps we need the step verifier here, this just makes sure this method does not throw an error if properly mocked
    var tup =  reactiveStationSohAnalysisManager.createAndInitializeKafkaUtility(dataContainer);
    Assertions.assertDoesNotThrow(() -> reactiveStationSohAnalysisManager.setupReactiveProcessingPipeline(tup));

  }

  @Test
  void testFailingInitializeCacheFromOsd() {

    StepVerifier.create(reactiveStationSohAnalysisManager.initializeCacheFromOsd())
      .assertNext(dataContainer -> {
        Assertions
          .assertEquals(Collections.EMPTY_MAP, dataContainer.latestStationSohByStation);
        Assertions.assertEquals(Collections.EMPTY_MAP,
          dataContainer.latestCapabilitySohRollupByStationGroup);
        Assertions.assertEquals(Collections.EMPTY_SET,
          dataContainer.quietedSohStatusChanges);
        Assertions.assertEquals(Collections.EMPTY_SET,
          dataContainer.unacknowledgedSohStatusChanges);
      })
      .verifyComplete();

  }
}
