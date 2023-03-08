package gms.shared.workflow.repository;

import com.google.common.collect.Range;
import gms.shared.utilities.bridge.database.converter.InstantToDoubleConverterPositiveNa;
import gms.shared.workflow.coi.AutomaticProcessingStage;
import gms.shared.workflow.coi.InteractiveAnalysisStage;
import gms.shared.workflow.coi.Stage;
import gms.shared.workflow.coi.StageInterval;
import gms.shared.workflow.coi.StageMode;
import gms.shared.workflow.coi.Workflow;
import gms.shared.workflow.dao.IntervalDao;
import gms.shared.workflow.repository.util.IntervalUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;

/**
 * Converts both Interactive and Automatic Processing {@link StageInterval}s to and from legacy {@link IntervalDao}s
 */
@Component
public class IntervalConverter {

  private final AutomaticIntervalConverter automaticIntervalConverter;
  private final InteractiveIntervalConverter interactiveIntervalConverter;
  private final Map<String, Stage> stagesByName;
  private static final InstantToDoubleConverterPositiveNa instantToDoubleConverter = new InstantToDoubleConverterPositiveNa();

  @Autowired
  public IntervalConverter(Workflow workflow) {

    this.automaticIntervalConverter = new AutomaticIntervalConverter();
    this.interactiveIntervalConverter = new InteractiveIntervalConverter();
    this.stagesByName = workflow.getStages().stream()
      .collect(Collectors.toMap(Stage::getName, identity()));
  }

  /**
   * Attempts to convert a legacy {@link IntervalDao} into a COI {@link StageInterval} using known conversions from the
   * legacy stages to configured system {@link Stage}s. The kind of StageInterval generated is dependent on the kind of
   * Stage the IntervalDao comes from.
   *
   * @param intervalDao Input legacy interval
   * @return An interval representing status for an automatic or interactive Stage
   * @throws IllegalArgumentException If no stage was found for the input interval
   */
  public StageInterval convert(IntervalDao intervalDao) {
    var stage = IntervalUtility.getStageName(intervalDao.getType(),
        intervalDao.getName())
      .flatMap(this::getStage)
      .orElseThrow(() -> new IllegalArgumentException(format("No matching stage found for interval %s:%s",
        intervalDao.getType(),
        intervalDao.getName())));

    return convertInternal(intervalDao, stage);
  }

  /**
   * Batch conversion method for converting legacy {@link IntervalDao}s from a wide variety of stages. Note that the
   * query times used to retrieved the IntervalDaos is included, as certain stages will require procedurally generated
   * intervals that do not always map one-to-one with a legacy IntervalDao. These stages will use a converter where the
   * expected interval time ranges are provided. Ordering will be respected as best as possible in the return based on
   * the input ordering of the IntervalDaos, given the grouping by Stage that occurs.
   *
   * @param intervalDaos List of legacy IntervalDaos
   * @param queryStart Start of the query used to retrieve the IntervalDaos
   * @param queryEnd End of the query used to retrieve the IntervalDaos
   * @return Map of Stage names to an ordered list of COI StageIntervals
   */
  public Map<String, List<StageInterval>> convert(List<IntervalDao> intervalDaos, Instant queryStart,
    Instant queryEnd) {
    Map<String, List<StageInterval>> intervalsByStageName = new HashMap<>();
    intervalDaos.stream()
      .collect(Collectors.groupingBy(
        intervalDao -> IntervalUtility.getStageName(intervalDao.getType(),
          intervalDao.getName()).flatMap(this::getStage)))
      .forEach((stageOpt, intervalGroup) -> {
        var stage = stageOpt.orElseThrow((() -> new IllegalArgumentException(
          format("No matching stage found for %s intervals", intervalGroup.size()))));
        if (IntervalUtility.AUTO_NETWORK.equals(stage.getName())) {
          intervalsByStageName.put(stage.getName(),
            convertForAutoNetwork(intervalGroup, (AutomaticProcessingStage) stage, queryStart, queryEnd));
        } else {
          intervalsByStageName.put(stage.getName(),
            intervalGroup.stream().map(interval -> convertInternal(interval, stage)).collect(toList()));
        }
      });
    return intervalsByStageName;
  }

  private StageInterval convertInternal(IntervalDao intervalDao, Stage stage) {
    return StageMode.INTERACTIVE.equals(stage.getMode())
      ? interactiveIntervalConverter.fromLegacy(intervalDao, (InteractiveAnalysisStage) stage)
      : automaticIntervalConverter.fromLegacy(intervalDao, (AutomaticProcessingStage) stage);
  }

  private Optional<Stage> getStage(String stageName) {
    return Optional.ofNullable(stagesByName.get(stageName));
  }

  private List<StageInterval> convertForAutoNetwork(List<IntervalDao> autoIntervalDaos,
    AutomaticProcessingStage stage, Instant startTime, Instant endTime) {

    var bestStartTime = findBestStartTime(autoIntervalDaos, startTime);
    var bestEndTime = findBestEndTime(autoIntervalDaos, endTime);

    List<StageInterval> stageIntervals = new LinkedList<>();
    var shiftedStart = getAdjustedStartTime(bestStartTime, stage.getDuration());
    var shiftedEnd = getAdjustedEndTime(bestEndTime, stage.getDuration());

    Instant currentEnd;
    Optional<IntervalDao> latestForTime;
    for (Instant currentStart = shiftedStart; currentStart.isBefore(shiftedEnd); currentStart = currentStart.plus(stage.getDuration())) {
      currentEnd = currentStart.plus(stage.getDuration());
      latestForTime = autoIntervalDaos.stream()
        .filter(encloses(currentStart, currentEnd))
        .max(Comparator.comparing(IntervalDao::getLastModificationDate).thenComparing(IntervalDao::getLoadDate));

      if (latestForTime.isPresent()) {
        stageIntervals.add(automaticIntervalConverter.fromLegacy(latestForTime.get(), stage, currentStart, currentEnd));
      }
    }

    return stageIntervals;
  }

  private static Instant findBestStartTime(List<IntervalDao> intervalDaos, Instant queryStartTime) {
    var minIntervalTime = intervalDaos.stream()
      .mapToDouble(IntervalDao::getTime)
      .min();

    var minIntervalStart = minIntervalTime.isPresent()
      ? instantToDoubleConverter.convertToEntityAttribute(minIntervalTime.getAsDouble())
      : Instant.MIN;

    return queryStartTime.isAfter(minIntervalStart) ? queryStartTime : minIntervalStart;
  }

  private static Instant findBestEndTime(List<IntervalDao> intervalDaos, Instant queryEndTime) {
    var maxIntervalEndTime = intervalDaos.stream()
      .mapToDouble(IntervalDao::getEndTime)
      .max();

    var maxIntervalEnd = maxIntervalEndTime.isPresent()
      ? instantToDoubleConverter.convertToEntityAttribute(maxIntervalEndTime.getAsDouble())
      : Instant.MAX;

    return queryEndTime.isBefore(maxIntervalEnd) ? queryEndTime : maxIntervalEnd;
  }

  private Instant getAdjustedStartTime(Instant startTime, Duration stageDuration) {
    var startEpochSeconds = startTime.getEpochSecond();
    long stageDurationSeconds = stageDuration.getSeconds();
    checkArgument(stageDurationSeconds > 0, "Error adjusting start time, stage duration must be > 0");

    var intervalsSinceEpoch = startEpochSeconds / stageDurationSeconds;
    intervalsSinceEpoch *= stageDurationSeconds;
    return Instant.ofEpochSecond(intervalsSinceEpoch);
  }

  private Instant getAdjustedEndTime(Instant endTime, Duration stageDuration) {
    var endInSeconds = endTime.getEpochSecond();
    long stageDurationSeconds = stageDuration.getSeconds();
    checkArgument(stageDurationSeconds > 0, "Error adjusting end time, stage duration must be > 0");

    var intervalsSinceEpoch = endInSeconds / stageDurationSeconds;
    if (endInSeconds % stageDurationSeconds != 0) {
      intervalsSinceEpoch += 1;
    }
    intervalsSinceEpoch *= stageDuration.getSeconds();
    return Instant.ofEpochSecond(intervalsSinceEpoch);
  }


  /**
   * Predicate generator for filtering out {@link IntervalDao}s that do not enclose the input time range. Used to narrow
   * down the possible IntervalDaos we'll use to generate a Auto Network {@link StageInterval}
   *
   * @param intervalStart Start of generated interval
   * @param intervalEnd End of generated interval
   * @return A Predicate that returns true if the IntervalDao encloses the generated interval, false otherwise
   */
  private static Predicate<IntervalDao> encloses(Instant intervalStart, Instant intervalEnd) {
    final Range<Instant> range = Range.closedOpen(intervalStart, intervalEnd);
    return intervalDao -> Range.closedOpen(
        instantToDoubleConverter.convertToEntityAttribute(intervalDao.getTime()),
        instantToDoubleConverter.convertToEntityAttribute(intervalDao.getEndTime()))
      .encloses(range);
  }
}
