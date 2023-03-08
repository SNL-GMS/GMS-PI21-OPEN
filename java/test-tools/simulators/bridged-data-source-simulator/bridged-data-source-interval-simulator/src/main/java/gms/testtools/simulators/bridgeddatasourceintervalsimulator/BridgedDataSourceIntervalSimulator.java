package gms.testtools.simulators.bridgeddatasourceintervalsimulator;

import com.google.common.annotations.VisibleForTesting;
import gms.shared.workflow.dao.IntervalDao;
import gms.shared.workflow.repository.IntervalDatabaseConnector;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.BridgedDataSourceDataSimulator;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.util.BridgedDataSourceSimulatorSpec;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.util.SourceInterval;
import gms.testtools.simulators.bridgeddatasourcesimulator.repository.BridgedDataSourceIntervalRepositoryJpa;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The Bridged Data Source Interval Simulator is responsible for taking a set of intervals from the
 * past and shifting them into a time span that started sometime in the past and goes until now.
 */
public class BridgedDataSourceIntervalSimulator implements BridgedDataSourceDataSimulator {

  //
  // Local data structure that will help keep track of previous and current delays, so that
  // the correction mechanism can work.
  //
  private static class IntervalDaoDelayPair {

    final IntervalDao intervalDao;
    Duration delay;

    IntervalDaoDelayPair(IntervalDao intervalDao, Duration delay) {
      this.intervalDao = intervalDao;
      this.delay = delay;
    }
  }

  private static final Logger logger = LoggerFactory.getLogger(BridgedDataSourceIntervalSimulator.class);

  // Link to the smarts of the DB, used to retrieve and store intervals
  private final IntervalDatabaseConnector intervalDatabaseConnector;

  // Direct connection to the database used for storing intervals
  private final BridgedDataSourceIntervalRepositoryJpa bridgedDataSourceIntervalRepositoryJpa;

  // The configuration parameters that are used to specify when in the past to pull data from and
  // then when in time to start copying and repeating that seed data
  private BridgedDataSourceSimulatorSpec intervalSimulatorSpec;

  // Grabbed as now() during initialise, marks the last time that data can be copied
  // into the simulation database
  private Instant initializationEndTime;

  // The data that was pulled out of the "seed database" and will be copied to fill in the
  // operationalTimePeriod that is a config parameter
  private List<IntervalDao> seedData;

  private Disposable simulationFluxDisposable;

  //Values needed by Start()
  // The time difference between the simulationStartTime and seedDataStartTime
  private Duration initializationSeedTimeOffset;
  // The last interval that was put into the DB during initialize()
  private long lastInitializedIntervalId;


  /**
   * Construct a new instance of the interval simulator
   *
   * @param intervalDatabaseConnector connector to use for the seed intervals
   * @param bridgedDataSourceIntervalRepositoryJpa repository to use for the simulated intervals
   */
  private BridgedDataSourceIntervalSimulator(
    IntervalDatabaseConnector intervalDatabaseConnector,
    BridgedDataSourceIntervalRepositoryJpa bridgedDataSourceIntervalRepositoryJpa
  ) {
    this.intervalDatabaseConnector = intervalDatabaseConnector;
    this.bridgedDataSourceIntervalRepositoryJpa = bridgedDataSourceIntervalRepositoryJpa;
  }

  /**
   * Create a new instance of the interval simulator
   *
   * @param intervalDatabaseConnector connector to use for the seed intervals
   * @param bridgedDataSourceIntervalRepositoryJpa repository to use for the simulated intervals
   */
  public static BridgedDataSourceIntervalSimulator create(
    IntervalDatabaseConnector intervalDatabaseConnector,
    BridgedDataSourceIntervalRepositoryJpa bridgedDataSourceIntervalRepositoryJpa
  ) {
    return new BridgedDataSourceIntervalSimulator(
      intervalDatabaseConnector,
      bridgedDataSourceIntervalRepositoryJpa
    );
  }

  /**
   * Using the BridgedDataSourceSimulatorSpec passed in load the specified data from the seed
   * database into the simulation database
   *
   * @param bridgedDataSourceSimulatorSpec - The {@link BridgedDataSourceSimulatorSpec} that
   * specifies simulation parameters.
   */
  @Override
  public void initialize(BridgedDataSourceSimulatorSpec bridgedDataSourceSimulatorSpec) {

    initialize(
      bridgedDataSourceSimulatorSpec,
      Instant.now()
    );
  }

  @Override
  public void load(String placeholder) {
//    function not needed as it is handled by the controller but due to interface inheritance is
//    required to be in this class as well
  }

  /**
   * Start the interval simulator.
   *
   * @param placeholder - Any string value. This is ignored and is here to match the interface.
   */
  @Override
  public void start(String placeholder) {
    simulationFluxDisposable = getSimulationFlux(
      this.intervalSimulatorSpec,
      this.initializationEndTime,
      this.initializationSeedTimeOffset,
      this.seedData,
      Instant::now,
      this.lastInitializedIntervalId,
      true
    ).publishOn(Schedulers.boundedElastic())
      .subscribe(
        shiftedIntervalDao -> {
          bridgedDataSourceIntervalRepositoryJpa.store(List.of(shiftedIntervalDao));

          logger.debug(
            "Stored IntervalDao {}",
            shiftedIntervalDao.getClassEndTimeNameTimeKey()
          );
        }
      );
  }

  /**
   * Start the simulator.
   *
   * @param placeholder - Any string value. This is ignored and is here to match the interface.
   */
  @Override
  public void stop(String placeholder) {
    logger.info("Stopping the interval simulator");
    simulationFluxDisposable.dispose();
  }

  /**
   * Delte all simulated intervals.
   *
   * @param placeholder - Any string value. This is ignored and is here to match the interface.
   */
  @Override
  public void cleanup(String placeholder) {
    logger.info("cleaning up INTERVAL table.");
    bridgedDataSourceIntervalRepositoryJpa.cleanupData();
  }

  /**
   * Store a list of intervals to the simulation database.
   *
   * @param intervalList the intervals to store.
   */
  public void storeIntervals(List<SourceInterval> intervalList) {

    bridgedDataSourceIntervalRepositoryJpa.storeOrUpdate(
      intervalList.stream()
        .map(s -> new IntervalDao.Builder()
          .intervalIdentifier(s.getIntervalIdentifier())
          .type(s.getType())
          .name(s.getName())
          .time(s.getTime())
          .endTime(s.getEndTime())
          .state(s.getState())
          .author(s.getAuthor())
          .percentAvailable(s.getPercentAvailable())
          .processStartDate(s.getProcessStartDate())
          .processEndDate(s.getProcessEndDate())
          .lastModificationDate(s.getLastModificationDate())
          .loadDate(s.getLoadDate())
          .build())
        .collect(Collectors.toList())
    );
  }

  //
  // Private and package-private methods
  //

  /**
   * The process of loading seed data into the simulation set has two parts:
   * BackwardPass, before the simulationStartTime and ForwardPass, after the simulationStartTime.
   * This is the backward pass:
   * <p>
   * BackwardPass - Align the seed data so that it's endTime aligns with the simulationStartTime
   * and then copy numberBackwardSeeds shifting each by the seed duration
   *
   * @param seedMetadata SeedMetadata object containing metadata need to perform the backward pass
   * @param simulationStartTime Time that the simulation starts.
   * @return The last interval ID that was generated by this pass.
   */
  private long shiftBackward(
    SeedMetadata seedMetadata,
    Instant simulationStartTime
  ) {

    long initialBackwardsTimeShift =
      Duration.between(seedMetadata.getAdjustedSeedDataEndTime(), simulationStartTime).toSeconds();

    var maxIntervalIdReference = new AtomicLong();

    logger.info("Copying {} seeds of length {} starting at {} going backwards in time.",
      seedMetadata.getNumberBackwardSeeds(), seedMetadata.getAdjustedSeedDuration(), simulationStartTime);

    //Create the backward shifted sim values
    Flux.range(1, seedMetadata.getNumberBackwardSeeds())
      .map(shiftIndex -> {

        logger.info("Copying seed #{}", shiftIndex);

        // create the list of intervals from the seed that have been shifted
        var shiftedList = backwardPassShift(initialBackwardsTimeShift, shiftIndex,
          seedMetadata.getAdjustedSeedDuration());

        // grab from the shifted list the last id, created for start(), only for non-empty lists
        if (!shiftedList.isEmpty()) {
          var lastId = shiftedList
            .get(shiftedList.size() - 1)
            .getIntervalIdentifier();
          maxIntervalIdReference.updateAndGet(
            current -> {
              if (lastId > current) {
                return lastId;
              }
              return current;
            }
          );
        }
        return shiftedList;
      })
      // store all the results, doing so in parallel as they take a while and are independent
      .publishOn(Schedulers.boundedElastic())
      .doOnNext(bridgedDataSourceIntervalRepositoryJpa::store)
      .blockLast();

    return maxIntervalIdReference.get();
  }

  /**
   * The process of loading seed data into the simulation set has two parts:
   * BackwardPass, before the simulationStartTime and ForwardPass, after the simulationStartTime.
   * This is the forward pass:
   * <p>
   * ForwardPass  - Align the seed data so that it's startTime aligns with simulationStartTime
   * and then copy numberForwardSeeds shifting each by the seed duration
   *
   * @param seedMetadata SeedMetadata object containing metadata need to perform the backward pass
   * @param simulationStartTime Time that the simulation starts.
   * @param initialIntervalId Which intervalID to start eith when generating the intervals
   * @param initializationSeedTimeRef This is like an "out" paramter - it will contain the time of
   * the first interval used by start
   * @return The last interval ID that was generated by this pass.
   */
  private long shiftForward(
    SeedMetadata seedMetadata,
    Instant simulationStartTime,
    long initialIntervalId,
    AtomicReference<Instant> initializationSeedTimeRef
  ) {

    long forwardTimeShift =
      Duration.between(seedMetadata.getAdjustedSeedDataStartTime(), simulationStartTime).toSeconds();

    logger.info("Copying {} seeds of length {} starting at {} going forwards in time.",
      seedMetadata.getNumberForwardSeeds(), seedMetadata.getAdjustedSeedDuration(), simulationStartTime);

    // The first forward pass id is the last backward pass interval id plus one
    long finalNumberBackwardChunks = seedMetadata.getNumberBackwardSeeds();

    var maxIntervalIdReference = new AtomicLong(initialIntervalId);

    Flux.range(seedMetadata.getNumberBackwardSeeds() + 1, seedMetadata.getNumberForwardSeeds())
      .map(i -> {
        initializationSeedTimeRef
          .set(initializationEndTime
            .minus(forwardTimeShift, ChronoUnit.SECONDS)
            .minus((i - finalNumberBackwardChunks - 1)
                * seedMetadata.getAdjustedSeedDuration().toSeconds(),
              ChronoUnit.SECONDS));

        var shiftedList =
          forwardPassShift(forwardTimeShift, (int) finalNumberBackwardChunks,
            i, seedMetadata.getAdjustedSeedDuration());

        // grab from the shifted list the last id created,
        // only for non empty lists as the forward pass filters out things after now and it is
        //possible to have nothing
        if (!shiftedList.isEmpty()) {
          var lastId = shiftedList
            .get(shiftedList.size() - 1)
            .getIntervalIdentifier();
          maxIntervalIdReference.updateAndGet(
            current -> {
              if (lastId > current) {
                return lastId;
              }
              return current;
            }
          );
        }
        return shiftedList;
      })
      // store all the results, doing so in parallel as they take a while and are independent
      .publishOn(Schedulers.boundedElastic())
      .doOnNext(
        shiftedList -> {
          if (!shiftedList.isEmpty()) {
            bridgedDataSourceIntervalRepositoryJpa.store(shiftedList);
          }
        })
      .blockLast();

    return maxIntervalIdReference.get();
  }

  @VisibleForTesting
  static IntervalDao buildTimeShiftedInterval(IntervalDao oldDao, long timeShift, int newId) {

    return new IntervalDao.Builder()
      .intervalIdentifier(newId)
      .type(oldDao.getType())
      .name(oldDao.getName())
      .time(oldDao.getTime() + timeShift)
      .endTime(oldDao.getEndTime() + timeShift)
      .state(oldDao.getState())
      .author(oldDao.getAuthor())
      .percentAvailable(oldDao.getPercentAvailable())
      .processStartDate(oldDao.getProcessStartDate().plusSeconds(timeShift))
      .processEndDate(oldDao.getProcessEndDate().plusSeconds(timeShift))
      .lastModificationDate(oldDao.getLastModificationDate().plusSeconds(timeShift))
      .loadDate(oldDao.getLoadDate())
      .build();
  }

  @VisibleForTesting
  void initialize(
    BridgedDataSourceSimulatorSpec bridgedDataSourceSimulatorSpec,
    Instant now
  ) {

    var seedMetadata = computeSeedMetadata(
      bridgedDataSourceSimulatorSpec, now
    );

    this.initializeSeedData(
      seedMetadata.getAdjustedSeedDataStartTime(),
      seedMetadata.getAdjustedSeedDataEndTime()
    );

    // The simulationStartTime marks the time when all tasks before that are "complete" in the UI
    // and all tasks after the simulationStartTime are "not started"
    Instant simulationStartTime = bridgedDataSourceSimulatorSpec.getSimulationStartTime();

    var lastGeneratedIntervalId = shiftBackward(
      seedMetadata,
      simulationStartTime
    );

    var initializationSeedTimeRef = new AtomicReference<>(simulationStartTime);

    lastGeneratedIntervalId = shiftForward(
      seedMetadata,
      simulationStartTime,
      lastGeneratedIntervalId,
      initializationSeedTimeRef
    );

    // The largest id created and the startTime of the last seed processed need to be stored for
    // BridgedDataSourceIntervalSimulator::start(String)
    lastInitializedIntervalId = lastGeneratedIntervalId;
    initializationSeedTimeOffset = Duration
      .between(seedMetadata.getAdjustedSeedDataStartTime(), initializationSeedTimeRef.get());

    intervalSimulatorSpec = bridgedDataSourceSimulatorSpec;
  }

  /**
   * Construct a Flux which emits a simulated interval in real time, based on the modification dates
   * of the seed data set.
   *
   * @param bridgedDataSourceSimulatorSpec Simulator spec which specifies behavior of simulation
   * @param initializationTime When the simulation was initialized
   * @param initializationSeedTimeOffset How far to go past the initialize time to find the start
   * of seed data.
   * @param seedData Set of seed intervals.
   * @param nowSupplier A supplier that indicates "now". Normally, would return
   * Instant.now, but can be made to return whatever is needed
   * for testing.
   * @param intervalIdStart The interval id after which new interval IDs will be
   * created.
   * @param applyCorrections Whether to perform self-correcting. This is set to false
   * for unit testing
   * @return A flux which once subscribed to, will emit simulated intervals on a "dynamic cadence"
   * dependant on the relative modification dates of the seed intervals.
   */
  @VisibleForTesting
  static Flux<IntervalDao> getSimulationFlux(
    BridgedDataSourceSimulatorSpec bridgedDataSourceSimulatorSpec,
    Instant initializationTime,
    Duration initializationSeedTimeOffset,
    List<IntervalDao> seedData,
    Supplier<Instant> nowSupplier,
    long intervalIdStart,
    boolean applyCorrections
  ) {

    Validate.notEmpty(seedData, "getSimulationFlux: Retrieved empty seed data");

    var seedDataLength = Duration.between(
      bridgedDataSourceSimulatorSpec.getSeedDataStartTime(),
      bridgedDataSourceSimulatorSpec.getSeedDataEndTime()
    );

    var idealStartTime = nowSupplier.get();
    if (applyCorrections) {
      // If we are applying corrections (ie, NOT unit testing), make the start time an "ideal"
      // start time that has no fractional seconds.
      idealStartTime = idealStartTime.truncatedTo(ChronoUnit.SECONDS);
    }

    var offset = Duration.ofMillis(
      initializationSeedTimeOffset
        .plus(Duration.between(
          initializationTime, idealStartTime
        ))
        .toMillis() % seedDataLength.toMillis()
    );

    //
    // Time of initial seed interval, which can be a seed other than the first in the list,
    // depending on the offset.
    //
    var currentSeedTime = bridgedDataSourceSimulatorSpec.getSeedDataStartTime()
      .truncatedTo(ChronoUnit.HOURS).plus(offset);

    var rotatedSeedData = sortSeedData(seedData, currentSeedTime, seedDataLength);

    logger.info(
      "INTERVAL START: initialID: {}, initializationTime: {}, initializationSeedTimeOffset {}, seedDataLength: {}, idealStartTime: {}, offset: {}, currentSeedTime: {}, rotatedSeedData size: {}",
      intervalIdStart,
      initializationTime,
      initializationSeedTimeOffset,
      seedDataLength,
      idealStartTime,
      offset,
      currentSeedTime,
      rotatedSeedData.size()
    );

    // Will increase with each new simulated interval.
    var currentIdRef = new AtomicLong(intervalIdStart);

    var veryFirstIterationRef = new AtomicBoolean(true);

    var currentIdealTimeRef = new AtomicReference<>(idealStartTime);

    return getAnnotatedSeedDataFlux(rotatedSeedData, seedDataLength)
      .repeat()
      .delayUntil(intervalDaoDelayPair -> {

        if (applyCorrections && !veryFirstIterationRef.get()) {
          // Find how far we have deviated from the ideal time.
          var difference = Duration.between(
            currentIdealTimeRef.get(),
            nowSupplier.get());

          // May not be good if the deviation is this much, so just log a warning.
          if (difference.abs().compareTo(Duration.ofSeconds(1)) > 0) {
            logger.warn("Deviation of > 1 second in interval simulation timer: " + difference);
          }

          var newIdealTime = currentIdealTimeRef.get().plus(
            intervalDaoDelayPair.delay
          );
          currentIdealTimeRef.set(newIdealTime);

          return Mono.delay(intervalDaoDelayPair.delay.minus(difference));
        } else if (veryFirstIterationRef.get()) {

          veryFirstIterationRef.set(false);

          currentIdealTimeRef.set(
            currentIdealTimeRef.get().plus(
              Duration.between(
                currentSeedTime,
                rotatedSeedData.get(0).getLastModificationDate())
            )
          );

          return Mono.delay(Duration.between(
            currentSeedTime,
            rotatedSeedData.get(0).getLastModificationDate())
          );
        } else {
          return Mono.delay(intervalDaoDelayPair.delay);
        }
      })
      .map(intervalDaoDelayPair -> {
        var currentInterval = intervalDaoDelayPair.intervalDao;

        var now = nowSupplier.get();

        return createSimulatedInterval(
          currentIdRef.incrementAndGet(),
          currentInterval,
          Duration.between(
            currentInterval.getLastModificationDate(),
            now
          )
        );
      });
  }

  private SeedMetadata computeSeedMetadata(
    BridgedDataSourceSimulatorSpec intervalSimulatorSpec,
    Instant now
  ) {

    // The Instants that define when in the past that the seed data starts and stops
    Instant seedDataStartTime = intervalSimulatorSpec.getSeedDataStartTime();
    Instant seedDataEndTime = intervalSimulatorSpec.getSeedDataEndTime();

    // Align the seed start/end times to hour boundaries and compute seed duration

    //Round down the start time to the nearest hour
    var adjustedSeedDataStartTime = seedDataStartTime.truncatedTo(ChronoUnit.HOURS);

    // To round up the endTime, check if there is a partial hour and then round up if needed
    var adjustedSeedDataEndTime = seedDataEndTime.truncatedTo(ChronoUnit.HOURS);

    if (seedDataEndTime.isAfter(adjustedSeedDataEndTime)) {
      adjustedSeedDataEndTime = adjustedSeedDataEndTime.plus(1, ChronoUnit.HOURS);
    }

    var adjustedSeedDuration = Duration.between(adjustedSeedDataStartTime,
      adjustedSeedDataEndTime);

    // The simulationStartTime signals when seed data goes from being considered "done" to pending
    // as it is copied into the simulation database
    Instant simulationStartTime = intervalSimulatorSpec.getSimulationStartTime();

    // Data is only to be created until "now" so grab a time to use to know when to stop
    initializationEndTime = now;

    // check that simStartTime is before now
    Validate.isTrue(initializationEndTime.isAfter(simulationStartTime),
      "Error, simulationStartTime %s is equal to or after now %s, ",
      simulationStartTime, initializationEndTime);

    var operationalDuration = intervalSimulatorSpec.getOperationalTimePeriod();
    var forwardDuration = Duration.between(simulationStartTime, initializationEndTime);
    var backwardsDuration = operationalDuration.minus(forwardDuration);

    // Using the seed duration and computed forward/backward durations,compute how many times
    // the seed will be used in backward/forward passes to fill the operational time period

    // This use case has seed data that is both before and after the simStartTime

    // Compute if the seed duration is a divisor of the forward duration of if there needs to be an
    // additional seed added that will overflow, and that is ok
    var numberForwardSeeds = (int) Math.abs(
      forwardDuration.dividedBy(adjustedSeedDuration));

    if (forwardDuration.toSeconds() % adjustedSeedDuration.toSeconds() != 0) {
      numberForwardSeeds++;
    }

    //Compute if the seed duration is a divisor of the backward duration of if there needs to be an
    // additional seed added that will overflow, and that is ok
    var numberBackwardSeeds = (int) Math.abs(
      backwardsDuration.dividedBy(adjustedSeedDuration));

    if (backwardsDuration.toSeconds() % adjustedSeedDuration.toSeconds() != 0) {
      numberBackwardSeeds++;
    }

    if (backwardsDuration.isNegative()) {
      //Having a negative backward duration means that all the data to create is
      // after the simStartTime so there are 0 chunks to copy backwards
      numberBackwardSeeds = 0;

      logger.info("The configured OperationalTimePeriod {} is less than the duration between  "
          + "the simStartTime, {} and now, {}.  No data will be created before the simStartTime",
        operationalDuration,
        simulationStartTime,
        initializationEndTime);
    }

    return SeedMetadata.create(
      adjustedSeedDataStartTime,
      adjustedSeedDataEndTime,
      adjustedSeedDuration,
      numberForwardSeeds,
      numberBackwardSeeds
    );
  }

  /**
   * "Sort" the given seed data by sorting by moddate, then spliting the data set at the given time,
   * then rearranging so that everything after the split time is placed first on the new list.
   *
   * @param seedData Data to sort
   * @param splitTime Where to create the split for rearranging
   * @return new sorted and rearranged list of seed data.
   */
  private static List<IntervalDao> sortSeedData(
    List<IntervalDao> seedData, Instant splitTime, Duration seedDataLength
  ) {

    NavigableMap<Instant, List<IntervalDao>> intervalDaoModDateMap = new TreeMap<>();

    seedData.forEach(intervalDao ->
      intervalDaoModDateMap
        .computeIfAbsent(intervalDao.getLastModificationDate(), k -> new ArrayList<>())
        .add(intervalDao)
    );

    var headMap = intervalDaoModDateMap.headMap(splitTime);
    var tailMap = intervalDaoModDateMap.tailMap(splitTime);

    var rotatedSeedData = new ArrayList<IntervalDao>();
    rotatedSeedData.addAll(
      tailMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList()));

    rotatedSeedData.addAll(
      headMap.values().stream().flatMap(Collection::stream)
        .map(intervalDao -> new IntervalDao.Builder()
          .intervalIdentifier(intervalDao.getIntervalIdentifier())
          .time(intervalDao.getTime() + seedDataLength.toMillis() / 1000.0)
          .endTime(intervalDao.getEndTime() + seedDataLength.toMillis() / 1000.0)
          .author(intervalDao.getAuthor())
          .loadDate(intervalDao.getLoadDate().plus(seedDataLength))
          .processStartDate(intervalDao.getProcessStartDate().plus(seedDataLength))
          .processEndDate(intervalDao.getProcessEndDate().plus(seedDataLength))
          .state(intervalDao.getState())
          .name(intervalDao.getName())
          .type(intervalDao.getType())
          .percentAvailable(intervalDao.getPercentAvailable())
          .lastModificationDate(intervalDao.getLastModificationDate().plus(seedDataLength))
          .build()
        )
        .collect(Collectors.toList()));

    return rotatedSeedData.stream().sorted(
      Comparator.comparing(
        IntervalDao::getLastModificationDate,
        Comparator.naturalOrder()
      )
    ).collect(Collectors.toList());
  }

  /**
   * Create a Flux of IntervalDaoNextDelayPair (in other words, "annotated" seed data) from the
   * given sorted seed data.
   * <p>
   * "Annotated" means we are attaching delay information for use in self-correction.
   *
   * @param sortedSeedData Seed data to use; it is assumed this is sorted/arranged correctly
   * @return Flux of IntervalDaoNextDelayPair
   */
  private static Flux<IntervalDaoDelayPair> getAnnotatedSeedDataFlux(
    List<IntervalDao> sortedSeedData,
    Duration seedDataLength
  ) {

    var daoDelayPairs = new ArrayDeque<IntervalDaoDelayPair>();

    IntStream.range(1, sortedSeedData.size() + 1).forEach(i -> {
      var myDelay = Duration.between(
        sortedSeedData.get(i - 1).getLastModificationDate(),
        sortedSeedData.get(i % sortedSeedData.size()).getLastModificationDate()
      );

      if (myDelay.isNegative()) {
        myDelay = myDelay.plus(seedDataLength);
      }

      var pair = new IntervalDaoDelayPair(
        sortedSeedData.get(i % sortedSeedData.size()),
        myDelay
      );

      daoDelayPairs.add(pair);
    });

    daoDelayPairs.addFirst(daoDelayPairs.removeLast());

    return Flux.fromIterable(daoDelayPairs);
  }

  private static IntervalDao createSimulatedInterval(
    long newId,
    IntervalDao intervalDao,
    Duration timeshift) {

    var primaryKey = intervalDao.getClassEndTimeNameTimeKey();

    double numericalTimeShift = timeshift.toMillis() / 1000.0;

    return new IntervalDao.Builder()
      .intervalIdentifier(newId)
      .type(primaryKey.getType())
      .name(primaryKey.getName())
      .time(primaryKey.getTime() + numericalTimeShift)
      .endTime(primaryKey.getEndTime() + numericalTimeShift)
      .state(getNewState(intervalDao))
      .author(intervalDao.getAuthor())
      .percentAvailable(intervalDao.getPercentAvailable())
      .processStartDate(intervalDao.getProcessStartDate().plus(timeshift))
      .processEndDate(intervalDao.getProcessEndDate().plus(timeshift))
      .lastModificationDate(intervalDao.getLastModificationDate().plus(timeshift))
      .loadDate(intervalDao.getLoadDate().plus(timeshift))
      .build();
  }

  /**
   * @param initialTimeShift The time difference from simStartTime and seedEndTime
   * @param passIndex The number nth time through this time shifting function, starts at 1
   * @param seedDuration The length of the seed in seconds
   * @return A copy of the seed data that has been shifted backwards in time byt the amount equal to
   * (passIndex * seedDuration)
   */
  @VisibleForTesting
  List<IntervalDao> backwardPassShift(long initialTimeShift, int passIndex, Duration seedDuration) {

    // For each DAO passed in:
    // Compute the shift in time for the next intervalId, newIndex
    // shift = simStart - initialTimeShift - seedDuration*((passIndex-1)*seedSize + newIndex)
    //  which aligns the end of the seeds to the start time and then shifts the seed to its proper
    //  place based on the iteration counter

    List<IntervalDao> shiftedList = new ArrayList<>();

    IntStream.range(0, seedData.size())
      .forEach(i -> {
          // The currentIndex is computed to be  ((passIndex -1) * seedData.size()) + i +1
          // This is due to the passIndex starting at 1, but needing to be shifted for 0 indexing
          // scaled by the number of intervals in the seed and then added to the current index in the
          // current seed
          int currentIndex = ((passIndex - 1) * seedData.size()) + i + 1;
          long timeShift =
            initialTimeShift - ((passIndex - 1) * seedDuration.getSeconds());

          IntervalDao newDao = buildTimeShiftedInterval(seedData.get(i), timeShift,
            currentIndex);

          // Mark this interval as done as this data is before the sim time and considered "done"
          newDao.setState("done");
          shiftedList.add(newDao);
        }
      );

    return shiftedList;
  }

  /**
   * @param initialTimeShift The time difference from simStartTime and seedEndTime
   * @param startingIndex How many seeds have been shifted so far, numBackwardSeeds
   * @param passIndex The number nth time through this time shifting function
   * @param seedDuration The length of the seed in seconds
   * @return A shifted list
   */
  private List<IntervalDao> forwardPassShift(long initialTimeShift, int startingIndex,
    int passIndex, Duration seedDuration) {

    // For each DAO passed in:
    // Compute the shift in time for this current iteration = simStart - seedStart + duration*index
    //  which aligns the end of the seeds to the start time and then shifts the seed to its proper
    //  place based on the iteration counter,
    //  note that the forward counter is the total index - backward index
    //
    // Mark them as done if they are automatic intervals, otherwise set them to pending indicating they
    // are ready for an analyst to review them
    //
    // If the shifted interval is after now, remove it from the list of returned values, otherwise add
    // the current dao to the list to be stored
    //

    List<IntervalDao> shiftedList = new ArrayList<>();

    IntStream.range(0, seedData.size())
      .forEach(i -> {
          long timeShift =
            initialTimeShift + ((passIndex - startingIndex - 1) * seedDuration.getSeconds());

          int currentIndex = (passIndex - 1) * seedData.size() + i + 1;

          IntervalDao newDao = buildTimeShiftedInterval(
            seedData.get(i), timeShift, currentIndex);

          newDao.setState(getNewState(newDao));
          shiftedList.add(newDao);
        }
      );

    return shiftedList.stream()
      .filter(dao -> dao.getLastModificationDate().isBefore(initializationEndTime))
      .collect(Collectors.toList());
  }

  private static String getNewState(IntervalDao intervalDao) {
    var primaryKey = intervalDao.getClassEndTimeNameTimeKey();
    return "NETNETS1".equals(primaryKey.getType() + primaryKey.getName()) ? "done" : "pending";
  }

  @VisibleForTesting
  void initializeSeedData(Instant seedDataStartTime, Instant seedDataEndTime) {

    seedData = intervalDatabaseConnector
      .findIntervalsByTimeRange(seedDataStartTime, seedDataEndTime);

    logger.info("Using {} to {} as the time range for seed data.",
      seedDataStartTime, seedDataEndTime);
    logger.info("Seed data consists of: {} intervals", seedData.size());

  }
}
