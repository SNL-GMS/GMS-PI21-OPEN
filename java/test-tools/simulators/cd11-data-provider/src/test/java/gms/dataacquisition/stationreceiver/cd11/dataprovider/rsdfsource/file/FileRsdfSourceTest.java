package gms.dataacquisition.stationreceiver.cd11.dataprovider.rsdfsource.file;

import gms.dataacquisition.stationreceiver.cd11.dataprovider.configuration.FileRsdfSourceConfig;
import gms.dataacquisition.stationreceiver.cd11.dataprovider.rsdfsource.ProviderUtils;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrameMetadata;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFramePayloadFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class FileRsdfSourceTest {

  private static final Logger logger = LoggerFactory.getLogger(FileRsdfSourceTest.class);

  private static final Instant DEFAULT_REF_TIME = Instant.EPOCH;
  private static final String DATA_SET_1_FOR_1 = "./src/test/resources/dataprovider/rsdfsource/file/1for1min";
  private static final String DATA_SET_1_FOR_1_RECEPTION_LAG = "./src/test/resources/dataprovider/rsdfsource/file/1for1min_rec_lag";
  private static final String DATA_SET_1_FOR_2 = "./src/test/resources/dataprovider/rsdfsource/file/1for2min";
  private static final String DATA_SET_1_FOR_3 = "./src/test/resources/dataprovider/rsdfsource/file/1for3min";
  private static final String DATA_SET_2_FOR_1 = "./src/test/resources/dataprovider/rsdfsource/file/2for1min";
  private static final String DATA_SET_3_FOR_1 = "./src/test/resources/dataprovider/rsdfsource/file/3for1min";
  private static final String DATA_SET_3_FOR_3 = "./src/test/resources/dataprovider/rsdfsource/file/3for3min";
  private static final String DATA_SET_3_FOR_3_DELAYED = "./src/test/resources/dataprovider/rsdfsource/file/3for3min_delayed";
  private static final String DATA_SET_3_FOR_3_DELAYED_TRUNCATED = "./src/test/resources/dataprovider/rsdfsource/file/3for3min_delayed_trunc";
  private static final String DATA_SET_3_FOR_3_TRUNCATED = "./src/test/resources/dataprovider/rsdfsource/file/3for3min_trunc";
  private static final String DATA_SET_3_FOR_3_RELAY = "./src/test/resources/dataprovider/rsdfsource/file/3for3min_relay";
  private static final String DATA_SET_3_FOR_3_RELAY_OFFSET = "./src/test/resources/dataprovider/rsdfsource/file/3for3min_relay_offset";
  private static final String DATA_SET_3_FOR_3_SPARSE = "./src/test/resources/dataprovider/rsdfsource/file/3for3min_sparse";
  private static final Duration oneMinute = Duration.ofMinutes(1);
  private static final Duration twoMinutes = Duration.ofMinutes(2);
  private static final Duration threeMinutes = Duration.ofMinutes(3);

  private static Stream<Arguments> getSeedSetConstraints() {

    return Stream.of(
      arguments(DATA_SET_1_FOR_1, new StationConfiguration[]{
          new StationConfiguration("BOSA", 6)},
        1, oneMinute, oneMinute, DEFAULT_REF_TIME, true),
      arguments(DATA_SET_1_FOR_1, new StationConfiguration[]{
          new StationConfiguration("BOSA", 6)},
        2, oneMinute, oneMinute, DEFAULT_REF_TIME, true),
      arguments(DATA_SET_1_FOR_1, new StationConfiguration[]{
          new StationConfiguration("BOSA", 6)},
        100, oneMinute, oneMinute, DEFAULT_REF_TIME, true),

      arguments(DATA_SET_1_FOR_1_RECEPTION_LAG, new StationConfiguration[]{
          new StationConfiguration("BOSA", 6)},
        1, oneMinute, oneMinute.plusSeconds(10), DEFAULT_REF_TIME.plusSeconds(10), true),
      arguments(DATA_SET_1_FOR_1_RECEPTION_LAG, new StationConfiguration[]{
          new StationConfiguration("BOSA", 6)},
        2, oneMinute, oneMinute.plusSeconds(10), DEFAULT_REF_TIME.plusSeconds(10), false),
      arguments(DATA_SET_1_FOR_1_RECEPTION_LAG, new StationConfiguration[]{
          new StationConfiguration("BOSA", 6)},
        100, oneMinute, oneMinute.plusSeconds(10), DEFAULT_REF_TIME.plusSeconds(10), false),

      arguments(DATA_SET_1_FOR_2, new StationConfiguration[]{
          new StationConfiguration("BOSA", 12)},
        1, twoMinutes, twoMinutes, DEFAULT_REF_TIME, true),
      arguments(DATA_SET_1_FOR_2, new StationConfiguration[]{
          new StationConfiguration("BOSA", 12)},
        2, twoMinutes, twoMinutes, DEFAULT_REF_TIME, true),
      arguments(DATA_SET_1_FOR_2, new StationConfiguration[]{
          new StationConfiguration("BOSA", 12)},
        100, twoMinutes, twoMinutes, DEFAULT_REF_TIME, true),

      arguments(DATA_SET_1_FOR_3, new StationConfiguration[]{
          new StationConfiguration("BOSA", 18)},
        1, threeMinutes, threeMinutes, DEFAULT_REF_TIME, true),
      arguments(DATA_SET_1_FOR_3, new StationConfiguration[]{
          new StationConfiguration("BOSA", 18)},
        2, threeMinutes, threeMinutes, DEFAULT_REF_TIME, true),
      arguments(DATA_SET_1_FOR_3, new StationConfiguration[]{
          new StationConfiguration("BOSA", 18)},
        100, threeMinutes, threeMinutes, DEFAULT_REF_TIME, true),

      arguments(DATA_SET_2_FOR_1, new StationConfiguration[]{
          new StationConfiguration("BOSA", 6),
          new StationConfiguration("PDAR", 6)},
        1, oneMinute.plusSeconds(5), oneMinute.plusSeconds(5), DEFAULT_REF_TIME, false),
      arguments(DATA_SET_2_FOR_1, new StationConfiguration[]{
          new StationConfiguration("BOSA", 6),
          new StationConfiguration("PDAR", 6)},
        2, oneMinute.plusSeconds(5), oneMinute.plusSeconds(5), DEFAULT_REF_TIME, false),
      arguments(DATA_SET_2_FOR_1, new StationConfiguration[]{
          new StationConfiguration("BOSA", 6),
          new StationConfiguration("PDAR", 6)},
        100, oneMinute.plusSeconds(5), oneMinute.plusSeconds(5), DEFAULT_REF_TIME, false),

      arguments(DATA_SET_3_FOR_1, new StationConfiguration[]{
          new StationConfiguration("BOSA", 6),
          new StationConfiguration("KMBO", 6),
          new StationConfiguration("PDAR", 6)},
        1, oneMinute.plusSeconds(6), oneMinute.plusSeconds(6), DEFAULT_REF_TIME, true),
      arguments(DATA_SET_3_FOR_1, new StationConfiguration[]{
          new StationConfiguration("BOSA", 6),
          new StationConfiguration("KMBO", 6),
          new StationConfiguration("PDAR", 6)},
        2, oneMinute.plusSeconds(6), oneMinute.plusSeconds(6), DEFAULT_REF_TIME, false),
      arguments(DATA_SET_3_FOR_1, new StationConfiguration[]{
          new StationConfiguration("BOSA", 6),
          new StationConfiguration("KMBO", 6),
          new StationConfiguration("PDAR", 6)},
        100, oneMinute.plusSeconds(6), oneMinute.plusSeconds(6), DEFAULT_REF_TIME, false),

      arguments(DATA_SET_3_FOR_3, new StationConfiguration[]{
          new StationConfiguration("BOSA", 18),
          new StationConfiguration("KMBO", 18),
          new StationConfiguration("PDAR", 18)},
        1, threeMinutes.plusSeconds(5), threeMinutes.plusSeconds(5), DEFAULT_REF_TIME, true),
      arguments(DATA_SET_3_FOR_3, new StationConfiguration[]{
          new StationConfiguration("BOSA", 18),
          new StationConfiguration("KMBO", 18),
          new StationConfiguration("PDAR", 18)},
        2, threeMinutes.plusSeconds(5), threeMinutes.plusSeconds(5), DEFAULT_REF_TIME, false),
      arguments(DATA_SET_3_FOR_3, new StationConfiguration[]{
          new StationConfiguration("BOSA", 18),
          new StationConfiguration("KMBO", 18),
          new StationConfiguration("PDAR", 18)},
        100, threeMinutes.plusSeconds(5), threeMinutes.plusSeconds(5), DEFAULT_REF_TIME, false),

      arguments(DATA_SET_3_FOR_3_DELAYED, new StationConfiguration[]{
          new StationConfiguration("BOSA", 18),
          new StationConfiguration("KMBO", 18),
          new StationConfiguration("PDAR", 12)},
        1, threeMinutes.plusSeconds(5), threeMinutes.plusSeconds(5), DEFAULT_REF_TIME, false),
      arguments(DATA_SET_3_FOR_3_DELAYED, new StationConfiguration[]{
          new StationConfiguration("BOSA", 18),
          new StationConfiguration("KMBO", 18),
          new StationConfiguration("PDAR", 12)},
        2, threeMinutes.plusSeconds(5), threeMinutes.plusSeconds(5), DEFAULT_REF_TIME, false),
      arguments(DATA_SET_3_FOR_3_DELAYED, new StationConfiguration[]{
          new StationConfiguration("BOSA", 18),
          new StationConfiguration("KMBO", 18),
          new StationConfiguration("PDAR", 12)},
        100, threeMinutes.plusSeconds(5), threeMinutes.plusSeconds(5), DEFAULT_REF_TIME, false),

      arguments(DATA_SET_3_FOR_3_DELAYED_TRUNCATED, new StationConfiguration[]{
          new StationConfiguration("BOSA", 18),
          new StationConfiguration("KMBO", 18),
          new StationConfiguration("PDAR", 6)},
        1, threeMinutes.plusSeconds(5), threeMinutes.plusSeconds(5), DEFAULT_REF_TIME, false),
      arguments(DATA_SET_3_FOR_3_DELAYED_TRUNCATED, new StationConfiguration[]{
          new StationConfiguration("BOSA", 18),
          new StationConfiguration("KMBO", 18),
          new StationConfiguration("PDAR", 6)},
        2, threeMinutes.plusSeconds(5), threeMinutes.plusSeconds(5), DEFAULT_REF_TIME, false),
      arguments(DATA_SET_3_FOR_3_DELAYED_TRUNCATED, new StationConfiguration[]{
          new StationConfiguration("BOSA", 18),
          new StationConfiguration("KMBO", 18),
          new StationConfiguration("PDAR", 6)},
        100, threeMinutes.plusSeconds(5), threeMinutes.plusSeconds(5), DEFAULT_REF_TIME, false),

      arguments(DATA_SET_3_FOR_3_TRUNCATED, new StationConfiguration[]{
          new StationConfiguration("BOSA", 18),
          new StationConfiguration("KMBO", 18),
          new StationConfiguration("PDAR", 12)},
        1, threeMinutes.plusSeconds(5), threeMinutes.plusSeconds(5), DEFAULT_REF_TIME, false),
      arguments(DATA_SET_3_FOR_3_TRUNCATED, new StationConfiguration[]{
          new StationConfiguration("BOSA", 18),
          new StationConfiguration("KMBO", 18),
          new StationConfiguration("PDAR", 12)},
        2, threeMinutes.plusSeconds(5), threeMinutes.plusSeconds(5), DEFAULT_REF_TIME, false),
      arguments(DATA_SET_3_FOR_3_TRUNCATED, new StationConfiguration[]{
          new StationConfiguration("BOSA", 18),
          new StationConfiguration("KMBO", 18),
          new StationConfiguration("PDAR", 12)},
        100, threeMinutes.plusSeconds(5), threeMinutes.plusSeconds(5), DEFAULT_REF_TIME, false),

      arguments(DATA_SET_3_FOR_3_RELAY, new StationConfiguration[]{
          new StationConfiguration("BOSA", 6),
          new StationConfiguration("KMBO", 6),
          new StationConfiguration("PDAR", 6)},
        1, threeMinutes, threeMinutes, DEFAULT_REF_TIME, false),
      arguments(DATA_SET_3_FOR_3_RELAY, new StationConfiguration[]{
          new StationConfiguration("BOSA", 6),
          new StationConfiguration("KMBO", 6),
          new StationConfiguration("PDAR", 6)},
        2, threeMinutes, threeMinutes, DEFAULT_REF_TIME, false),
      arguments(DATA_SET_3_FOR_3_RELAY, new StationConfiguration[]{
          new StationConfiguration("BOSA", 6),
          new StationConfiguration("KMBO", 6),
          new StationConfiguration("PDAR", 6)},
        100, threeMinutes, threeMinutes, DEFAULT_REF_TIME, false),

      arguments(DATA_SET_3_FOR_3_RELAY_OFFSET, new StationConfiguration[]{
          new StationConfiguration("BOSA", 6),
          new StationConfiguration("KMBO", 6),
          new StationConfiguration("PDAR", 6)},
        1, threeMinutes.plusSeconds(5), threeMinutes.plusSeconds(5), DEFAULT_REF_TIME, false),
      arguments(DATA_SET_3_FOR_3_RELAY_OFFSET, new StationConfiguration[]{
          new StationConfiguration("BOSA", 6),
          new StationConfiguration("KMBO", 6),
          new StationConfiguration("PDAR", 6)},
        2, threeMinutes.plusSeconds(5), threeMinutes.plusSeconds(5), DEFAULT_REF_TIME, false),
      arguments(DATA_SET_3_FOR_3_RELAY_OFFSET, new StationConfiguration[]{
          new StationConfiguration("BOSA", 6),
          new StationConfiguration("KMBO", 6),
          new StationConfiguration("PDAR", 6)},
        100, threeMinutes.plusSeconds(5), threeMinutes.plusSeconds(5), DEFAULT_REF_TIME, false),

      arguments(DATA_SET_3_FOR_3_SPARSE, new StationConfiguration[]{
          new StationConfiguration("BOSA", 4),
          new StationConfiguration("KMBO", 3),
          new StationConfiguration("PDAR", 2)},
        1, threeMinutes.plusSeconds(5), threeMinutes.plusSeconds(5), DEFAULT_REF_TIME, false),
      arguments(DATA_SET_3_FOR_3_SPARSE, new StationConfiguration[]{
          new StationConfiguration("BOSA", 4),
          new StationConfiguration("KMBO", 3),
          new StationConfiguration("PDAR", 2)},
        2, threeMinutes.plusSeconds(5), threeMinutes.plusSeconds(5), DEFAULT_REF_TIME, false),
      arguments(DATA_SET_3_FOR_3_SPARSE, new StationConfiguration[]{
          new StationConfiguration("BOSA", 4),
          new StationConfiguration("KMBO", 3),
          new StationConfiguration("PDAR", 2)},
        100, threeMinutes.plusSeconds(5), threeMinutes.plusSeconds(5), DEFAULT_REF_TIME, false)
    );
  }

  @BeforeEach
  public void testSetup() {
  }

  @ParameterizedTest
  @MethodSource("getSeedSetConstraints")
  void testGetRsdfFlux(final String dataDirectoryLocation,
    final StationConfiguration[] stationConfigurations, final int numberOfRounds,
    final Duration expectedDataDuration, final Duration expectedRunDuration,
    Instant referenceTime, boolean expectContiguous) {
    final int expectedEmittedItemCount = numberOfRounds * Stream.of(stationConfigurations)
      .map(StationConfiguration::getRsdfCount)
      .reduce(Integer::sum).orElseThrow();
    FileRsdfSourceConfig config = FileRsdfSourceConfig.from(dataDirectoryLocation, referenceTime,
      Optional.empty());
    FileRsdfSource fileRsdfSource = new FileRsdfSource(config);
    final List<RawStationDataFrame> rsdfs = new ArrayList<>();

    final var virtualTimeScheduler = VirtualTimeScheduler.getOrSet();
    final var rsdfFlux = fileRsdfSource.getRsdfFlux()
      .take(expectedEmittedItemCount)
      .doOnNext(rsdfs::add);

    StepVerifier.withVirtualTime(() -> rsdfFlux)
      .thenAwait(expectedRunDuration.multipliedBy(numberOfRounds))
      .expectNextCount(expectedEmittedItemCount)
      .verifyComplete();

    assertNotNull(rsdfs);
    assertFalse(rsdfs.isEmpty());
    assertEquals(expectedEmittedItemCount, rsdfs.size());
    final var stationNames = rsdfs.stream()
      .map(r -> r.getMetadata().getStationName()).distinct()
      .collect(Collectors.toList());
    assertEquals(stationConfigurations.length, stationNames.size());

    final var stationConfigurationMap = Stream.of(stationConfigurations)
      .collect(Collectors.toMap(StationConfiguration::getStationName, c -> c));
    verifyStationData(rsdfs, stationConfigurationMap, numberOfRounds, expectContiguous);
    final Duration totalDataDuration = getTotalDataDuration(rsdfs);
    assertEquals(expectedDataDuration.multipliedBy(numberOfRounds).toMillis(),
      totalDataDuration.toMillis(), 100);
  }

  private Duration getTotalDataDuration(List<RawStationDataFrame> rsdfs) {
    final var startTimes = rsdfs.stream()
      .map(r -> r.getMetadata().getPayloadStartTime())
      .sorted(Instant::compareTo)
      .collect(Collectors.toList());
    final var endTimes = rsdfs.stream()
      .map(r -> r.getMetadata().getPayloadEndTime())
      .sorted(Instant::compareTo)
      .collect(Collectors.toList());
    return Duration.between(startTimes.get(0), endTimes.get(endTimes.size() - 1));
  }

  private void verifyStationData(List<RawStationDataFrame> rsdfs,
    Map<String, StationConfiguration> stationConfigurationMap, int numberOfRounds,
    boolean expectContiguous) {
    rsdfs.stream().collect(Collectors.groupingBy(r -> r.getMetadata().getStationName()))
      .forEach((stationName, rawStationDataFrames) -> {
        assertEquals(stationConfigurationMap.get(stationName).getRsdfCount() * numberOfRounds,
          rawStationDataFrames.size());
        verifyUniqueStationRsdfsIds(stationName, rawStationDataFrames);
        verifyUniqueStationRsdfStartTimes(stationName, rawStationDataFrames);
        verifyUniqueStationRsdfEndTimes(stationName, rawStationDataFrames);
        verifyUniqueStationRsdfSequenceNumbers(stationName, rawStationDataFrames);
        rawStationDataFrames.forEach(r -> {
          assertFalse(r.getMetadata().getWaveformSummaries().isEmpty());
          r.getMetadata().getWaveformSummaries().forEach((key, value) -> {
            assertEquals(value.getStartTime(), r.getMetadata().getPayloadStartTime());
            assertEquals(value.getEndTime(), r.getMetadata().getPayloadEndTime());
            assertFalse(value.getStartTime().isAfter(value.getEndTime()));
          });
        });

        if (expectContiguous) {
          verifyContiguousRsdfPayloadTimes(stationName, rawStationDataFrames);
        }
      });
  }

  private void verifyUniqueStationRsdfSequenceNumbers(String stationName,
    List<RawStationDataFrame> rawStationDataFrames) {
    final var rsdfsBySequencNumber = rawStationDataFrames.stream()
      .collect(Collectors.groupingBy(ProviderUtils::getSequenceNumber));
    assertEquals(rawStationDataFrames.size(), rsdfsBySequencNumber.size(),
      () -> reportDuplicateFound(stationName, "Sequence Numbers", rsdfsBySequencNumber,
        Object::toString));
  }

  private void verifyUniqueStationRsdfEndTimes(String stationName,
    List<RawStationDataFrame> rawStationDataFrames) {
    final var rsdfsByPayloadEndTime = rawStationDataFrames.stream()
      .collect(Collectors.groupingBy(r -> r.getMetadata().getPayloadEndTime()));
    assertEquals(rawStationDataFrames.size(), rsdfsByPayloadEndTime.size(),
      () -> reportDuplicateFound(stationName, "End Times", rsdfsByPayloadEndTime,
        Instant::toString));
  }

  private void verifyUniqueStationRsdfStartTimes(String stationName,
    List<RawStationDataFrame> rawStationDataFrames) {
    final var rsdfsByPayloadStartTime = rawStationDataFrames.stream()
      .collect(Collectors.groupingBy(r -> r.getMetadata().getPayloadStartTime()));
    assertEquals(rawStationDataFrames.size(), rsdfsByPayloadStartTime.size(),
      () -> reportDuplicateFound(stationName, "Start Times", rsdfsByPayloadStartTime,
        Instant::toString));
  }

  private void verifyUniqueStationRsdfsIds(String stationName,
    List<RawStationDataFrame> rawStationDataFrames) {
    final var rsdfsById = rawStationDataFrames.stream()
      .collect(Collectors.groupingBy(RawStationDataFrame::getId));
    assertEquals(rawStationDataFrames.size(), rsdfsById.size(),
      () -> reportDuplicateFound(stationName, "RSDF Ids", rsdfsById, UUID::toString));
  }

  private <T> String reportDuplicateFound(String stationName,
    final String itemType, Map<T, List<RawStationDataFrame>> items,
    Function<T, String> toString) {
    return String.format("Duplicate " + itemType + " Found For Station %s: %s", stationName,
      items.entrySet().stream().filter(e -> e.getValue().size() > 1)
        .map(Entry::getKey)
        .map(toString)
        .reduce((i1, i2) -> i1 + ", " + i2));
  }

  private void verifyContiguousRsdfPayloadTimes(String stationName,
    List<RawStationDataFrame> rawStationDataFrames) {
    AtomicReference<RawStationDataFrame> previousRsdf = new AtomicReference<>(null);
    rawStationDataFrames.forEach(r -> {
      if (previousRsdf.get() != null) {
        assertEquals(previousRsdf.get().getMetadata().getPayloadEndTime(),
          r.getMetadata().getPayloadStartTime(),
          String.format("Time Gap Detected Between RSDFs In Station: %s", stationName));
      }
      previousRsdf.set(r);
    });
  }

  @Test
  void testModifyRawPayload() {
    var fileRsdfSource = FileRsdfSource.create(Mockito.mock(FileRsdfSourceConfig.class));
    var malformedRawData = new byte[]{(byte) 0xC0, 0x00, 0x00, 0x00};
    final var rsdf = RawStationDataFrame.builder()
      .setId(UUID.randomUUID())
      .setMetadata(RawStationDataFrameMetadata.builder().setPayloadFormat(RawStationDataFramePayloadFormat.CD11)
        .setStationName("fake station name")
        .setPayloadEndTime(Instant.EPOCH)
        .setReceptionTime(Instant.EPOCH)
        .setPayloadStartTime(Instant.EPOCH)
        .setChannelNames(Collections.emptyList())
        .setAuthenticationStatus(RawStationDataFrame.AuthenticationStatus.NOT_APPLICABLE)
        .setWaveformSummaries(Map.of()).build()).build();

    Assertions.assertThrows(IllegalStateException.class, () -> fileRsdfSource.modifyRawPayload(rsdf, Instant.EPOCH));

    final var rsdfWtihMalFormedData = rsdf.toBuilder().setRawPayload(malformedRawData).build();
    Assertions.assertEquals(malformedRawData, fileRsdfSource.modifyRawPayload(rsdfWtihMalFormedData, Instant.EPOCH));
  }
}