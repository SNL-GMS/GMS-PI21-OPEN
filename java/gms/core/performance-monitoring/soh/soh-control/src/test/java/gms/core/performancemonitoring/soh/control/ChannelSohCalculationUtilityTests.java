package gms.core.performancemonitoring.soh.control;

import gms.core.performancemonitoring.soh.control.configuration.ChannelSohDefinition;
import gms.core.performancemonitoring.soh.control.configuration.DurationSohMonitorStatusThresholdDefinition;
import gms.core.performancemonitoring.soh.control.configuration.PercentSohMonitorStatusThresholdDefinition;
import gms.core.performancemonitoring.soh.control.configuration.StationSohDefinition;
import gms.core.performancemonitoring.soh.control.configuration.TimeWindowDefinition;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;
import gms.shared.frameworks.osd.coi.soh.AcquiredStationSohExtract;
import gms.shared.frameworks.osd.coi.soh.ChannelSoh;
import gms.shared.frameworks.osd.coi.soh.DurationSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.PercentSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.SohStatus;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame.AuthenticationStatus;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrameMetadata;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFramePayloadFormat;
import gms.shared.frameworks.osd.coi.waveforms.WaveformSummary;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
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
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChannelSohCalculationUtilityTests {

  @Test
  void testChannelSohSet() throws IOException {

    Set<AcquiredStationSohExtract> extracts = new HashSet<>(TestFixture.loadExtracts());
    Set<StationSohDefinition> stationSohDefinitions = TestFixture
      .computeStationSohDefinitions(extracts, new SecureRandom("0xDEADBEEF".getBytes()));

    Optional<Instant> maxEndTimeOpt = TestFixture.maxEndTime(extracts);

    assertTrue(maxEndTimeOpt.isPresent());

    // Make it a fixed "now", so it'll be deterministic.
    Instant calcTime = maxEndTimeOpt.orElseThrow(
      //
      // It is not possible to throw this because of the line assertTrue(maxEndTimeOpt.isPresent())
      // above. This is here to satisfy SonarQube.
      //
      () -> new NullPointerException("")
    ).plus(Duration.ofSeconds(10L));

    AcquiredSampleTimesByChannel acquiredSampleTimesByChannel = new AcquiredSampleTimesByChannel();
    acquiredSampleTimesByChannel.setLatestChannelToEndTime(Map.of());

    ChannelSohCalculationUtility calculationUtility =
      new ChannelSohCalculationUtility(calcTime, acquiredSampleTimesByChannel);
    var waveformSummaryAndReceptionTimes =
      Mono.just(StationSohCalculationUtility
        .createWsRtMapAndPopulateSampleTimes(extracts, acquiredSampleTimesByChannel));
    var aceiBooleanMap =
      Mono.just(StationSohCalculationUtility.createAceiBooleanMap(extracts));

    int n = 0;
    for (StationSohDefinition stationSohDefinition : stationSohDefinitions) {
      Set<ChannelSoh> channelSohs = calculationUtility
        .buildChannelSohSetMono(waveformSummaryAndReceptionTimes,
          aceiBooleanMap, stationSohDefinition, Instant.now()).block();
      assertFalse(channelSohs.isEmpty(), String.format("zero channel sohs for element %d", n));
      n++;
    }

  }

  @ParameterizedTest
  @MethodSource("rollupTestProvider")
  void testRollup(
    Set<SohMonitorValueAndStatus<?>> monitorValueAndStatusSet,
    Set<SohMonitorType> monitorTypesInRollup,
    SohStatus expectedSohStatus,
    Class<? extends Throwable> expectedThrowable,
    String expectedThrowableMessage,
    boolean sanityCheck
  ) {

    Validate.isTrue(
      (expectedSohStatus != null && expectedThrowable == null
        && expectedThrowableMessage == null)
        ||
        (expectedSohStatus == null && expectedThrowable != null
          && expectedThrowableMessage != null)
    );

    AtomicReference<SohStatus> statusRollupAtomicReference = new AtomicReference<>();

    Executable executable = () ->
      statusRollupAtomicReference.set(
        ChannelSohCalculationUtility.rollup(
          monitorValueAndStatusSet,
          monitorTypesInRollup).get()
      );

    if (expectedThrowable == null) {
      Assertions.assertDoesNotThrow(executable);

      if (!sanityCheck) {
        Assertions.assertSame(expectedSohStatus, statusRollupAtomicReference.get());
      } else {
        assertNotSame(expectedSohStatus, statusRollupAtomicReference.get());
      }
    } else {
      Throwable actualThrowable = Assertions.assertThrows(expectedThrowable, executable);
      Assertions.assertEquals(expectedThrowableMessage, actualThrowable.getMessage());
    }
  }

  static Stream<Arguments> rollupTestProvider() {

    return Stream.of(
      //
      // Some run-of-the-mill stuff
      //
      Arguments.arguments(
        Set.of(
          PercentSohMonitorValueAndStatus.from(
            0.0,
            SohStatus.GOOD,
            SohMonitorType.MISSING
          ),
          PercentSohMonitorValueAndStatus.from(
            1.0,
            SohStatus.BAD,
            SohMonitorType.MISSING
          ),
          DurationSohMonitorValueAndStatus.from(
            Duration.ofHours(1),
            SohStatus.MARGINAL,
            SohMonitorType.LAG
          )
        ),
        Set.of(SohMonitorType.MISSING, SohMonitorType.LAG),
        SohStatus.BAD,
        null,
        null,
        false
      ),
      Arguments.arguments(
        Set.of(
          PercentSohMonitorValueAndStatus.from(
            0.0,
            SohStatus.GOOD,
            SohMonitorType.MISSING
          ),
          PercentSohMonitorValueAndStatus.from(
            1.0,
            SohStatus.BAD,
            SohMonitorType.MISSING
          ),
          DurationSohMonitorValueAndStatus.from(
            Duration.ofHours(1),
            SohStatus.MARGINAL,
            SohMonitorType.LAG
          )
        ),
        Set.of(SohMonitorType.MISSING),
        SohStatus.BAD,
        null,
        null,
        false
      ),
      Arguments.arguments(
        Set.of(
          PercentSohMonitorValueAndStatus.from(
            0.0,
            SohStatus.GOOD,
            SohMonitorType.MISSING
          ),
          PercentSohMonitorValueAndStatus.from(
            1.0,
            SohStatus.MARGINAL,
            SohMonitorType.MISSING
          ),
          DurationSohMonitorValueAndStatus.from(
            Duration.ofHours(1),
            SohStatus.BAD,
            SohMonitorType.LAG
          )
        ),
        Set.of(SohMonitorType.MISSING, SohMonitorType.LAG),
        SohStatus.BAD,
        null,
        null,
        false
      ),

      Arguments.arguments(
        Set.of(
          PercentSohMonitorValueAndStatus.from(
            0.0,
            SohStatus.GOOD,
            SohMonitorType.MISSING
          ),
          PercentSohMonitorValueAndStatus.from(
            1.0,
            SohStatus.MARGINAL,
            SohMonitorType.MISSING
          ),
          DurationSohMonitorValueAndStatus.from(
            Duration.ofHours(1),
            SohStatus.BAD,
            SohMonitorType.LAG
          )
        ),
        Set.of(SohMonitorType.ENV_BACKUP_POWER_UNSTABLE),
        null,
        NoSuchElementException.class,
        "No value present",
        false
      ),

      //
      // Sanity checks, make sure we are not getting what we dont expect.
      //
      Arguments.arguments(
        Set.of(
          PercentSohMonitorValueAndStatus.from(
            0.0,
            SohStatus.GOOD,
            SohMonitorType.MISSING
          ),
          PercentSohMonitorValueAndStatus.from(
            1.0,
            SohStatus.MARGINAL,
            SohMonitorType.MISSING
          ),
          DurationSohMonitorValueAndStatus.from(
            Duration.ofHours(1),
            SohStatus.BAD,
            SohMonitorType.LAG
          )
        ),
        Set.of(SohMonitorType.MISSING, SohMonitorType.LAG),
        SohStatus.MARGINAL,
        null,
        null,
        true
      )
    );
  }


  @ParameterizedTest
  @MethodSource("testCalculateLagInvalidArgumentsProvider")
  void testCalculateLagInvalidArguments(
    StationSohDefinition stationSohDefinition,
    Collection<WaveformSummaryAndReceptionTime> waveformSummaryAndReceptionTimes,
    DurationSohMonitorStatusThresholdDefinition definition,
    Class<Throwable> expectedExceptionClass,
    String expectedExceptionMessage) {

    // TODO need to initialize this to not null
    AcquiredSampleTimesByChannel acquiredSampleTimesByChannel = new AcquiredSampleTimesByChannel();

    var exception = Assertions.assertThrows(expectedExceptionClass,
      () -> new ChannelSohCalculationUtility(acquiredSampleTimesByChannel)
        .lag(stationSohDefinition, definition, waveformSummaryAndReceptionTimes));

    Assertions.assertEquals(expectedExceptionMessage, exception.getMessage());
  }

  //
  // SonarQube finding: remove unused private method
  //  This is not an unused method. see @MethodSource annotations on parameterized tests.
  //
  @SuppressWarnings("squid:UnusedPrivateMethod")
  private static Stream<Arguments> testCalculateLagInvalidArgumentsProvider() {

    final StationSohDefinition mockStationSohDefinition = Mockito.mock(StationSohDefinition.class);

    final DurationSohMonitorStatusThresholdDefinition mockDurationDefinition = Mockito
      .mock(DurationSohMonitorStatusThresholdDefinition.class);

    final List<Pair<Instant, Instant>> times = List
      .of(Pair.of(Instant.EPOCH, Instant.EPOCH.plusMillis(1)));

    return Stream.of(
      Arguments.arguments(
        null,
        null,
        mockDurationDefinition,
        NullPointerException.class,
        "Null waveformSummaryAndReceptionTimes"
      ),
      Arguments.arguments(
        null,
        times,
        null,
        NullPointerException.class,
        "Null durationDefinition"
      ),
      Arguments.arguments(
        mockStationSohDefinition,
        List.of(),
        mockDurationDefinition,
        IllegalArgumentException.class,
        "Empty waveformSummaryAndReceptionTimes"
      )
    );
  }


  @ParameterizedTest
  @MethodSource("testCalculateLagProvider")
  void testCalculateLag(
    StationSohDefinition stationSohDefinition,
    List<WaveformSummaryAndReceptionTime> waveformSummaryAndReceptionTimes,
    DurationSohMonitorStatusThresholdDefinition definition,
    DurationSohMonitorValueAndStatus expectedMonitorValueAndStatus) {

    var acquiredSampleTimesByChannel = new AcquiredSampleTimesByChannel();

    Optional<DurationSohMonitorValueAndStatus> computedMonitorValueAndStatus =
      new ChannelSohCalculationUtility(
        Instant.ofEpochSecond(20),
        acquiredSampleTimesByChannel
      ).lag(stationSohDefinition, definition, waveformSummaryAndReceptionTimes);

    if (computedMonitorValueAndStatus.isEmpty()) {
      // Test exclusion logic based on time lying outside of calculation window.
      assertTrue(waveformSummaryAndReceptionTimes.get(1).getReceptionTime()
        .compareTo(waveformSummaryAndReceptionTimes.get(0).getReceptionTime()) < 0);
    } else {
      Assertions.assertNotNull(computedMonitorValueAndStatus);
      Assertions.assertEquals(expectedMonitorValueAndStatus, computedMonitorValueAndStatus.get());
    }
  }

  //
  // SonarQube finding: remove unused private method
  //  This is not an unused method. see @MethodSource annotations on parameterized tests.
  //
  @SuppressWarnings("squid:UnusedPrivateMethod")
  private static Stream<Arguments> testCalculateLagProvider() {

    // WaveformSummary whose end time is EPOCH
    var waveformSummary = WaveformSummary.from(
      "channelName",
      Instant.EPOCH,
      Instant.EPOCH
    );

    // For testing negative lag
    var waveformSummaryFromTheFuture = WaveformSummary.from(
      "channelName",
      Instant.EPOCH,
      Instant.ofEpochMilli(20)
    );

    var waveformSummaryFiltered = WaveformSummary.from(
      "channelName",
      Instant.EPOCH.plusSeconds(10000),
      Instant.EPOCH.plusSeconds(10000)
    );

    var stationSohDefinition = StationSohDefinition
      .create("stationA", Set.of(SohMonitorType.LAG),
        Map.of(SohMonitorType.LAG, Set.of("channelA")), Set.of(ChannelSohDefinition
          .create("channelA", Set.of(SohMonitorType.LAG), Map.of(SohMonitorType.LAG,
            DurationSohMonitorStatusThresholdDefinition
              .create(Duration.ofSeconds(10), Duration.ofSeconds(20))), 0.0)),
        Map.of(SohMonitorType.LAG,
          TimeWindowDefinition.create(Duration.ofMinutes(30), Duration.ofSeconds(0))));

    return Stream.of(
      // Test BAD status
      Arguments.arguments(
        stationSohDefinition,
        List.of(
          WaveformSummaryAndReceptionTime.create(waveformSummary, Instant.ofEpochMilli(10)),
          WaveformSummaryAndReceptionTime.create(waveformSummary, Instant.ofEpochMilli(20))
        ),
        createMockDurationSohMonitorValueAndStatusThresholdDefinition(
          Duration.ofSeconds(2000),
          Duration.ofSeconds(0)
        ),
        DurationSohMonitorValueAndStatus.from(
          Duration.ofMillis(20),
          SohStatus.GOOD,
          SohMonitorType.LAG
        )
      ),
      // Test MARGINAL where average is equal to MARGINAL threshold
      Arguments.arguments(
        stationSohDefinition,
        List.of(
          WaveformSummaryAndReceptionTime.create(waveformSummary, Instant.ofEpochMilli(10)),
          WaveformSummaryAndReceptionTime.create(waveformSummary, Instant.ofEpochMilli(20))
        ),
        createMockDurationSohMonitorValueAndStatusThresholdDefinition(
          Duration.ofSeconds(2000),
          Duration.ofSeconds(0)
        ),
        DurationSohMonitorValueAndStatus.from(
          Duration.ofMillis(20),
          SohStatus.GOOD,
          SohMonitorType.LAG
        )
      ),
      // Test MARGINAL where average is greater than the MARGINAL threshold
      Arguments.arguments(
        stationSohDefinition,
        List.of(
          WaveformSummaryAndReceptionTime.create(waveformSummary, Instant.ofEpochMilli(10)),
          WaveformSummaryAndReceptionTime.create(waveformSummary, Instant.ofEpochMilli(20))
        ),
        createMockDurationSohMonitorValueAndStatusThresholdDefinition(
          Duration.ofSeconds(0),
          Duration.ofSeconds(2000)
        ),
        DurationSohMonitorValueAndStatus.from(
          Duration.ofMillis(20),
          SohStatus.MARGINAL,
          SohMonitorType.LAG
        )
      ),
      // Test GOOD where average is equal to GOOD threshold
      Arguments.arguments(
        stationSohDefinition,
        List.of(
          WaveformSummaryAndReceptionTime.create(waveformSummary, Instant.ofEpochMilli(10)),
          WaveformSummaryAndReceptionTime.create(waveformSummary, Instant.ofEpochMilli(20))
        ),
        createMockDurationSohMonitorValueAndStatusThresholdDefinition(
          Duration.ofSeconds(0),
          Duration.ofSeconds(0)
        ),
        DurationSohMonitorValueAndStatus.from(
          Duration.ofMillis(20),
          SohStatus.BAD,
          SohMonitorType.LAG
        )
      ),
      // Test GOOD where average is greater than the GOOD threshold
      Arguments.arguments(
        stationSohDefinition,
        List.of(
          WaveformSummaryAndReceptionTime.create(waveformSummary, Instant.ofEpochMilli(10)),
          WaveformSummaryAndReceptionTime.create(waveformSummary, Instant.ofEpochMilli(20))
        ),
        createMockDurationSohMonitorValueAndStatusThresholdDefinition(
          Duration.ofSeconds(0),
          Duration.ofSeconds(0)
        ),
        DurationSohMonitorValueAndStatus.from(
          Duration.ofMillis(20),
          SohStatus.BAD,
          SohMonitorType.LAG
        )
      ),
      // Test GOOD when 2nd waveform is trimmed
      Arguments.arguments(
        stationSohDefinition,
        List.of(
          WaveformSummaryAndReceptionTime
            .create(waveformSummary, Instant.ofEpochMilli(5_000)),
          WaveformSummaryAndReceptionTime.create(waveformSummary, Instant.ofEpochMilli(1_000))
        ),
        createMockDurationSohMonitorValueAndStatusThresholdDefinition(
          Duration.ofSeconds(0),
          Duration.ofSeconds(17)
        ),
        DurationSohMonitorValueAndStatus.from(
          Duration.ofMillis(5_000),
          SohStatus.MARGINAL,
          SohMonitorType.LAG
        )
      ),

      Arguments.arguments(
        stationSohDefinition,
        List.of(
          WaveformSummaryAndReceptionTime
            .create(waveformSummaryFiltered, Instant.ofEpochMilli(5_000)),
          WaveformSummaryAndReceptionTime
            .create(waveformSummaryFiltered, Instant.ofEpochMilli(1_000))
        ),
        createMockDurationSohMonitorValueAndStatusThresholdDefinition(
          Duration.ofSeconds(0),
          Duration.ofSeconds(17)
        ),
        DurationSohMonitorValueAndStatus.from(
          null,
          SohStatus.MARGINAL,
          SohMonitorType.LAG
        )
      ),

      // Test negative lag
      Arguments.arguments(
        stationSohDefinition,
        List.of(
          WaveformSummaryAndReceptionTime
            .create(waveformSummaryFromTheFuture, Instant.ofEpochMilli(10)),
          WaveformSummaryAndReceptionTime
            .create(waveformSummaryFromTheFuture, Instant.ofEpochMilli(15))
        ),
        createMockDurationSohMonitorValueAndStatusThresholdDefinition(
          Duration.ofSeconds(2000),
          Duration.ofSeconds(0)
        ),
        DurationSohMonitorValueAndStatus.from(
          Duration.ofMillis(-5),
          SohStatus.BAD,
          SohMonitorType.LAG
        )
      )
    );
  }

  @ParameterizedTest
  @MethodSource("environmentStatusNoPacketsTestProvider")
  void testEnvironmentStatusNoPackets(
    Set<AcquiredChannelEnvironmentIssueBoolean> acquiredChannelEnvironmentIssueSet,
    Map<SohMonitorType, PercentSohMonitorStatusThresholdDefinition> definitionMap,
    Set<PercentSohMonitorValueAndStatus> expected,
    Instant latestTime
  ) {

    AtomicReference<Set<PercentSohMonitorValueAndStatus>> percentSohMonitorValueAndStatusAtomicReference = new AtomicReference<>();

    // TODO need to initialize this to not null
    AcquiredSampleTimesByChannel acquiredSampleTimesByChannel = new AcquiredSampleTimesByChannel();
    Executable executable = () -> {
      percentSohMonitorValueAndStatusAtomicReference.set(
        new HashSet<>(new ChannelSohCalculationUtility(
          latestTime,
          acquiredSampleTimesByChannel
        ).environmentStatus(
          StationSohDefinition
            .create("BLUB", Set.of(SohMonitorType.ENV_BACKUP_POWER_UNSTABLE),
              Map.of(
                SohMonitorType.ENV_BACKUP_POWER_UNSTABLE,
                Set.of("X"),
                SohMonitorType.LAG, Set.of("X"),
                SohMonitorType.MISSING, Set.of("X")
              ), Set.of(
                ChannelSohDefinition.create(
                  "X",
                  Set.of(SohMonitorType.ENV_BACKUP_POWER_UNSTABLE),
                  Map.of(
                    SohMonitorType.ENV_BACKUP_POWER_UNSTABLE,
                    PercentSohMonitorStatusThresholdDefinition
                      .create(25.0, 50.0)
                  ),
                  0.0
                )
              ), TestFixture.createTimeWindowDefMap(
                Set.of(SohMonitorType.ENV_BACKUP_POWER_UNSTABLE))),
          acquiredChannelEnvironmentIssueSet,
          definitionMap
        ))
      );
    };

    Assertions.assertDoesNotThrow(executable);
    Assertions.assertEquals(expected, percentSohMonitorValueAndStatusAtomicReference.get());
  }

  //
  // SonarQube finding: remove unused private method
  //  This is not an unused method. see @MethodSource annotations on parameterized tests.
  //
  @SuppressWarnings("squid:UnusedPrivateMethod")
  private static Stream<Arguments> environmentStatusNoPacketsTestProvider() {
    return Stream.of(
      Arguments.arguments(
        Set.of(
          AcquiredChannelEnvironmentIssueBoolean.from(
            "X",
            AcquiredChannelEnvironmentIssueType.BACKUP_POWER_UNSTABLE,
            Instant.ofEpochSecond(1),
            Instant.ofEpochSecond(2),
            true
          ),
          AcquiredChannelEnvironmentIssueBoolean.from(
            "X",
            AcquiredChannelEnvironmentIssueType.BACKUP_POWER_UNSTABLE,
            Instant.ofEpochSecond(1),
            Instant.ofEpochSecond(2),
            false
          )
        ),
        Map.of(
          SohMonitorType.ENV_BACKUP_POWER_UNSTABLE,
          createMockPercentSohMonitorValueAndStatusThresholdDefinition(
            10.0,
            50.0
          )
        ),
        Set.of(PercentSohMonitorValueAndStatus.from(
          null,
          SohStatus.MARGINAL,
          SohMonitorType.ENV_BACKUP_POWER_UNSTABLE
        )),
        Instant.now()
      )
    );
  }


  @ParameterizedTest
  @MethodSource("environmentStatusTestProvider")
  void testEnvironmentStatus(
    Set<AcquiredChannelEnvironmentIssueBoolean> acquiredChannelEnvironmentIssueSet,
    Map<SohMonitorType, PercentSohMonitorStatusThresholdDefinition> definitionMap,
    Set<PercentSohMonitorValueAndStatus> expected,
    Instant latestTime,
    Class<? extends Throwable> expectedThrowable,
    String expectedThrowableMessage
  ) {

    Validate.isTrue(
      (expected != null && expectedThrowable == null
        && expectedThrowableMessage == null)
        ||
        (expected == null && expectedThrowable != null
          && expectedThrowableMessage != null)
    );

    AtomicReference<Set<PercentSohMonitorValueAndStatus>> percentSohMonitorValueAndStatusAtomicReference = new AtomicReference<>();

    Executable executable = () -> {
      // TODO need to initialize this to not null
      AcquiredSampleTimesByChannel acquiredSampleTimesByChannel = new AcquiredSampleTimesByChannel();
      percentSohMonitorValueAndStatusAtomicReference.set(
        new HashSet<>(new ChannelSohCalculationUtility(
          latestTime,
          acquiredSampleTimesByChannel
        ).environmentStatus(
          StationSohDefinition
            .create("BLUB", Set.of(SohMonitorType.ENV_BACKUP_POWER_UNSTABLE),
              Map.of(
                SohMonitorType.ENV_BACKUP_POWER_UNSTABLE,
                Set.of("X"),
                SohMonitorType.LAG, Set.of("X"),
                SohMonitorType.MISSING, Set.of("X")
              ), Set.of(
                ChannelSohDefinition.create(
                  "X",
                  Set.of(SohMonitorType.ENV_BACKUP_POWER_UNSTABLE,
                    SohMonitorType.LAG,
                    SohMonitorType.MISSING),
                  Map.of(
                    SohMonitorType.ENV_BACKUP_POWER_UNSTABLE,
                    PercentSohMonitorStatusThresholdDefinition.create(
                      25.0, 50.0),

                    SohMonitorType.LAG,
                    DurationSohMonitorStatusThresholdDefinition.create(
                      Duration.ofSeconds(1),
                      Duration.ofSeconds(2)
                    ),

                    SohMonitorType.MISSING,
                    PercentSohMonitorStatusThresholdDefinition.create(
                      25.0, 50.0)
                  ),
                  0.0
                )
              ), Map.of(SohMonitorType.ENV_BACKUP_POWER_UNSTABLE, TimeWindowDefinition
                  .create(Duration.ofSeconds(4), Duration.ofSeconds(0)),
                SohMonitorType.ENV_DIGITIZING_EQUIPMENT_OPEN, TimeWindowDefinition
                  .create(Duration.ofSeconds(4), Duration.ofSeconds(0)))
            ),
          acquiredChannelEnvironmentIssueSet,
          definitionMap
        ))
      );
    };

    if (expectedThrowable == null) {
      Assertions.assertDoesNotThrow(executable);
      Assertions.assertEquals(expected, percentSohMonitorValueAndStatusAtomicReference.get());
    } else {
      Throwable actualThrowable = Assertions.assertThrows(expectedThrowable, executable);
      Assertions.assertEquals(expectedThrowableMessage, actualThrowable.getMessage());
    }
  }

  //
  // SonarQube finding: remove unused private method
  //  This is not an unused method. see @MethodSource annotations on parameterized tests.
  //
  @SuppressWarnings("squid:UnusedPrivateMethod")
  private static Stream<Arguments> environmentStatusTestProvider() {
    return Stream.of(
      Arguments.arguments(
        Set.of(
          AcquiredChannelEnvironmentIssueBoolean.from(
            "X",
            AcquiredChannelEnvironmentIssueType.BACKUP_POWER_UNSTABLE,
            Instant.ofEpochSecond(1),
            Instant.ofEpochSecond(2),
            true
          ),
          AcquiredChannelEnvironmentIssueBoolean.from(
            "X",
            AcquiredChannelEnvironmentIssueType.BACKUP_POWER_UNSTABLE,
            Instant.ofEpochSecond(1),
            Instant.ofEpochSecond(2),
            false
          )
        ),
        Map.of(
          SohMonitorType.ENV_BACKUP_POWER_UNSTABLE,
          createMockPercentSohMonitorValueAndStatusThresholdDefinition(
            10.0,
            50.0
          )
        ),
        Set.of(PercentSohMonitorValueAndStatus.from(
          50.0,
          SohStatus.MARGINAL,
          SohMonitorType.ENV_BACKUP_POWER_UNSTABLE
        )),
        Instant.ofEpochSecond(3),
        null,
        null
      ),

      Arguments.arguments(
        Set.of(
          AcquiredChannelEnvironmentIssueBoolean.from(
            "X",
            AcquiredChannelEnvironmentIssueType.BACKUP_POWER_UNSTABLE,
            Instant.ofEpochSecond(1),
            Instant.ofEpochSecond(2),
            true
          ),
          AcquiredChannelEnvironmentIssueBoolean.from(
            "X",
            AcquiredChannelEnvironmentIssueType.BACKUP_POWER_UNSTABLE,
            Instant.ofEpochSecond(1),
            Instant.ofEpochSecond(2),
            false
          )
        ),
        Map.of(
          SohMonitorType.ENV_BACKUP_POWER_UNSTABLE,
          createMockPercentSohMonitorValueAndStatusThresholdDefinition(
            50.0,
            60.0
          )
        ),
        Set.of(PercentSohMonitorValueAndStatus.from(
          50.0,
          SohStatus.GOOD,
          SohMonitorType.ENV_BACKUP_POWER_UNSTABLE
        )),
        Instant.ofEpochSecond(3),
        null,
        null
      ),

      Arguments.arguments(
        Set.of(
          AcquiredChannelEnvironmentIssueBoolean.from(
            "X",
            AcquiredChannelEnvironmentIssueType.BACKUP_POWER_UNSTABLE,
            Instant.ofEpochSecond(1),
            Instant.ofEpochSecond(2),
            true
          ),
          AcquiredChannelEnvironmentIssueBoolean.from(
            "X",
            AcquiredChannelEnvironmentIssueType.BACKUP_POWER_UNSTABLE,
            Instant.ofEpochSecond(1),
            Instant.ofEpochSecond(2),
            false
          )
        ),
        Map.of(
          SohMonitorType.ENV_BACKUP_POWER_UNSTABLE,
          createMockPercentSohMonitorValueAndStatusThresholdDefinition(
            20.0,
            10.0
          )
        ),
        Set.of(PercentSohMonitorValueAndStatus.from(
          50.0,
          SohStatus.BAD,
          SohMonitorType.ENV_BACKUP_POWER_UNSTABLE
        )),
        Instant.ofEpochSecond(3),
        null,
        null
      ),

      Arguments.arguments(
        Set.of(
          AcquiredChannelEnvironmentIssueBoolean.from(
            "X",
            AcquiredChannelEnvironmentIssueType.BACKUP_POWER_UNSTABLE,
            Instant.ofEpochSecond(1),
            Instant.ofEpochSecond(2),
            true
          ),
          AcquiredChannelEnvironmentIssueBoolean.from(
            "X",
            AcquiredChannelEnvironmentIssueType.BACKUP_POWER_UNSTABLE,
            Instant.ofEpochSecond(1),
            Instant.ofEpochSecond(2),
            false
          ),

          AcquiredChannelEnvironmentIssueBoolean.from(
            "X",
            AcquiredChannelEnvironmentIssueType.DIGITIZING_EQUIPMENT_OPEN,
            Instant.ofEpochSecond(1),
            Instant.ofEpochSecond(2),
            true
          ),
          AcquiredChannelEnvironmentIssueBoolean.from(
            "X",
            AcquiredChannelEnvironmentIssueType.DIGITIZING_EQUIPMENT_OPEN,
            Instant.ofEpochSecond(1),
            Instant.ofEpochSecond(2),
            false
          )
        ),
        Map.of(
          SohMonitorType.ENV_BACKUP_POWER_UNSTABLE,
          createMockPercentSohMonitorValueAndStatusThresholdDefinition(
            20.0,
            10.0
          ),
          SohMonitorType.ENV_DIGITIZING_EQUIPMENT_OPEN,
          createMockPercentSohMonitorValueAndStatusThresholdDefinition(
            50.0,
            60.0
          )
        ),
        Set.of(
          PercentSohMonitorValueAndStatus.from(
            50.0,
            SohStatus.BAD,
            SohMonitorType.ENV_BACKUP_POWER_UNSTABLE
          ),
          PercentSohMonitorValueAndStatus.from(
            50.0,
            SohStatus.GOOD,
            SohMonitorType.ENV_DIGITIZING_EQUIPMENT_OPEN
          )
        ),
        Instant.ofEpochSecond(3),
        null,
        null
      ),

      //
      // Test that multiple unique channels throws an exception.
      //
      Arguments.arguments(
        Set.of(
          AcquiredChannelEnvironmentIssueBoolean.from(
            "CHANNEL-BARKYMARK",
            AcquiredChannelEnvironmentIssueType.BACKUP_POWER_UNSTABLE,
            Instant.ofEpochSecond(1),
            Instant.ofEpochSecond(2),
            true
          ),
          AcquiredChannelEnvironmentIssueBoolean.from(
            "CHANNEL-BOBOFLANKS",
            AcquiredChannelEnvironmentIssueType.BACKUP_POWER_UNSTABLE,
            Instant.ofEpochSecond(1),
            Instant.ofEpochSecond(2),
            false
          )
        ),
        Map.of(SohMonitorType.ENV_BACKUP_POWER_UNSTABLE,
          createMockPercentSohMonitorValueAndStatusThresholdDefinition(
            20.0,
            10.0
          )
        ),
        null,
        Instant.ofEpochSecond(3),
        IllegalArgumentException.class,
        "All acquiredChannelEnvironmentIssues must be from the same Channel."
      )

    );
  }

  @ParameterizedTest
  @MethodSource("testCalculateMissingInvalidArgumentsProvider")
  void testCalculateMissingInvalidArguments(
    StationSohDefinition stationSohDefinition,
    ChannelSohCalculationUtility channelSohCalculationUtility,
    ChannelSohDefinition definition,
    Collection<WaveformSummaryAndReceptionTime> waveformSummaryAndReceptionTimes,
    Class<Throwable> expectedExceptionClass,
    String expectedExceptionMessage) {

    final Throwable exception = Assertions.assertThrows(expectedExceptionClass, () ->
      channelSohCalculationUtility
        .missing(stationSohDefinition, definition, waveformSummaryAndReceptionTimes)
    );

    Assertions.assertEquals(expectedExceptionMessage, exception.getMessage());
  }

  //
  // SonarQube finding: remove unused private method
  //  This is not an unused method. see @MethodSource annotations on parameterized tests.
  //
  @SuppressWarnings("squid:UnusedPrivateMethod")
  private static Stream<Arguments> testCalculateMissingInvalidArgumentsProvider() {

    var now = Instant.now();
    var backOffDuration = Duration.ofMinutes(30);
    var calculationInterval = Duration.ofMinutes(20);
    var waveEnd = now.minus(backOffDuration).minus(1, ChronoUnit.MINUTES);
    var waveBegin = waveEnd.minus(5, ChronoUnit.MINUTES);
    var receptionTime = now.minus(1, ChronoUnit.MINUTES);

    var acquiredSampleTimesByChannel = new AcquiredSampleTimesByChannel();
    var channelSohCalculationUtility = new ChannelSohCalculationUtility(
      now, acquiredSampleTimesByChannel);

    var waveformSummaryAndReceptionTimes = List.of(
      WaveformSummaryAndReceptionTime.create(
        WaveformSummary.from("foo", waveBegin, waveEnd), receptionTime)
    );

    var stationSohDefinition = StationSohDefinition
      .create("BLUB", Set.of(SohMonitorType.MISSING),
        Map.of(
          SohMonitorType.MISSING,
          Set.of("X")
        ), Set.of(
          ChannelSohDefinition.create(
            "X",
            Set.of(SohMonitorType.MISSING),
            Map.of(
              SohMonitorType.MISSING,
              PercentSohMonitorStatusThresholdDefinition.create(
                10.0, 30.0)
            ),
            0.0
          )
        ), Map.of(SohMonitorType.MISSING,
          TimeWindowDefinition.create(calculationInterval, backOffDuration))
      );

    var percentSohMonitorStatusThresholdDefinition =
      PercentSohMonitorStatusThresholdDefinition.create(10, 30);

    var channelSohDefinition = Mockito.mock(ChannelSohDefinition.class);
    Mockito.when(channelSohDefinition.getSohMonitorStatusThresholdDefinitionsBySohMonitorType())
      .thenReturn(
        Map.of(SohMonitorType.MISSING, percentSohMonitorStatusThresholdDefinition));

    return Stream.of(
      Arguments.arguments(
        stationSohDefinition,
        channelSohCalculationUtility,
        null,
        waveformSummaryAndReceptionTimes,
        NullPointerException.class,
        "Null definition"
      ),
      Arguments.arguments(
        null,
        channelSohCalculationUtility,
        channelSohDefinition,
        waveformSummaryAndReceptionTimes,
        NullPointerException.class,
        "Null stationSohDefinition"
      ),
      Arguments.arguments(
        stationSohDefinition,
        channelSohCalculationUtility,
        channelSohDefinition,
        null,
        NullPointerException.class,
        "Null waveformSummaryAndReceptionTimes"
      )
    );
  }

  @ParameterizedTest
  @MethodSource("testCalculateMissingProvider")
  void testCalculateMissing(
    StationSohDefinition stationSohDefinition,
    ChannelSohCalculationUtility channelSohCalculationUtility,
    ChannelSohDefinition definition,
    Collection<WaveformSummaryAndReceptionTime> waveformSummaryAndReceptionTimes,
    PercentSohMonitorValueAndStatus expectedMonitorValueAndStatus) {

    var computedMonitorValueAndStatus =
      channelSohCalculationUtility
        .missing(stationSohDefinition, definition, waveformSummaryAndReceptionTimes);

    Assertions.assertTrue(equal(expectedMonitorValueAndStatus,
        computedMonitorValueAndStatus, 1.0e-12),
      String.format("expected %s, got %s",
        expectedMonitorValueAndStatus,
        computedMonitorValueAndStatus));
  }

  //
  // SonarQube finding: remove unused private method
  //  This is not an unused method. see @MethodSource annotations on parameterized tests.
  //
  @SuppressWarnings("squid:UnusedPrivateMethod")
  private static Stream<Arguments> testCalculateMissingProvider() {

    double marginalThreshold = 30.0;
    double goodThreshold = 10.0;

    var calculationInterval = Duration.ofSeconds(100);
    var backOffDuration = Duration.ofSeconds(30);

    var now = Instant.now();
    var zero = now.minus(backOffDuration).minus(calculationInterval);

    var acquiredSampleTimesByChannel = new AcquiredSampleTimesByChannel();
    var channelSohCalculationUtility =
      new ChannelSohCalculationUtility(now, acquiredSampleTimesByChannel);

    var waveformSummariesAndReceptionTimes =
      List.of(
        // one waveform, no trim:            (0,100)                    100s, good, 0% missing
        List.of(
          WaveformSummaryAndReceptionTime.create(WaveformSummary.from("",
            zero, now), now)
        ),
        // one waveform, trim start and end: (-10, 110)                 100s, good, 0% missing
        List.of(
          WaveformSummaryAndReceptionTime.create(WaveformSummary.from("",
            zero.minus(10, ChronoUnit.SECONDS),
            zero.plus(110, ChronoUnit.SECONDS)), now)
        ),
        // no overlap, no trim:              (50,52), (60,68)           10s,  bad, 89.95% missing
        List.of(
          WaveformSummaryAndReceptionTime.create(WaveformSummary.from("",
            zero.plus(50, ChronoUnit.SECONDS),
            zero.plus(52, ChronoUnit.SECONDS)), now),
          WaveformSummaryAndReceptionTime.create(WaveformSummary.from("",
            zero.plus(60, ChronoUnit.SECONDS),
            zero.plus(68, ChronoUnit.SECONDS)), now)
        ),
        // no overlap, trim start:           (-2,50), (60,81)           71s,  marginal, 29% missing
        List.of(
          WaveformSummaryAndReceptionTime.create(WaveformSummary.from("",
            zero.minus(2, ChronoUnit.SECONDS),
            zero.plus(50, ChronoUnit.SECONDS)), now),
          WaveformSummaryAndReceptionTime.create(WaveformSummary.from("",
            zero.plus(60, ChronoUnit.SECONDS),
            zero.plus(81, ChronoUnit.SECONDS)), now)
        ),
        // no overlap, trim end:             (10,50), (60,120)          80s,  marginal, 20% missing
        List.of(
          WaveformSummaryAndReceptionTime.create(WaveformSummary.from("",
            zero.plus(10, ChronoUnit.SECONDS),
            zero.plus(50, ChronoUnit.SECONDS)), now),
          WaveformSummaryAndReceptionTime.create(WaveformSummary.from("",
            zero.plus(60, ChronoUnit.SECONDS),
            zero.plus(120, ChronoUnit.SECONDS)), now)
        ),
        // overlap, no trim:                 (4,55), (40,95)            91s,  good, 9% missing
        List.of(
          WaveformSummaryAndReceptionTime.create(WaveformSummary.from("",
            zero.plus(4, ChronoUnit.SECONDS),
            zero.plus(55, ChronoUnit.SECONDS)), now),
          WaveformSummaryAndReceptionTime.create(WaveformSummary.from("",
            zero.plus(40, ChronoUnit.SECONDS),
            zero.plus(95, ChronoUnit.SECONDS)), now)
        ),
        // overlap, trim start and end:      (-3,20), (10,75), (80,200) 95s,  good, 5% missing
        List.of(
          WaveformSummaryAndReceptionTime.create(WaveformSummary.from("",
            zero.minus(3, ChronoUnit.SECONDS),
            zero.plus(20, ChronoUnit.SECONDS)), now),
          WaveformSummaryAndReceptionTime.create(WaveformSummary.from("",
            zero.plus(10, ChronoUnit.SECONDS),
            zero.plus(75, ChronoUnit.SECONDS)), now),
          WaveformSummaryAndReceptionTime.create(WaveformSummary.from("",
            zero.plus(80, ChronoUnit.SECONDS),
            zero.plus(200, ChronoUnit.SECONDS)), now)
        ),
        // everything gets trimmed:         (-10, -5), (105, 110)  0s, bad
        List.of(
          WaveformSummaryAndReceptionTime.create(WaveformSummary.from("",
            zero.minus(10, ChronoUnit.SECONDS),
            zero.minus(5, ChronoUnit.SECONDS)), now),
          WaveformSummaryAndReceptionTime.create(WaveformSummary.from("",
            zero.plus(105, ChronoUnit.SECONDS),
            zero.plus(110, ChronoUnit.SECONDS)), now)
        ),
        // No waveform summaries
        List.of()
      );

    double[] expectedMissingValues = {0.0, 0.0, 89.95, 28.95, 19.950000000000003, 8.975,
      4.949999999999988, 100.0, 100.0, 100.0};

    SohStatus[] status = {SohStatus.GOOD, SohStatus.GOOD, SohStatus.BAD, SohStatus.MARGINAL,
      SohStatus.MARGINAL, SohStatus.GOOD, SohStatus.GOOD, SohStatus.BAD, SohStatus.BAD
    };

    var percentSohMonitorStatusThresholdDefinition = PercentSohMonitorStatusThresholdDefinition
      .create(goodThreshold, marginalThreshold);
    var channelSohDefinition = Mockito.mock(ChannelSohDefinition.class);
    Mockito.when(channelSohDefinition.getSohMonitorStatusThresholdDefinitionsBySohMonitorType())
      .thenReturn(
        Map.of(SohMonitorType.MISSING, percentSohMonitorStatusThresholdDefinition));
    Mockito.when(channelSohDefinition.getNominalSampleRateHz()).thenReturn(40.0);

    Stream.Builder<Arguments> streamBuilder = Stream.builder();
    for (int i = 0; i < status.length; ++i) {
      streamBuilder.add(Arguments.arguments(
        StationSohDefinition.create(
          "StationX", Set.of(SohMonitorType.MISSING),
          Map.of(SohMonitorType.MISSING, Set.of("ChannelX")),
          Set.of(ChannelSohDefinition.create("ChannelX", Set.of(SohMonitorType.MISSING),
            Map.of(SohMonitorType.MISSING,
              PercentSohMonitorStatusThresholdDefinition
                .create(goodThreshold, marginalThreshold)),
            channelSohDefinition.getNominalSampleRateHz())),
          Map.of(SohMonitorType.MISSING,
            TimeWindowDefinition.create(calculationInterval, backOffDuration))
        ),
        channelSohCalculationUtility,
        channelSohDefinition,
        waveformSummariesAndReceptionTimes.get(i),
        PercentSohMonitorValueAndStatus.from(
          // Only works if the calculation interval is exactly 100 seconds.
          expectedMissingValues[i],
          status[i],
          SohMonitorType.MISSING
        )
      ));
    }
    return streamBuilder.build();
  }

  @Test
  void testTrim() {
    var channelName = "A";
    var now = Instant.now();
    // The interval is ten to five minutes ago.
    var intervalStart = now.minus(Duration.ofSeconds(10L));
    var intervalEnd = now.minus(Duration.ofSeconds(5L));

    var midpoint = intervalStart.plusMillis(2500L);

    // WaveformSummary's interval exactly matches the interval of interest.
    final WaveformSummary waveformSummaryMatches = WaveformSummary.from(channelName, intervalStart, intervalEnd);

    // Reverse order of interval endpoints.
    assertThrows(IllegalArgumentException.class, () -> ChannelSohCalculationUtility
      .trim(waveformSummaryMatches, intervalEnd, intervalStart));
    // Interval of 0 length
    assertThrows(IllegalArgumentException.class, () -> ChannelSohCalculationUtility
      .trim(waveformSummaryMatches, intervalStart, intervalStart));

    // WaveformSummary's start time doesn't come before its endtime.
    Optional<WaveformSummary> opt = ChannelSohCalculationUtility.trim(
      WaveformSummary.from(channelName, midpoint, midpoint),
      intervalStart, intervalEnd
    );

    assertFalse(opt.isPresent());

    // WaveformSummary's interval comes before the interval of interest.
    opt = ChannelSohCalculationUtility.trim(
      WaveformSummary.from(channelName, intervalStart.minusSeconds(2L), intervalStart),
      intervalStart, intervalEnd
    );

    assertFalse(opt.isPresent());

    // WaveformSummary's interval comes after the interval of interest.
    opt = ChannelSohCalculationUtility.trim(
      WaveformSummary.from(channelName, intervalEnd, intervalEnd.plusSeconds(1L)),
      intervalStart, intervalEnd
    );

    assertFalse(opt.isPresent());

    opt = ChannelSohCalculationUtility.trim(
      waveformSummaryMatches,
      intervalStart, intervalEnd
    );

    assertTrue(opt.isPresent());

    checkTrimmed(opt.get(), intervalStart, intervalEnd);
    // They should be equals, since no trimming was necessary.
    assertEquals(waveformSummaryMatches, opt.get());

    // WaveformSummary's interval is entirely within the interval of interest.
    WaveformSummary summaryWithin = WaveformSummary.from(channelName,
      intervalStart.plusSeconds(1L), intervalEnd.minusSeconds(1L));

    opt = ChannelSohCalculationUtility.trim(
      summaryWithin,
      intervalStart, intervalEnd
    );

    assertTrue(opt.isPresent());

    checkTrimmed(opt.get(), intervalStart, intervalEnd);

    // They should be equal, since no trimming was necessary.
    assertEquals(summaryWithin, opt.get());

    // WaveformSummary's interval starts before and ends within the interval of interest.
    WaveformSummary summaryBeforeAndWithin = WaveformSummary.from(channelName,
      intervalStart.minusSeconds(3L), midpoint);

    opt = ChannelSohCalculationUtility.trim(
      summaryBeforeAndWithin,
      intervalStart, intervalEnd
    );

    assertTrue(opt.isPresent());

    checkTrimmed(opt.get(), intervalStart, intervalEnd);

    // They should be not be equal, since trimming was necessary.
    assertNotEquals(summaryBeforeAndWithin, opt.get());

    // WaveformSummary's interval starts within and ends after the interval of interest.
    WaveformSummary summaryWithinAndAfter = WaveformSummary.from(channelName,
      midpoint, intervalEnd.plusSeconds(1L));

    opt = ChannelSohCalculationUtility.trim(
      summaryWithinAndAfter,
      intervalStart, intervalEnd
    );

    assertTrue(opt.isPresent());

    checkTrimmed(opt.get(), intervalStart, intervalEnd);

    // They should be not be equal, since trimming was necessary.
    assertNotEquals(summaryWithinAndAfter, opt.get());

    // WaveformSummary's interval starts before and ends after the interval of interest.
    WaveformSummary summaryBeforeAndAfter = WaveformSummary.from(channelName,
      intervalStart.minusSeconds(3L), intervalEnd.plusSeconds(1L));

    opt = ChannelSohCalculationUtility.trim(
      summaryBeforeAndAfter,
      intervalStart, intervalEnd
    );

    assertTrue(opt.isPresent());

    checkTrimmed(opt.get(), intervalStart, intervalEnd);

    // They should be not be equal, since trimming was necessary.
    assertNotEquals(summaryBeforeAndAfter, opt.get());
  }

  /**
   * Verify that a waveform summary's interval is entirely contained within an interval of
   * interest.
   */
  private static void checkTrimmed(WaveformSummary waveformSummary,
    Instant intervalStart, Instant intervalEnd) {
    assertTrue(waveformSummary.getStartTime().isBefore(waveformSummary.getEndTime()));
    assertFalse(waveformSummary.getStartTime().isBefore(intervalStart));
    assertFalse(waveformSummary.getEndTime().isAfter(intervalEnd));
  }

  /**
   * To test equality of {@link PercentSohMonitorValueAndStatus} instances allowing for a small
   * deviation in the values.
   */
  private static boolean equal(PercentSohMonitorValueAndStatus expected,
    PercentSohMonitorValueAndStatus actual, double epsilon) {

    if (!Objects.equals(expected, actual)) {

      double value1 = expected.getValue().orElse(Double.NaN);
      double value2 = actual.getValue().orElse(Double.NaN);

      if (!Double.isNaN(value1) && !Double.isNaN(value2)) {
        boolean differentInValueOnly = Objects.equals(expected,
          PercentSohMonitorValueAndStatus.from(value1,
            actual.getStatus(), actual.getMonitorType()));
        if (differentInValueOnly) {
          // If within the epsilon, regard as equal.
          return Math.abs(value1 - value2) <= Math.abs(epsilon);
        }
      }

      return false;
    }

    return true;
  }

  @ParameterizedTest
  @MethodSource("channelSohSetTestProvider")
  void testChannelSohSet(
    Set<AcquiredStationSohExtract> extracts,
    StationSohDefinition stationSohDefinition,
    Set<ChannelSoh> expectedSet
  ) {

    AcquiredSampleTimesByChannel acquiredSampleTimesByChannel = new AcquiredSampleTimesByChannel();
    acquiredSampleTimesByChannel.setLatestChannelToEndTime(Map.of());
    ChannelSohCalculationUtility calculationUtility = new ChannelSohCalculationUtility(
      Instant.ofEpochSecond(3),
      acquiredSampleTimesByChannel
    );

    var waveformSummaryAndReceptionTimes =
      Mono.just(StationSohCalculationUtility.createWsRtMapAndPopulateSampleTimes(
        extracts, acquiredSampleTimesByChannel));
    var aceiBooleanMap =
      Mono.just(StationSohCalculationUtility.createAceiBooleanMap(extracts));

    var theMono = calculationUtility.buildChannelSohSetMono(
      waveformSummaryAndReceptionTimes,
      aceiBooleanMap,
      stationSohDefinition,
      Instant.now()
    );

    StepVerifier.create(theMono)
      .expectNext(expectedSet)
      .verifyComplete();

  }

  //
  // SonarQube finding: remove unused private method
  //  This is not an unused method. see @MethodSource annotations on parameterized tests.
  //
  @SuppressWarnings("squid:UnusedPrivateMethod")
  private static Stream<Arguments> channelSohSetTestProvider() {

    final String CHANNEL_NAME_1 = "BOBOFLECK";
    final String STATION_NAME_1 = "BLUB";

    return Stream.of(
      Arguments.arguments(
        Set.of(
          AcquiredStationSohExtract.create(
            List.of(
              RawStationDataFrameMetadata.builder()
                .setAuthenticationStatus(AuthenticationStatus.AUTHENTICATION_SUCCEEDED)
                .setChannelNames(Set.of(CHANNEL_NAME_1))
                .setStationName(STATION_NAME_1)
                .setPayloadStartTime(Instant.ofEpochSecond(0))
                .setPayloadEndTime(Instant.ofEpochSecond(2))
                .setReceptionTime(Instant.ofEpochSecond(3))
                .setPayloadFormat(RawStationDataFramePayloadFormat.IMS20_WAVEFORM)
                .setWaveformSummaries(
                  Map.of(CHANNEL_NAME_1, WaveformSummary.from(
                    CHANNEL_NAME_1,
                    Instant.EPOCH,
                    Instant.ofEpochSecond(2)
                  ))
                )
                .build()),
            List.of(
              AcquiredChannelEnvironmentIssueBoolean.from(
                CHANNEL_NAME_1,
                AcquiredChannelEnvironmentIssueType.BACKUP_POWER_UNSTABLE,
                Instant.ofEpochSecond(1),
                Instant.ofEpochSecond(2),
                false
              ),
              AcquiredChannelEnvironmentIssueBoolean.from(
                CHANNEL_NAME_1,
                AcquiredChannelEnvironmentIssueType.BACKUP_POWER_UNSTABLE,
                Instant.ofEpochSecond(1),
                Instant.ofEpochSecond(2),
                true
              )
            )
          )
        ),
        StationSohDefinition.create(
          STATION_NAME_1,
          Set.of(SohMonitorType.ENV_BACKUP_POWER_UNSTABLE),
          Map.of(
            SohMonitorType.ENV_BACKUP_POWER_UNSTABLE, Set.of(CHANNEL_NAME_1),
            SohMonitorType.LAG, Set.of(CHANNEL_NAME_1),
            SohMonitorType.MISSING, Set.of(CHANNEL_NAME_1)
          ),
          Set.of(
            ChannelSohDefinition.create(
              CHANNEL_NAME_1,
              Set.of(SohMonitorType.ENV_BACKUP_POWER_UNSTABLE,
                SohMonitorType.LAG,
                SohMonitorType.MISSING),
              Map.of(
                SohMonitorType.ENV_BACKUP_POWER_UNSTABLE,
                PercentSohMonitorStatusThresholdDefinition.create(
                  25.0, 50.0),

                SohMonitorType.LAG,
                DurationSohMonitorStatusThresholdDefinition.create(
                  Duration.ofSeconds(1),
                  Duration.ofSeconds(2)
                ),

                SohMonitorType.MISSING,
                PercentSohMonitorStatusThresholdDefinition.create(
                  25.0, 50.0)
              ),
              0.0
            )
          ), Map.of(SohMonitorType.ENV_BACKUP_POWER_UNSTABLE,
            TimeWindowDefinition.create(Duration.ofSeconds(4), Duration.ofSeconds(0)),
            SohMonitorType.LAG,
            TimeWindowDefinition.create(Duration.ofSeconds(2000), Duration.ofSeconds(0)),
            SohMonitorType.MISSING,
            TimeWindowDefinition.create(Duration.ofSeconds(4), Duration.ofSeconds(0)))
        ),
        Set.of(
          ChannelSoh.from(
            CHANNEL_NAME_1,
            SohStatus.MARGINAL,
            Set.of(
              DurationSohMonitorValueAndStatus.from(
                Duration.ofSeconds(1L),
                SohStatus.GOOD,
                SohMonitorType.LAG
              ),
              PercentSohMonitorValueAndStatus.from(
                50.0,
                SohStatus.MARGINAL,
                SohMonitorType.ENV_BACKUP_POWER_UNSTABLE
              ),
              PercentSohMonitorValueAndStatus.from(
                50.0,
                SohStatus.MARGINAL,
                SohMonitorType.MISSING
              )
            )
          )
        )
      ),

      Arguments.arguments(
        Set.of(
          AcquiredStationSohExtract.create(
            List.of(
              RawStationDataFrameMetadata.builder()
                .setAuthenticationStatus(AuthenticationStatus.AUTHENTICATION_SUCCEEDED)
                .setChannelNames(Set.of(CHANNEL_NAME_1))
                .setStationName(STATION_NAME_1)
                .setPayloadStartTime(Instant.ofEpochSecond(0))
                .setPayloadEndTime(Instant.ofEpochSecond(2))
                .setReceptionTime(Instant.ofEpochSecond(3))
                .setPayloadFormat(RawStationDataFramePayloadFormat.IMS20_WAVEFORM)
                .setWaveformSummaries(
                  Map.of(CHANNEL_NAME_1, WaveformSummary.from(
                    CHANNEL_NAME_1,
                    Instant.EPOCH,
                    Instant.ofEpochSecond(2)
                  ))
                )
                .build()),
            List.of(
              AcquiredChannelEnvironmentIssueBoolean.from(
                CHANNEL_NAME_1,
                AcquiredChannelEnvironmentIssueType.VAULT_DOOR_OPENED,
                Instant.ofEpochSecond(1),
                Instant.ofEpochSecond(2),
                false
              ),
              AcquiredChannelEnvironmentIssueBoolean.from(
                CHANNEL_NAME_1,
                AcquiredChannelEnvironmentIssueType.VAULT_DOOR_OPENED,
                Instant.ofEpochSecond(1),
                Instant.ofEpochSecond(2),
                true
              ),
              //ACEI is purposely not in the StationSohDefinition
              AcquiredChannelEnvironmentIssueBoolean.from(
                CHANNEL_NAME_1,
                AcquiredChannelEnvironmentIssueType.CLIPPED,
                Instant.ofEpochSecond(1),
                Instant.ofEpochSecond(2),
                true
              )
            )
          )
        ),
        StationSohDefinition.create(
          STATION_NAME_1,
          Set.of(SohMonitorType.ENV_VAULT_DOOR_OPENED),
          Map.of(
            SohMonitorType.ENV_VAULT_DOOR_OPENED, Set.of(CHANNEL_NAME_1),
            SohMonitorType.LAG, Set.of(CHANNEL_NAME_1),
            SohMonitorType.MISSING, Set.of(CHANNEL_NAME_1)
          ),
          Set.of(
            ChannelSohDefinition.create(
              CHANNEL_NAME_1,
              Set.of(SohMonitorType.ENV_VAULT_DOOR_OPENED),
              Map.of(
                SohMonitorType.ENV_VAULT_DOOR_OPENED,
                PercentSohMonitorStatusThresholdDefinition.create(
                  25.0, 50.0),

                SohMonitorType.LAG,
                DurationSohMonitorStatusThresholdDefinition.create(
                  Duration.ofSeconds(1),
                  Duration.ofSeconds(2)
                ),

                SohMonitorType.MISSING,
                PercentSohMonitorStatusThresholdDefinition
                  .create(25.0, 50.0)
              ),
              0.0
            )
          ), Map.of(SohMonitorType.ENV_VAULT_DOOR_OPENED,
            TimeWindowDefinition.create(Duration.ofSeconds(4), Duration.ofSeconds(0)),
            SohMonitorType.LAG,
            TimeWindowDefinition.create(Duration.ofSeconds(2000), Duration.ofSeconds(0)),
            SohMonitorType.MISSING,
            TimeWindowDefinition.create(Duration.ofSeconds(4), Duration.ofSeconds(0)))
        ),
        Set.of(
          ChannelSoh.from(
            CHANNEL_NAME_1,
            SohStatus.MARGINAL,
            Set.of(
              PercentSohMonitorValueAndStatus.from(
                50.0,
                SohStatus.MARGINAL,
                SohMonitorType.ENV_VAULT_DOOR_OPENED
              ),
              PercentSohMonitorValueAndStatus.from(
                50.0,
                SohStatus.MARGINAL,
                SohMonitorType.MISSING
              ),
              DurationSohMonitorValueAndStatus.from(
                Duration.ofSeconds(1),
                SohStatus.GOOD,
                SohMonitorType.LAG
              )
            )
          )
        )
      )
    );
  }

  @ParameterizedTest
  @MethodSource("stationChannelTimelinessStatusesTestProvider")
  void testStationChannelTimelinessStatuses(
    StationSohDefinition stationSohDefinition,
    AcquiredSampleTimesByChannel acquiredSampleTimesByChannel,
    Instant now,
    Map<String, DurationSohMonitorValueAndStatus> expectedValueMap
  ) {

    var channelSohCalculationUtility = new ChannelSohCalculationUtility(
      now,
      acquiredSampleTimesByChannel
    );

    var actualValueMap = channelSohCalculationUtility.stationChannelTimelinessStatuses(
      stationSohDefinition,
      now
    );

    Assertions.assertEquals(
      expectedValueMap,
      actualValueMap
    );
  }

  private static Stream<Arguments> stationChannelTimelinessStatusesTestProvider() {

    var now = Instant.EPOCH.plusSeconds(1000);

    var acquiredSampleTimesByChannelMarginal = new AcquiredSampleTimesByChannel();

    acquiredSampleTimesByChannelMarginal.setLatestChannelToEndTime(
      Map.of(
        "MyEverSoLovelyStation.UglyChannel1",
        now.minusSeconds(2)
      )
    );

    var acquiredSampleTimesByChannelBad = new AcquiredSampleTimesByChannel();

    acquiredSampleTimesByChannelBad.setLatestChannelToEndTime(
      Map.of(
        "MyEverSoLovelyStation.UglyChannel1",
        now.minusSeconds(4)
      )
    );

    var acquiredSampleTimesByChannelEmpty = new AcquiredSampleTimesByChannel();

    var acquiredSampleTimesByChannelFuture = new AcquiredSampleTimesByChannel();

    acquiredSampleTimesByChannelFuture.setLatestChannelToEndTime(
      Map.of(
        "MyEverSoLovelyStation.UglyChannel1",
        now.plusSeconds(4)
      )
    );

    return Stream.of(
      Arguments.arguments(
        StationSohDefinition.create(
          "MyEverSoLovelyStation",
          Set.of(SohMonitorType.TIMELINESS),
          Map.of(
            SohMonitorType.TIMELINESS,
            Set.of("MyEverSoLovelyStation.UglyChannel1")
          ),
          Set.of(
            ChannelSohDefinition.create(
              "MyEverSoLovelyStation.UglyChannel1",
              Set.of(SohMonitorType.TIMELINESS),
              Map.of(
                SohMonitorType.TIMELINESS,
                createMockDurationSohMonitorValueAndStatusThresholdDefinition(
                  Duration.ofSeconds(1),
                  Duration.ofSeconds(3)
                )
              ),
              0.0
            )
          ),
          Map.of(
            SohMonitorType.TIMELINESS,
            TimeWindowDefinition.create(
              Duration.ofSeconds(10),
              Duration.ZERO
            )
          )
        ),
        acquiredSampleTimesByChannelMarginal,
        now,
        Map.of(
          "MyEverSoLovelyStation.UglyChannel1",
          DurationSohMonitorValueAndStatus.from(
            Duration.ofSeconds(2),
            SohStatus.MARGINAL,
            SohMonitorType.TIMELINESS
          )
        )

      ),

      Arguments.arguments(
        StationSohDefinition.create(
          "MyEverSoLovelyStation",
          Set.of(SohMonitorType.TIMELINESS),
          Map.of(
            SohMonitorType.TIMELINESS,
            Set.of("MyEverSoLovelyStation.UglyChannel1")
          ),
          Set.of(
            ChannelSohDefinition.create(
              "MyEverSoLovelyStation.UglyChannel1",
              Set.of(SohMonitorType.TIMELINESS),
              Map.of(
                SohMonitorType.TIMELINESS,
                createMockDurationSohMonitorValueAndStatusThresholdDefinition(
                  Duration.ofSeconds(1),
                  Duration.ofSeconds(3)
                )
              ),
              0.0
            )
          ),
          Map.of(
            SohMonitorType.TIMELINESS,
            TimeWindowDefinition.create(
              Duration.ofSeconds(10),
              Duration.ZERO
            )
          )
        ),
        acquiredSampleTimesByChannelBad,
        now,
        Map.of(
          "MyEverSoLovelyStation.UglyChannel1",
          DurationSohMonitorValueAndStatus.from(
            Duration.ofSeconds(4),
            SohStatus.BAD,
            SohMonitorType.TIMELINESS
          )
        )
      ),

      Arguments.arguments(
        StationSohDefinition.create(
          "MyEverSoLovelyStation",
          Set.of(SohMonitorType.TIMELINESS),
          Map.of(
            SohMonitorType.TIMELINESS,
            Set.of("MyEverSoLovelyStation.UglyChannel1")
          ),
          Set.of(
            ChannelSohDefinition.create(
              "MyEverSoLovelyStation.UglyChannel1",
              Set.of(SohMonitorType.TIMELINESS),
              Map.of(
                SohMonitorType.TIMELINESS,
                createMockDurationSohMonitorValueAndStatusThresholdDefinition(
                  Duration.ofSeconds(1),
                  Duration.ofSeconds(3)
                )
              ),
              0.0
            )
          ),
          Map.of(
            SohMonitorType.TIMELINESS,
            TimeWindowDefinition.create(
              Duration.ofSeconds(10),
              Duration.ZERO
            )
          )
        ),
        acquiredSampleTimesByChannelEmpty,
        now,
        Map.of(
          "MyEverSoLovelyStation.UglyChannel1",
          DurationSohMonitorValueAndStatus.from(
            null,
            SohStatus.MARGINAL,
            SohMonitorType.TIMELINESS
          )
        )
      ),

      Arguments.arguments(
        StationSohDefinition.create(
          "MyEverSoLovelyStation",
          Set.of(SohMonitorType.TIMELINESS),
          Map.of(
            SohMonitorType.TIMELINESS,
            Set.of("MyEverSoLovelyStation.UglyChannel1")
          ),
          Set.of(
            ChannelSohDefinition.create(
              "MyEverSoLovelyStation.UglyChannel1",
              Set.of(SohMonitorType.TIMELINESS),
              Map.of(
                SohMonitorType.TIMELINESS,
                createMockDurationSohMonitorValueAndStatusThresholdDefinition(
                  Duration.ofSeconds(1),
                  Duration.ofSeconds(3)
                )
              ),
              0.0
            )
          ),
          Map.of(
            SohMonitorType.TIMELINESS,
            TimeWindowDefinition.create(
              Duration.ofSeconds(10),
              Duration.ZERO
            )
          )
        ),
        acquiredSampleTimesByChannelFuture,
        now,
        Map.of(
          "MyEverSoLovelyStation.UglyChannel1",
          DurationSohMonitorValueAndStatus.from(
            Duration.ofSeconds(-4),
            SohStatus.BAD,
            SohMonitorType.TIMELINESS
          )
        )
      )
    );
  }

  private static DurationSohMonitorStatusThresholdDefinition createMockDurationSohMonitorValueAndStatusThresholdDefinition(
    Duration goodThreshold, Duration marginalThreshold) {

    DurationSohMonitorStatusThresholdDefinition mockDefinition = Mockito
      .mock(DurationSohMonitorStatusThresholdDefinition.class);

    Mockito.when(mockDefinition.getMarginalThreshold())
      .thenReturn(marginalThreshold);

    Mockito.when(mockDefinition.getGoodThreshold())
      .thenReturn(goodThreshold);

    return mockDefinition;
  }

  private static PercentSohMonitorStatusThresholdDefinition createMockPercentSohMonitorValueAndStatusThresholdDefinition(
    double goodThreshold, double marginalThreshold) {

    PercentSohMonitorStatusThresholdDefinition mockDefinition = Mockito
      .mock(PercentSohMonitorStatusThresholdDefinition.class);

    Mockito.when(mockDefinition.getMarginalThreshold())
      .thenReturn(marginalThreshold);

    Mockito.when(mockDefinition.getGoodThreshold())
      .thenReturn(goodThreshold);

    return mockDefinition;
  }

}
