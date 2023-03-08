package gms.shared.frameworks.injector.ui;

import com.google.common.collect.Streams;
import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.soh.ChannelSoh;
import gms.shared.frameworks.osd.coi.soh.DurationSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.DurationStationAggregate;
import gms.shared.frameworks.osd.coi.soh.PercentSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.PercentStationAggregate;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType.SohValueType;
import gms.shared.frameworks.osd.coi.soh.SohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.SohStatus;
import gms.shared.frameworks.osd.coi.soh.StationAggregate;
import gms.shared.frameworks.osd.coi.soh.StationAggregateType;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
import org.apache.commons.lang3.Validate;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Makes mock station sohs with random values changed. Simulates Hypnotoads Station Soh Processing from raw data.
 */
public class StationSohGenerator {

  private SecureRandom random;
  private ArrayList<MockStationSoh> mockStations;
  private Set<Station> stations;

  /**
   * Constructor for StationSohGenerator that generates mock stations based on our station list.
   *
   * @param stationGroups
   */
  public StationSohGenerator(List<StationGroup> stationGroups) {
    // Call utility for unique set of stations
    this.random = new SecureRandom();
    this.stations = UiDataInjectorUtility.getStationSet(stationGroups);
    this.mockStations = new ArrayList<>();
    this.generateMockStations();
  }

  /**
   * Checks that our mock stations are initialized for use.
   *
   * @return boolean are mock stations empty
   */
  public boolean mockStationsAreInitialized() {
    return !mockStations.isEmpty();
  }

  /**
   * Generates our mock station list with variability. Called initially.
   */
  private void generateMockStations() {
    // Create Mock Stations from station list
    var i = 0;
    for (Station station : this.stations) {
      final var newStation = new MockStationSoh(
        station,
        i * (1.0 / this.stations.size()),
        2000,
        50.0,
        3000,
        50.0
      );
      this.mockStations.add(newStation);
      updateStation(newStation);
      i++;
    }
  }

  /**
   * Returns a new list of station sohs with random values.
   *
   * @return updatedList
   */
  public Set<StationSoh> getUpdatedStations() {
    final Set<StationSoh> updatedList = new HashSet<>();
    for (MockStationSoh mockStation : this.mockStations) {
      updatedList.add(updateStation(mockStation));
    }
    return updatedList;
  }

  /**
   * Updates the {@link MockStationSoh}} data and attempts to send the station to the Kafka
   * producer
   *
   * @param mockStation the station to update
   */
  private StationSoh updateStation(MockStationSoh mockStation) {

    mockStation.update();
    return createStationSohAtCurrentTime(mockStation);
  }

  /**
   * Creates a new {@link StationSoh} with a status set randomly set to {@link SohStatus#GOOD},
   * {@link SohStatus#MARGINAL} or {@link SohStatus#BAD}for {@link SohMonitorType#LAG} on all
   * its channels at the current time.
   *
   * @param mockStationSoh the station soh details for the mock producer
   * @return New StationSoh with random SohStatus at the current time.
   */
  private StationSoh createStationSohAtCurrentTime(MockStationSoh mockStationSoh) {

    final var station = mockStationSoh.getStation();

    final Set<Channel> channels = new HashSet<>(station.getChannels());
    final Set<String> channelNames = channels.stream().map(Channel::getName)
      .collect(Collectors.toSet());

    Duration lagDuration =
      mockStationSoh.getLag() != -1 ? Duration.ofMillis(mockStationSoh.getLag()) : null;
    SohMonitorValueAndStatus<Duration> lagSohMonitorValueAndStatus =
      DurationSohMonitorValueAndStatus.from(
        lagDuration,
        MockStationSoh.getStatusForSohMonitorValue(SohMonitorType.LAG,
          mockStationSoh.getLag()),
        SohMonitorType.LAG
      );
    Duration timelinessDuration =

      mockStationSoh.getLag() != -1 ? Duration.ofMillis(mockStationSoh.getTimeliness()) : null;
    SohMonitorValueAndStatus<Duration> timelinessSohMonitorValueAndStatus =
      DurationSohMonitorValueAndStatus.from(
        timelinessDuration,
        MockStationSoh.getStatusForSohMonitorValue(SohMonitorType.TIMELINESS,
          mockStationSoh.getTimeliness()),
        SohMonitorType.TIMELINESS
      );

    SohMonitorValueAndStatus<Double> missingSohMonitorValueAndStatus =
      PercentSohMonitorValueAndStatus.from(
        mockStationSoh.getMissing(),
        MockStationSoh.getStatusForSohMonitorValue(SohMonitorType.MISSING,
          mockStationSoh.getMissing()),
        SohMonitorType.MISSING
      );
    Stream<SohMonitorValueAndStatus<?>> envStatuses = this.createStationEnvironmentStatuses();
    Set<SohMonitorValueAndStatus<?>> sohMonitorValueAndStatuses = new HashSet<>();
    sohMonitorValueAndStatuses.add(lagSohMonitorValueAndStatus);
    sohMonitorValueAndStatuses.add(missingSohMonitorValueAndStatus);
    sohMonitorValueAndStatuses.add(timelinessSohMonitorValueAndStatus);
    envStatuses.forEach(sohMonitorValueAndStatuses::add);

    final long maxLag = mockStationSoh.getLag();
    final long maxTimeliness = mockStationSoh.getTimeliness();
    final double maxMissing = mockStationSoh.getMissing();
    final double maxEnvironmentalIssue = mockStationSoh.getMissing();

    // Create Station Aggregates
    Set<StationAggregate<?>> stationAggregates = new HashSet<>();
    stationAggregates.add(DurationStationAggregate.from(
      Duration.ofMillis(modifyLagTimeliness(maxLag, this.random.nextDouble())),
      StationAggregateType.LAG));
    stationAggregates.add(DurationStationAggregate.from(
      Duration.ofMillis(modifyLagTimeliness(maxTimeliness, this.random.nextDouble())),
      StationAggregateType.TIMELINESS));
    stationAggregates.add(PercentStationAggregate.from(
      modifyMissingEnviron(maxMissing, this.random.nextDouble()),
      StationAggregateType.MISSING));
    stationAggregates.add(PercentStationAggregate.from(
      modifyMissingEnviron(maxEnvironmentalIssue, this.random.nextDouble()),
      StationAggregateType.ENVIRONMENTAL_ISSUES));

    // streams.concat with infinitely limited stream and our stream of 'good; channel's
    SohStatus envStatusRollup = calculateEnvStatusRollup(
      sohMonitorValueAndStatuses,
      mockStationSoh.getContributingMonitorTypes()
    );

    Set<ChannelSoh> channelSohs = Streams.mapWithIndex(channelNames.stream(),
      (channel, index) -> buildChannelSoh(channel,
        modifyLagTimeliness(maxLag, index != 0 ? this.random.nextDouble() : 0),
        modifyMissingEnviron(maxMissing, index != 0 ? this.random.nextDouble() : 0),
        modifyLagTimeliness(maxTimeliness, index != 0 ? this.random.nextDouble() : 0),
        envStatusRollup)).collect(Collectors.toSet());

    var stationSoh = StationSoh.create(
      Instant.now(),
      station.getName(),
      sohMonitorValueAndStatuses,
      envStatusRollup,
      channelSohs,
      stationAggregates
    );
    // Set the new UUID back on the MockStation to keep in sync (used by Capability Rollup)
    mockStationSoh.setId(stationSoh.getId());
    return stationSoh;
  }

  /**
   * Calculates GOOD, BAD, or MARGINAL environmental rollup status.
   *
   * @param sohMonitorValueAndStatuses
   * @param monitorsInRollup
   * @return GOOD, BAD, or MARGINAL, whichever is worst of rollup
   */
  private SohStatus calculateEnvStatusRollup(
    Set<SohMonitorValueAndStatus<?>> sohMonitorValueAndStatuses,
    Set<SohMonitorType> monitorsInRollup) {
    SohStatus worstEnvStatus = SohStatus.GOOD;
    for (SohMonitorValueAndStatus<?> s : sohMonitorValueAndStatuses) {
      if (monitorsInRollup.contains(s.getMonitorType())) {
        SohStatus thisEnvStatus = s.getStatus();
        if (thisEnvStatus == SohStatus.BAD || worstEnvStatus == SohStatus.BAD) {
          worstEnvStatus = SohStatus.BAD;
        } else if (worstEnvStatus == SohStatus.GOOD) {
          worstEnvStatus = thisEnvStatus;
        } else {
          worstEnvStatus = SohStatus.MARGINAL;
        }
      }
    }
    return worstEnvStatus;
  }

  /**
   * Creates randomized environmental status for each monitor type of a channel.
   * Some forced good values.
   *
   * @return Stream of SohMonitorValueAndStatus'
   */
  private Stream<SohMonitorValueAndStatus<Double>> createChannelEnvironmentStatuses() {
    ArrayList<SohMonitorType> forcedGoodMonitors = new ArrayList<>();
    forcedGoodMonitors.add(SohMonitorType.ENV_BEGINNING_DATE_OUTAGE);
    forcedGoodMonitors.add(SohMonitorType.ENV_MAXIMUM_DATA_TIME);
    forcedGoodMonitors.add(SohMonitorType.ENV_CLOCK_DIFFERENTIAL_TOO_LARGE);

    ArrayList<SohMonitorValueAndStatus<Double>> environmentStatus = new ArrayList<>();
    var rand = new SecureRandom();
    var randomStatus = rand.nextDouble();
    boolean isGood = randomStatus < .7;
    boolean isMarginal = randomStatus >= .7 && randomStatus < .9;

    Arrays.stream(SohMonitorType.values()).forEach(sohMonitor -> {
      if (sohMonitor.isEnvironmentIssue() && (sohMonitor.getSohValueType()
        != SohValueType.INVALID)) {
        boolean isForcedGoodMonitor = forcedGoodMonitors.contains(sohMonitor);
        double randomEnv =
          getRandomEnv(isGood, isMarginal, isForcedGoodMonitor);

        SohMonitorValueAndStatus<Double> mvs = PercentSohMonitorValueAndStatus
          .from(
            randomEnv,
            MockStationSoh.getStatusForSohMonitorValue(sohMonitor, randomEnv),
            sohMonitor
          );
        environmentStatus.add((mvs));
      }
    });
    return environmentStatus.stream();
  }

  /**
   * Gets a random environmental value based on whether status is GOOD or MARGINAL and if it is a forced good value.
   *
   * @param isGood
   * @param isMarginal
   * @param isForcedGoodMonitor
   * @return random value
   */
  private double getRandomEnv(boolean isGood, boolean isMarginal, boolean isForcedGoodMonitor) {
    double baseVal;
    if (isGood) {
      baseVal = 68;
    } else if (isMarginal) {
      baseVal = 19;
    } else {
      baseVal = 50;
    }

    double modifier;
    if (isGood || isForcedGoodMonitor) {
      modifier = 0;
    } else if (isMarginal) {
      modifier = 70;
    } else {
      modifier = 50;
    }
    return new SecureRandom().nextDouble() * baseVal + modifier;
  }

  /**
   * Creates random environmental statuses for a set of a station's channels.
   *
   * @return stream of SohMonitorValueAndStatus
   */
  private Stream<SohMonitorValueAndStatus<?>> createStationEnvironmentStatuses() {
    ArrayList<SohMonitorValueAndStatus<?>> environmentStatus = new ArrayList<>();
    Arrays.stream(SohMonitorType.values()).forEach(sohMonitor -> {
      if (sohMonitor.isEnvironmentIssue() && (sohMonitor.getSohValueType()
        != SohMonitorType.SohValueType.INVALID)) {
        Double randomEnv = weightedRandom(0, 100, 10);
        SohMonitorValueAndStatus<?> mvs =
          PercentSohMonitorValueAndStatus.from(
            randomEnv,
            MockStationSoh.getStatusForSohMonitorValue(sohMonitor, randomEnv),
            sohMonitor
          );
        environmentStatus.add((mvs));
      }
    });
    return environmentStatus.stream();
  }

  /**
   * Builds a channelSoh to be added to a StationSoh's ChannelSoh list. Every channel has a
   * lag, missing value, and list of environmental status.
   *
   * @param channelName
   * @param lag
   * @param missing
   * @param sohMonitorStatusRollup
   * @return ChannelSoh
   */
  private ChannelSoh buildChannelSoh(String channelName, long lag, double missing, long timeliness,
    SohStatus sohMonitorStatusRollup) {

    SohMonitorValueAndStatus<?> lagMonitorValueAndStatus = DurationSohMonitorValueAndStatus
      .from(
        Duration.ofMillis(lag),
        MockStationSoh.getStatusForSohMonitorValue(SohMonitorType.LAG, lag),
        SohMonitorType.LAG
      );

    SohMonitorValueAndStatus<?> timelinessMonitorValueAndStatus = DurationSohMonitorValueAndStatus
      .from(
        Duration.ofMillis(timeliness),
        MockStationSoh.getStatusForSohMonitorValue(SohMonitorType.TIMELINESS, lag),
        SohMonitorType.TIMELINESS
      );
    SohMonitorValueAndStatus<?> missingMonitorValueAndStatus = PercentSohMonitorValueAndStatus
      .from(
        missing,
        MockStationSoh.getStatusForSohMonitorValue(SohMonitorType.MISSING, missing),
        SohMonitorType.MISSING
      );
    Stream<SohMonitorValueAndStatus<Double>> environmentStatus = this
      .createChannelEnvironmentStatuses();

    Set<SohMonitorValueAndStatus<?>> sohMonitorValueAndStatuses = new HashSet<>();
    sohMonitorValueAndStatuses.add(lagMonitorValueAndStatus);
    sohMonitorValueAndStatuses.add(missingMonitorValueAndStatus);
    sohMonitorValueAndStatuses.add(timelinessMonitorValueAndStatus);
    environmentStatus.forEach(sohMonitorValueAndStatuses::add);

    return ChannelSoh.from(
      channelName,
      sohMonitorStatusRollup,
      sohMonitorValueAndStatuses
    );
  }

  private static double modifyMissingEnviron(double maxMissing, double modifierPercentage) {
    return maxMissing - (modifierPercentage * maxMissing);
  }

  private static long modifyLagTimeliness(long maxLag, double modifierPercentage) {
    return maxLag - (long) (modifierPercentage * maxLag);
  }

  /**
   * a random number to the power provided, to scale it.
   *
   * @param min the lower bound of the range to generate. Must be greater than or equal to 0.
   * @param max the upper bound of the range to generate. Must be greater than 0.
   * @param bias a double greater than 0. Numbers above 1 scale it to the lower end. Numbers below 1
   * scale it to the higher end.
   * @return a double between min and max, weighted by the bias.
   */
  public static double weightedRandom(double min, double max, double bias) {
    Validate.isTrue(bias > 0.0, "weightedRandom requires a bias between greater than 0");
    Validate.isTrue(min >= 0 && max > 0,
      "weightedRandom expects a values greater than or equal to zero for min and max.");
    var rand = new SecureRandom();
    var seed = rand.nextDouble();
    double weightedSeed = Math.pow(seed, bias);
    return (max - min) * weightedSeed + min;
  }
}
