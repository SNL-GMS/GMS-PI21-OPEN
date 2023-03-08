package gms.shared.workflow.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;
import gms.shared.utilities.javautilities.objectmapper.ObjectMapperFactory;
import gms.shared.workflow.coi.ActivityInterval;
import gms.shared.workflow.coi.AutomaticProcessingStageInterval;
import gms.shared.workflow.coi.InteractiveAnalysisStageInterval;
import gms.shared.workflow.coi.IntervalStatus;
import gms.shared.workflow.coi.ProcessingSequenceInterval;
import gms.shared.workflow.coi.StageInterval;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gms.shared.workflow.repository.util.IntervalShiftingUtility.shiftStageInterval;

public class MockedBridgedIntervalRepository extends BridgedIntervalRepository {

  private static final Logger logger = LoggerFactory.getLogger(MockedBridgedIntervalRepository.class);

  private final AtomicInteger intervalMapUpdatesCount = new AtomicInteger();
  private final SecureRandom randomSeed = new SecureRandom();
  private static final String AL1 = "AL1";
  private static final String AL2 = "AL2";
  private static final int SIXTY_MINUTES = 60;
  private static final int ONE_HUNDRED_TWENTY_MINUTES = 120;
  private static final String AUTO_NETWORK = "Auto Network";
  private static final String AUTO_POST_AL1 = "Auto Post-AL1";
  private static final String BASE_FILE_PATH = "gms/shared/workflow/repository/mockworkflow/";
  private static final String WORKFLOW_INTERVALS_FILE_PATH = BASE_FILE_PATH + "workflow-intervals.json";

  private final Duration operationalTimeSpan;
  private final ConcurrentMap<String, NavigableMap<Instant, StageInterval>> intervalMap;
  private final Supplier<Instant> nowSupplier;

  public MockedBridgedIntervalRepository(Instant operationalPeriodStart, Instant operationalPeriodEnd,
    Supplier<Instant> nowSupplier) {
    super(null, null);
    this.nowSupplier = nowSupplier;

    // Currently, we always assume operational time period is based on projecting backward from "now", so
    // only use these start/ends to generate a span of time to shift loaded data by
    this.operationalTimeSpan = Duration.between(operationalPeriodStart, operationalPeriodEnd);

    var loadedMap = loadWorkflowIntervalsFromFile();

    ZonedDateTime now = nowSupplier.get().atZone(ZoneOffset.UTC);

    // We want the seed data, which has intervals that span increments of hours (e.g. AL1/AL2) to always start and end
    // on the hour boundary after shifting, so need to adjust "now" with which we will shift the seed data by to be on
    // the hour boundary
    var roundedNowToHour = now.withMinute(0).withSecond(0).withNano(0).toInstant();

    logger.debug("Rounded now: {}", roundedNowToHour);

    var referenceEndTime = getLatestIntervalEndTime(loadedMap);

    var loadedTimeSpan = Duration.between(getEarliestIntervalStartTime(loadedMap), referenceEndTime);

    logger.debug("Loaded time span: {}", loadedTimeSpan);

    var referenceEndTimeShift = Duration.between(referenceEndTime,
      roundedNowToHour.minus(operationalTimeSpan).plus(loadedTimeSpan));

    logger.debug("Reference end time shift: {}, New Latest end time: {}", referenceEndTimeShift,
      referenceEndTime.plus(referenceEndTimeShift));

    loadedMap.replaceAll((key, value) -> shiftData(value, referenceEndTimeShift));

    intervalMap = loadedMap;

    var latestIntervalEndBeforeNow = now.withMinute((now.getMinute() / 5) * 5).withNano(0).toInstant();

    var operationalMinusLoadedSpan = operationalTimeSpan.minus(loadedTimeSpan);
    var prePopulationSpan = operationalMinusLoadedSpan
      .plus(Duration.between(
        referenceEndTime
          .plus(referenceEndTimeShift)
          .plus(operationalMinusLoadedSpan),
        latestIntervalEndBeforeNow));

    logger.debug("Pre-Population Span Calculated: {}", prePopulationSpan);

    prePopulateIntervals(prePopulationSpan);

    logger.debug("Populated Time Span: {}", Duration.between(getEarliestIntervalStartTime(intervalMap), getLatestIntervalEndTime(intervalMap)));
  }

  @VisibleForTesting
  MockedBridgedIntervalRepository(Duration operationalTimeSpan,
    ConcurrentMap<String, NavigableMap<Instant, StageInterval>> intervalMap, Supplier<Instant> nowSupplier) {
    super(null, null);
    this.operationalTimeSpan = operationalTimeSpan;
    this.intervalMap = intervalMap;
    this.nowSupplier = nowSupplier;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, List<StageInterval>> findStageIntervalsByStageIdAndTime(Instant startTime, Instant endTime,
    Collection<WorkflowDefinitionId> stageIds) {
    var stageNames = stageIds.stream().map(WorkflowDefinitionId::getName).collect(Collectors.toSet());
    return intervalMap.entrySet().stream()
      .filter(entry -> stageNames.contains(entry.getKey()))
      .map(entry -> Map.entry(entry.getKey(), List.copyOf(entry.getValue().subMap(startTime, endTime).values())))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Override
  public Map<String, List<StageInterval>> findStageIntervalsByStageIdAndTime(Instant startTime, Instant endTime,
    Collection<WorkflowDefinitionId> stageIds, Instant modifiedAfter) {
    var stageNames = stageIds.stream().map(WorkflowDefinitionId::getName).collect(Collectors.toSet());
    return intervalMap.entrySet().stream()
      .filter(entry -> stageNames.contains(entry.getKey()))
      .map(entry -> Map.entry(entry.getKey(), List.copyOf(entry.getValue()
        .subMap(startTime, endTime) //This breaks interval caching and viewing when generating faster than real-time
        .values().stream()
        .filter(stageInterval -> stageInterval.getModificationTime().isAfter(modifiedAfter))
        .collect(Collectors.toList()))))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /**
   * Update the interval map "cache" with the provided intervals, if applicable
   *
   * @param stageIntervals Intervals to attempt to "cache"
   */
  public void updateIntervalMap(List<StageInterval> stageIntervals) {
    intervalMapUpdatesCount.incrementAndGet();
    // Nothing to do
    if (stageIntervals == null || stageIntervals.isEmpty()) {
      return;
    }

    // Figure which type of interval it is Interactive or Automated
    for (StageInterval stageInterval : stageIntervals) {
      this.addToIntervalMap(stageInterval);
    }
  }

  private void addToIntervalMap(StageInterval interval) {
    var intervals = this.intervalMap.get(interval.getName());

    var latestBefore = Optional.ofNullable(intervals.floorEntry(interval.getStartTime()));
    var earliestAfter = Optional.ofNullable(intervals.ceilingEntry(interval.getStartTime()));

    AtomicReference<Set<StageInterval>> intervalsToRemove = new AtomicReference<>(new HashSet<>());
    var shouldInsert = new AtomicBoolean(true);

    latestBefore.ifPresent(lbEntry -> {
      var lbInterval = lbEntry.getValue();
      if (lbInterval.getIntervalId().equals(interval.getIntervalId())) {
        if (!lbInterval.getModificationTime().isAfter(interval.getModificationTime())) {
          intervalsToRemove.get().add(lbInterval);
        } else {
          shouldInsert.set(false);
        }
      }
    });

    earliestAfter.ifPresent(eaEntry -> {
      var eaInterval = eaEntry.getValue();
      if (eaInterval.getIntervalId().equals(interval.getIntervalId())) {
        if (!eaInterval.getModificationTime().isAfter(interval.getModificationTime())) {
          intervalsToRemove.get().add(eaInterval);
        } else {
          shouldInsert.set(false);
        }
      }
    });

    intervalsToRemove.get().forEach(stageInterval -> intervals.remove(stageInterval.getStartTime()));
    if (shouldInsert.get()) {
      intervals.put(interval.getStartTime(), interval);
    } else {
      logger.debug("Skipping insertion of interval Name: {}\nStage Mode: {}\nID: {}\nWF Def ID: {}\n", interval.getName(),
        interval.getStageMode(), interval.getIntervalId(), interval.getWorkflowDefinitionId());
      logger.debug("Reason: \n\tLBModTime: {}, EAModTime: {}, ModTime: {}",
        latestBefore.map(lbEntry -> lbEntry.getValue().getModificationTime()).orElse(Instant.MAX),
        earliestAfter.map(eaEntry -> eaEntry.getValue().getModificationTime()).orElse(Instant.MAX),
        interval.getModificationTime());
    }
  }

  /**
   * Get the modification time of the latest interval in the repository map "cache"
   *
   * @return Latest interval modification time of "cached" repository intervals
   */
  public Instant getLatestIntervalModTime() {
    return intervalMap.values().stream()
      .map(Map::values)
      .flatMap(Collection::stream)
      .map(StageInterval::getModificationTime)
      .max(Instant::compareTo)
      .orElseThrow();
  }

  @Scheduled(fixedDelayString = "${service.run-state.repository.generator.fixedDelay-millis}",
    initialDelayString = "${service.run-state.repository.generator.initialDelay-millis}")
  void runIntervalGeneration() {
    logger.debug("Generating new intervals in local repository");
    var now = nowSupplier.get();
    List<StageInterval> newIntervals = generateIntervals(Optional.of(now));
    updateIntervalMap(newIntervals);
    logger.debug("Generated {} new intervals", newIntervals.size());
    pruneIntervalMap(now.minus(operationalTimeSpan));
  }

  @VisibleForTesting
  void pruneIntervalMap(Instant olderThan) {
    intervalMap.forEach((key, value) -> intervalMap.put(key, value.tailMap(olderThan, true)));
  }

  @VisibleForTesting
  ConcurrentMap<String, NavigableMap<Instant, StageInterval>> getIntervalMap() {
    return intervalMap;
  }

  @VisibleForTesting
  List<StageInterval> generateIntervals() {
    return generateIntervals(Optional.empty());
  }

  private static ConcurrentMap<String, NavigableMap<Instant, StageInterval>> loadWorkflowIntervalsFromFile() {
    try (final InputStream dataStream = MockedBridgedIntervalRepository.class.getClassLoader()
      .getResourceAsStream(WORKFLOW_INTERVALS_FILE_PATH)) {
      Validate.notNull(dataStream, String.format("File at [%s] does not exist.", WORKFLOW_INTERVALS_FILE_PATH));

      var intervalListMap = ObjectMapperFactory.getJsonObjectMapper()
        .readValue(dataStream, new TypeReference<Map<String, List<StageInterval>>>() {
        });

      return intervalListMap.entrySet().stream()
        .map(entry -> Map.entry(entry.getKey(), new TreeMap<>(entry.getValue().stream()
          .collect(Collectors.toMap(StageInterval::getStartTime, Function.identity())))))
        .collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue));
    } catch (IOException e) {
      throw new IllegalArgumentException("Unable to load Stage Intervals from Json file", e);
    }
  }

  // Populate the interval map with data within operational time period
  private void prePopulateIntervals(Duration duration) {
    long numberIterations = duration.toMinutes() / 5;
    logger.debug("Number of interval generation rounds: {}", numberIterations);
    var now = nowSupplier.get();
    for (long i = 0; i < numberIterations; i++) {
      var intervalsDefaultModTime = generateIntervals();
      var intervalsEndingBeforeNow = intervalsDefaultModTime.stream()
        .map(interval -> Map.entry(interval.getEndTime(), interval))
        .filter(intervalEntry -> intervalEntry.getKey().isBefore(now))
        .map(Map.Entry::getValue)
        .collect(Collectors.toList());
      if (intervalsEndingBeforeNow.size() == intervalsDefaultModTime.size()) {
        updateIntervalMap(intervalsDefaultModTime);
      } else {
        // Need to factor for the case when the last intervals are generated where their end times are in the future
        var intervalsNowModTime = generateIntervals(now);

        var intervalsEndingAfterNowNewModTime = intervalsNowModTime.stream()
          .map(interval -> Map.entry(interval.getEndTime(), interval))
          .filter(intervalEntry -> !intervalEntry.getKey().isBefore(now))
          .map(Map.Entry::getValue)
          .collect(Collectors.toList());

        updateIntervalMap(Stream.concat(intervalsEndingBeforeNow.stream(), intervalsEndingAfterNowNewModTime.stream())
          .collect(Collectors.toList()));
      }
    }
  }

  private static NavigableMap<Instant, StageInterval> shiftData(NavigableMap<Instant, StageInterval> intervalsByTime,
    Duration relativeTimeShift) {
    return new TreeMap<>(intervalsByTime.values()
      .stream()
      .map(interval -> shiftStageInterval(interval, relativeTimeShift))
      .collect(Collectors.toMap(StageInterval::getStartTime, Function.identity())));
  }

  private static Instant getEarliestIntervalStartTime(Map<String, NavigableMap<Instant, StageInterval>> intervalMap) {
    return intervalMap.values().stream()
      .map(Map::values)
      .flatMap(Collection::stream)
      .map(StageInterval::getStartTime)
      .min(Instant::compareTo)
      .orElseThrow();
  }

  private static Instant getLatestIntervalEndTime(Map<String, NavigableMap<Instant, StageInterval>> intervalMap) {
    return intervalMap.values().stream()
      .map(Map::values)
      .flatMap(Collection::stream)
      .map(StageInterval::getEndTime)
      .max(Instant::compareTo)
      .orElseThrow();
  }

  private List<StageInterval> generateIntervals(Instant modificationTime) {
    return generateIntervals(Optional.of(modificationTime));
  }

  private List<StageInterval> generateIntervals(Optional<Instant> modificationTime) {

    // Calc percent and post increment count sent
    var intervalMapUpdates = intervalMapUpdatesCount.get();
    double oneHourPercentComplete = (intervalMapUpdates % 12) / 12.0 * 100;
    double twoHourPercentComplete = (intervalMapUpdates % 24) / 24.0 * 100;

    // send an Auto Network processing message every time
    List<StageInterval> stageIntervals = this.getAutoNetworkProcessingIntervals(modificationTime);

    // Every 12 processing stage messages sent, send a new AL1 stage interval
    this.getInteractiveStageInterval(AL1, SIXTY_MINUTES, oneHourPercentComplete, modificationTime).ifPresent(stageIntervals::add);
    // Every 24 processing stage messages sent, send a new AL2 stage interval
    this.getInteractiveStageInterval(AL2, ONE_HUNDRED_TWENTY_MINUTES, twoHourPercentComplete, modificationTime).ifPresent(stageIntervals::add);
    // Every 12 processing stage messages sent, send a post-AL1 stage interval,
    // but update % complete every time for current interval
    stageIntervals.addAll(this.getAutoPostProcessingIntervals(oneHourPercentComplete, modificationTime));

    return stageIntervals;
  }

  private Optional<InteractiveAnalysisStageInterval> getInteractiveStageInterval(String stageName, int intervalSpanMins,
    double percentComplete, Optional<Instant> modificationTime) {
    // Set to complete and add to list
    var stageInterval = latestAnalysisInterval(stageName);

    logger.debug("Percent Complete: {}", percentComplete);
    if (percentComplete == 0.0) {
      Instant startTime = stageInterval.getStartTime();
      startTime = startTime.plus(intervalSpanMins, ChronoUnit.MINUTES);
      Instant endTime = stageInterval.getEndTime();
      endTime = endTime.plus(intervalSpanMins, ChronoUnit.MINUTES);

      // Create the next stage interval and add it to the map
      var nextStageInterval = stageInterval.toBuilder().setProcessingEndTime(endTime)
        .setProcessingStartTime(startTime)
        .setStartTime(startTime)
        .setEndTime(endTime)
        .setModificationTime(modificationTime.orElse(endTime))
        .setStatus(IntervalStatus.NOT_STARTED)
        .setPercentAvailable(0.0)
        .setComment("New interval in progress")
        .setActivityIntervals(rebuildActivities(
          stageInterval.getActivityIntervals(), startTime, endTime, modificationTime.orElse(endTime)))
        .build();

      // Add the stage interval to the list to be returned
      return Optional.of(nextStageInterval);
    }
    return Optional.empty();
  }

  private List<StageInterval> getAutoNetworkProcessingIntervals(Optional<Instant> modificationTime) {

    var stageIntervals = new ArrayList<StageInterval>();
    int randStatus = Math.abs(randomSeed.nextInt() % 20);
    var intervalStatus = IntervalStatus.COMPLETE;
    if (randStatus >= 18) {
      intervalStatus = IntervalStatus.SKIPPED;
    } else if (randStatus <= 2) {
      intervalStatus = IntervalStatus.FAILED;
    }

    var autoNetworkStageInterval = latestAutoInterval(AUTO_NETWORK);
    var intervalBuilder = autoNetworkStageInterval.toBuilder();
    intervalBuilder.setStatus(intervalStatus)
      .setModificationTime(modificationTime.orElse(autoNetworkStageInterval.getModificationTime()));

    intervalBuilder.setSequenceIntervals(
      rebuildSequenceIntervals(
        autoNetworkStageInterval.getSequenceIntervals(),
        intervalStatus,
        autoNetworkStageInterval.getStartTime(),
        autoNetworkStageInterval.getEndTime(),
        100.0, modificationTime.orElse(autoNetworkStageInterval.getEndTime())));
    var updatedAutoNetwork = intervalBuilder.build();

    // Add updated auto network interval
    stageIntervals.add(updatedAutoNetwork);

    var startTime = updatedAutoNetwork.getStartTime();
    startTime = startTime.plus(5, ChronoUnit.MINUTES);
    var endTime = updatedAutoNetwork.getEndTime();
    endTime = endTime.plus(5, ChronoUnit.MINUTES);
    intervalBuilder.setProcessingEndTime(endTime)
      .setProcessingStartTime(startTime)
      .setStartTime(startTime)
      .setEndTime(endTime)
      .setModificationTime(modificationTime.orElse(endTime))
      .setStatus(IntervalStatus.IN_PROGRESS)
      .setSequenceIntervals(
        rebuildSequenceIntervals(
          updatedAutoNetwork.getSequenceIntervals(),
          IntervalStatus.IN_PROGRESS,
          startTime,
          endTime,
          0.0, modificationTime.orElse(endTime)));

    // Create next Auto Network stage interval and add it to the map
    stageIntervals.add(intervalBuilder.build());
    return stageIntervals;
  }

  private List<StageInterval> getAutoPostProcessingIntervals(double percentComplete,
    Optional<Instant> modificationTime) {

    var stageIntervals = new ArrayList<StageInterval>();
    var autoPostStageInterval = latestAutoInterval(AUTO_POST_AL1);
    var intervalBuilder = autoPostStageInterval.toBuilder();
    var intervalComplete = percentComplete == 0.0;
    var status = intervalComplete ? IntervalStatus.COMPLETE : IntervalStatus.IN_PROGRESS;
    intervalBuilder.setStatus(status)
      .setPercentAvailable(percentComplete)
      .setModificationTime(modificationTime.orElse(autoPostStageInterval.getModificationTime()));
    intervalBuilder.setSequenceIntervals(
      rebuildSequenceIntervals(
        autoPostStageInterval.getSequenceIntervals(),
        status,
        autoPostStageInterval.getStartTime(),
        autoPostStageInterval.getEndTime(),
        intervalComplete ? 100 : percentComplete,
        modificationTime.orElse(autoPostStageInterval.getEndTime())));
    var updatedAutoPost = intervalBuilder.build();

    // Add it to the list
    stageIntervals.add(updatedAutoPost);

    // Every 12th send an update message
    if (intervalComplete) {
      var startTime = updatedAutoPost.getStartTime();
      startTime = startTime.plus(60, ChronoUnit.MINUTES);
      var endTime = updatedAutoPost.getEndTime();
      endTime = endTime.plus(60, ChronoUnit.MINUTES);
      intervalBuilder.setProcessingEndTime(endTime)
        .setProcessingStartTime(startTime)
        .setStartTime(startTime)
        .setEndTime(endTime)
        .setModificationTime(modificationTime.orElse(endTime))
        .setStatus(IntervalStatus.IN_PROGRESS)
        .setSequenceIntervals(
          rebuildSequenceIntervals(
            updatedAutoPost.getSequenceIntervals(),
            IntervalStatus.IN_PROGRESS,
            startTime,
            endTime,
            0.0, modificationTime.orElse(endTime)));
      // Create next Auto Network stage interval and add it to the map
      stageIntervals.add(intervalBuilder.build());
    }
    return stageIntervals;
  }

  private AutomaticProcessingStageInterval latestAutoInterval(String automaticName) {
    return (AutomaticProcessingStageInterval) this.intervalMap.get(automaticName)
      .lastEntry().getValue();
  }

  private InteractiveAnalysisStageInterval latestAnalysisInterval(String stageName) {
    return (InteractiveAnalysisStageInterval) this.intervalMap.get(stageName)
      .lastEntry().getValue();
  }

  private List<ProcessingSequenceInterval> rebuildSequenceIntervals(List<ProcessingSequenceInterval> sequenceIntervals,
    IntervalStatus status, Instant startTime, Instant endTime, double percentComplete,
    Instant modificationTime) {
    return sequenceIntervals.stream()
      .map(ProcessingSequenceInterval::toBuilder)
      .map(seq -> seq.setStatus(status)
        .setPercentComplete(percentComplete)
        .setStartTime(startTime)
        .setEndTime(endTime)
        .setModificationTime(modificationTime)
        .build())
      .collect(Collectors.toList());
  }

  private List<ActivityInterval> rebuildActivities(List<ActivityInterval> activities,
    Instant startTime, Instant endTime, Instant modificationTime) {
    return activities.stream()
      .map(ActivityInterval::toBuilder)
      .map(activityBuilder -> activityBuilder.setStatus(IntervalStatus.NOT_STARTED)
        .setActiveAnalysts(new ArrayList<>())
        .setStartTime(startTime)
        .setEndTime(endTime)
        .setModificationTime(modificationTime)
        .build())
      .collect(Collectors.toList());
  }
}
