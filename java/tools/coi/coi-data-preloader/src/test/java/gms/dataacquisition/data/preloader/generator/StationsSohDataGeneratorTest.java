package gms.dataacquisition.data.preloader.generator;

import gms.dataacquisition.data.preloader.GenerationSpec;
import gms.dataacquisition.data.preloader.GenerationType;
import gms.shared.frameworks.injector.StationSohIdModifier;
import gms.shared.frameworks.osd.api.OsdRepositoryInterface;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
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

class StationsSohDataGeneratorTest extends
  CoiDataGeneratorTest<StationSohDataGenerator, StationSoh, StationSohIdModifier> {

  @Captor
  protected ArgumentCaptor<Collection<StationSoh>> stationSohCaptor;

  @Override
  protected StationSohDataGenerator getDataGenerator(GenerationSpec generationSpec,
    OsdRepositoryInterface sohRepository) {
    return new StationSohDataGenerator(generationSpec, sohRepository);
  }

  @Override
  protected GenerationType getGeneratorDataType() {
    return GenerationType.STATION_SOH;
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
    assertEquals(stationName, result.getStationName());
    assertEquals(seedTime, result.getTime());
    assertNotNullOrEmpty(result.getChannelSohs());
    assertNotNull(result.getSohStatusRollup());
    assertNotNullOrEmpty(result.getAllStationAggregates());
    assertNotNullOrEmpty(result.getSohMonitorValueAndStatuses());
  }

  @Override
  protected List<StationSoh> getRecordsToSend() {
    return new ArrayList<>();
  }

  @Override
  protected void verifyTimes(Collection<StationSoh> capturedRecords) {
    final var timeOfRun = generationSpec.getStartTime();

    final var lowestStartTime = capturedRecords.stream()
      .map(StationSoh::getTime).min(Instant::compareTo)
      .orElse(Instant.MAX);
    assertEquals(timeOfRun, lowestStartTime);

    final var highestStartTime = capturedRecords.stream()
      .map(StationSoh::getTime).max(Instant::compareTo)
      .orElse(Instant.MAX);
    assertTrue(highestStartTime.isAfter(timeOfRun));
    assertTrue(highestStartTime.isAfter(lowestStartTime));
  }

  @Override
  protected void mockIntermittentSohRepositoryFailure(int failsOnNthCall) {
    doAnswer(i -> throwErrorOnNthCall(failsOnNthCall)).when(sohRepository).storeStationSoh(any());
  }

  @Override
  protected void verifyRepositoryInteraction(int wantedNumberOfInvocations) {
    verify(sohRepository, times(wantedNumberOfInvocations)).storeStationSoh(stationSohCaptor.capture());

    final var ids = stationSohCaptor.getAllValues().stream()
      .flatMap(Collection::stream)
      .map(StationSoh::getId)
      .map(UUID::toString)
      .collect(Collectors.toList());

    validateIds(ids);
  }

  @Override
  protected int getWantedNumberOfItemsGenerated() {
    return (int) Math.ceil(
      ((double) generationDuration.toNanos() / generationFrequency.toNanos()) * getSeedNames()
        .size());
  }
}