package gms.core.performancemonitoring.uimaterializedview;

import com.google.common.base.Functions;
import gms.core.performancemonitoring.ssam.control.config.StationSohMonitoringUiClientParameters;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.soh.CapabilitySohRollup;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohStatus;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageType;
import gms.shared.frameworks.osd.coi.systemmessages.util.StationCapabilityStatusChangedBuilder;
import gms.shared.frameworks.osd.coi.systemmessages.util.StationGroupCapabilityStatusChangedBuilder;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Sinks;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gms.core.performancemonitoring.uimaterializedview.utils.MaterializedViewTestFixtures.MARGINAL_STATION_GROUPS;
import static gms.core.performancemonitoring.uimaterializedview.utils.MaterializedViewTestFixtures.MARGINAL_UI_STATION_SOH;
import static gms.core.performancemonitoring.uimaterializedview.utils.MaterializedViewTestFixtures.QUIETED_CHANGE_1;
import static gms.core.performancemonitoring.uimaterializedview.utils.MaterializedViewTestFixtures.STATION_SOH_PARAMETERS;
import static gms.core.performancemonitoring.uimaterializedview.utils.MaterializedViewTestFixtures.UNACK_CHANGE_1;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.ALTERNATE_GROUP_NAME;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.ALT_MARGINAL_STATION_GROUP_BAD_STATION_CAPABILITY_ROLLUP;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.ALT_MARGINAL_STATION_GROUP_GOOD_STATION_CAPABILITY_ROLLUP;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.BAD_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.BAD_STATION_SOH;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.MARGINAL_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.MARGINAL_STATION_GROUP_BAD_STATION_CAPABILITY_ROLLUP;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.MARGINAL_STATION_SOH;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.SIMPLE_MARGINAL_STATION_SOH;
import static gms.shared.frameworks.osd.coi.systemmessages.SystemMessageType.STATION_CAPABILITY_STATUS_CHANGED;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.STATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class UiStationAndStationGroupGeneratorTest {
  private static final Object NULL_OBJECT = null;

  @BeforeAll
  static void initializeContributingMap() {
    // Initialize the StationSohContributingUtility before creating a channel soh
    StationSohContributingUtility.getInstance().initialize(STATION_SOH_PARAMETERS);
  }

  @ParameterizedTest
  @MethodSource("getGenerateArguments")
  void testGenerateUiStationSohValidation(Set<StationSoh> stationSohs,
    UIStationAndStationGroupsChanges uiStationAndStationGroupsChanges,
    Set<CapabilitySohRollup> capabilitySohRollups,
    StationSohMonitoringUiClientParameters stationSohConfig,
    List<StationGroup> stationGroups,
    Class<? extends Exception> expectedException) {

    Sinks.Many<SystemMessage> systemMessageSink = Sinks.many().multicast().onBackpressureBuffer();
    assertThrows(expectedException, () ->
      UiStationAndStationGroupGenerator.generateUiStationAndStationGroups(
        stationSohs,
        uiStationAndStationGroupsChanges,
        capabilitySohRollups,
        stationSohConfig,
        stationGroups,
        false,
        systemMessageSink
      ));
  }

  static Stream<Arguments> getGenerateArguments() {
    return Stream.of(
      arguments(NULL_OBJECT,
        UIStationAndStationGroupsChanges.builder().
          setQuietedSohStatusChanges(List.of(QUIETED_CHANGE_1))
          .setUnacknowledgedStatusChanges(List.of(UNACK_CHANGE_1))
          .build(),
        Set.of(MARGINAL_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP,
          BAD_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP),
        STATION_SOH_PARAMETERS,
        List.of(UtilsTestFixtures.STATION_GROUP),
        NullPointerException.class),
      arguments(Set.of(),
        UIStationAndStationGroupsChanges.builder().
          setQuietedSohStatusChanges(List.of(QUIETED_CHANGE_1))
          .setUnacknowledgedStatusChanges(List.of(UNACK_CHANGE_1))
          .build(),
        Set.of(MARGINAL_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP,
          BAD_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP),
        STATION_SOH_PARAMETERS,
        List.of(UtilsTestFixtures.STATION_GROUP),
        IllegalStateException.class),
      arguments(Set.of(BAD_STATION_SOH),
        UIStationAndStationGroupsChanges.builder().
          setQuietedSohStatusChanges(List.of(QUIETED_CHANGE_1))
          .setUnacknowledgedStatusChanges(List.of(UNACK_CHANGE_1))
          .build(),
        NULL_OBJECT,
        STATION_SOH_PARAMETERS,
        List.of(UtilsTestFixtures.STATION_GROUP),
        NullPointerException.class),
      arguments(Set.of(BAD_STATION_SOH),
        UIStationAndStationGroupsChanges.builder().
          setQuietedSohStatusChanges(List.of(QUIETED_CHANGE_1))
          .setUnacknowledgedStatusChanges(List.of(UNACK_CHANGE_1))
          .build(),
        Set.of(MARGINAL_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP,
          BAD_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP),
        NULL_OBJECT,
        List.of(UtilsTestFixtures.STATION_GROUP),
        NullPointerException.class));
  }

  @Test
  void testGeneratorStationAndStationGroups() {
    Sinks.Many<SystemMessage> systemMessageSink = Sinks.many().multicast().onBackpressureBuffer();
    List<UiStationAndStationGroups> actual = assertDoesNotThrow(() ->
      UiStationAndStationGroupGenerator.generateUiStationAndStationGroups(
        Set.of(MARGINAL_STATION_SOH),
        UIStationAndStationGroupsChanges.builder()
          .setQuietedSohStatusChanges(List.of())
          .setUnacknowledgedStatusChanges(List.of(UNACK_CHANGE_1)).build(),
        Set.of(MARGINAL_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP),
        STATION_SOH_PARAMETERS,
        List.of(UtilsTestFixtures.STATION_GROUP),
        false,
        systemMessageSink
      ));

    assertEquals(1, actual.size());

    UiStationAndStationGroups actualStationAndGroups = actual.get(0);
    assertEquals(1, actualStationAndGroups.getStationGroups().size());

    assertTrue(EqualsBuilder.reflectionEquals(MARGINAL_STATION_GROUPS,
      actualStationAndGroups.getStationGroups().get(0),
      "time"));

    assertEquals(1, actualStationAndGroups.getStationSoh().size());
    UiStationSoh actualStationSoh = actualStationAndGroups.getStationSoh().get(0);
    UiStationSoh expectedStationSoh = MARGINAL_UI_STATION_SOH;
    assertTrue(
      EqualsBuilder.reflectionEquals(expectedStationSoh,
        actualStationSoh,
        "uuid", "time", "statusContributors", "stationGroups", "channelSohs"));

    // TODO: Uncomment after fix to gms.shared.frameworks.osd.coi.SOHTestFixtures
    assertEquals(MARGINAL_UI_STATION_SOH.getStatusContributors().size(),
      actualStationSoh.getStatusContributors().size());

    // TODO: Fix this assert. Will need a change to the Test Fixture not to have duplicate
    // Contributor entries based on monitor type
//    assertTrue(MARGINAL_UI_STATION_SOH.getStatusContributors()
//        .containsAll(actualStationSoh.getStatusContributors()));

    assertEquals(MARGINAL_UI_STATION_SOH.getStationGroups().size(),
      actualStationSoh.getStationGroups().size());
    assertTrue(MARGINAL_UI_STATION_SOH.getStationGroups()
      .containsAll(actualStationSoh.getStationGroups()));

    assertEquals(MARGINAL_UI_STATION_SOH.getChannelSohs().size(),
      actualStationSoh.getChannelSohs().size());
    Map<String, UiChannelSoh> expectedChannelSohs = MARGINAL_UI_STATION_SOH.getChannelSohs()
      .stream()
      .collect(Collectors.toMap(UiChannelSoh::getChannelName, Functions.identity()));
    actualStationSoh.getChannelSohs().stream()
      .forEach(actualChannelSoh -> {
        assertTrue(expectedChannelSohs.containsKey(actualChannelSoh.getChannelName()));
        UiChannelSoh expectedChannelSoh = expectedChannelSohs
          .get(actualChannelSoh.getChannelName());
        assertTrue(EqualsBuilder.reflectionEquals(expectedChannelSoh,
          actualChannelSoh,
          "allSohMonitorValueAndStatuses"));

        assertEquals(expectedChannelSoh.getAllSohMonitorValueAndStatuses().size(),
          actualChannelSoh.getAllSohMonitorValueAndStatuses().size());

        Map<SohMonitorType, UiSohMonitorValueAndStatus> expectedMonitorsByType =
          expectedChannelSoh.getAllSohMonitorValueAndStatuses().stream()
            .collect(Collectors
              .toMap(UiSohMonitorValueAndStatus::getMonitorType, Functions.identity()));
        actualChannelSoh.getAllSohMonitorValueAndStatuses().stream()
          .forEach(actualSmvs -> {
            assertTrue(expectedMonitorsByType.containsKey(actualSmvs.getMonitorType()));
            UiSohMonitorValueAndStatus expectedSmvs = expectedMonitorsByType
              .get(actualSmvs.getMonitorType());
            assertTrue(
              EqualsBuilder.reflectionEquals(expectedSmvs, actualSmvs, "quietDurationMs",
                "contributing"));
          });
      });
  }

  /**
   * Tests the breaking up of the UiStationAndStationGroup message into multiple messages
   * which stay below the 1MB limit.
   */
  @Test
  void testMakeGroupsMessage() {

    // Test the number of messages returned is 1
    // and it only has one UiStationGroup and one UiStationSoh entry
    Sinks.Many<SystemMessage> systemMessageSink = Sinks.many().multicast().onBackpressureBuffer();
    List<UiStationAndStationGroups> actual = assertDoesNotThrow(() ->
      UiStationAndStationGroupGenerator.generateUiStationAndStationGroups(
        Set.of(MARGINAL_STATION_SOH),
        UIStationAndStationGroupsChanges.builder()
          .setQuietedSohStatusChanges(List.of(QUIETED_CHANGE_1))
          .setUnacknowledgedStatusChanges(List.of(UNACK_CHANGE_1)).build(),
        Set.of(MARGINAL_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP),
        STATION_SOH_PARAMETERS,
        List.of(UtilsTestFixtures.STATION_GROUP),
        false,
        systemMessageSink
      ));

    assertEquals(1, actual.size());
    UiStationAndStationGroups actualStationAndGroups = actual.get(0);
    assertEquals(1, actualStationAndGroups.getStationGroups().size());
    assertEquals(1, actualStationAndGroups.getStationSoh().size());

    // Okay build the list up with 2000 UiStationSohs
    int numUiStationSohs = 2000;
    var stationSohs = new HashSet<StationSoh>();
    for (int i = 0; i < numUiStationSohs; i++) {
      stationSohs.add(SIMPLE_MARGINAL_STATION_SOH
        .toBuilder()
        .setTime(SIMPLE_MARGINAL_STATION_SOH.getTime().plusSeconds(1))
        .setId(UUID.randomUUID())
        .build());
    }
    List<UiStationAndStationGroups> bigger = assertDoesNotThrow(() ->
      UiStationAndStationGroupGenerator.generateUiStationAndStationGroups(
        stationSohs,
        UIStationAndStationGroupsChanges.builder()
          .setQuietedSohStatusChanges(List.of(QUIETED_CHANGE_1))
          .setUnacknowledgedStatusChanges(List.of(UNACK_CHANGE_1)).build(),
        Set.of(MARGINAL_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP),
        STATION_SOH_PARAMETERS,
        List.of(UtilsTestFixtures.STATION_GROUP),
        false,
        systemMessageSink
      ));

    // Should get back two messages
    assertEquals(2, bigger.size());

    // Check each message is less than the Kafka message size limit 1mb
    byte[] b;
    StringSerializer serializer = new StringSerializer();
    int numUiStationFound = 0; // While we are at it count the number of UiStationSoh in the list of msgs
    for (UiStationAndStationGroups msg : bigger) {
      b = serializer.serialize(null, String.valueOf(msg));
      assertTrue(b.length < UiStationAndStationGroupGenerator.KAFKA_MSG_SIZE_LIMIT);
      numUiStationFound += msg.getStationSoh().size();
    }
    assertEquals(numUiStationFound, numUiStationSohs);
  }

  @ParameterizedTest
  @MethodSource("statusChangedSystemMessagesProvider")
  void testStatusChangedSystemMessages(
    Set<StationSoh> previousStationSohs,
    Set<StationSoh> currentStationSohs,
    Set<CapabilitySohRollup> previousCapabilityRollups,
    Set<CapabilitySohRollup> currentCapabilityRollups,
    List<StationGroup> stationGroups,
    Map<SystemMessageType, List<SystemMessage>> expectedSystemMessages
  ) {

    var outputSystemMessages = new HashSet<SystemMessage>();

    Sinks.Many<SystemMessage> systemMessageSink = Sinks.many().multicast().onBackpressureBuffer();

    var disposable = systemMessageSink.asFlux().doOnNext(outputSystemMessages::add).subscribe();

    UiStationAndStationGroupGenerator.clearPrevious();

    //
    // call generateUiStationAndStationGroups twice.
    //
    // First call: Should have empty status messages for the keys we are interested in
    //
    UiStationAndStationGroupGenerator.generateUiStationAndStationGroups(
      previousStationSohs,
      UIStationAndStationGroupsChanges.builder()
        .setQuietedSohStatusChanges(List.of())
        .setUnacknowledgedStatusChanges(List.of()).build(),
      previousCapabilityRollups,
      STATION_SOH_PARAMETERS,
      stationGroups,
      false,
      systemMessageSink
    );

    //
    // Wait some time for threads to finish
    //
    var startMs = System.currentTimeMillis();
    while (System.currentTimeMillis() - startMs < 500) {
      Thread.yield();
    }

    //
    // Just tests the message types we are interested in.
    //
    List.of(
      SystemMessageType.STATION_GROUP_CAPABILITY_STATUS_CHANGED,
      STATION_CAPABILITY_STATUS_CHANGED
    ).forEach(
      systemMessageType -> Assertions.assertTrue(outputSystemMessages.stream()
        .noneMatch(systemMessage -> systemMessage.getType() == systemMessageType))
    );

    outputSystemMessages.clear();

    //
    // Second call will create messages if there are changes.
    //
    UiStationAndStationGroupGenerator.generateUiStationAndStationGroups(
      currentStationSohs,
      UIStationAndStationGroupsChanges.builder()
        .setQuietedSohStatusChanges(List.of())
        .setUnacknowledgedStatusChanges(List.of()).build(),
      currentCapabilityRollups,
      STATION_SOH_PARAMETERS,
      stationGroups,
      false,
      systemMessageSink
    );

    //
    // Wait some time for threads to finish
    //
    startMs = System.currentTimeMillis();
    while (System.currentTimeMillis() - startMs < 500) {
      Thread.yield();
    }

    //
    // Just tests the message types we are interested in.
    //
    List.of(
      SystemMessageType.STATION_GROUP_CAPABILITY_STATUS_CHANGED,
      STATION_CAPABILITY_STATUS_CHANGED
    ).forEach(
      systemMessageType -> {
        var expectedMessagesForType = expectedSystemMessages.get(systemMessageType);

        var actualMessagesForType = outputSystemMessages.stream()
          .filter(systemMessage -> systemMessage.getType() == systemMessageType)
          .collect(Collectors.toSet());

        if (Objects.isNull(expectedMessagesForType)) {
          Assertions.assertTrue(
            actualMessagesForType.isEmpty()
          );
        } else {
          Assertions.assertEquals(
            expectedMessagesForType.size(),
            actualMessagesForType.size()
          );

          assertEquals(expectedMessagesForType.size(), actualMessagesForType.size());
          actualMessagesForType.forEach(actualMessage ->
            assertThat(expectedMessagesForType.stream().filter(message ->
              message.getMessage().equals(actualMessage.getMessage()) && message.getMessageTags().equals(actualMessage.getMessageTags())).findFirst()).isPresent()
          );

        }
      }
    );

    // Just so that the subscriber isnt waiting around for more SystemMessages that will
    // never come.
    systemMessageSink.tryEmitComplete();
    disposable.dispose();
  }

  private static Stream<Arguments> statusChangedSystemMessagesProvider() {

    return Stream.of(

      //
      // No changes
      //
      Arguments.arguments(
        Set.of(MARGINAL_STATION_SOH),
        Set.of(MARGINAL_STATION_SOH),
        Set.of(MARGINAL_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP),
        Set.of(MARGINAL_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP),
        List.of(UtilsTestFixtures.STATION_GROUP),
        Map.of()
      ),

      //
      // Station Group capability rollup change
      //
      Arguments.arguments(
        Set.of(SIMPLE_MARGINAL_STATION_SOH),
        Set.of(SIMPLE_MARGINAL_STATION_SOH),
        Set.of(BAD_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP),
        Set.of(MARGINAL_STATION_GROUP_BAD_STATION_CAPABILITY_ROLLUP),
        List.of(UtilsTestFixtures.STATION_GROUP),
        Map.of(
          SystemMessageType.STATION_GROUP_CAPABILITY_STATUS_CHANGED,
          List.of(
            new StationGroupCapabilityStatusChangedBuilder(
              UtilsTestFixtures.STATION_GROUP.getName(),
              SohStatus.BAD,
              SohStatus.MARGINAL
            ).build()
          )
        )
      ),

      //
      // Station capability rollup change, no change to station group
      //
      Arguments.arguments(
        Set.of(SIMPLE_MARGINAL_STATION_SOH),
        Set.of(SIMPLE_MARGINAL_STATION_SOH),
        Set.of(MARGINAL_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP),
        Set.of(MARGINAL_STATION_GROUP_BAD_STATION_CAPABILITY_ROLLUP),
        List.of(UtilsTestFixtures.STATION_GROUP),
        Map.of(
          STATION_CAPABILITY_STATUS_CHANGED,
          List.of(
            new StationCapabilityStatusChangedBuilder(
              STATION.getName(),
              UtilsTestFixtures.STATION_GROUP.getName(),
              SohStatus.MARGINAL,
              SohStatus.BAD
            ).build()
          )
        )
      ),

      //
      // Change to station capability rollup AND station group capability rollup
      //
      Arguments.arguments(
        Set.of(SIMPLE_MARGINAL_STATION_SOH),
        Set.of(SIMPLE_MARGINAL_STATION_SOH),
        Set.of(BAD_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP),
        Set.of(MARGINAL_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP),
        List.of(UtilsTestFixtures.STATION_GROUP),
        Map.of(
          SystemMessageType.STATION_GROUP_CAPABILITY_STATUS_CHANGED,
          List.of(
            new StationGroupCapabilityStatusChangedBuilder(
              UtilsTestFixtures.STATION_GROUP.getName(),
              SohStatus.BAD,
              SohStatus.MARGINAL
            ).build()
          ),
          STATION_CAPABILITY_STATUS_CHANGED,
          List.of(
            new StationCapabilityStatusChangedBuilder(
              STATION.getName(),
              UtilsTestFixtures.STATION_GROUP.getName(),
              SohStatus.BAD,
              SohStatus.MARGINAL
            ).build()
          )
        )
      ),

      //
      // A station that belongs to two groups changed its status in both groups without changing
      // the status of either group
      //
      Arguments.arguments(
        Set.of(SIMPLE_MARGINAL_STATION_SOH),
        Set.of(SIMPLE_MARGINAL_STATION_SOH),
        Set.of(
          MARGINAL_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP,
          ALT_MARGINAL_STATION_GROUP_GOOD_STATION_CAPABILITY_ROLLUP
        ),
        Set.of(
          MARGINAL_STATION_GROUP_BAD_STATION_CAPABILITY_ROLLUP,
          ALT_MARGINAL_STATION_GROUP_BAD_STATION_CAPABILITY_ROLLUP
        ),
        List.of(UtilsTestFixtures.STATION_GROUP),
        Map.of(
          STATION_CAPABILITY_STATUS_CHANGED,
          List.of(
            new StationCapabilityStatusChangedBuilder(
              STATION.getName(),
              UtilsTestFixtures.STATION_GROUP.getName(),
              SohStatus.MARGINAL,
              SohStatus.BAD
            ).build(),
            new StationCapabilityStatusChangedBuilder(
              STATION.getName(),
              ALTERNATE_GROUP_NAME,
              SohStatus.GOOD,
              SohStatus.BAD
            ).build()
          )
        )
      ),

      //
      // A Station SOH changed, A station capability rollup changed, and a station group
      // capability rollup changed.
      //
      Arguments.arguments(
        Set.of(SIMPLE_MARGINAL_STATION_SOH),
        Set.of(BAD_STATION_SOH),
        Set.of(BAD_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP),
        Set.of(MARGINAL_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP),
        List.of(UtilsTestFixtures.STATION_GROUP),
        Map.of(
          SystemMessageType.STATION_GROUP_CAPABILITY_STATUS_CHANGED,
          List.of(
            new StationGroupCapabilityStatusChangedBuilder(
              UtilsTestFixtures.STATION_GROUP.getName(),
              SohStatus.BAD,
              SohStatus.MARGINAL
            ).build()
          ),
          STATION_CAPABILITY_STATUS_CHANGED,
          List.of(
            new StationCapabilityStatusChangedBuilder(
              STATION.getName(),
              UtilsTestFixtures.STATION_GROUP.getName(),
              SohStatus.BAD,
              SohStatus.MARGINAL
            ).build()
          )
        )
      )
    );
  }
}
