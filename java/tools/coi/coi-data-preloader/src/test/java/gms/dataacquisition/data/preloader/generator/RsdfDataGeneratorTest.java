package gms.dataacquisition.data.preloader.generator;

import gms.dataacquisition.data.preloader.GenerationSpec;
import gms.dataacquisition.data.preloader.GenerationType;
import gms.shared.frameworks.injector.RsdfIdModifier;
import gms.shared.frameworks.osd.api.OsdRepositoryInterface;
import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame.AuthenticationStatus;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFramePayloadFormat;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RsdfDataGeneratorTest extends
  CoiDataGeneratorTest<RsdfDataGenerator, RawStationDataFrame, RsdfIdModifier> {

  @Captor
  protected ArgumentCaptor<Collection<RawStationDataFrame>> rsdfsCaptor;

  @Override
  protected RsdfDataGenerator getDataGenerator(GenerationSpec generationSpec,
    OsdRepositoryInterface sohRepository) {
    return new RsdfDataGenerator(generationSpec, sohRepository);
  }

  @Override
  protected GenerationType getGeneratorDataType() {
    return GenerationType.RAW_STATION_DATA_FRAME;
  }

  @Override
  protected List<String> getSeedNames() {
    return stationGroups.stream()
      .flatMap(g -> g.getStations().stream())
      .map(Station::getName)
      .distinct()
      .collect(Collectors.toList());
  }

  @Test
  @Override
  void testGenerateSeed() {
    final var stationName = stationGroups.stream().flatMap(g -> g.getStations().stream())
      .findFirst().orElseThrow().getName();

    final var result = dataGenerator.generateSeed(stationName);

    assertNotNull(result);
    assertNotNull(result.getId());
    assertNotNull(result.getRawPayload());
    assertNotNull(result.getMetadata());
    assertEquals(stationName, result.getMetadata().getStationName());
    assertNotNull(result.getMetadata().getChannelNames());
    final var channelNames = stationGroups.stream()
      .flatMap(g -> g.getStations().stream())
      .filter(s -> stationName.equals(s.getName()))
      .flatMap(s -> s.getChannels().stream())
      .map(Channel::getName)
      .collect(Collectors.toSet());
    assertEquals(channelNames, result.getMetadata().getChannelNames());
    assertEquals(RawStationDataFramePayloadFormat.CD11, result.getMetadata().getPayloadFormat());
    assertEquals(seedTime, result.getMetadata().getPayloadStartTime());
    assertEquals(seedTime.plus(generationFrequency),
      result.getMetadata().getPayloadEndTime());
    assertEquals(receptionTime, result.getMetadata().getReceptionTime());
    assertEquals(AuthenticationStatus.AUTHENTICATION_SUCCEEDED,
      result.getMetadata().getAuthenticationStatus());
    assertNotNull(result.getMetadata().getWaveformSummaries());
    assertTrue(result.getMetadata().getWaveformSummaries().entrySet().stream()
      .allMatch(kv -> channelNames.contains(kv.getKey()) && kv.getValue() != null));
    assertEquals(channelNames.size(), result.getMetadata().getWaveformSummaries().size());
  }

  @Override
  protected List<RawStationDataFrame> getRecordsToSend() {
    return new ArrayList<>();
  }

  @Override
  protected void verifyTimes(Collection<RawStationDataFrame> capturedRecords) {
    final var timeOfRun = generationSpec.getStartTime();

    final var lowestStartTime = capturedRecords.stream()
      .map(rawStationDataFrame -> rawStationDataFrame.getMetadata().getPayloadStartTime())
      .min(Instant::compareTo)
      .orElse(Instant.MAX);
    assertEquals(timeOfRun, lowestStartTime);

    final var highestStartTime = capturedRecords.stream()
      .map(rawStationDataFrame -> rawStationDataFrame.getMetadata().getPayloadStartTime())
      .max(Instant::compareTo)
      .orElse(Instant.MAX);
    assertTrue(highestStartTime.isAfter(timeOfRun));
    assertTrue(highestStartTime.isAfter(lowestStartTime));

    final var lowestEndTime = capturedRecords.stream()
      .map(rawStationDataFrame -> rawStationDataFrame.getMetadata().getPayloadEndTime())
      .min(Instant::compareTo)
      .orElse(Instant.MAX);
    assertTrue(lowestEndTime.isAfter(timeOfRun));
    assertTrue(lowestEndTime.isAfter(lowestStartTime));

    final var highestEndTime = capturedRecords.stream()
      .map(rawStationDataFrame -> rawStationDataFrame.getMetadata().getPayloadEndTime())
      .max(Instant::compareTo)
      .orElse(Instant.MAX);
    assertTrue(highestEndTime.isAfter(timeOfRun));
    assertTrue(highestEndTime.isAfter(lowestStartTime));
    assertTrue(highestEndTime.isAfter(lowestEndTime));
  }

  @Override
  protected void mockIntermittentSohRepositoryFailure(int failsOnNthCall) {
    doAnswer(i -> throwErrorOnNthCall(failsOnNthCall)).when(sohRepository)
      .storeRawStationDataFrames(any());
  }

  @Override
  protected void verifyRepositoryInteraction(int wantedNumberOfInvocations) {
    verify(sohRepository, times(wantedNumberOfInvocations))
      .storeRawStationDataFrames(rsdfsCaptor.capture());

    final var ids = rsdfsCaptor.getAllValues().stream()
      .flatMap(Collection::stream)
      .map(RawStationDataFrame::getId)
      .map(UUID::toString)
      .collect(Collectors.toList());

    validateIds(ids);
  }

  @Override
  protected int getWantedNumberOfItemsGenerated() {
    final var numberOfChannels = stationGroups.stream()
      .flatMap(stationGroup -> stationGroup.getStations().stream())
      .flatMap(station -> station.getChannels().stream())
      .map(Channel::getName)
      .distinct()
      .count();

    return (int) Math.ceil(
      ((double) generationDuration.toNanos() / generationFrequency.toNanos()) * numberOfChannels);
  }
}