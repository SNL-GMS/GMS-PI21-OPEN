package gms.core.performancemonitoring.soh.control.capabilityrollup;

import com.google.common.collect.ImmutableSet;
import gms.core.performancemonitoring.soh.control.configuration.BestOfRollupOperator;
import gms.core.performancemonitoring.soh.control.configuration.CapabilitySohRollupDefinition;
import gms.core.performancemonitoring.soh.control.configuration.ChannelRollupDefinition;
import gms.core.performancemonitoring.soh.control.configuration.StationRollupDefinition;
import gms.core.performancemonitoring.soh.control.configuration.WorstOfRollupOperator;
import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.soh.CapabilitySohRollup;
import gms.shared.frameworks.osd.coi.soh.ChannelSoh;
import gms.shared.frameworks.osd.coi.soh.DurationSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.PercentSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.SohStatus;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class CapabilityRollupUtilityTests {

  @ParameterizedTest
  @MethodSource("calculateSohMvasSetRollupTestSource")
  void testCalculateSohMvasSetRollup(
    ChannelRollupDefinition definition,
    Set<SohMonitorValueAndStatus<?>> sohMonitorValueAndStatusSet,
    SohStatus expectedStatus
  ) {

    Assertions.assertSame(
      expectedStatus,
      CapabilityRollupUtility.calculateSohMvasSetRollup(
        definition,
        sohMonitorValueAndStatusSet.stream()
          .collect(Collectors.toMap(
            SohMonitorValueAndStatus::getMonitorType,
            Function.identity()
          ))
      )
    );
  }

  private static Stream<Arguments> calculateSohMvasSetRollupTestSource() {

    return Stream.of(

      Arguments.arguments(
        ChannelRollupDefinition.from(
          BestOfRollupOperator.from(
            List.of(),
            List.of(),
            List.of(
              SohMonitorType.MISSING,
              SohMonitorType.LAG,
              SohMonitorType.ENV_DIGITIZING_EQUIPMENT_OPEN
            ),
            List.of()
          )
        ),
        Set.of(
          PercentSohMonitorValueAndStatus.from(
            1.0,
            SohStatus.MARGINAL,
            SohMonitorType.MISSING
          ),
          DurationSohMonitorValueAndStatus.from(
            Duration.ofMillis(1),
            SohStatus.BAD,
            SohMonitorType.LAG
          ),
          PercentSohMonitorValueAndStatus.from(
            2.0,
            SohStatus.GOOD,
            SohMonitorType.ENV_DIGITIZING_EQUIPMENT_OPEN
          )
        ),
        SohStatus.GOOD
      ),

      Arguments.arguments(
        ChannelRollupDefinition.from(
          WorstOfRollupOperator.from(
            List.of(),
            List.of(),
            List.of(
              SohMonitorType.MISSING,
              SohMonitorType.LAG,
              SohMonitorType.ENV_DIGITIZING_EQUIPMENT_OPEN
            ),
            List.of()
          )
        ),
        Set.of(
          PercentSohMonitorValueAndStatus.from(
            1.0,
            SohStatus.MARGINAL,
            SohMonitorType.MISSING
          ),
          DurationSohMonitorValueAndStatus.from(
            Duration.ofMillis(1),
            SohStatus.BAD,
            SohMonitorType.LAG
          ),
          PercentSohMonitorValueAndStatus.from(
            2.0,
            SohStatus.GOOD,
            SohMonitorType.ENV_DIGITIZING_EQUIPMENT_OPEN
          )
        ),
        SohStatus.BAD
      ),

      Arguments.arguments(
        ChannelRollupDefinition.from(
          WorstOfRollupOperator.from(
            List.of(),
            List.of(),
            List.of(
              SohMonitorType.MISSING,
              SohMonitorType.ENV_DIGITIZING_EQUIPMENT_OPEN
            ),
            List.of()
          )
        ),
        Set.of(
          PercentSohMonitorValueAndStatus.from(
            1.0,
            SohStatus.MARGINAL,
            SohMonitorType.MISSING
          ),
          DurationSohMonitorValueAndStatus.from(
            Duration.ofMillis(1),
            SohStatus.BAD,
            SohMonitorType.LAG
          ),
          PercentSohMonitorValueAndStatus.from(
            2.0,
            SohStatus.GOOD,
            SohMonitorType.ENV_DIGITIZING_EQUIPMENT_OPEN
          )
        ),
        SohStatus.MARGINAL
      ),

      Arguments.arguments(
        ChannelRollupDefinition.from(
          BestOfRollupOperator.from(
            List.of(),
            List.of(),
            List.of(
              SohMonitorType.MISSING,
              SohMonitorType.LAG
            ),
            List.of()
          )
        ),
        Set.of(
          PercentSohMonitorValueAndStatus.from(
            1.0,
            SohStatus.MARGINAL,
            SohMonitorType.MISSING
          ),
          DurationSohMonitorValueAndStatus.from(
            Duration.ofMillis(1),
            SohStatus.BAD,
            SohMonitorType.LAG
          ),
          PercentSohMonitorValueAndStatus.from(
            2.0,
            SohStatus.GOOD,
            SohMonitorType.ENV_DIGITIZING_EQUIPMENT_OPEN
          )
        ),
        SohStatus.MARGINAL
      ),

      //
      // Should use MARGINAL for missing SohMonitorTypes
      //
      Arguments.arguments(
        ChannelRollupDefinition.from(
          BestOfRollupOperator.from(
            List.of(),
            List.of(),
            List.of(
              SohMonitorType.MISSING,
              SohMonitorType.LAG
            ),
            List.of()
          )
        ),
        Set.of(
          //
          // no MISSING and LAG, but MISSING and LAG are in config
          //

          PercentSohMonitorValueAndStatus.from(
            2.0,
            SohStatus.GOOD,
            SohMonitorType.ENV_DIGITIZING_EQUIPMENT_OPEN
          )
        ),
        SohStatus.MARGINAL
      )
    );
  }

  @ParameterizedTest
  @MethodSource("calculateChannelSohSetRollupTestSource")
  void testCalculateChannelSohSetRollup(
    StationRollupDefinition definition,
    Set<ChannelSoh> channelSohSet,
    SohStatus expectedStatus
  ) {

    Assertions.assertSame(
      expectedStatus,
      CapabilityRollupUtility.calculateChannelSohSetRollup(
        definition,
        channelSohSet
      )
    );
  }

  private static Stream<Arguments> calculateChannelSohSetRollupTestSource() {

    return Stream.of(
      //
      // Best of of best of
      //
      Arguments.arguments(
        StationRollupDefinition.from(
          BestOfRollupOperator.from(

            List.of(),
            List.of("A", "B"),
            List.of(),
            List.of()
          ),
          Map.of(
            "A", ChannelRollupDefinition.from(
              BestOfRollupOperator.from(

                List.of(),
                List.of(),
                List.of(
                  SohMonitorType.MISSING,
                  SohMonitorType.LAG,
                  SohMonitorType.ENV_DURATION_OUTAGE
                ),
                List.of()
              )
            ),
            "B", ChannelRollupDefinition.from(
              BestOfRollupOperator.from(

                List.of(),
                List.of(),
                List.of(
                  SohMonitorType.MISSING,
                  SohMonitorType.LAG
                ),
                List.of()
              )
            )
          )
        ),
        Set.of(
          //
          // Lets have only one status out of all of them GOOD. Lets make it the LAG
          //  for Channel B.
          //
          getMockChannelSohOnlyStatuses(
            "A",
            Set.of(
              PercentSohMonitorValueAndStatus.from(
                1.0,
                SohStatus.MARGINAL,
                SohMonitorType.MISSING
              ),
              DurationSohMonitorValueAndStatus.from(
                Duration.ofMillis(1),
                SohStatus.BAD,
                SohMonitorType.LAG
              ),
              PercentSohMonitorValueAndStatus.from(
                2.0,
                SohStatus.MARGINAL,
                SohMonitorType.ENV_DIGITIZING_EQUIPMENT_OPEN
              )
            )
          ),
          getMockChannelSohOnlyStatuses(
            "B",
            Set.of(
              PercentSohMonitorValueAndStatus.from(
                1.0,
                SohStatus.MARGINAL,
                SohMonitorType.MISSING
              ),
              DurationSohMonitorValueAndStatus.from(
                Duration.ofMillis(1),
                //
                // This should be the ovarall return value.
                //
                SohStatus.GOOD,
                SohMonitorType.LAG
              ),
              PercentSohMonitorValueAndStatus.from(
                2.0,
                SohStatus.MARGINAL,
                SohMonitorType.ENV_DIGITIZING_EQUIPMENT_OPEN
              )
            )
          )
        ),

        SohStatus.GOOD
      ),

      //
      // Best of the worst of
      //
      Arguments.arguments(
        StationRollupDefinition.from(
          BestOfRollupOperator.from(

            List.of(),
            List.of("A", "B"),
            List.of(),
            List.of()
          ),
          Map.of(
            "A", ChannelRollupDefinition.from(
              WorstOfRollupOperator.from(
                List.of(),
                List.of(),
                List.of(
                  SohMonitorType.MISSING,
                  SohMonitorType.LAG,
                  SohMonitorType.ENV_DURATION_OUTAGE
                ),
                List.of()
              )
            ),
            "B", ChannelRollupDefinition.from(
              WorstOfRollupOperator.from(
                List.of(),
                List.of(),
                List.of(
                  SohMonitorType.MISSING,
                  SohMonitorType.LAG
                ),
                List.of()
              )
            )
          )
        ),
        Set.of(
          getMockChannelSohOnlyStatuses(
            "A",
            Set.of(
              PercentSohMonitorValueAndStatus.from(
                1.0,
                SohStatus.MARGINAL,
                SohMonitorType.MISSING
              ),
              DurationSohMonitorValueAndStatus.from(
                Duration.ofMillis(1),
                SohStatus.GOOD,
                SohMonitorType.LAG
              ),
              PercentSohMonitorValueAndStatus.from(
                2.0,
                SohStatus.MARGINAL,
                SohMonitorType.ENV_DIGITIZING_EQUIPMENT_OPEN
              )
            )
          ),
          getMockChannelSohOnlyStatuses(
            "B",
            Set.of(
              PercentSohMonitorValueAndStatus.from(
                1.0,
                SohStatus.MARGINAL,
                SohMonitorType.MISSING
              ),
              DurationSohMonitorValueAndStatus.from(
                Duration.ofMillis(1),
                SohStatus.BAD,
                SohMonitorType.LAG
              ),
              PercentSohMonitorValueAndStatus.from(
                2.0,
                SohStatus.MARGINAL,
                SohMonitorType.ENV_DIGITIZING_EQUIPMENT_OPEN
              )
            )
          )
        ),

        SohStatus.MARGINAL
      ),

      Arguments.arguments(
        StationRollupDefinition.from(
          BestOfRollupOperator.from(

            List.of(),
            List.of("A", "B"),
            List.of(),
            List.of()
          ),
          Map.of(
            "A", ChannelRollupDefinition.from(
              WorstOfRollupOperator.from(
                List.of(),
                List.of(),
                List.of(
                  SohMonitorType.MISSING,
                  SohMonitorType.LAG,
                  SohMonitorType.ENV_DURATION_OUTAGE
                ),
                List.of()
              )
            ),
            "B", ChannelRollupDefinition.from(
              WorstOfRollupOperator.from(
                List.of(),
                List.of(),
                List.of(
                  SohMonitorType.MISSING,
                  SohMonitorType.LAG
                ),
                List.of()
              )
            )
          )
        ),
        Set.of(
          //
          // No channel "A", so status should be MARGINAL for that channel.
          //

          getMockChannelSohOnlyStatuses(
            "B",
            Set.of(
              PercentSohMonitorValueAndStatus.from(
                1.0,
                SohStatus.GOOD,
                SohMonitorType.MISSING
              ),
              DurationSohMonitorValueAndStatus.from(
                Duration.ofMillis(1),
                SohStatus.BAD,
                SohMonitorType.LAG
              ),
              PercentSohMonitorValueAndStatus.from(
                2.0,
                SohStatus.GOOD,
                SohMonitorType.ENV_DIGITIZING_EQUIPMENT_OPEN
              )
            )
          )
        ),

        SohStatus.MARGINAL
      )
    );
  }

  @ParameterizedTest
  @MethodSource("calculateCapabilitySohRollupTestSource")
  void testCalculateCapabilitySohRollup(
    CapabilitySohRollupDefinition definition,
    Set<StationSoh> stationSohSet,
    CapabilitySohRollup expected
  ) {

    var fakeUnusedStation = Mockito.mock(Station.class);

    Mockito.when(fakeUnusedStation.getChannels())
      .thenReturn(new TreeSet<>(Comparator.comparing(Channel::getName)));

    Mockito.when(fakeUnusedStation.getName()).thenReturn("Fake unused station");
    var fakeUnusedStationGroup = Mockito.mock(StationGroup.class);

    var fakeUnusedStations = new TreeSet<>(Comparator.comparing(Station::getName));
    fakeUnusedStations.add(fakeUnusedStation);
    Mockito.when(fakeUnusedStationGroup.getStations()).thenReturn(fakeUnusedStations);

    var theMono = CapabilityRollupUtility.buildCapabilitySohRollupMono(
      definition,
      Flux.fromIterable(stationSohSet),
      Instant.now()
    );

    // Used for sanity check.
    var tested = new AtomicBoolean(false);

    StepVerifier.create(theMono)
      .assertNext(
        actual -> {
          // Sanity check. Set the boolean in tested to TRUE.
          tested.set(true);

          Assertions.assertEquals(expected,
            CapabilitySohRollup.create(
              expected.getId(),
              expected.getTime(),
              actual.getGroupRollupSohStatus(),
              actual.getForStationGroup(),
              actual.getBasedOnStationSohs(),
              actual.getRollupSohStatusByStation()
            )
          );
        }
      ).verifyComplete(); // verifyComplete needs to be called! Its the reason for the sanity check.

    // Sanity check. If this fails, then the lambda in StepVerifier.assertNext was never called.
    Assertions.assertTrue(tested.get());
  }

  private static Stream<Arguments> calculateCapabilitySohRollupTestSource() {

    UUID uuidA = UUID.randomUUID();
    UUID uuidB = UUID.randomUUID();

    return Stream.of(
      Arguments.arguments(
        CapabilitySohRollupDefinition.from(
          "IGNORED",
          BestOfRollupOperator.from(
            List.of("STATION-A", "STATION-B"),
            List.of(),
            List.of(),
            List.of()
          ),
          Map.of(
            "STATION-A",
            StationRollupDefinition.from(
              BestOfRollupOperator.from(
                List.of(),
                List.of("A", "B"),
                List.of(),
                List.of()
              ),
              Map.of(
                "A", ChannelRollupDefinition.from(
                  WorstOfRollupOperator.from(
                    List.of(),
                    List.of(),
                    List.of(
                      SohMonitorType.MISSING,
                      SohMonitorType.LAG,
                      SohMonitorType.ENV_DURATION_OUTAGE
                    ),
                    List.of()
                  )
                ),
                "B", ChannelRollupDefinition.from(
                  WorstOfRollupOperator.from(
                    List.of(),
                    List.of(),
                    List.of(
                      SohMonitorType.MISSING,
                      SohMonitorType.LAG
                    ),
                    List.of()
                  )
                )
              )
            ),
            "STATION-B",
            StationRollupDefinition.from(
              BestOfRollupOperator.from(

                List.of(),
                List.of("C", "D"),
                List.of(),
                List.of()
              ),
              Map.of(
                "C", ChannelRollupDefinition.from(
                  WorstOfRollupOperator.from(
                    List.of(),
                    List.of(),
                    List.of(
                      SohMonitorType.MISSING,
                      SohMonitorType.LAG,
                      SohMonitorType.ENV_DURATION_OUTAGE
                    ),
                    List.of()
                  )
                ),
                "D", ChannelRollupDefinition.from(
                  BestOfRollupOperator.from(
                    List.of(),
                    List.of(),
                    List.of(
                      SohMonitorType.MISSING,
                      SohMonitorType.LAG
                    ),
                    List.of()
                  )
                )
              )
            )
          )
        ),
        Set.of(
          getMockStationSohOnlyChannelSohs(
            uuidA,
            "STATION-A",
            Set.of(
              getMockChannelSohOnlyStatuses(
                "A",
                Set.of(
                  PercentSohMonitorValueAndStatus.from(
                    1.0,
                    SohStatus.MARGINAL,
                    SohMonitorType.MISSING
                  ),
                  DurationSohMonitorValueAndStatus.from(
                    Duration.ofMillis(1),
                    SohStatus.GOOD,
                    SohMonitorType.LAG
                  ),
                  PercentSohMonitorValueAndStatus.from(
                    2.0,
                    SohStatus.MARGINAL,
                    SohMonitorType.ENV_DIGITIZING_EQUIPMENT_OPEN
                  )
                )
              ),
              getMockChannelSohOnlyStatuses(
                "B",
                Set.of(
                  PercentSohMonitorValueAndStatus.from(
                    1.0,
                    SohStatus.MARGINAL,
                    SohMonitorType.MISSING
                  ),
                  DurationSohMonitorValueAndStatus.from(
                    Duration.ofMillis(1),
                    SohStatus.BAD,
                    SohMonitorType.LAG
                  ),
                  PercentSohMonitorValueAndStatus.from(
                    2.0,
                    SohStatus.MARGINAL,
                    SohMonitorType.ENV_DIGITIZING_EQUIPMENT_OPEN
                  )
                )
              )
            )
          ),
          getMockStationSohOnlyChannelSohs(
            uuidB,
            "STATION-B",
            Set.of(
              getMockChannelSohOnlyStatuses(
                "C",
                Set.of(
                  PercentSohMonitorValueAndStatus.from(
                    1.0,
                    SohStatus.MARGINAL,
                    SohMonitorType.MISSING
                  ),
                  DurationSohMonitorValueAndStatus.from(
                    Duration.ofMillis(1),
                    SohStatus.GOOD,
                    SohMonitorType.LAG
                  ),
                  PercentSohMonitorValueAndStatus.from(
                    2.0,
                    SohStatus.MARGINAL,
                    SohMonitorType.ENV_DIGITIZING_EQUIPMENT_OPEN
                  )
                )
              ),
              getMockChannelSohOnlyStatuses(
                "D",
                Set.of(
                  PercentSohMonitorValueAndStatus.from(
                    1.0,
                    SohStatus.GOOD,
                    SohMonitorType.MISSING
                  ),
                  DurationSohMonitorValueAndStatus.from(
                    Duration.ofMillis(1),
                    SohStatus.BAD,
                    SohMonitorType.LAG
                  ),
                  PercentSohMonitorValueAndStatus.from(
                    2.0,
                    SohStatus.MARGINAL,
                    SohMonitorType.ENV_DIGITIZING_EQUIPMENT_OPEN
                  )
                )
              )
            )
          )
        ),

        CapabilitySohRollup.create(
          UUID.randomUUID(),
          Instant.EPOCH,
          SohStatus.GOOD,
          "IGNORED",
          Set.of(uuidA, uuidB),
          Map.of(
            "STATION-A", SohStatus.MARGINAL,
            "STATION-B", SohStatus.GOOD
          )

        )
      )
    );
  }

  @Test
  void testCalculateCapabilitySohRollupSet() {

    UUID uuidA = UUID.randomUUID();
    UUID uuidB = UUID.randomUUID();
    UUID uuidC = UUID.randomUUID();
    UUID uuidD = UUID.randomUUID();

    var capabilitySohRollupDefinitions = Set.of(
      CapabilitySohRollupDefinition.from(
        "GROUP1",
        BestOfRollupOperator.from(
          List.of("STATION-A", "STATION-B"),
          List.of(),
          List.of(),
          List.of()
        ),
        Map.of(
          "STATION-A",
          StationRollupDefinition.from(
            BestOfRollupOperator.from(
              List.of(),
              List.of("A", "B"),
              List.of(),
              List.of()
            ),
            Map.of(
              "A", ChannelRollupDefinition.from(
                WorstOfRollupOperator.from(
                  List.of(),
                  List.of(),
                  List.of(
                    SohMonitorType.MISSING,
                    SohMonitorType.LAG,
                    SohMonitorType.ENV_DURATION_OUTAGE
                  ),
                  List.of()
                )
              ),
              "B", ChannelRollupDefinition.from(
                WorstOfRollupOperator.from(
                  List.of(),
                  List.of(),
                  List.of(
                    SohMonitorType.MISSING,
                    SohMonitorType.LAG
                  ),
                  List.of()
                )
              )
            )
          ),
          "STATION-B",
          StationRollupDefinition.from(
            BestOfRollupOperator.from(

              List.of(),
              List.of("C", "D"),
              List.of(),
              List.of()
            ),
            Map.of(
              "C", ChannelRollupDefinition.from(
                WorstOfRollupOperator.from(
                  List.of(),
                  List.of(),
                  List.of(
                    SohMonitorType.MISSING,
                    SohMonitorType.LAG,
                    SohMonitorType.ENV_DURATION_OUTAGE
                  ),
                  List.of()
                )
              ),
              "D", ChannelRollupDefinition.from(
                BestOfRollupOperator.from(
                  List.of(),
                  List.of(),
                  List.of(
                    SohMonitorType.MISSING,
                    SohMonitorType.LAG
                  ),
                  List.of()
                )
              )
            )
          )
        )
      ),

      CapabilitySohRollupDefinition.from(
        "GROUP2",
        BestOfRollupOperator.from(
          List.of("STATION-C", "STATION-D"),
          List.of(),
          List.of(),
          List.of()
        ),
        Map.of(
          "STATION-C",
          StationRollupDefinition.from(
            BestOfRollupOperator.from(
              List.of(),
              List.of("E", "F"),
              List.of(),
              List.of()
            ),
            Map.of(
              "E", ChannelRollupDefinition.from(
                WorstOfRollupOperator.from(
                  List.of(),
                  List.of(),
                  List.of(
                    SohMonitorType.MISSING,
                    SohMonitorType.LAG,
                    SohMonitorType.ENV_DURATION_OUTAGE
                  ),
                  List.of()
                )
              ),
              "F", ChannelRollupDefinition.from(
                WorstOfRollupOperator.from(
                  List.of(),
                  List.of(),
                  List.of(
                    SohMonitorType.MISSING,
                    SohMonitorType.LAG
                  ),
                  List.of()
                )
              )
            )
          ),
          "STATION-D",
          StationRollupDefinition.from(
            BestOfRollupOperator.from(
              List.of(),
              List.of("G", "H"),
              List.of(),
              List.of()
            ),
            Map.of(
              "G", ChannelRollupDefinition.from(
                WorstOfRollupOperator.from(
                  List.of(),
                  List.of(),
                  List.of(
                    SohMonitorType.MISSING,
                    SohMonitorType.LAG,
                    SohMonitorType.ENV_DURATION_OUTAGE
                  ),
                  List.of()
                )
              ),
              "H", ChannelRollupDefinition.from(
                BestOfRollupOperator.from(
                  List.of(),
                  List.of(),
                  List.of(
                    SohMonitorType.MISSING,
                    SohMonitorType.LAG
                  ),
                  List.of()
                )
              )
            )
          )
        )
      )
    );

    Set<StationSoh> stationSohs = Set.of(
      getMockStationSohOnlyChannelSohs(
        uuidA,
        "STATION-A",
        Set.of(
          getMockChannelSohOnlyStatuses(
            "A",
            Set.of(
              PercentSohMonitorValueAndStatus.from(
                1.0,
                SohStatus.MARGINAL,
                SohMonitorType.MISSING
              ),
              DurationSohMonitorValueAndStatus.from(
                Duration.ofMillis(1),
                SohStatus.GOOD,
                SohMonitorType.LAG
              ),
              PercentSohMonitorValueAndStatus.from(
                2.0,
                SohStatus.MARGINAL,
                SohMonitorType.ENV_DIGITIZING_EQUIPMENT_OPEN
              )
            )
          ),
          getMockChannelSohOnlyStatuses(
            "B",
            Set.of(
              PercentSohMonitorValueAndStatus.from(
                1.0,
                SohStatus.MARGINAL,
                SohMonitorType.MISSING
              ),
              DurationSohMonitorValueAndStatus.from(
                Duration.ofMillis(1),
                SohStatus.BAD,
                SohMonitorType.LAG
              ),
              PercentSohMonitorValueAndStatus.from(
                2.0,
                SohStatus.MARGINAL,
                SohMonitorType.ENV_DIGITIZING_EQUIPMENT_OPEN
              )
            )
          )
        )
      ),
      getMockStationSohOnlyChannelSohs(
        uuidB,
        "STATION-B",
        Set.of(
          getMockChannelSohOnlyStatuses(
            "C",
            Set.of(
              PercentSohMonitorValueAndStatus.from(
                1.0,
                SohStatus.MARGINAL,
                SohMonitorType.MISSING
              ),
              DurationSohMonitorValueAndStatus.from(
                Duration.ofMillis(1),
                SohStatus.GOOD,
                SohMonitorType.LAG
              ),
              PercentSohMonitorValueAndStatus.from(
                2.0,
                SohStatus.MARGINAL,
                SohMonitorType.ENV_DIGITIZING_EQUIPMENT_OPEN
              )
            )
          ),
          getMockChannelSohOnlyStatuses(
            "D",
            Set.of(
              PercentSohMonitorValueAndStatus.from(
                1.0,
                SohStatus.GOOD,
                SohMonitorType.MISSING
              ),
              DurationSohMonitorValueAndStatus.from(
                Duration.ofMillis(1),
                SohStatus.BAD,
                SohMonitorType.LAG
              ),
              PercentSohMonitorValueAndStatus.from(
                2.0,
                SohStatus.MARGINAL,
                SohMonitorType.ENV_DIGITIZING_EQUIPMENT_OPEN
              )
            )
          )
        )
      ),

      getMockStationSohOnlyChannelSohs(
        uuidC,
        "STATION-C",
        Set.of(
          getMockChannelSohOnlyStatuses(
            "E",
            Set.of(
              PercentSohMonitorValueAndStatus.from(
                1.0,
                SohStatus.MARGINAL,
                SohMonitorType.MISSING
              ),
              DurationSohMonitorValueAndStatus.from(
                Duration.ofMillis(1),
                SohStatus.GOOD,
                SohMonitorType.LAG
              ),
              PercentSohMonitorValueAndStatus.from(
                2.0,
                SohStatus.MARGINAL,
                SohMonitorType.ENV_DIGITIZING_EQUIPMENT_OPEN
              )
            )
          ),
          getMockChannelSohOnlyStatuses(
            "F",
            Set.of(
              PercentSohMonitorValueAndStatus.from(
                1.0,
                SohStatus.MARGINAL,
                SohMonitorType.MISSING
              ),
              DurationSohMonitorValueAndStatus.from(
                Duration.ofMillis(1),
                SohStatus.BAD,
                SohMonitorType.LAG
              ),
              PercentSohMonitorValueAndStatus.from(
                2.0,
                SohStatus.MARGINAL,
                SohMonitorType.ENV_DIGITIZING_EQUIPMENT_OPEN
              )
            )
          )
        )
      ),
      getMockStationSohOnlyChannelSohs(
        uuidD,
        "STATION-D",
        Set.of(
          getMockChannelSohOnlyStatuses(
            "G",
            Set.of(
              PercentSohMonitorValueAndStatus.from(
                1.0,
                SohStatus.MARGINAL,
                SohMonitorType.MISSING
              ),
              DurationSohMonitorValueAndStatus.from(
                Duration.ofMillis(1),
                SohStatus.GOOD,
                SohMonitorType.LAG
              ),
              PercentSohMonitorValueAndStatus.from(
                2.0,
                SohStatus.MARGINAL,
                SohMonitorType.ENV_DIGITIZING_EQUIPMENT_OPEN
              )
            )
          ),
          getMockChannelSohOnlyStatuses(
            "H",
            Set.of(
              PercentSohMonitorValueAndStatus.from(
                1.0,
                SohStatus.GOOD,
                SohMonitorType.MISSING
              ),
              DurationSohMonitorValueAndStatus.from(
                Duration.ofMillis(1),
                SohStatus.BAD,
                SohMonitorType.LAG
              ),
              PercentSohMonitorValueAndStatus.from(
                2.0,
                SohStatus.MARGINAL,
                SohMonitorType.ENV_DIGITIZING_EQUIPMENT_OPEN
              )
            )
          )
        )
      )
    );

    var theFlux = CapabilityRollupUtility.buildCapabilitySohRollupFlux(
      capabilitySohRollupDefinitions, Flux.fromIterable(stationSohs), Instant.now()
    );

    var tested = new AtomicBoolean(false);

    StepVerifier.create(theFlux)
      .recordWith(ArrayList::new)
      .expectNextCount(2)
      .consumeRecordedWith(
        capabilitySohRollups -> {

          tested.set(true);

          Assertions.assertEquals(2, capabilitySohRollups.size());

          Assertions.assertEquals(1, capabilitySohRollups.stream()
            .filter(capabilitySohRollup ->
              "GROUP1".equals(capabilitySohRollup.getForStationGroup()))
            .count());

          Assertions.assertEquals(1, capabilitySohRollups.stream()
            .filter(capabilitySohRollup ->
              "GROUP2".equals(capabilitySohRollup.getForStationGroup()))
            .count());

          Assertions.assertEquals(
            Set.of("STATION-A", "STATION-B"),
            capabilitySohRollups.stream()
              .filter(capabilitySohRollup ->
                "GROUP1".equals(capabilitySohRollup.getForStationGroup()))
              .findFirst().get().getRollupSohStatusByStation().keySet()
          );

          Assertions.assertEquals(
            Set.of("STATION-C", "STATION-D"),
            capabilitySohRollups.stream()
              .filter(capabilitySohRollup ->
                "GROUP2".equals(capabilitySohRollup.getForStationGroup()))
              .findFirst().get().getRollupSohStatusByStation().keySet()
          );
        }
      ).verifyComplete();

    Assertions.assertTrue(tested.get());

  }

  static ChannelSoh getMockChannelSohOnlyStatuses(
    String channelName,
    Set<SohMonitorValueAndStatus<?>> allSohMonitorValueAndStatuses
  ) {

    ChannelSoh mockChannelSoh = Mockito.mock(ChannelSoh.class);

    Mockito.when(mockChannelSoh.getChannelName()).thenReturn(channelName);
    Mockito.when(mockChannelSoh.getAllSohMonitorValueAndStatuses())
      .thenReturn(ImmutableSet.copyOf(allSohMonitorValueAndStatuses));
    Mockito.when(mockChannelSoh.getSohMonitorValueAndStatusMap()).thenCallRealMethod();

    return mockChannelSoh;
  }

  static StationSoh getMockStationSohOnlyChannelSohs(
    UUID uuid,
    String stationName,
    Set<ChannelSoh> channelSohSet
  ) {

    StationSoh mockStationSoh = Mockito.mock(StationSoh.class);

    Mockito.when(mockStationSoh.getChannelSohs()).thenReturn(ImmutableSet.copyOf(channelSohSet));

    Mockito.when(mockStationSoh.getStationName()).thenReturn(stationName);

    Mockito.when(mockStationSoh.getId()).thenReturn(uuid);

    return mockStationSoh;
  }

}
