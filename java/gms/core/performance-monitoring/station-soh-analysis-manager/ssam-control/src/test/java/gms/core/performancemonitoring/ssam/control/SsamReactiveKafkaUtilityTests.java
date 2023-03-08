package gms.core.performancemonitoring.ssam.control;

import gms.core.performancemonitoring.ssam.control.SsamReactiveKafkaUtility.SohWrapper;
import gms.core.performancemonitoring.ssam.control.config.StationSohMonitoringDefinition;
import gms.core.performancemonitoring.ssam.control.config.StationSohMonitoringDisplayParameters;
import gms.core.performancemonitoring.ssam.control.config.StationSohMonitoringUiClientParameters;
import gms.core.performancemonitoring.ssam.control.dataprovider.ReactiveConsumer;
import gms.core.performancemonitoring.ssam.control.datapublisher.KafkaProducer;
import gms.core.performancemonitoring.ssam.control.processor.MaterializedViewProcessor;
import gms.core.performancemonitoring.uimaterializedview.AcknowledgedSohStatusChange;
import gms.core.performancemonitoring.uimaterializedview.QuietedSohStatusChangeUpdate;
import gms.core.performancemonitoring.uimaterializedview.UiStationAndStationGroups;
import gms.shared.frameworks.osd.coi.soh.CapabilitySohRollup;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
import gms.shared.frameworks.osd.coi.soh.quieting.UnacknowledgedSohStatusChange;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class SsamReactiveKafkaUtilityTests {

  static int calcIntervalMillis = 1000;
  static Instant timeGroup1 = Instant.ofEpochMilli(1000);
  static Instant timeGroup2 = Instant.ofEpochMilli(1000 + calcIntervalMillis);
  static StationSoh mockStationSoh1Group1 = Mockito.mock(StationSoh.class);
  static StationSoh mockStationSoh2Group1 = Mockito.mock(StationSoh.class);
  static CapabilitySohRollup mockCapabilitySohRollupGroup1 = Mockito
    .mock(CapabilitySohRollup.class);
  static StationSoh mockStationSoh1Group2 = Mockito.mock(StationSoh.class);
  static StationSoh mockStationSoh2Group2 = Mockito.mock(StationSoh.class);
  static CapabilitySohRollup mockCapabilitySohRollupGroup2 = Mockito
    .mock(CapabilitySohRollup.class);

  static ReactiveConsumer<StationSoh> mockStationSohReactiveConsumer = Mockito.mock(
    ReactiveConsumer.class);

  static ReactiveConsumer<CapabilitySohRollup> mockCapabilitySohRollupKafkaConsumer = Mockito.mock(
    ReactiveConsumer.class);

  static ReactiveConsumer<QuietedSohStatusChangeUpdate> mockQuietedSohStatusChangeUpdateReactiveConsumer = Mockito.mock(
    ReactiveConsumer.class);

  static ReactiveConsumer<AcknowledgedSohStatusChange> mockAcknowledgedSohStatusChangeReactiveConsumer = Mockito.mock(
    ReactiveConsumer.class);


  @BeforeAll
  static void initialize() {
    Mockito.when(mockStationSoh1Group1.getTime()).thenReturn(timeGroup1);
    Mockito.when(mockStationSoh1Group1.getStationName()).thenReturn("Station1Group1");

    Mockito.when(mockStationSoh2Group1.getTime()).thenReturn(timeGroup1);
    Mockito.when(mockCapabilitySohRollupGroup1.getTime()).thenReturn(timeGroup1);
    Mockito.when(mockCapabilitySohRollupGroup1.getForStationGroup()).thenReturn("Group1");

    Mockito.when(mockStationSoh1Group2.getTime()).thenReturn(timeGroup2);

    Mockito.when(mockStationSoh1Group2.getStationName()).thenReturn("Station1Group2");

    Mockito.when(mockStationSoh2Group2.getTime()).thenReturn(timeGroup2);

    Mockito.when(mockCapabilitySohRollupGroup2.getTime()).thenReturn(timeGroup2);
    Mockito.when(mockCapabilitySohRollupGroup2.getForStationGroup()).thenReturn("Group2");

    Mockito.when(mockStationSohReactiveConsumer.getFlux()).thenReturn(Flux.empty());
    Mockito.when(mockCapabilitySohRollupKafkaConsumer.getFlux()).thenReturn(Flux.empty());
    Mockito.when(mockAcknowledgedSohStatusChangeReactiveConsumer.getFlux()).thenReturn(Flux.empty());
    Mockito.when(mockQuietedSohStatusChangeUpdateReactiveConsumer.getFlux()).thenReturn(Flux.empty());
  }

  private static SsamReactiveKafkaUtility getSsamReactiveKafkaUtility(
    KafkaProducer.Builder<Object> reactiveProducerBuilder
  ) {

    Sinks.Many<SystemMessage> systemMessageSink = Sinks.many().multicast().onBackpressureBuffer();
    Sinks.Many<UnacknowledgedSohStatusChange> unacknowledgedSohStatusChangeSink = Sinks.many().multicast().onBackpressureBuffer();
    Sinks.Many<QuietedSohStatusChangeUpdate> quietedSohStatusChangeUpdateSink = Sinks.many().multicast().onBackpressureBuffer();

    var processingConfig = Mockito.mock(StationSohAnalysisManagerConfiguration.class);
    var uiParameters = Mockito.mock(StationSohMonitoringUiClientParameters.class);
    var stationSohMonitoringDefinition = Mockito.mock(StationSohMonitoringDefinition.class);
    var displayParameters = Mockito.mock(StationSohMonitoringDisplayParameters.class);

    Mockito.when(processingConfig.resolveDisplayParameters()).thenReturn(uiParameters);
    Mockito.when(uiParameters.getStationSohControlConfiguration())
      .thenReturn(stationSohMonitoringDefinition);
    Mockito.when(uiParameters.getStationSohMonitoringDisplayParameters())
      .thenReturn(displayParameters);
    Mockito.when(displayParameters.getAcknowledgementQuietDuration())
      .thenReturn(Duration.ofMillis(1));

    var systemConfig = Mockito.mock(SystemConfig.class);

    Arrays.stream(KafkaTopicConfigurationKeys.values())
      .forEach(key -> Mockito.when(systemConfig.getValue(key.getConfigKeyString()))
        .thenReturn(key.getDefaultValue()));

    var ssamKafkaConsumers = SsamKafkaConsumers.builder()
      .setStationSohReactiveConsumer(mockStationSohReactiveConsumer)
      .setCapabilitySohRollupReactiveConsumer(mockCapabilitySohRollupKafkaConsumer)
      .setQuietedSohStatusChangeUpdateReactiveConsumer(mockQuietedSohStatusChangeUpdateReactiveConsumer)
      .setAcknowledgedSohStatusChangeReactiveConsumer(mockAcknowledgedSohStatusChangeReactiveConsumer)
      .build();

    var ssamMessageSinks = SsamMessageSinks.builder()
      .setSystemMessageEmitterSink(systemMessageSink)
      .setUnacknowledgedSohStatusChangeSink(unacknowledgedSohStatusChangeSink)
      .setQuietedSohStatusChangeUpdateSink(quietedSohStatusChangeUpdateSink)
      .build();

    return new SsamReactiveKafkaUtility(
      ssamMessageSinks,
      processingConfig,
      systemConfig,
      ssamKafkaConsumers
    );
  }


  @RepeatedTest(10)
  void testCreateSohPackageFlux() {

    StepVerifier.create(
      SsamReactiveKafkaUtility.createSohPackageFlux(
        Flux.just(
          SohWrapper.ofStationSoh(mockStationSoh1Group1),
          SohWrapper.ofStationSoh(mockStationSoh2Group1),
          SohWrapper.ofCapabilitySohRollup(mockCapabilitySohRollupGroup1),

          SohWrapper.ofStationSoh(mockStationSoh1Group2),
          SohWrapper.ofCapabilitySohRollup(mockCapabilitySohRollupGroup2),
          SohWrapper.ofStationSoh(mockStationSoh2Group2)

        ),
        3
      )
    ).expectNext(
      SohPackage.create(
        Set.of(mockCapabilitySohRollupGroup1),
        Set.of(mockStationSoh1Group1, mockStationSoh2Group1)
      ),
      SohPackage.create(
        Set.of(mockCapabilitySohRollupGroup2),
        Set.of(mockStationSoh1Group2, mockStationSoh2Group2)
      )
    ).verifyComplete();
  }

  @RepeatedTest(10)
  void testCreateMaterializedViewFlux() {

    var mockUiStationAndStationGroups1 = Mockito.mock(UiStationAndStationGroups.class);
    Mockito.when(mockUiStationAndStationGroups1.getIsUpdateResponse()).thenReturn(true);

    var mockUiStationAndStationGroups2 = Mockito.mock(UiStationAndStationGroups.class);
    Mockito.when(mockUiStationAndStationGroups2.getIsUpdateResponse()).thenReturn(false);

    MaterializedViewProcessor mockProcessor = x -> {
      if (x.getCapabilitySohRollups().iterator().next().getTime().equals(timeGroup1)) {
        return List.of(mockUiStationAndStationGroups1);
      } else if (x.getCapabilitySohRollups().iterator().next().getTime().equals(timeGroup2)) {
        return List.of(mockUiStationAndStationGroups2);
      } else {
        throw new IllegalStateException("Something is wrong with this test!");
      }
    };

    StepVerifier.create(
      SsamReactiveKafkaUtility.createMaterializedViewFlux(
        Flux.just(
          SohPackage.create(
            Set.of(mockCapabilitySohRollupGroup1),
            Set.of(mockStationSoh1Group1, mockStationSoh2Group1)
          ),
          SohPackage.create(
            Set.of(mockCapabilitySohRollupGroup2),
            Set.of(mockStationSoh1Group2, mockStationSoh2Group2)
          )
        ),
        mockProcessor
      )
    ).assertNext(uiStationAndStationGroups -> assertTrue(uiStationAndStationGroups.getIsUpdateResponse())
    ).assertNext(uiStationAndStationGroups -> Assertions
      .assertFalse(uiStationAndStationGroups.getIsUpdateResponse())
    ).verifyComplete();
  }

  @RepeatedTest(10)
  void testCreateSohWrapperFlux() {

    Map<String, StationSoh> latestStationSohByStation = new HashMap<>();
    Map<String, CapabilitySohRollup> latestCapabilitySohRollupByStationGroup = new HashMap<>();

    StepVerifier.withVirtualTime(
        () -> SsamReactiveKafkaUtility.createSohWrapperFlux(
          //
          // Create the following order:
          //  - Soh2 for Group 1
          //  - 100 ms later, Capability for Group 1
          //  - Soh1 for Group 2, calcIntervalMillis after Soh2 for Group 1
          //  - 100 ms later, Capability for Group 2
          //
          Flux.just(
            mockStationSoh2Group1,
            mockStationSoh1Group2
          ).delayElements(Duration.ofMillis(calcIntervalMillis)),
          Flux.just(
              mockCapabilitySohRollupGroup1,
              mockCapabilitySohRollupGroup2
            ).delaySequence(Duration.ofMillis(100))
            .delayElements(Duration.ofMillis(calcIntervalMillis)),
          latestStationSohByStation,
          latestCapabilitySohRollupByStationGroup
        )
      )
      .thenAwait(Duration.ofMillis(calcIntervalMillis * 2L).plusMillis(100))
      .assertNext(sohWrapper -> {
          Assertions.assertNotNull(
            sohWrapper.stationSoh()
          );

          assertEquals(
            mockStationSoh2Group1.getStationName(),
            sohWrapper.stationSoh().getStationName()
          );
        }
      ).assertNext(sohWrapper -> {
          Assertions.assertNotNull(
            sohWrapper.capabilitySohRollup()
          );

          assertEquals(
            mockCapabilitySohRollupGroup1.getForStationGroup(),
            sohWrapper.capabilitySohRollup().getForStationGroup()
          );
        }
      ).assertNext(sohWrapper -> {
          Assertions.assertNotNull(
            sohWrapper.stationSoh()
          );

          assertEquals(
            mockStationSoh1Group2.getStationName(),
            sohWrapper.stationSoh().getStationName()
          );
        }
      ).assertNext(sohWrapper -> {
          Assertions.assertNotNull(
            sohWrapper.capabilitySohRollup()
          );

          assertEquals(
            mockCapabilitySohRollupGroup2.getForStationGroup(),
            sohWrapper.capabilitySohRollup().getForStationGroup()
          );
        }
      ).verifyComplete();

    Assertions.assertSame(
      mockCapabilitySohRollupGroup1,
      latestCapabilitySohRollupByStationGroup.get(
        mockCapabilitySohRollupGroup1.getForStationGroup()
      )
    );

    Assertions.assertSame(
      mockCapabilitySohRollupGroup2,
      latestCapabilitySohRollupByStationGroup.get(
        mockCapabilitySohRollupGroup2.getForStationGroup()
      )
    );

    Assertions.assertSame(
      mockStationSoh1Group2,
      latestStationSohByStation.get(mockStationSoh1Group2.getStationName())
    );

    Assertions.assertSame(
      mockStationSoh2Group1,
      latestStationSohByStation.get(mockStationSoh2Group1.getStationName())
    );
  }

  @Test
  void testSohPackageFluxEmptiness() {

    //
    // Absolutely no data has arrived, so don't produce any SohPackage
    //
    StepVerifier.create(
        SsamReactiveKafkaUtility.createSohPackageFlux(
          Flux.empty(),
          2
        )
      ).expectNextCount(0)
      .verifyComplete();

    //
    // A capability rollup arrived but no StationSoh, and that is all, so don't produce any SohPackage
    //
    StepVerifier.create(
        SsamReactiveKafkaUtility.createSohPackageFlux(
          Flux.just(SohWrapper.ofCapabilitySohRollup(mockCapabilitySohRollupGroup1)),
          2
        )
      ).expectNextCount(0)
      .verifyComplete();

    //
    // One calculation interval had no data for StationSoh, the next had ALL data,
    // so produce exactly one SohPackage for the second calc. interval
    //
    StepVerifier.create(
        SsamReactiveKafkaUtility.createSohPackageFlux(
          Flux.just(
            // calc interval 1 - no StationSoh
            SohWrapper.ofCapabilitySohRollup(mockCapabilitySohRollupGroup1),

            // calc interval 2
            SohWrapper.ofStationSoh(mockStationSoh1Group2),
            SohWrapper.ofCapabilitySohRollup(mockCapabilitySohRollupGroup2)
          ),
          2
        )
      ).expectNextCount(1)
      .verifyComplete();
  }


  @Test
  void testInstantiation() {

    var mockSinks = Mockito.mock(SsamMessageSinks.class);
    var mockStationSohAnalysisManagerConfiguration = Mockito.mock(StationSohAnalysisManagerConfiguration.class);
    var mockSystemConfig = Mockito.mock(SystemConfig.class);
    var mockSsamKafkaConsumers = Mockito.mock(SsamKafkaConsumers.class);
    Assertions.assertDoesNotThrow(() ->
      new SsamReactiveKafkaUtility(
        mockSinks,
        mockStationSohAnalysisManagerConfiguration,
        mockSystemConfig,
        mockSsamKafkaConsumers));
  }
}




