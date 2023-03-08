package gms.dataacquisition.data.preloader.generator;

import com.google.common.collect.ImmutableMap;
import gms.dataacquisition.data.preloader.GenerationSpec;
import gms.dataacquisition.data.preloader.GenerationType;
import gms.dataacquisition.data.preloader.InitialCondition;
import gms.shared.frameworks.injector.DataGeneratorState;
import gms.shared.frameworks.injector.FluxFactory;
import gms.shared.frameworks.injector.Modifier;
import gms.shared.frameworks.osd.api.OsdRepositoryInterface;
import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Base data generator class
 *
 * @param <T>
 * @param <M>
 */
public abstract class CoiDataGenerator<T, M extends Modifier<?>> implements Runnable {

  protected static final int INITIAL_DELAY = 0;
  public static final int MAX_ATTEMPTS = 10;
  private static final Logger logger = LoggerFactory.getLogger(CoiDataGenerator.class);

  protected final GenerationSpec generationSpec;
  protected final Duration generationFrequency;
  protected final Duration generationDuration;
  protected Instant seedTime;
  protected final OsdRepositoryInterface sohRepository;
  protected final ImmutableMap<InitialCondition, String> initialConditions;
  protected final int batchSize;
  private List<String> cd11StationNames = null;
  private List<String> cd11ChannelNames = null;
  private List<StationGroup> stationGroups = null;
  private List<StationGroup> cd11StationGroup = null;

  /**
   * Constructor
   *
   * @param generationSpec blueprint for generating data
   * @param sohRepository connection to OSD
   */
  protected CoiDataGenerator(GenerationSpec generationSpec, OsdRepositoryInterface sohRepository) {
    this.sohRepository = sohRepository;
    this.generationSpec = generationSpec;
    this.initialConditions = generationSpec.getInitialConditions();
    this.generationFrequency = generationSpec.getSampleDuration();
    this.generationDuration = generationSpec.getDuration();
    this.batchSize = generationSpec.getBatchSize();
    this.seedTime = getStartTime();
  }

  protected List<StationGroup> getStationGroups() {
    if (this.stationGroups == null) {
      this.stationGroups = sohRepository.retrieveStationGroups(
        Arrays.asList(initialConditions.get(InitialCondition.STATION_GROUPS).split(",")));
    }
    return this.stationGroups;
  }

  protected Stream<StationGroup> stationGroups() {
    return getStationGroups().stream();
  }

  protected Stream<Station> stations() {
    return stationGroups().flatMap(StationGroup::stations).distinct();
  }

  protected Stream<Station> cd11Stations() {
    final List<String> cd11StationGroupNames = List.of("CD1.1");
    if (this.cd11StationGroup == null) {
      this.cd11StationGroup = sohRepository.retrieveStationGroups(cd11StationGroupNames);
    }

    return cd11StationGroup.stream().flatMap(StationGroup::stations).distinct();
  }

  protected List<String> cd11StationNames() {
    if (this.cd11StationNames == null) {
      this.cd11StationNames = cd11Stations()
        .map(Station::getName)
        .distinct()
        .collect(Collectors.toList());
    }

    return this.cd11StationNames;
  }

  protected Stream<Channel> channels() {
    return stations().flatMap(Station::channels);
  }

  protected Stream<Channel> cd11Channels() {
    return cd11Stations().flatMap(Station::channels);
  }

  protected List<String> getCd11ChannelNames() {
    if (this.cd11ChannelNames == null) {
      this.cd11ChannelNames = cd11Channels()
        .map(Channel::getName)
        .distinct()
        .collect(Collectors.toList());
    }

    return this.cd11ChannelNames;
  }

  protected Supplier<Stream<AcquiredChannelEnvironmentIssueType>> getEnvironmentIssueTypes(
    GenerationType generationType, String seed) {
    if (generationType.equals(GenerationType.ACQUIRED_CHANNEL_ENV_ISSUE_BOOLEAN)
      && !getCd11ChannelNames().contains(seed)) {
      return Stream::of;
    } else {
      return () -> Stream.of(
        AcquiredChannelEnvironmentIssueType.AUTHENTICATION_SEAL_BROKEN,
        AcquiredChannelEnvironmentIssueType.BACKUP_POWER_UNSTABLE,
        AcquiredChannelEnvironmentIssueType.CALIBRATION_UNDERWAY,
        AcquiredChannelEnvironmentIssueType.CLIPPED,
        AcquiredChannelEnvironmentIssueType.CLOCK_DIFFERENTIAL_TOO_LARGE,
        AcquiredChannelEnvironmentIssueType.DEAD_SENSOR_CHANNEL,
        AcquiredChannelEnvironmentIssueType.DIGITIZER_ANALOG_INPUT_SHORTED,
        AcquiredChannelEnvironmentIssueType.DIGITIZER_CALIBRATION_LOOP_BACK,
        AcquiredChannelEnvironmentIssueType.DIGITIZING_EQUIPMENT_OPEN,
        AcquiredChannelEnvironmentIssueType.EQUIPMENT_HOUSING_OPEN,
        AcquiredChannelEnvironmentIssueType.EQUIPMENT_MOVED,
        AcquiredChannelEnvironmentIssueType.GPS_RECEIVER_OFF,
        AcquiredChannelEnvironmentIssueType.GPS_RECEIVER_UNLOCKED,
        AcquiredChannelEnvironmentIssueType.MAIN_POWER_FAILURE,
        AcquiredChannelEnvironmentIssueType.VAULT_DOOR_OPENED,
        AcquiredChannelEnvironmentIssueType.ZEROED_DATA
      );
    }
  }

  private Instant getStartTime() {
    return generationSpec.getStartTime();
  }

  /**
   * Start generating data
   */
  public void run() {
    logger.info("~~~~~~STARTING DATA LOAD FOR: {}~~~~~~", generationSpec.getType());

    Flux.fromIterable(this.getSeedNames())
      .parallel()
      .runOn(Schedulers.boundedElastic())
      .map(seed -> {
          logger.info("~~~~~~~~~STARTING DATA LOAD FOR: {} FOR {}~~~~~~~~~",
            generationSpec.getType(), seed);
          if (generationTypeUsedWithCuratedDataGeneration(
            generationSpec.getUseCuratedDataGeneration(), generationSpec.getType())) {
            if (generationSpec.getType()
              .equals(GenerationType.ACQUIRED_CHANNEL_ENV_ISSUE_BOOLEAN)) {
              return Flux
                .fromStream(getEnvironmentIssueTypes(generationSpec.getType(), seed).get())
                .map(t -> {
                  logger.info("~~~~~~~~~STARTING DATA LOAD FOR: {} FOR {} FOR {}~~~~~~~~~",
                    generationSpec.getType(), seed, t);
                  var generationSpecBuilder = generationSpec.toBuilder();
                  generationSpecBuilder.setAcquiredChannelEnvironmentIssueType(t);
                  final var generationSpecCopy = generationSpecBuilder.build();
                  final Optional<DataGeneratorState<T, Object>> dataGeneratorState = getDataGeneratorState(
                    generationSpecCopy,
                    seed);
                  return FluxFactory
                    .createOrderedFiniteFlux(
                      dataGeneratorState.orElseThrow().getStateSupplier(),
                      dataGeneratorState.orElseThrow().getGenerator(),
                      dataGeneratorState.orElseThrow()::runRecordConsumer,
                      batchSize
                    )
                    .doOnComplete(
                      () -> logger
                        .info(
                          "~~~~~~~~~FINISHED DATA LOAD FOR: {} FOR {} FOR {}~~~~~~~~~",
                          generationSpec.getType(), seed, t))
                    .blockLast();
                })
                .doOnComplete(
                  () -> logger
                    .info(
                      getFinishedDataLoadLogMessage(generationSpec.getType(), seed)));
            } else {
              var generationSpecBuilder = generationSpec.toBuilder();
              if (cd11StationNames().contains(seed)) {
                generationSpecBuilder.setIsCd11Station(true);
              }
              final var generationSpecCopy = generationSpecBuilder.build();
              final Optional<DataGeneratorState<T, Object>> dataGeneratorState = getDataGeneratorState(
                generationSpecCopy,
                seed);
              return FluxFactory
                .createOrderedFiniteFlux(
                  dataGeneratorState.orElseThrow().getStateSupplier(),
                  dataGeneratorState.orElseThrow().getGenerator(),
                  dataGeneratorState.orElseThrow()::runRecordConsumer,
                  batchSize
                )
                .doOnComplete(
                  () -> logger
                    .info(getFinishedDataLoadLogMessage(generationSpec.getType(), seed)));
            }
          } else {
            return FluxFactory.createBoundedFlux(
                getBatchCount(),
                INITIAL_DELAY,
                generationFrequency,
                batchSize,
                () -> this.runSeedGenerator(seed),
                getModifier(generationFrequency),
                this::runRecordConsumer,
                e -> logger.error(e.getMessage(), e))
              .doOnComplete(
                () -> logger
                  .info(getFinishedDataLoadLogMessage(generationSpec.getType(), seed)));
          }
        }
      )
      .doOnNext(Flux::blockLast)
      .sequential()
      .blockLast();

    logger.info("~~~~~~FINISHED DATA LOAD FOR: {}~~~~~~", generationSpec.getType());
  }

  private String getFinishedDataLoadLogMessage(GenerationType type, String seed) {
    return "~~~~~~~~~FINISHED DATA LOAD FOR: "
      + type
      + " FOR "
      + seed
      + "~~~~~~~~~";
  }

  private boolean generationTypeUsedWithCuratedDataGeneration(boolean useCuratedDataGeneration,
    GenerationType generationType) {
    final List<GenerationType> typesUsedWithCuratedDataGeneration = List
      .of(GenerationType.ACQUIRED_CHANNEL_ENV_ISSUE_BOOLEAN, GenerationType.STATION_SOH);
    return useCuratedDataGeneration && typesUsedWithCuratedDataGeneration.contains(generationType);
  }

  private int getBatchCount() {
    return (int) Math
      .ceil(((double) generationDuration.toNanos()) / generationFrequency.toNanos() / batchSize);
  }

  protected abstract Collection<String> getSeedNames();

  protected T runSeedGenerator(String seedName) {
    logger.debug("make a seed");
    final var seed = this.generateSeed(seedName);
    logger.debug("made a seed");
    return seed;
  }

  protected abstract T generateSeed(String seedName);

  protected abstract M getModifier(Duration generationFrequency);

  protected void runRecordConsumer(Iterable<T> records) {
    try {
      logger.debug("run record consumer");
      this.tryConsume(records);
      logger.debug("records consumed");
    } catch (Exception e) {
      final var error = new GmsPreloaderException("Failed to consume records", e);
      logger.error(error.getMessage(), error);
      throw error;
    }
  }

  private void tryConsume(Iterable<T> records) {
    final RetryPolicy<T> retryPolicy = new RetryPolicy<T>()
      .withBackoff(100, 3000, ChronoUnit.MILLIS)
      .withMaxAttempts(MAX_ATTEMPTS)
      .handle(List.of(ExecutionException.class, IllegalStateException.class,
        InterruptedException.class, PSQLException.class))
      .onFailedAttempt(e -> logger.warn("Unable to consume records, retrying: {}", e));
    Failsafe.with(retryPolicy).run(() -> this.consumeRecords(records));
  }

  protected abstract void consumeRecords(Iterable<T> records);

  protected <D> Set<D> convertToSet(Iterable<D> records) {
    final var data = new HashSet<D>();
    records.iterator().forEachRemaining(data::add);
    return data;
  }

  protected <S> Optional<DataGeneratorState<T, S>> getDataGeneratorState(
    GenerationSpec generationSpec, String seedName) {
    logger.debug("Current seed name: {}", seedName);
    logger.debug("GenerationSpec copy: {}", generationSpec);
    return Optional.empty();
  }

}
