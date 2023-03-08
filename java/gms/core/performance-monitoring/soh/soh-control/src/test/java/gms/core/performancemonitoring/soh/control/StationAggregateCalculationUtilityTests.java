package gms.core.performancemonitoring.soh.control;

import gms.core.performancemonitoring.soh.control.configuration.ChannelSohDefinition;
import gms.core.performancemonitoring.soh.control.configuration.StationSohDefinition;
import gms.core.performancemonitoring.soh.control.configuration.TimeWindowDefinition;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;
import gms.shared.frameworks.osd.coi.soh.ChannelSoh;
import gms.shared.frameworks.osd.coi.soh.DurationStationAggregate;
import gms.shared.frameworks.osd.coi.soh.PercentSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.PercentStationAggregate;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.SohStatus;
import gms.shared.frameworks.osd.coi.soh.StationAggregate;
import gms.shared.frameworks.osd.coi.soh.StationAggregateType;
import gms.shared.frameworks.osd.coi.waveforms.WaveformSummary;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class StationAggregateCalculationUtilityTests {

  @ParameterizedTest
  @MethodSource("lagTestSource")
  void testLag(
    Map<String, Set<WaveformSummaryAndReceptionTime>> waveformSummaryAndReceptionTimeMap,
    StationSohDefinition stationSohDefinition,
    DurationStationAggregate expectedLagAggregate,
    Instant now
  ) {

    var cache = new AcquiredSampleTimesByChannel();
    cache.setLatestChannelToEndTime(Map.of());

    var utility = new StationAggregateCalculationUtility(cache, now);

    Set<StationAggregate<?>> stationAggregates = utility.buildStationAggregateMono(
      Mono.just(waveformSummaryAndReceptionTimeMap),
      Mono.just(Map.of()),
      Mono.just(Set.of()),
      stationSohDefinition
    ).block();

    var actualLagAggregateOpt = stationAggregates.stream().filter(
      stationAggregate -> stationAggregate.getAggregateType() == StationAggregateType.LAG
    ).findFirst();

    Assertions.assertTrue(
      actualLagAggregateOpt.isPresent()
    );

    Assertions.assertEquals(
      expectedLagAggregate,
      actualLagAggregateOpt.get()
    );
  }

  private static Stream<Arguments> lagTestSource() {

    var waveformSummary1 = WaveformSummary.from(
      "MyLovelyStation.channelName",
      Instant.EPOCH,
      Instant.EPOCH
    );

    var waveformSummary2 = WaveformSummary.from(
      "MyLovelyStation.channelName2",
      Instant.EPOCH,
      Instant.EPOCH
    );

    return Stream.of(
      Arguments.arguments(
        Map.of(
          "MyLovelyStation.channelName",
          Set.of(
            WaveformSummaryAndReceptionTime
              .create(waveformSummary1, Instant.ofEpochMilli(10)),
            WaveformSummaryAndReceptionTime
              .create(waveformSummary1, Instant.ofEpochMilli(20))
          )
        ),
        getMockStationSohDefinition(
          "MyLovelyStation",
          Set.of("MyLovelyStation.channelName"),
          Map.of(
            SohMonitorType.LAG,
            TimeWindowDefinition.create(
              Duration.ofSeconds(2000),
              Duration.ofSeconds(0)
            )
          )
        ),
        DurationStationAggregate.from(
          Duration.ofMillis(15),
          StationAggregateType.LAG
        ),
        Instant.ofEpochMilli(20)
      ),

      Arguments.arguments(
        Map.of(
          "MyLovelyStation.channelName1",
          Set.of(
            WaveformSummaryAndReceptionTime
              .create(waveformSummary1, Instant.ofEpochMilli(10))
          ),
          "MyLovelyStation.channelName2",
          Set.of(
            WaveformSummaryAndReceptionTime
              .create(waveformSummary2, Instant.ofEpochMilli(20))
          )
        ),
        getMockStationSohDefinition(
          "MyLovelyStation",
          Set.of("MyLovelyStation.channelName1", "MyLovelyStation.channelName2"),
          Map.of(
            SohMonitorType.LAG,
            TimeWindowDefinition.create(
              Duration.ofSeconds(2000),
              Duration.ofSeconds(0)
            )
          )
        ),
        DurationStationAggregate.from(
          Duration.ofMillis(15),
          StationAggregateType.LAG
        ),
        Instant.ofEpochMilli(20)
      ),

      Arguments.arguments(
        Map.of(
          "MyLovelyStation.channelName1",
          Set.of(
            WaveformSummaryAndReceptionTime
              .create(waveformSummary1, Instant.ofEpochMilli(10)),
            WaveformSummaryAndReceptionTime
              .create(waveformSummary1, Instant.ofEpochMilli(20))
          ),
          "MyLovelyStation.channelName2",
          Set.of(
            WaveformSummaryAndReceptionTime
              .create(waveformSummary2, Instant.ofEpochMilli(20))
          )
        ),
        getMockStationSohDefinition(
          "MyLovelyStation",
          Set.of("MyLovelyStation.channelName1", "MyLovelyStation.channelName2"),
          Map.of(
            SohMonitorType.LAG,
            TimeWindowDefinition.create(
              Duration.ofSeconds(2000),
              Duration.ofSeconds(0)
            )
          )
        ),
        DurationStationAggregate.from(
          Duration.parse("PT0.016666666S"),
          StationAggregateType.LAG
        ),
        Instant.ofEpochMilli(20)
      )
    );
  }

  @Test
  void testMissingAveraging() {

    var mockedChannelSoh1 = Mockito.mock(ChannelSoh.class);
    var mockedChannelSoh2 = Mockito.mock(ChannelSoh.class);
    var mockedChannelSoh3 = Mockito.mock(ChannelSoh.class);
    var mockedChannelSoh4 = Mockito.mock(ChannelSoh.class);
    var mockedChannelSoh5 = Mockito.mock(ChannelSoh.class);
    var mockedChannelSoh6 = Mockito.mock(ChannelSoh.class);

    Mockito.when(mockedChannelSoh1.getChannelName()).thenReturn("channel1");
    Mockito.when(mockedChannelSoh2.getChannelName()).thenReturn("channel2");
    Mockito.when(mockedChannelSoh3.getChannelName()).thenReturn("channel3");
    Mockito.when(mockedChannelSoh4.getChannelName()).thenReturn("channel4");
    Mockito.when(mockedChannelSoh5.getChannelName()).thenReturn("channel5");
    Mockito.when(mockedChannelSoh6.getChannelName()).thenReturn("channel6");

    Map<SohMonitorType, SohMonitorValueAndStatus<?>> value1 = Map.of(SohMonitorType.MISSING,
      PercentSohMonitorValueAndStatus.from(45.0, SohStatus.GOOD, SohMonitorType.MISSING));

    Map<SohMonitorType, SohMonitorValueAndStatus<?>> value2 = Map.of(SohMonitorType.MISSING,
      PercentSohMonitorValueAndStatus.from(100.00, SohStatus.GOOD, SohMonitorType.MISSING));

    Map<SohMonitorType, SohMonitorValueAndStatus<?>> value3 = Map.of(SohMonitorType.MISSING,
      PercentSohMonitorValueAndStatus.from(0.0, SohStatus.GOOD, SohMonitorType.MISSING));

    Map<SohMonitorType, SohMonitorValueAndStatus<?>> value4 = Map.of(SohMonitorType.MISSING,
      PercentSohMonitorValueAndStatus.from(89.0, SohStatus.GOOD, SohMonitorType.MISSING));

    Map<SohMonitorType, SohMonitorValueAndStatus<?>> value5 = Map.of(SohMonitorType.MISSING,
      PercentSohMonitorValueAndStatus.from(100.00, SohStatus.GOOD, SohMonitorType.MISSING));

    Map<SohMonitorType, SohMonitorValueAndStatus<?>> value6 = Map.of(SohMonitorType.MISSING,
      PercentSohMonitorValueAndStatus.from(55.0, SohStatus.GOOD, SohMonitorType.MISSING));

    var expectedAverage = PercentStationAggregate.from(66.8, StationAggregateType.MISSING);

    Mockito.when(mockedChannelSoh1.getSohMonitorValueAndStatusMap()).thenReturn(value1);
    Mockito.when(mockedChannelSoh2.getSohMonitorValueAndStatusMap()).thenReturn(value2);
    Mockito.when(mockedChannelSoh3.getSohMonitorValueAndStatusMap()).thenReturn(value3);
    Mockito.when(mockedChannelSoh4.getSohMonitorValueAndStatusMap()).thenReturn(value4);
    Mockito.when(mockedChannelSoh5.getSohMonitorValueAndStatusMap()).thenReturn(value5);
    Mockito.when(mockedChannelSoh6.getSohMonitorValueAndStatusMap()).thenReturn(value6);

    var stationSohDefinition = Mockito.mock(StationSohDefinition.class);
    var m = Map.of(SohMonitorType.MISSING,
      Set.of("channel1", "channel2", "channel3", "channel4", "channel5"));

    Mockito.when(stationSohDefinition.getChannelsBySohMonitorType()).thenReturn(m);

    var cache = new AcquiredSampleTimesByChannel();
    cache.setLatestChannelToEndTime(Map.of());
    var now = Instant.now();

    var utility = new StationAggregateCalculationUtility(cache, now);

    Set<StationAggregate<?>> stationAggregates = utility.buildStationAggregateMono(
      Mono.just(Map.of()),
      Mono.just(Map.of()),
      Mono.just(
        Set.of(mockedChannelSoh1, mockedChannelSoh2, mockedChannelSoh3, mockedChannelSoh4,
          mockedChannelSoh5, mockedChannelSoh6)),
      stationSohDefinition
    ).block();

    var actualMissingAggregateOpt = stationAggregates.stream().filter(
      stationAggregate -> stationAggregate.getAggregateType()
        == StationAggregateType.MISSING
    ).findFirst();

    Assertions.assertTrue(
      actualMissingAggregateOpt.isPresent()
    );

    Assertions.assertEquals(
      expectedAverage,
      actualMissingAggregateOpt.get()
    );
  }

  @ParameterizedTest
  @MethodSource("envIssuesTestSource")
  void testEnvIssues(
    Map<String, Set<AcquiredChannelEnvironmentIssueBoolean>> aceiMap,
    StationSohDefinition stationSohDefinition,
    PercentStationAggregate expectedPercentAggregate,
    Instant now
  ) {
    var waveformSummaryDummy = WaveformSummary.from(
      (String) aceiMap.keySet().toArray()[0],
      Instant.EPOCH,
      Instant.EPOCH
    );

    var cache = new AcquiredSampleTimesByChannel();
    cache.setLatestChannelToEndTime(Map.of());

    var utility = new StationAggregateCalculationUtility(cache, now);

    Set<StationAggregate<?>> stationAggregates = utility.buildStationAggregateMono(
      Mono.just(Map.of(
        (String) aceiMap.keySet().toArray()[0],
        Set.of(WaveformSummaryAndReceptionTime.create(
          waveformSummaryDummy,
          Instant.ofEpochMilli(100)
        ))
      )),
      Mono.just(aceiMap),
      Mono.just(Set.of()),
      stationSohDefinition
    ).block();

    var actualLagAggregateOpt = stationAggregates.stream().filter(
      stationAggregate -> stationAggregate.getAggregateType()
        == StationAggregateType.ENVIRONMENTAL_ISSUES
    ).findFirst();

    Assertions.assertTrue(
      actualLagAggregateOpt.isPresent()
    );

    Assertions.assertEquals(
      expectedPercentAggregate,
      actualLagAggregateOpt.get()
    );
  }

  private static Stream<Arguments> envIssuesTestSource() {

    return Stream.of(
      Arguments.arguments(
        Map.of(
          "StationA.ChannelA",
          Set.of(
            AcquiredChannelEnvironmentIssueBoolean.from(
              "StationA.ChannelA",
              AcquiredChannelEnvironmentIssueType.CLIPPED,
              Instant.ofEpochMilli(20),
              Instant.ofEpochMilli(40),
              true
            )
          )
        ),
        getMockStationSohDefinition(
          "StationA",
          Set.of("StationA.ChannelA"),
          Map.of(
            SohMonitorType.ENV_CLIPPED,
            TimeWindowDefinition.create(
              Duration.ofMillis(60),
              Duration.ZERO
            )
          )
        ),
        PercentStationAggregate.from(
          100.0,
          StationAggregateType.ENVIRONMENTAL_ISSUES
        ),
        Instant.ofEpochMilli(60)
      ),

      //
      // Two of same type with different values, same channel
      //
      Arguments.arguments(
        Map.of(
          "StationA.ChannelA",
          Set.of(
            AcquiredChannelEnvironmentIssueBoolean.from(
              "StationA.ChannelA",
              AcquiredChannelEnvironmentIssueType.CLIPPED,
              Instant.ofEpochMilli(20),
              Instant.ofEpochMilli(40),
              true
            ),
            AcquiredChannelEnvironmentIssueBoolean.from(
              "StationA.ChannelA",
              AcquiredChannelEnvironmentIssueType.CLIPPED,
              Instant.ofEpochMilli(21),
              Instant.ofEpochMilli(39),
              false
            )
          )
        ),
        getMockStationSohDefinition(
          "StationA",
          Set.of("StationA.ChannelA"),
          Map.of(
            SohMonitorType.ENV_CLIPPED,
            TimeWindowDefinition.create(
              Duration.ofMillis(60),
              Duration.ZERO
            )
          )
        ),
        PercentStationAggregate.from(
          50.0,
          StationAggregateType.ENVIRONMENTAL_ISSUES
        ),
        Instant.ofEpochMilli(60)
      ),

      //
      // Two of different type with different values, same channel
      //
      Arguments.arguments(
        Map.of(
          "StationA.ChannelA",
          Set.of(
            AcquiredChannelEnvironmentIssueBoolean.from(
              "StationA.ChannelA",
              AcquiredChannelEnvironmentIssueType.CLIPPED,
              Instant.ofEpochMilli(20),
              Instant.ofEpochMilli(40),
              true
            ),
            AcquiredChannelEnvironmentIssueBoolean.from(
              "StationA.ChannelA",
              AcquiredChannelEnvironmentIssueType.AUTHENTICATION_SEAL_BROKEN,
              Instant.ofEpochMilli(21),
              Instant.ofEpochMilli(39),
              false
            )
          )
        ),
        getMockStationSohDefinition(
          "StationA",
          Set.of("StationA.ChannelA"),
          Map.of(
            SohMonitorType.ENV_CLIPPED,
            TimeWindowDefinition.create(
              Duration.ofMillis(60),
              Duration.ZERO
            ),
            SohMonitorType.ENV_AUTHENTICATION_SEAL_BROKEN,
            TimeWindowDefinition.create(
              Duration.ofMillis(50),
              Duration.ZERO
            )
          )
        ),
        PercentStationAggregate.from(
          50.0,
          StationAggregateType.ENVIRONMENTAL_ISSUES
        ),
        Instant.ofEpochMilli(60)
      ),
      //
      // Two of different type with different values, same channel, one monitor type
      // not in sohMonitorTypesForRollup
      //
      Arguments.arguments(
        Map.of(
          "StationA.ChannelA",
          Set.of(
            AcquiredChannelEnvironmentIssueBoolean.from(
              "StationA.ChannelA",
              AcquiredChannelEnvironmentIssueType.CLIPPED,
              Instant.ofEpochMilli(20),
              Instant.ofEpochMilli(40),
              true
            ),
            AcquiredChannelEnvironmentIssueBoolean.from(
              "StationA.ChannelA",
              AcquiredChannelEnvironmentIssueType.VAULT_DOOR_OPENED,
              Instant.ofEpochMilli(21),
              Instant.ofEpochMilli(39),
              false
            )
          )
        ),
        getMockStationSohDefinition(
          "StationA",
          Set.of("StationA.ChannelA"),
          Map.of(
            SohMonitorType.ENV_CLIPPED,
            TimeWindowDefinition.create(
              Duration.ofMillis(60),
              Duration.ZERO
            ),
            SohMonitorType.ENV_AUTHENTICATION_SEAL_BROKEN,
            TimeWindowDefinition.create(
              Duration.ofMillis(50),
              Duration.ZERO
            )
          )
        ),
        PercentStationAggregate.from(
          100.0,
          StationAggregateType.ENVIRONMENTAL_ISSUES
        ),
        Instant.ofEpochMilli(60)
      ),
      //
      // Two of different type with different values, same channel, but one
      // outside calculation window (large back-off duration)
      //
      Arguments.arguments(
        Map.of(
          "StationA.ChannelA",
          Set.of(
            AcquiredChannelEnvironmentIssueBoolean.from(
              "StationA.ChannelA",
              AcquiredChannelEnvironmentIssueType.CLIPPED,
              Instant.ofEpochMilli(20),
              Instant.ofEpochMilli(40),
              true
            ),
            AcquiredChannelEnvironmentIssueBoolean.from(
              "StationA.ChannelA",
              AcquiredChannelEnvironmentIssueType.AUTHENTICATION_SEAL_BROKEN,
              Instant.ofEpochMilli(21),
              Instant.ofEpochMilli(39),
              false
            )
          )
        ),
        getMockStationSohDefinition(
          "StationA",
          Set.of("StationA.ChannelA"),
          Map.of(
            SohMonitorType.ENV_CLIPPED,
            TimeWindowDefinition.create(
              Duration.ofMillis(60),
              Duration.ZERO
            ),
            SohMonitorType.ENV_AUTHENTICATION_SEAL_BROKEN,
            TimeWindowDefinition.create(
              Duration.ofMillis(50),
              Duration.ofMillis(61)
            )
          )
        ),
        PercentStationAggregate.from(
          100.0,
          StationAggregateType.ENVIRONMENTAL_ISSUES
        ),
        Instant.ofEpochMilli(60)
      ),

      //
      // Four of different type with different values, different channels
      //
      Arguments.arguments(
        Map.of(
          "StationA.ChannelA",
          Set.of(
            AcquiredChannelEnvironmentIssueBoolean.from(
              "StationA.ChannelA",
              AcquiredChannelEnvironmentIssueType.CLIPPED,
              Instant.ofEpochMilli(20),
              Instant.ofEpochMilli(40),
              true
            ),
            AcquiredChannelEnvironmentIssueBoolean.from(
              "StationA.ChannelA",
              AcquiredChannelEnvironmentIssueType.BACKUP_POWER_UNSTABLE,
              Instant.ofEpochMilli(20),
              Instant.ofEpochMilli(40),
              true
            ),
            AcquiredChannelEnvironmentIssueBoolean.from(
              "StationA.ChannelA",
              AcquiredChannelEnvironmentIssueType.AMPLIFIER_SATURATION_DETECTED,
              Instant.ofEpochMilli(20),
              Instant.ofEpochMilli(40),
              true
            )
          ),
          "StationA.ChannelB",
          Set.of(
            AcquiredChannelEnvironmentIssueBoolean.from(
              "ChannelB",
              AcquiredChannelEnvironmentIssueType.AUTHENTICATION_SEAL_BROKEN,
              Instant.ofEpochMilli(21),
              Instant.ofEpochMilli(39),
              false
            )
          )
        ),
        getMockStationSohDefinition(
          "StationA",
          Set.of("StationA.ChannelA", "StationA.ChannelB"),
          Map.of(
            SohMonitorType.ENV_CLIPPED,
            TimeWindowDefinition.create(
              Duration.ofMillis(60),
              Duration.ZERO
            ),
            SohMonitorType.ENV_BACKUP_POWER_UNSTABLE,
            TimeWindowDefinition.create(
              Duration.ofMillis(60),
              Duration.ZERO
            ),
            SohMonitorType.ENV_AMPLIFIER_SATURATION_DETECTED,
            TimeWindowDefinition.create(
              Duration.ofMillis(60),
              Duration.ZERO
            ),
            SohMonitorType.ENV_AUTHENTICATION_SEAL_BROKEN,
            TimeWindowDefinition.create(
              Duration.ofMillis(50),
              Duration.ZERO
            )
          )
        ),
        PercentStationAggregate.from(
          100.0,
          StationAggregateType.ENVIRONMENTAL_ISSUES
        ),
        Instant.ofEpochMilli(60)
      ),

      Arguments.arguments(
        Map.of(
          "StationA.ChannelA",
          Set.of(
            AcquiredChannelEnvironmentIssueBoolean.from(
              "StationA.ChannelA",
              AcquiredChannelEnvironmentIssueType.CLIPPED,
              Instant.ofEpochMilli(20),
              Instant.ofEpochMilli(40),
              true
            ),
            AcquiredChannelEnvironmentIssueBoolean.from(
              "StationA.ChannelA",
              AcquiredChannelEnvironmentIssueType.BACKUP_POWER_UNSTABLE,
              Instant.ofEpochMilli(20),
              Instant.ofEpochMilli(40),
              true
            ),
            AcquiredChannelEnvironmentIssueBoolean.from(
              "StationA.ChannelA",
              AcquiredChannelEnvironmentIssueType.AMPLIFIER_SATURATION_DETECTED,
              Instant.ofEpochMilli(20),
              Instant.ofEpochMilli(40),
              true
            )
          ),
          "StationA.ChannelB",
          Set.of(
            AcquiredChannelEnvironmentIssueBoolean.from(
              "StationA.ChannelB",
              AcquiredChannelEnvironmentIssueType.AUTHENTICATION_SEAL_BROKEN,
              Instant.ofEpochMilli(21),
              Instant.ofEpochMilli(39),
              false
            )
          )
        ),
        getMockStationSohDefinition(
          "StationA",
          Set.of("StationA.ChannelA", "StationA.ChannelB"),
          Map.of(
            SohMonitorType.ENV_CLIPPED,
            TimeWindowDefinition.create(
              Duration.ofMillis(60),
              Duration.ZERO
            ),
            SohMonitorType.ENV_BACKUP_POWER_UNSTABLE,
            TimeWindowDefinition.create(
              Duration.ofMillis(60),
              Duration.ZERO
            ),
            SohMonitorType.ENV_AMPLIFIER_SATURATION_DETECTED,
            TimeWindowDefinition.create(
              Duration.ofMillis(60),
              Duration.ZERO
            ),
            SohMonitorType.ENV_AUTHENTICATION_SEAL_BROKEN,
            TimeWindowDefinition.create(
              Duration.ofMillis(50),
              Duration.ZERO
            )
          )
        ),
        PercentStationAggregate.from(
          (2.0 / 3.0) * 100.0,
          StationAggregateType.ENVIRONMENTAL_ISSUES
        ),
        Instant.ofEpochMilli(60)
      ),

      Arguments.arguments(
        Map.of(
          "StationA.ChannelA",
          Set.of()
        ),
        getMockStationSohDefinition(
          "StationA",
          Set.of("StationA.ChannelA"),
          Map.of(
            SohMonitorType.ENV_CLIPPED,
            TimeWindowDefinition.create(
              Duration.ofMillis(60),
              Duration.ZERO
            )
          )
        ),
        PercentStationAggregate.from(
          null,
          StationAggregateType.ENVIRONMENTAL_ISSUES
        ),
        Instant.ofEpochMilli(60)
      )
    );
  }

  @ParameterizedTest
  @MethodSource("timelinessTestSource")
  void testTimeliness(
    Map<String, Set<WaveformSummaryAndReceptionTime>> waveformSummaryAndReceptionTimeMap,
    StationSohDefinition stationSohDefinition,
    DurationStationAggregate expectedTimelinessAggregate,
    Instant now
  ) {

    var cache = new AcquiredSampleTimesByChannel();

    waveformSummaryAndReceptionTimeMap.forEach(
      (k, v) -> v.forEach(
        waveformSummaryAndReceptionTime -> cache.update(
          k, waveformSummaryAndReceptionTime.getWaveformSummary().getEndTime()
        )
      )
    );

    var utility = new StationAggregateCalculationUtility(cache, now);

    var stationAggregates = utility.buildStationAggregateMono(
      Mono.just(waveformSummaryAndReceptionTimeMap),
      Mono.just(Map.of()),
      Mono.just(Set.of()),
      stationSohDefinition
    ).block();

    var actualLagAggregateOpt = stationAggregates.stream().filter(
      stationAggregate -> stationAggregate.getAggregateType()
        == StationAggregateType.TIMELINESS
    ).findFirst();

    Assertions.assertTrue(
      actualLagAggregateOpt.isPresent()
    );

    Assertions.assertEquals(
      expectedTimelinessAggregate,
      actualLagAggregateOpt.get()
    );
  }

  private static Stream<Arguments> timelinessTestSource() {

    var waveformSummary1 = WaveformSummary.from(
      "MyLovelyStation.channelName",
      Instant.EPOCH,
      Instant.ofEpochMilli(20)
    );

    var waveformSummary2 = WaveformSummary.from(
      "MyLovelyStation.channelName",
      Instant.EPOCH,
      Instant.ofEpochMilli(30)
    );

    var waveformSummary3 = WaveformSummary.from(
      "MyLovelyStation.channelNameA",
      Instant.EPOCH,
      Instant.ofEpochMilli(50)
    );

    return Stream.of(
      Arguments.arguments(
        Map.of(
          "MyLovelyStation.channelName",
          Set.of(
            WaveformSummaryAndReceptionTime
              .create(waveformSummary1, Instant.ofEpochMilli(100)),
            WaveformSummaryAndReceptionTime
              .create(waveformSummary2, Instant.ofEpochMilli(200))
          )
        ),
        getMockStationSohDefinition(
          "MyLovelyStation",
          Set.of("MyLovelyStation.channelName"),
          Map.of()
        ),
        DurationStationAggregate.from(
          Duration.ofMillis(20),
          StationAggregateType.TIMELINESS
        ),
        Instant.ofEpochMilli(50)
      ),

      Arguments.arguments(
        Map.of(
          "MyLovelyStation.channelName",
          Set.of(
            WaveformSummaryAndReceptionTime
              .create(waveformSummary1, Instant.ofEpochMilli(100)),
            WaveformSummaryAndReceptionTime
              .create(waveformSummary2, Instant.ofEpochMilli(200))
          ),
          "MyLovelyStation.channelNameA",
          Set.of(
            WaveformSummaryAndReceptionTime
              .create(waveformSummary3, Instant.ofEpochMilli(100))
          )
        ),
        getMockStationSohDefinition(
          "MyLovelyStation",
          Set.of("MyLovelyStation.channelName", "MyLovelyStation.channelNameA"),
          Map.of()
        ),
        DurationStationAggregate.from(
          Duration.ofMillis(0),
          StationAggregateType.TIMELINESS
        ),
        Instant.ofEpochMilli(50)
      )
    );
  }

  /**
   * Cannot simply use Assertions.assertEquals of these, since a small delta in the values will make it fail.
   *
   * @param expected
   * @param actual
   */
  private static void assertEqual(PercentStationAggregate expected,
    PercentStationAggregate actual) {

    Assertions.assertSame(expected.getAggregateType(), actual.getAggregateType());
    Assertions.assertSame(expected.stationValueType(), actual.stationValueType());

    Assertions.assertEquals(expected.getValue().isPresent(), actual.getValue().isPresent());

    if (expected.getValue().isPresent()) {
      double expectedValue = expected.getValue().get();
      double actualValue = actual.getValue().get();
      Assertions.assertEquals(expectedValue, actualValue, 1e-9);
    }
  }

  private static StationSohDefinition getMockStationSohDefinition(
    String stationName,
    Set<String> channelNames,
    Map<SohMonitorType, TimeWindowDefinition> timeWindowDefinitionMap
  ) {

    StationSohDefinition mockStationSohDefinition = Mockito.mock(StationSohDefinition.class);

    Mockito.when(mockStationSohDefinition.getStationName()).thenReturn(stationName);

    //ChannelSohDefinition mockChannelSohDefinition = Mockito.mock(ChannelSohDefinition.class);

    Map<String, ChannelSohDefinition> fakeChannelSohDefinitionMap = channelNames.stream().map(
      channelName -> {
        ChannelSohDefinition mockChannelSohDefinition = Mockito.mock(ChannelSohDefinition.class);

        Mockito.when(mockChannelSohDefinition.getChannelName()).thenReturn(channelName);

        return Map.entry(
          channelName,
          mockChannelSohDefinition
        );
      }
    ).collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    Mockito.when(mockStationSohDefinition.getChannelDefinitionMap()).thenReturn(
      fakeChannelSohDefinitionMap
    );

    Mockito.when(mockStationSohDefinition.getChannelSohDefinitions()).thenReturn(
      new HashSet<>(fakeChannelSohDefinitionMap.values())
    );

    var channelsByMonitorType = new HashMap<SohMonitorType, Set<String>>();

    //
    // Just associate all types with all channels
    //
    channelsByMonitorType.putAll(
      Map.of(
        SohMonitorType.LAG, channelNames,
        SohMonitorType.TIMELINESS, channelNames,
        SohMonitorType.MISSING, channelNames
      )
    );

    channelsByMonitorType.putAll(
      SohMonitorType.validTypes().stream().filter(SohMonitorType::isEnvironmentIssue)
        .map(
          sohMonitorType -> Map.entry(
            sohMonitorType,
            channelNames
          )
        ).collect(Collectors.toMap(Entry::getKey, Entry::getValue))
    );

    Mockito.when(mockStationSohDefinition.getChannelsBySohMonitorType()).thenReturn(
      channelsByMonitorType
    );

    //
    // Add all monitor types except one to sohMonitorTypesForRollup
    //
    var sohMonitortypesForRollup = SohMonitorType.validTypes().stream()
      .filter(SohMonitorType::isEnvironmentIssue).collect(Collectors.toSet());

    sohMonitortypesForRollup.remove(SohMonitorType.ENV_VAULT_DOOR_OPENED);

    Mockito.when(mockStationSohDefinition.getSohMonitorTypesForRollup())
      .thenReturn(sohMonitortypesForRollup);

    var augmentedTimeWindowDefinitionMap = new HashMap<SohMonitorType, TimeWindowDefinition>();

    //
    // Assuming here that the definitions always contain all SohMonitorTypes, because that
    // should be what the config mechanism is doing.
    //
    augmentedTimeWindowDefinitionMap.put(
      SohMonitorType.LAG,
      TimeWindowDefinition.create(
        Duration.ZERO,
        Duration.ZERO
      )
    );

    augmentedTimeWindowDefinitionMap.put(
      SohMonitorType.MISSING,
      TimeWindowDefinition.create(
        Duration.ZERO,
        Duration.ZERO
      )
    );

    augmentedTimeWindowDefinitionMap.put(
      SohMonitorType.TIMELINESS,
      TimeWindowDefinition.create(
        Duration.ZERO,
        Duration.ZERO
      )
    );

    augmentedTimeWindowDefinitionMap.putAll(
      timeWindowDefinitionMap
    );

    Mockito.when(mockStationSohDefinition.getTimeWindowBySohMonitorType()).thenReturn(
      augmentedTimeWindowDefinitionMap
    );

    return mockStationSohDefinition;
  }
}
