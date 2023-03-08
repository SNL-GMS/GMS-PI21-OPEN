package gms.core.performancemonitoring.uimaterializedview;

import gms.shared.frameworks.osd.coi.soh.StationSoh;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageType;
import gms.shared.frameworks.osd.coi.systemmessages.util.StationNeedsAttentionBuilder;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Sinks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gms.core.performancemonitoring.uimaterializedview.utils.MaterializedViewTestFixtures.ALT_UNACK_CHANGE_1;
import static gms.core.performancemonitoring.uimaterializedview.utils.MaterializedViewTestFixtures.ENV_GAP_UNACK_CHANGE_1;
import static gms.core.performancemonitoring.uimaterializedview.utils.MaterializedViewTestFixtures.QUIETED_CHANGE_1;
import static gms.core.performancemonitoring.uimaterializedview.utils.MaterializedViewTestFixtures.STATION_SOH_PARAMETERS;
import static gms.core.performancemonitoring.uimaterializedview.utils.MaterializedViewTestFixtures.UNACK_CHANGE_1;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.ALT_SIMPLE_MARGINAL_STATION_SOH;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.MARGINAL_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.MARGINAL_STATION_GROUP_BAD_STATION_CAPABILITY_ROLLUP;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.SIMPLE_MARGINAL_STATION_SOH;
import static gms.shared.frameworks.osd.coi.systemmessages.SystemMessageType.STATION_CAPABILITY_STATUS_CHANGED;
import static gms.shared.frameworks.osd.coi.systemmessages.SystemMessageType.STATION_NEEDS_ATTENTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UiStationGeneratorTest {

  @BeforeAll
  static void initializeContributingMap() {
    // Initialize the StationSohContributingUtility before creating a channel soh
    StationSohContributingUtility.getInstance().initialize(STATION_SOH_PARAMETERS);
  }

  @Test
  void testNewSameStationCapabilityStatusDoesNotGenerateMessage() {

    var outputSystemMessages = new ArrayList<SystemMessage>();

    Sinks.Many<SystemMessage> systemMessagesSink = Sinks.many().unicast().onBackpressureError();

    var disposable = systemMessagesSink.asFlux().doOnNext(outputSystemMessages::add).subscribe();

    //
    // Starting off, there should be no messages for STATION_CAPABILITY_STATUS_CHANGED
    //
    UiStationGenerator.buildUiStationSohList(
      Set.of(SIMPLE_MARGINAL_STATION_SOH),
      List.of(),
      List.of(),
      Set.of(MARGINAL_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP),
      STATION_SOH_PARAMETERS,
      List.of(UtilsTestFixtures.STATION_GROUP),
      systemMessagesSink
    );

    //
    // Wait some time for threads to finish
    //
    var startMs = System.currentTimeMillis();
    while (System.currentTimeMillis() - startMs < 500) {
      Thread.yield();
    }

    Assertions.assertTrue(outputSystemMessages.stream()
      .noneMatch(systemMessage -> systemMessage.getType() == STATION_CAPABILITY_STATUS_CHANGED));

    //
    // Change the status of the station. There should be a message for
    // STATION_CAPABILITY_STATUS_CHANGED
    //
    UiStationGenerator.buildUiStationSohList(
      Set.of(SIMPLE_MARGINAL_STATION_SOH),
      List.of(),
      List.of(),
      Set.of(MARGINAL_STATION_GROUP_BAD_STATION_CAPABILITY_ROLLUP),
      STATION_SOH_PARAMETERS,
      List.of(UtilsTestFixtures.STATION_GROUP),
      systemMessagesSink
    );

    //
    // Wait some time for threads to finish
    //
    startMs = System.currentTimeMillis();
    while (System.currentTimeMillis() - startMs < 500) {
      Thread.yield();
    }

    Assertions.assertTrue(
      outputSystemMessages.stream().anyMatch(systemMessage ->
        systemMessage.getType() == STATION_CAPABILITY_STATUS_CHANGED)
    );

    // clear this, so its easy to test that there are no new messages
    outputSystemMessages.clear();

    //
    // Another CapabilitySohRollup with the station staying in the same status. There should
    // be no new message for STATION_CAPABILITY_STATUS_CHANGED
    //
    UiStationGenerator.buildUiStationSohList(
      Set.of(SIMPLE_MARGINAL_STATION_SOH),
      List.of(),
      List.of(),
      Set.of(MARGINAL_STATION_GROUP_BAD_STATION_CAPABILITY_ROLLUP),
      STATION_SOH_PARAMETERS,
      List.of(UtilsTestFixtures.STATION_GROUP),
      systemMessagesSink
    );

    //
    // Wait some time for threads to finish
    //
    startMs = System.currentTimeMillis();
    while (System.currentTimeMillis() - startMs < 500) {
      Thread.yield();
    }

    Assertions.assertFalse(
      outputSystemMessages.stream().anyMatch(systemMessage ->
        systemMessage.getType() == STATION_CAPABILITY_STATUS_CHANGED)
    );

    // Just so that the subscriber isnt waiting around for more SystemMessages that will
    // never come.
    systemMessagesSink.tryEmitComplete();
    disposable.dispose();
  }

  @ParameterizedTest
  @MethodSource("needsAttentionSystemMessagesTestSource")
  void testNeedsAttentionSystemMessages(
    Set<StationSoh> stationSohs,
    UIStationAndStationGroupsChanges uiStationAndStationGroupsChanges,
    Map<SystemMessageType, List<SystemMessage>> expectedMessages
  ) {

    var outputSystemMessages = new ArrayList<SystemMessage>();


    Sinks.Many<SystemMessage> systemMessagesSink = Sinks.many().unicast().onBackpressureError();

    var disposable = systemMessagesSink.asFlux().doOnNext(outputSystemMessages::add).subscribe();

    UiStationGenerator.clearPrevious();

    UiStationGenerator.buildUiStationSohList(
      stationSohs,
      uiStationAndStationGroupsChanges.getUnacknowledgedStatusChanges(),
      uiStationAndStationGroupsChanges.getQuietedSohStatusChanges(),
      Set.of(MARGINAL_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP),
      STATION_SOH_PARAMETERS,
      List.of(UtilsTestFixtures.STATION_GROUP),
      systemMessagesSink
    );

    var expectedMessagesForType = expectedMessages.get(STATION_NEEDS_ATTENTION);

    var actualMessagesForType = outputSystemMessages.stream()
      .filter(systemMessage -> systemMessage.getType() == STATION_NEEDS_ATTENTION)
      .collect(Collectors.toList());

    Assertions.assertEquals(
      expectedMessagesForType.size(),
      actualMessagesForType.size()
    );

    assertEquals(expectedMessagesForType.size(), actualMessagesForType.size());
    actualMessagesForType.forEach(actualMessage ->
      assertThat(expectedMessagesForType.stream().filter(message ->
        message.getMessage().equals(actualMessage.getMessage()) && message.getMessageTags().equals(actualMessage.getMessageTags())).findFirst()).isPresent()
    );

    outputSystemMessages.clear();

    UiStationGenerator.buildUiStationSohList(
      Set.of(SIMPLE_MARGINAL_STATION_SOH),
      uiStationAndStationGroupsChanges.getUnacknowledgedStatusChanges(),
      uiStationAndStationGroupsChanges.getQuietedSohStatusChanges(),
      Set.of(),
      STATION_SOH_PARAMETERS,
      List.of(UtilsTestFixtures.STATION_GROUP),
      systemMessagesSink
    );

    //
    // Wait some time for threads to finish
    //
    var startMs = System.currentTimeMillis();
    while (System.currentTimeMillis() - startMs < 500) {
      Thread.yield();
    }

    Assertions.assertTrue(outputSystemMessages.stream()
      .noneMatch(systemMessage -> systemMessage.getType() == STATION_NEEDS_ATTENTION));

    // Just so that the subscriber isnt waiting around for more SystemMessages that will
    // never come.
    systemMessagesSink.tryEmitComplete();
    disposable.dispose();

  }

  private static Stream<Arguments> needsAttentionSystemMessagesTestSource() {

    return Stream.of(
      //
      // Single new unacknowledged change should produce a message
      //
      Arguments.arguments(
        Set.of(SIMPLE_MARGINAL_STATION_SOH),
        UIStationAndStationGroupsChanges.builder()
          .setUnacknowledgedStatusChanges(List.of(UNACK_CHANGE_1))
          .setQuietedSohStatusChanges(List.of())
          .build(),
        Map.of(
          STATION_NEEDS_ATTENTION,
          List.of(
            new StationNeedsAttentionBuilder(UNACK_CHANGE_1.getStation())
              .build())
        )
      ),

      //
      // Two new unacknowledged changes should produce two new messages
      //
      Arguments.arguments(
        Set.of(SIMPLE_MARGINAL_STATION_SOH, ALT_SIMPLE_MARGINAL_STATION_SOH),
        UIStationAndStationGroupsChanges.builder()
          .setUnacknowledgedStatusChanges(List.of(UNACK_CHANGE_1, ALT_UNACK_CHANGE_1))
          .setQuietedSohStatusChanges(List.of())
          .build(),
        Map.of(
          STATION_NEEDS_ATTENTION,
          List.of(
            new StationNeedsAttentionBuilder(UNACK_CHANGE_1.getStation())
              .build(),
            new StationNeedsAttentionBuilder(ALT_UNACK_CHANGE_1.getStation())
              .build()
          )
        )
      ),

      //
      // Two new unacknowledged changes, where one is quited, should produce one new message
      //
      Arguments.arguments(
        Set.of(SIMPLE_MARGINAL_STATION_SOH, ALT_SIMPLE_MARGINAL_STATION_SOH),
        UIStationAndStationGroupsChanges.builder()
          .setUnacknowledgedStatusChanges(List.of(ENV_GAP_UNACK_CHANGE_1, ALT_UNACK_CHANGE_1))
          .setQuietedSohStatusChanges(List.of(QUIETED_CHANGE_1))
          .build(),
        Map.of(
          STATION_NEEDS_ATTENTION,
          List.of(
            new StationNeedsAttentionBuilder(ALT_UNACK_CHANGE_1.getStation())
              .build()
          )
        )
      )
    );
  }
}
