package gms.shared.frameworks.osd.repository.rawstationdataframe;

import com.google.common.base.Functions;
import gms.shared.frameworks.osd.api.util.StationTimeRangeRequest;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrameMetadata;
import gms.shared.frameworks.osd.repository.SohPostgresTest;
import gms.shared.frameworks.osd.repository.util.TestFixtures;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@Testcontainers
class RawStationDataFrameRepositoryQueryViewJpaTests extends SohPostgresTest {

  private static RawStationDataFrameRepositoryQueryViewJpa rsdfQueryView;

  @BeforeAll
  static void testSuiteSetUp() {
    rsdfQueryView = new RawStationDataFrameRepositoryQueryViewJpa(entityManagerFactory);
    new RawStationDataFrameRepositoryJpa(entityManagerFactory).storeRawStationDataFrames(List.of(TestFixtures.frame1,
      TestFixtures.frame2));
  }

  @Test
  void testRetrieveMetadataByStationAndTimeValidation() {
    assertThrows(NullPointerException.class,
      () -> rsdfQueryView.retrieveRawStationDataFrameMetadataByStationAndTime(null));
  }

  @ParameterizedTest
  @MethodSource("getRetrieveMetadataByStationAndTimeArguments")
  void testRetrieveMetadataByStationAndTime(
    StationTimeRangeRequest request,
    List<RawStationDataFrameMetadata> expected) {

    List<RawStationDataFrameMetadata> actual = rsdfQueryView
      .retrieveRawStationDataFrameMetadataByStationAndTime(request);
    assertEquals(expected.size(), actual.size());

    Map<String, RawStationDataFrameMetadata> actualByStation = actual.stream()
      .collect(Collectors.toMap(RawStationDataFrameMetadata::getStationName, Functions.identity()));
    expected.stream().forEach(expectedMetadata -> {
      assertTrue(actualByStation.containsKey(expectedMetadata.getStationName()));
      RawStationDataFrameMetadata actualMetadata = actualByStation.get(expectedMetadata.getStationName());
      assertTrue(EqualsBuilder.reflectionEquals(expectedMetadata, actualMetadata, "channelNames"));
      assertEquals(expectedMetadata.getChannelNames().size(), actualMetadata.getChannelNames().size());
      assertTrue(expectedMetadata.getChannelNames().containsAll(actualMetadata.getChannelNames()));
    });
  }

  static Stream<Arguments> getRetrieveMetadataByStationAndTimeArguments() {
    return Stream.of(
      arguments(
        StationTimeRangeRequest.create(
          TestFixtures.frame1.getMetadata().getStationName(),
          TestFixtures.SEGMENT1_END, TestFixtures.SEGMENT_END2
        ),
        List.of(TestFixtures.frame1.getMetadata())
      ),
      arguments(
        StationTimeRangeRequest.create(
          TestFixtures.frame2.getMetadata().getStationName(),
          TestFixtures.SEGMENT1_END, TestFixtures.SEGMENT_END2
        ),
        List.of(TestFixtures.frame2.getMetadata())
      ),
      arguments(
        StationTimeRangeRequest.create(
          "NON-EXISTENT-STATION!-!--!---!",
          TestFixtures.SEGMENT1_END, TestFixtures.SEGMENT_END2
        ),
        List.of()
      ),
      arguments(
        StationTimeRangeRequest.create(
          TestFixtures.frame2.getMetadata().getStationName(),
          Instant.now(), Instant.now().plusSeconds(2)
        ),
        List.of()
      )
    );
  }

  @ParameterizedTest
  @MethodSource("getRetrieveLatestSampleTimeArguments")
  void testRetrieveLatestSampleTimeValidation(Class<? extends Exception> expectedException, List<String> channelNames) {
    assertThrows(expectedException, () -> rsdfQueryView.retrieveLatestSampleTimeByChannel(channelNames));
  }

  static Stream<Arguments> getRetrieveLatestSampleTimeArguments() {
    return Stream.of(arguments(NullPointerException.class, null),
      arguments(IllegalStateException.class, List.of()));
  }

  @Test
  void testRetrieveLatestSampleTime() {
    Map<String, Instant> latestSampleTimes = rsdfQueryView.retrieveLatestSampleTimeByChannel(List.of(TestFixtures.channel1.getName()));
    assertEquals(1, latestSampleTimes.size());
    assertTrue(latestSampleTimes.containsKey(TestFixtures.channel1.getName()));
    Assertions.assertEquals(
      TestFixtures.waveformSummaries.get(TestFixtures.channel1.getName()).getEndTime(), latestSampleTimes.get(TestFixtures.channel1.getName()));
  }

  @Test
  void testRetrieverLatestSampleTimeDuplicates() {
    new RawStationDataFrameRepositoryJpa(entityManagerFactory).storeRawStationDataFrames(List.of(
      TestFixtures.frame1.toBuilder().setId(UUID.randomUUID()).build()));
    Map<String, Instant> latestSampleTimes = rsdfQueryView.retrieveLatestSampleTimeByChannel(List.of(TestFixtures.channel1.getName()));
    assertEquals(1, latestSampleTimes.size());
    assertTrue(latestSampleTimes.containsKey(TestFixtures.channel1.getName()));
    Assertions.assertEquals(
      TestFixtures.waveformSummaries.get(TestFixtures.channel1.getName()).getEndTime(), latestSampleTimes.get(TestFixtures.channel1.getName()));
  }

}