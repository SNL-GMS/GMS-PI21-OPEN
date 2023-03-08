package gms.dataacquisition.data.preloader.generator;

import gms.dataacquisition.data.preloader.GenerationSpec;
import gms.dataacquisition.data.preloader.InitialCondition;
import gms.shared.frameworks.injector.DataGeneratorState;
import gms.shared.frameworks.osd.api.OsdRepositoryInterface;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.soh.ChannelSoh;
import gms.shared.frameworks.osd.coi.soh.DurationSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.DurationStationAggregate;
import gms.shared.frameworks.osd.coi.soh.PercentSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.PercentStationAggregate;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType.SohValueType;
import gms.shared.frameworks.osd.coi.soh.SohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.SohStatus;
import gms.shared.frameworks.osd.coi.soh.StationAggregateType;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.SynchronousSink;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This maintains the state needed to generate station SOH based on previously generated station
 * SOH. It keeps track of a current instance of station SOH and calls the probabilistic status
 * generator.
 */
public class StationSohDataGeneratorState extends
  AbstractDataGeneratorState<StationSoh, StationSohDataGeneratorState> implements
  DataGeneratorState<StationSoh, StationSohDataGeneratorState> {

  private static final Logger logger = LoggerFactory.getLogger(StationSohDataGeneratorState.class);

  private final OsdRepositoryInterface sohRepository;
  private final Instant proposedEndTime;
  private final GenerationSpec generationSpec;
  private final Station station;
  private final String stationName;
  private final Supplier<Stream<AcquiredChannelEnvironmentIssueType>> environmentIssueTypeStream;
  private StationSoh current;
  private AceiAnalogStatusGenerator durationStatusGenerator;
  private AceiAnalogStatusGenerator percentStatusGenerator;

  /**
   * Initialize {@link StationSohDataGeneratorState}
   *
   * @param generationSpec that contains the blueprint for data generation
   * @param seedName a seed name as as string
   * @param station an instance of {@link Station}
   * @param environmentIssueTypeStream a {@link Stream} of environment issues types for which to
   * generate SOH
   * @param sohRepository an instance of a connection to the OSD
   */
  public StationSohDataGeneratorState(GenerationSpec generationSpec, String seedName,
    Station station,
    Supplier<Stream<AcquiredChannelEnvironmentIssueType>> environmentIssueTypeStream,
    OsdRepositoryInterface sohRepository) {
    this.generationSpec = generationSpec;
    this.proposedEndTime = this.generationSpec.getStartTime()
      .plus(this.generationSpec.getDuration()).minus(this.generationSpec.getSampleDuration())
      .minus(Duration.ofSeconds(1));
    this.station = station;
    this.stationName = seedName;
    this.environmentIssueTypeStream = environmentIssueTypeStream;
    this.sohRepository = sohRepository;
    initializeStatusGenerators();
  }

  private void initializeStatusGenerators() {
    double durationMax = this.generationSpec
      .getDurationStatusGeneratorParameter(InitialCondition.DURATION_ANALOG_STATUS_MAX)
      .orElse(Double.MAX_VALUE);
    double durationMin = this.generationSpec
      .getDurationStatusGeneratorParameter(InitialCondition.DURATION_ANALOG_STATUS_MIN)
      .orElse(0.0);
    double durationBeta0 = this.generationSpec
      .getDurationStatusGeneratorParameter(InitialCondition.DURATION_BETA0)
      .orElse(5.0);
    double durationBeta1 = this.generationSpec
      .getDurationStatusGeneratorParameter(InitialCondition.DURATION_BETA1)
      .orElse(-0.1);
    double durationStdErr = this.generationSpec
      .getDurationStatusGeneratorParameter(InitialCondition.DURATION_STDERR)
      .orElse(10.0);
    double durationInitialValue = this.generationSpec
      .getDurationStatusGeneratorParameter(InitialCondition.DURATION_ANALOG_INITIAL_VALUE)
      .orElse(1.0);
    double percentMax = this.generationSpec
      .getPercentStatusGeneratorParameter(InitialCondition.PERCENT_ANALOG_STATUS_MAX)
      .orElse(1.0);
    double percentMin = this.generationSpec
      .getPercentStatusGeneratorParameter(InitialCondition.PERCENT_ANALOG_STATUS_MIN)
      .orElse(0.0);
    double percentBeta0 = this.generationSpec
      .getPercentStatusGeneratorParameter(InitialCondition.PERCENT_BETA0)
      .orElse(0.5);
    double percentBeta1 = this.generationSpec
      .getPercentStatusGeneratorParameter(InitialCondition.PERCENT_BETA1)
      .orElse(-0.1);
    double percentStdErr = this.generationSpec
      .getPercentStatusGeneratorParameter(InitialCondition.PERCENT_STDERR)
      .orElse(0.5);
    double percentInitialValue = this.generationSpec
      .getPercentStatusGeneratorParameter(InitialCondition.PERCENT_ANALOG_INITIAL_VALUE)
      .orElse(0.5);
    this.durationStatusGenerator = new AceiAnalogStatusGenerator(durationMin, durationMax,
      durationBeta0, durationBeta1, durationStdErr, durationInitialValue);
    this.percentStatusGenerator = new AceiAnalogStatusGenerator(percentMin, percentMax,
      percentBeta0, percentBeta1, percentStdErr, percentInitialValue);
  }

  private void setCurrent(StationSoh stationSoh) {
    this.current = stationSoh;
  }

  private StationSoh getCurrent() {
    return this.current;
  }

  private StationSoh getStartingSoh() {
    return getNextStationSoh(
      this.generationSpec.getStartTime().minus(this.generationSpec.getSampleDuration()));
  }

  @Override
  public Callable<StationSohDataGeneratorState> getStateSupplier() {
    this.current = getStartingSoh();
    return () -> this;
  }

  @Override
  public BiFunction<StationSohDataGeneratorState, SynchronousSink<StationSoh>, StationSohDataGeneratorState> getGenerator() {
    return (state, sink) -> {
      sink.next(state.getCurrent());
      if (state.getCurrent().getTime().isAfter(this.proposedEndTime)) {
        sink.complete();
      }
      StationSoh temp = state.getCurrent();
      state.setCurrent(getNextStationSoh(temp.getTime()));

      return state;
    };
  }

  private StationSoh getNextStationSoh(Instant time) {
    DurationStationAggregate timelinessStationAggregate = DurationStationAggregate
      .from(nextDuration(), StationAggregateType.TIMELINESS);
    PercentStationAggregate missingStationAggregate = PercentStationAggregate
      .from(this.percentStatusGenerator.next(), StationAggregateType.MISSING);
    DurationStationAggregate lagStationAggregate = DurationStationAggregate
      .from(nextDuration(), StationAggregateType.LAG);

    Set<SohMonitorValueAndStatus<?>> smvs = getSmvs();

    Set<ChannelSoh> channelSoh = this.station.getChannels()
      .stream().map(channel -> ChannelSoh.from(channel.getName(),
        SohStatus.MARGINAL, smvs)).collect(Collectors.toSet());

    return StationSoh
      .create(time.plus(this.generationSpec.getSampleDuration()),
        this.stationName, smvs, SohStatus.MARGINAL, channelSoh,
        Set.of(missingStationAggregate, lagStationAggregate, timelinessStationAggregate));

  }

  private Set<SohMonitorValueAndStatus<?>> getSmvs() {
    HashSet<SohMonitorValueAndStatus<?>> smvs = new HashSet<>();
    DurationSohMonitorValueAndStatus marginalLagSohMonitorValueAndStatus =
      DurationSohMonitorValueAndStatus
        .from(nextDuration(), SohStatus.MARGINAL, SohMonitorType.LAG);
    PercentSohMonitorValueAndStatus marginalMissingSohMonitorValueAndStatus =
      PercentSohMonitorValueAndStatus
        .from(this.percentStatusGenerator.next(), SohStatus.MARGINAL, SohMonitorType.MISSING);
    DurationSohMonitorValueAndStatus marginalTimelinessSohMonitorValueAndStatus =
      DurationSohMonitorValueAndStatus.from(nextDuration(), SohStatus.MARGINAL,
        SohMonitorType.TIMELINESS);

    if (this.generationSpec.getIsCd11Station()) {
      this.environmentIssueTypeStream.get()
        .forEach(t -> {
          SohValueType sohValueType = t.getMatchingSohMonitorType().getSohValueType();
          if (sohValueType.equals(SohValueType.INVALID)) {
            logger.warn("Invalid SohMonitorType {}, ignoring", t.getMatchingSohMonitorType());
          } else {
            if (sohValueType.equals(SohValueType.PERCENT)) {
              smvs.add(PercentSohMonitorValueAndStatus.from(this.percentStatusGenerator.next(),
                SohStatus.MARGINAL, t.getMatchingSohMonitorType()));
            } else {
              smvs.add(
                DurationSohMonitorValueAndStatus.from(nextDuration(),
                  SohStatus.MARGINAL, t.getMatchingSohMonitorType()));
            }
          }
        });
    }

    smvs.add(marginalLagSohMonitorValueAndStatus);
    smvs.add(marginalMissingSohMonitorValueAndStatus);
    smvs.add(marginalTimelinessSohMonitorValueAndStatus);

    return smvs;
  }

  private Duration nextDuration() {
    double nextDuration = this.durationStatusGenerator.next();
    return Duration.ofSeconds((long) Math.floor(nextDuration * 3600));
  }

  @Override
  protected void consumeRecords(Iterable<StationSoh> records) {
    logger.debug("StationSoh consuming records - STARTING...");
    sohRepository.storeStationSoh(convertToSet(records));
    logger.debug("StationSoh consuming records - COMPLETE");
  }
}
