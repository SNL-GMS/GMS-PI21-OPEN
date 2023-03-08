package gms.shared.frameworks.osd.repository.performancemonitoring.transform;

import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueAnalog;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import gms.shared.frameworks.osd.dto.soh.DoubleOrInteger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AcquiredChannelEnvironmentalIssuesTransformerTests {

  private static final int NUMBER_OF_CHANNELS = 1000;
  private static final List<AcquiredChannelEnvironmentIssueAnalog> analogList = new ArrayList<>();
  private static final List<AcquiredChannelEnvironmentIssueBoolean> booleanList = new ArrayList<>();

  @BeforeAll
  static void setUp() {
    String channelName;

    for (int i = 0; i < NUMBER_OF_CHANNELS; i++) {
      channelName = "channel" + i;
      analogList.add(AcquiredChannelEnvironmentIssueAnalog
        .from(channelName, UtilsTestFixtures.channelSohAnalog.getType(),
          UtilsTestFixtures.channelSohAnalog.getStartTime(),
          UtilsTestFixtures.channelSohAnalog.getEndTime(),
          UtilsTestFixtures.channelSohAnalog.getStatus()));
      booleanList.add(AcquiredChannelEnvironmentIssueBoolean
        .from(channelName, UtilsTestFixtures.channelSohBoolean.getType(),
          UtilsTestFixtures.channelSohBoolean.getStartTime(),
          UtilsTestFixtures.channelSohBoolean.getEndTime(),
          UtilsTestFixtures.channelSohBoolean.getStatus()));
    }

  }

  @Test
  void testTransform() {
    final var transformedAnalogList = AcquiredChannelEnvironmentalIssuesTransformer
      .toHistoricalAcquiredChannelEnvironmentalIssues(analogList);

    final var transformedBooleanList = AcquiredChannelEnvironmentalIssuesTransformer
      .toHistoricalAcquiredChannelEnvironmentalIssues(booleanList);

    assertEquals(NUMBER_OF_CHANNELS, transformedAnalogList.size());
    assertEquals(NUMBER_OF_CHANNELS, transformedBooleanList.size());

    transformedAnalogList.forEach(t -> {
      final var channelSohAnalogTypeAsString = UtilsTestFixtures.channelSohAnalog.getType().name();
      assertEquals(channelSohAnalogTypeAsString, t.getMonitorType());
    });
    transformedBooleanList.forEach(t -> {
      final var channelSohBooleanTypeAsString = UtilsTestFixtures.channelSohBoolean.getType()
        .name();
      assertEquals(channelSohBooleanTypeAsString, t.getMonitorType());
    });

    transformedAnalogList
      .forEach(t -> assertTrue(t.getTrendLine().get(0).getDataPoints().size() >= 2));
    transformedBooleanList
      .forEach(t -> assertTrue(t.getTrendLine().get(0).getDataPoints().size() >= 2));
  }

  @Test
  void testThatStatusesWereCreatedCorrectly() {
    final var transformedAnalogList = AcquiredChannelEnvironmentalIssuesTransformer
      .toHistoricalAcquiredChannelEnvironmentalIssues(analogList);

    final var transformedBooleanList = AcquiredChannelEnvironmentalIssuesTransformer
      .toHistoricalAcquiredChannelEnvironmentalIssues(booleanList);

    final var booleanDataPointsFromFirstLineSegment = transformedBooleanList.get(0).getTrendLine()
      .get(0)
      .getDataPoints();

    // sample the DataPoint statuses
    transformedAnalogList
      .forEach(t -> {
        final var expectedStatus = DoubleOrInteger.ofDouble(1.5);
        assertEquals(expectedStatus, t.getTrendLine().get(0).getDataPoints().get(0).getStatus());
      });
    transformedBooleanList
      .forEach(t -> {
        final var expectedStatus = DoubleOrInteger.ofInteger(1);
        assertEquals(expectedStatus, t.getTrendLine().get(0).getDataPoints().get(0).getStatus());
      });

    // verify that the terminal status equals the preceding status,
    // this lets the consumer know they have reached the end of a line segment/gap in the data
    assertEquals(
      booleanDataPointsFromFirstLineSegment.get(booleanDataPointsFromFirstLineSegment.size() > 2 ?
          booleanDataPointsFromFirstLineSegment.size() - 2 : 0)
        .getStatus(),
      booleanDataPointsFromFirstLineSegment.get(booleanDataPointsFromFirstLineSegment.size() - 1)
        .getStatus());
  }

  @Test
  void testThatTimestampsWereCreatedCorrectly() {
    final var transformedAnalogList = AcquiredChannelEnvironmentalIssuesTransformer
      .toHistoricalAcquiredChannelEnvironmentalIssues(analogList);

    final var transformedBooleanList = AcquiredChannelEnvironmentalIssuesTransformer
      .toHistoricalAcquiredChannelEnvironmentalIssues(booleanList);

    final var analogDataPointsFromFirstLineSegment = transformedAnalogList.get(0).getTrendLine()
      .get(0).getDataPoints();

    // sample the DataPoint timestamps
    transformedAnalogList
      .forEach(t -> {
        final var expectedTimestamp = UtilsTestFixtures.channelSohAnalog.getStartTime()
          .toEpochMilli();
        assertEquals(expectedTimestamp,
          t.getTrendLine().get(0).getDataPoints().get(0).getTimeStamp());
      });
    transformedBooleanList
      .forEach(t -> {
        final var expectedTimestamp = UtilsTestFixtures.channelSohBoolean.getStartTime()
          .toEpochMilli();
        assertEquals(expectedTimestamp,
          t.getTrendLine().get(0).getDataPoints().get(0).getTimeStamp());
      });

    // make sure the second to last data point before a gap has the ACEI start time as its timestamp
    assertEquals(UtilsTestFixtures.channelSohAnalog.getStartTime().toEpochMilli(),
      analogDataPointsFromFirstLineSegment.get(analogDataPointsFromFirstLineSegment.size() > 2 ?
          analogDataPointsFromFirstLineSegment.size() - 2 : 0)
        .getTimeStamp());

    // make sure the last data point before a gap has the ACEI end time as its timestamp
    assertEquals(UtilsTestFixtures.channelSohAnalog.getEndTime().toEpochMilli(),
      analogDataPointsFromFirstLineSegment.get(analogDataPointsFromFirstLineSegment.size() - 1)
        .getTimeStamp());
  }

}
