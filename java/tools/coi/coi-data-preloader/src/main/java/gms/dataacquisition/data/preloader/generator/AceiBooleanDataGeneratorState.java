package gms.dataacquisition.data.preloader.generator;

import gms.dataacquisition.data.preloader.GenerationSpec;
import gms.dataacquisition.data.preloader.InitialCondition;
import gms.shared.frameworks.injector.DataGeneratorState;
import gms.shared.frameworks.osd.api.OsdRepositoryInterface;
import gms.shared.frameworks.osd.api.rawstationdataframe.AceiUpdates;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.SynchronousSink;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;

/**
 * This maintains the state needed to generate boolean acquired channel environment issues based on
 * previously generated ACEI. It keeps track of a current instance of boolean acquired channel
 * environment issue, a collection of statuses, and a collection of issue durations.
 */
public class AceiBooleanDataGeneratorState extends
  AbstractDataGeneratorState<AcquiredChannelEnvironmentIssueBoolean, AceiBooleanDataGeneratorState> implements
  DataGeneratorState<AcquiredChannelEnvironmentIssueBoolean, AceiBooleanDataGeneratorState> {

  protected static final Logger logger = LoggerFactory
    .getLogger(AceiBooleanDataGeneratorState.class);

  private final int sampleStatusesSize;
  private final long elapsedTimeInSecondsToProposedEndTime;
  private final String channelName;
  private final GenerationSpec generationSpec;
  private final Iterator<Duration> eventIntervalsIterator;
  private final Iterator<Boolean> statusesIterator;
  private final OsdRepositoryInterface sohRepository;
  private Instant proposedEndTime;
  private AcquiredChannelEnvironmentIssueBoolean current;

  /**
   * Initialize {@link AceiBooleanDataGeneratorState}
   *
   * @param generationSpec that contains the blueprint for data generation
   * @param seedName a seed name as a string
   * @param sohRepository an instance of a connection to the OSD
   */
  public AceiBooleanDataGeneratorState(GenerationSpec generationSpec, String seedName,
    OsdRepositoryInterface sohRepository) {
    this.sohRepository = sohRepository;
    this.generationSpec = generationSpec;
    boolean initialStatus = (boolean) this.generationSpec
      .getBooleanStatusGeneratorParameter(InitialCondition.BOOLEAN_INITIAL_STATUS).orElse(
        false);
    List<Duration> sampleDurations = AceiBooleanStatusGenerator
      .generateDurations(this.generationSpec.getDuration(),
        (Duration) this.generationSpec
          .getBooleanStatusGeneratorParameter(InitialCondition.DURATION_INCREMENT)
          .orElse(Duration.ofHours(1)),
        initialStatus,
        (double) this.generationSpec
          .getBooleanStatusGeneratorParameter(InitialCondition.MEAN_OCCURRENCES_PER_YEAR)
          .orElse(1.5e5),
        (double) this.generationSpec
          .getBooleanStatusGeneratorParameter(InitialCondition.MEAN_HOURS_OF_PERSISTENCE)
          .orElse(12.0));

    List<Boolean> sampleStatuses = new ArrayList<>();
    boolean currentStatus = initialStatus;
    for (Duration ignored : sampleDurations) {
      sampleStatuses.add(currentStatus);
      currentStatus = !currentStatus;
    }
    this.sampleStatusesSize = sampleStatuses.size();
    this.channelName = seedName;
    this.eventIntervalsIterator = sampleDurations.listIterator();
    this.statusesIterator = sampleStatuses.listIterator();

    long totalElapsedTimeInSeconds = sampleDurations
      .stream()
      .map(Duration::getSeconds)
      .mapToLong(Long::longValue)
      .sum();

    this.elapsedTimeInSecondsToProposedEndTime = sampleDurations.size() > 1 ?
      totalElapsedTimeInSeconds - sampleDurations.get(sampleDurations.size() - 1).getSeconds() :
      totalElapsedTimeInSeconds - sampleDurations.get(sampleDurations.size() - 1).getSeconds()
        - 1;
  }

  private void setCurrent(AcquiredChannelEnvironmentIssueBoolean current) {
    this.current = current;
  }

  private AcquiredChannelEnvironmentIssueBoolean getCurrent() {
    return this.current;
  }

  private Duration getNextInterval() {
    var nextDuration = Duration.ofSeconds(10);
    if (this.eventIntervalsIterator.hasNext()) {
      nextDuration = this.eventIntervalsIterator.next();
    } else {
      logger.info("Using default duration of {} seconds", nextDuration.getSeconds());
    }

    return nextDuration;
  }

  private boolean getNextStatus() {
    var isNext = false;
    if (this.statusesIterator.hasNext()) {
      isNext = this.statusesIterator.next();
    } else {
      logger.info("Using default false status");
    }

    return isNext;
  }

  private AcquiredChannelEnvironmentIssueBoolean getStartingIssue() {
    final Duration startingIssueDuration =
      this.eventIntervalsIterator.hasNext() ? this.eventIntervalsIterator.next()
        : Duration.ofSeconds(10);
    final var isStartingIssueStatus =
      this.statusesIterator.hasNext() ? this.statusesIterator.next() : Boolean.FALSE;
    final var startingAcquiredChannelEnvironmentIssueType =
      this.generationSpec.getAcquiredChannelEnvironmentIssueType()
        .orElse(AcquiredChannelEnvironmentIssueType.VAULT_DOOR_OPENED);

    return AcquiredChannelEnvironmentIssueBoolean.from(
      this.channelName,
      startingAcquiredChannelEnvironmentIssueType,
      this.generationSpec.getStartTime(),
      this.generationSpec.getStartTime().plus(startingIssueDuration),
      isStartingIssueStatus);
  }

  protected int getSampleStatusesSize() {
    return this.sampleStatusesSize;
  }

  @Override
  public Callable<AceiBooleanDataGeneratorState> getStateSupplier() {
    this.current = getStartingIssue();
    this.proposedEndTime = this.current.getStartTime()
      .plusSeconds(this.elapsedTimeInSecondsToProposedEndTime);
    return () -> this;
  }

  @Override
  public BiFunction<AceiBooleanDataGeneratorState,
    SynchronousSink<AcquiredChannelEnvironmentIssueBoolean>,
    AceiBooleanDataGeneratorState> getGenerator() {
    return (state, sink) -> {
      sink.next(state.getCurrent());
      if (state.getCurrent().getStartTime().isAfter(this.proposedEndTime)) {
        sink.complete();
      }
      Duration nextIntervalLength = state.getNextInterval();
      AcquiredChannelEnvironmentIssueBoolean temp = state.getCurrent();
      Instant newStartTime = temp.getEndTime();
      Instant newEndTime = newStartTime.plus(nextIntervalLength);
      state.setCurrent(
        AcquiredChannelEnvironmentIssueBoolean.from(
          this.channelName,
          this.generationSpec.getAcquiredChannelEnvironmentIssueType().orElseThrow(),
          newStartTime,
          newEndTime,
          state.getNextStatus()
        ));
      return state;
    };
  }

  @Override
  protected void consumeRecords(Iterable<AcquiredChannelEnvironmentIssueBoolean> records) {
    logger.debug("ACEI consuming records - STARTING...");
    sohRepository.syncAceiUpdates(AceiUpdates.builder().setBooleanInserts(convertToSet(records)).build());
    logger.debug("ACEI consuming records - COMPLETE");
  }
}
