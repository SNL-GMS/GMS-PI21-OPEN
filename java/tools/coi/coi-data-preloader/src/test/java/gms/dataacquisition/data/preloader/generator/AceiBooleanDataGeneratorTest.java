package gms.dataacquisition.data.preloader.generator;

import gms.dataacquisition.data.preloader.GenerationSpec;
import gms.dataacquisition.data.preloader.GenerationType;
import gms.shared.frameworks.osd.api.OsdRepositoryInterface;
import gms.shared.frameworks.osd.api.rawstationdataframe.AceiUpdates;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AceiBooleanDataGeneratorTest extends
  AceiDataGeneratorTest<AceiBooleanDataGenerator, AcquiredChannelEnvironmentIssueBoolean> {

  @Override
  protected AceiBooleanDataGenerator getDataGenerator(GenerationSpec generationSpec,
    OsdRepositoryInterface sohRepository) {
    return new AceiBooleanDataGenerator(generationSpec, sohRepository);
  }

  @Override
  protected GenerationType getGeneratorDataType() {
    return GenerationType.ACQUIRED_CHANNEL_ENV_ISSUE_BOOLEAN;
  }

  @Test
  @Override
  void testGenerateSeed() {
    final var stationName = stationGroups.stream().flatMap(g -> g.getStations().stream())
      .findFirst().orElseThrow().getName();

    final var result = dataGenerator.generateSeed(stationName);

    assertNotNull(result);
    assertEquals(stationName, result.getChannelName());
    assertEquals(AcquiredChannelEnvironmentIssueType.VAULT_DOOR_OPENED, result.getType());
    assertEquals(seedTime, result.getStartTime());
    assertEquals(seedTime.plus(generationFrequency), result.getEndTime());
    assertEquals(false, result.getStatus());
  }

  @Override
  protected void verifyTimes(Collection<AcquiredChannelEnvironmentIssueBoolean> capturedRecords) {
    final var timeOfRun = generationSpec.getStartTime();

    final var lowestStartTime = capturedRecords.stream()
      .map(AcquiredChannelEnvironmentIssue::getStartTime).min(Instant::compareTo)
      .orElse(Instant.MAX);
    assertEquals(timeOfRun, lowestStartTime);

    final var highestStartTime = capturedRecords.stream()
      .map(AcquiredChannelEnvironmentIssue::getStartTime).max(Instant::compareTo)
      .orElse(Instant.MAX);
    assertTrue(highestStartTime.isAfter(timeOfRun) || highestStartTime.equals(timeOfRun));
    assertTrue(
      highestStartTime.isAfter(lowestStartTime) || highestStartTime.equals(lowestStartTime));

    final var lowestEndTime = capturedRecords.stream()
      .map(AcquiredChannelEnvironmentIssue::getEndTime).min(Instant::compareTo)
      .orElse(Instant.MAX);
    assertFalse(lowestEndTime.isBefore(timeOfRun));
    assertTrue(lowestEndTime.isAfter(lowestStartTime));

    final var highestEndTime = capturedRecords.stream()
      .map(AcquiredChannelEnvironmentIssue::getEndTime).max(Instant::compareTo)
      .orElse(Instant.MAX);
    assertTrue(highestEndTime.isAfter(timeOfRun));
    assertTrue(highestEndTime.isAfter(lowestStartTime));
    assertTrue(highestEndTime.isAfter(lowestEndTime) || highestEndTime.equals(lowestEndTime));
  }

  @Override
  protected void mockIntermittentSohRepositoryFailure(int failsOnNthCall) {
    doAnswer(i -> throwErrorOnNthCall(failsOnNthCall)).when(sohRepository)
      .syncAceiUpdates(any());
  }

  @Override
  protected void verifyRepositoryInteraction(int wantedNumberOfInvocations) {
    verify(sohRepository, times(wantedNumberOfInvocations))
      .syncAceiUpdates(updatesCaptor.capture());

    final var ids = updatesCaptor.getAllValues().stream()
      .map(AceiUpdates::getBooleanInserts)
      .flatMap(Collection::stream)
      .map(issue -> Objects.hash(
        issue.getChannelName(),
        issue.getStartTime(),
        issue.getType().name()
      ))
      .collect(Collectors.toList());
    validateIds(ids);
  }

  @Override
  protected int getWantedNumberOfItemsGenerated() {
    if (generationSpec.getUseCuratedDataGeneration()) {
      return (int) ((int) getNumberOfChannels() * getEnvironmentIssueTypes().count()
        * dataGenerator.getAceiBooleanDataGeneratorStateStatusesSize());
    } else {
      return super.getWantedNumberOfItemsGenerated();
    }
  }

  @Override
  protected int getWantedNumberOfConsumeInvocations() {
    if (generationSpec.getUseCuratedDataGeneration()) {
      return (int) ((int) getNumberOfChannels() * getEnvironmentIssueTypes().count() * (Math
        .ceil(
          (double) dataGenerator.getAceiBooleanDataGeneratorStateStatusesSize() / BATCH_SIZE)));
    } else {
      return super.getWantedNumberOfConsumeInvocations();
    }
  }

  @Override
  protected int getExpectedDuplicateIdsSize() {
    if (generationSpec.getUseCuratedDataGeneration()) {
      return (int) Math.ceil(
        dataGenerator.getAceiBooleanDataGeneratorStateStatusesSize()
          / (double) BATCH_SIZE);
    } else {
      return BATCH_SIZE;
    }
  }
}