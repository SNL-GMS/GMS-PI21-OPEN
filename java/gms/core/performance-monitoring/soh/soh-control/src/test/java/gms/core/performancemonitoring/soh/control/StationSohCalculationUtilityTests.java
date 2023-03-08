package gms.core.performancemonitoring.soh.control;

import gms.core.performancemonitoring.soh.control.configuration.ChannelSohDefinition;
import gms.core.performancemonitoring.soh.control.configuration.StationSohDefinition;
import gms.shared.frameworks.osd.coi.soh.AcquiredStationSohExtract;
import gms.shared.frameworks.osd.coi.soh.ChannelSoh;
import gms.shared.frameworks.osd.coi.soh.DurationSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.PercentSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohStatus;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gms.core.performancemonitoring.soh.control.TestFixture.ChannelSohSets.CHANNEL_NAME_1;
import static gms.core.performancemonitoring.soh.control.TestFixture.ChannelSohSets.CHANNEL_NAME_2;
import static gms.core.performancemonitoring.soh.control.TestFixture.ChannelSohSets.CHANNEL_NAME_3;
import static gms.core.performancemonitoring.soh.control.TestFixture.ChannelSohSets.STATION_NAME_1;
import static gms.core.performancemonitoring.soh.control.TestFixture.ChannelSohSets.singleChannel__M_B10__E_B10__L_B1h;
import static gms.core.performancemonitoring.soh.control.TestFixture.ChannelSohSets.threeChannel__M_Mnull__E_B10__L_B1h__M_M15__E_B10__L_B1h__M_M25__E_B10__L_B1h;
import static gms.core.performancemonitoring.soh.control.TestFixture.ChannelSohSets.twoChannel__M_B10__E_B10__L_B1h__M_M15__E_B10__L_B1h;
import static gms.core.performancemonitoring.soh.control.TestFixture.ChannelSohSets.twoChannel__M_B10__E_B10__L_B1h__M_Mnull__E_B10__L_B1h;
import static gms.core.performancemonitoring.soh.control.TestFixture.ChannelSohSets.twoChannel__M_Mnull__E_B10__L_B1h__M_M15__E_B10__L_B1h;
import static gms.core.performancemonitoring.soh.control.TestFixture.ChannelSohSets.twoChannel__T_B1D__T_B2D;
import static gms.core.performancemonitoring.soh.control.TestFixture.StationAggregateSets.percent_missing_90;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

class StationSohCalculationUtilityTests {

  @Test
  void testComputeStationSoh() throws IOException {

    final Set<AcquiredStationSohExtract> extracts = new HashSet<>(TestFixture.loadExtracts());
    final Set<StationSohDefinition> stationSohDefinitions =
      TestFixture.computeStationSohDefinitions(extracts, new SecureRandom("0xDEADBEEF".getBytes()));

    Optional<Instant> maxEndTimeOpt = TestFixture.maxEndTime(extracts);

    assertTrue(maxEndTimeOpt.isPresent());

    // Make it a fixed "now", so it'll be deterministic.
    Instant calcTime = maxEndTimeOpt.orElseThrow(
        //
        // It is not possible to throw this because of the line assertTrue(maxEndTimeOpt.isPresent())
        // above. This is here to satisfy SonarQube.
        //
        () -> new NullPointerException(""))
      .plus(Duration.ofSeconds(10L));

    AcquiredSampleTimesByChannel acquiredSampleTimesByChannel = new AcquiredSampleTimesByChannel();
    acquiredSampleTimesByChannel.setLatestChannelToEndTime(Map.of());

    var theFlux = StationSohCalculationUtility.buildStationSohFlux(
      extracts,
      stationSohDefinitions,
      calcTime,
      acquiredSampleTimesByChannel
    );

    StepVerifier.create(theFlux)
      .expectNextCount(stationSohDefinitions.size())
      .verifyComplete();
  }

  /**
   * Test the station soh utility GIVEN that the channel soh utility returns the given set of
   * ChannelSoh objects. This is meant to (hopefuly) simplify testing by not having to come up with
   * AcquiredStationSohExtract objects. (Testing against those should already be done in
   * ChannelSohCalculationUtility.)
   *
   * @param dummySohUUID - used to set all of the UUIDs of the returned StationSoh objects to the
   * same UUID so that they can be compared to the expected result.
   * @param stationSohDefinitionSetMap - Map of station definition to set of channelSohs returned by
   * the mock ChannelSohCalculationUtility
   * @param stationSohTime "now" for ChannelSohCalculationUtility
   * @param expectedStationSohSet expected return set of StationSoh.
   */
  @ParameterizedTest
  @MethodSource("computeStationSohTestSource")
  void testComputeStationSoh(
    UUID dummySohUUID,
    Map<StationSohDefinition, Set<ChannelSoh>> stationSohDefinitionSetMap,
    Instant stationSohTime,
    Set<StationSoh> expectedStationSohSet
  ) {

    ChannelSohCalculationUtility mockChannelSohCalculationUtility = Mockito
      .mock(ChannelSohCalculationUtility.class);

    StationAggregateCalculationUtility mockStationAggregateCalculationUtility = Mockito
      .mock(StationAggregateCalculationUtility.class);
    Mockito.when(mockStationAggregateCalculationUtility.buildStationAggregateMono(
      any(Mono.class),
      any(Mono.class),
      any(Mono.class),
      any()
    )).thenAnswer(invocation -> Mono.just(
      percent_missing_90
    ));
    //
    // Used to test that ChannelSohCalculationUtility.channelSohSet is being called properly.
    //
    AcquiredStationSohExtract mockAcquiredStationSohExtract = Mockito
      .mock(AcquiredStationSohExtract.class);

    Mockito.when(mockChannelSohCalculationUtility.buildChannelSohSetMono(
      any(Mono.class),
      any(Mono.class),
      any(),
      any()
    )).thenAnswer(invocation -> Mono.just(
      stationSohDefinitionSetMap.get((StationSohDefinition) invocation.getArgument(2))));

    var theFlux = StationSohCalculationUtility.buildStationSohFlux(
      Set.of(mockAcquiredStationSohExtract),
      stationSohDefinitionSetMap.keySet(),
      stationSohTime,
      mockChannelSohCalculationUtility,
      mockStationAggregateCalculationUtility,
      new AcquiredSampleTimesByChannel()
    );

    var tested = new AtomicBoolean(false);

    StepVerifier.create(theFlux)
      .recordWith(ArrayList::new)
      .expectNextCount(expectedStationSohSet.size())
      .consumeRecordedWith(actualStationSohSet -> {

        tested.set(true);

        //
        // We need to set all of the UUIDs to an expected value so that they are easily compared
        // with the expected set, otherwise the UUIDs will be different, and the StationSohs objects
        // can never be equal.
        //
        List<StationSoh> fixedUuidStationSohList = actualStationSohSet.stream()
          .map(stationSoh ->
            StationSoh.from(
              dummySohUUID,
              stationSoh.getTime(),
              stationSoh.getStationName(),
              stationSoh.getSohMonitorValueAndStatuses(),
              stationSoh.getSohStatusRollup(),
              stationSoh.getChannelSohs(),
              stationSoh.getAllStationAggregates()
            )
          ).collect(Collectors.toList());

        Assertions.assertTrue(fixedUuidStationSohList.containsAll(expectedStationSohSet),
          "Expected: \t " + expectedStationSohSet + "\nActual: \t   " + fixedUuidStationSohList
        );

        Assertions.assertTrue(expectedStationSohSet.containsAll(fixedUuidStationSohList),
          "Expected: \t " + expectedStationSohSet + "\nActual: \t   " + fixedUuidStationSohList
        );
      }).verifyComplete();

    Assertions.assertTrue(tested.get());

  }

  private static Stream<Arguments> computeStationSohTestSource() {

    Instant now = Instant.now();

    UUID myUUID = UUID.randomUUID();

    return Stream.of(
      Arguments.arguments(
        myUUID,
        Map.of(
          StationSohDefinition.create(
            STATION_NAME_1,
            Set.of(SohMonitorType.MISSING),
            Map.of(
              SohMonitorType.MISSING,
              Set.of(CHANNEL_NAME_1)
            ),
            Set.of(
              getTotallyEmptyMockChannelDefinition(CHANNEL_NAME_1)
            ),
            TestFixture.createTimeWindowDefMap(Set.of(SohMonitorType.MISSING))
          ),
          singleChannel__M_B10__E_B10__L_B1h
        ),
        now,
        Set.of(
          StationSoh.from(
            myUUID,
            now,
            STATION_NAME_1,
            Set.of(PercentSohMonitorValueAndStatus
              .from(10.0, SohStatus.BAD, SohMonitorType.MISSING)),
            SohStatus.BAD,
            singleChannel__M_B10__E_B10__L_B1h,
            percent_missing_90
          )
        )
      ),

      Arguments.arguments(
        myUUID,
        Map.of(
          StationSohDefinition.create(
            STATION_NAME_1,
            Set.of(SohMonitorType.MISSING),
            Map.of(
              SohMonitorType.MISSING,
              Set.of(CHANNEL_NAME_1)
            ),
            Set.of(
              getTotallyEmptyMockChannelDefinition(CHANNEL_NAME_1)
            ),
            TestFixture.createTimeWindowDefMap(Set.of(SohMonitorType.MISSING))
          ),
          twoChannel__M_B10__E_B10__L_B1h__M_M15__E_B10__L_B1h
        ),
        now,
        Set.of(
          StationSoh.from(
            myUUID,
            now,
            STATION_NAME_1,
            Set.of(PercentSohMonitorValueAndStatus.from(
              10.0,
              SohStatus.BAD,
              SohMonitorType.MISSING
            )),
            SohStatus.BAD,
            twoChannel__M_B10__E_B10__L_B1h__M_M15__E_B10__L_B1h,
            percent_missing_90
          )
        )
      ),

      Arguments.arguments(
        myUUID,
        Map.of(
          StationSohDefinition.create(
            STATION_NAME_1,
            Set.of(SohMonitorType.MISSING),
            Map.of(
              SohMonitorType.MISSING,
              Set.of(CHANNEL_NAME_1)
            ),
            Set.of(
              getTotallyEmptyMockChannelDefinition(CHANNEL_NAME_1)
            ),
            TestFixture.createTimeWindowDefMap(Set.of(SohMonitorType.MISSING))
          ),
          twoChannel__M_B10__E_B10__L_B1h__M_Mnull__E_B10__L_B1h
        ),
        now,
        Set.of(
          StationSoh.from(
            myUUID,
            now,
            STATION_NAME_1,
            Set.of(PercentSohMonitorValueAndStatus.from(
              10.0,
              SohStatus.BAD,
              SohMonitorType.MISSING)),
            SohStatus.BAD,
            twoChannel__M_B10__E_B10__L_B1h__M_Mnull__E_B10__L_B1h,
            percent_missing_90
          )
        )
      ),

      Arguments.arguments(
        myUUID,
        Map.of(
          StationSohDefinition.create(
            STATION_NAME_1,
            Set.of(SohMonitorType.MISSING),
            Map.of(
              SohMonitorType.MISSING,
              Set.of(CHANNEL_NAME_2)
            ),
            Set.of(
              getTotallyEmptyMockChannelDefinition(CHANNEL_NAME_2)
            ),
            TestFixture.createTimeWindowDefMap(Set.of(SohMonitorType.MISSING))
          ),
          twoChannel__M_Mnull__E_B10__L_B1h__M_M15__E_B10__L_B1h
        ),
        now,
        Set.of(
          StationSoh.from(
            myUUID,
            now,
            STATION_NAME_1,
            Set.of(PercentSohMonitorValueAndStatus.from(
              15.0,
              SohStatus.MARGINAL,
              SohMonitorType.MISSING
            )),
            SohStatus.MARGINAL,
            twoChannel__M_Mnull__E_B10__L_B1h__M_M15__E_B10__L_B1h,
            percent_missing_90
          )
        )
      ),

      Arguments.arguments(
        myUUID,
        Map.of(
          StationSohDefinition.create(
            STATION_NAME_1,
            Set.of(SohMonitorType.MISSING),
            Map.of(
              SohMonitorType.MISSING,
              Set.of(CHANNEL_NAME_3)
            ),
            Set.of(
              getTotallyEmptyMockChannelDefinition(CHANNEL_NAME_3)
            ),
            TestFixture.createTimeWindowDefMap(Set.of(SohMonitorType.MISSING))
          ),
          threeChannel__M_Mnull__E_B10__L_B1h__M_M15__E_B10__L_B1h__M_M25__E_B10__L_B1h
        ),
        now,
        Set.of(
          StationSoh.from(
            myUUID,
            now,
            STATION_NAME_1,
            Set.of(PercentSohMonitorValueAndStatus.from(
              25.0,
              SohStatus.MARGINAL,
              SohMonitorType.MISSING
            )),
            SohStatus.MARGINAL,
            threeChannel__M_Mnull__E_B10__L_B1h__M_M15__E_B10__L_B1h__M_M25__E_B10__L_B1h,
            percent_missing_90
          )
        )
      )
      ,
      Arguments.arguments(
        myUUID,
        Map.of(
          StationSohDefinition.create(
            STATION_NAME_1,
            Set.of(SohMonitorType.TIMELINESS),
            Map.of(
              SohMonitorType.TIMELINESS,
              Set.of(CHANNEL_NAME_2)
            ),
            Set.of(
              getTotallyEmptyMockChannelDefinition(CHANNEL_NAME_2)
            ),
            TestFixture.createTimeWindowDefMap(Set.of(SohMonitorType.TIMELINESS))
          ),
          twoChannel__T_B1D__T_B2D
        ),
        now,
        Set.of(
          StationSoh.from(
            myUUID,
            now,
            STATION_NAME_1,
            Set.of(DurationSohMonitorValueAndStatus.from(
              Duration.ofDays(2),
              SohStatus.BAD,
              SohMonitorType.TIMELINESS
            )),
            SohStatus.BAD,
            twoChannel__T_B1D__T_B2D,
            percent_missing_90
          )
        )
      )
    );
  }

  private static ChannelSohDefinition getTotallyEmptyMockChannelDefinition(String channelName) {

    ChannelSohDefinition mockDefinition = Mockito.mock(ChannelSohDefinition.class);

    Mockito.when(mockDefinition.getChannelName()).thenReturn(channelName);

    return mockDefinition;
  }
}
