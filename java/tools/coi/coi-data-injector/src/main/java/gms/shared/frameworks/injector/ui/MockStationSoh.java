package gms.shared.frameworks.injector.ui;

import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohStatus;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.Validate;

/**
 * MockStationSOH class Mocks changes to the SOH values for a station. This then
 * generates a {@link Station}. MockStationSohs randomly walk their values on
 * update, based on their variability.
 */
class MockStationSoh {

  private static int mockNumber = 0;

  private double variability;

  private UUID id;

  private SohStatus status;

  private long lag;

  private double missing;

  private long timeliness;

  private double environment;

  private long baseLag;

  private double baseMissing;

  private long baseTimeliness;

  private double baseEnvironment;

  private ArrayList<SohMonitorType> contributingMonitorTypes;

  private final Station station;

  private SecureRandom random = new SecureRandom();

  /**
   * getter for the {@link UUID} that the mock station updates
   *
   * @return the UUID
   */
  UUID getId() {
    return id;
  }

  /**
   * setter for the {@link UUID} that the mock station updates
   *
   * @param id the UUID to set
   */
  void setId(UUID id) {
    this.id = id;
  }

  /**
   * Get a Set of {@link SohMonitorType} that contribute to this station's rollup
   * status.
   *
   * @return a Set of the {@link SohMonitorType}s that contribut
   */
  Set<SohMonitorType> getContributingMonitorTypes() {
    return new HashSet<>(contributingMonitorTypes);
  }

  /**
   * getter for the {@link Station} that the mock station updates
   *
   * @return the Station
   */
  Station getStation() {
    return station;
  }

  /**
   * getter for the {@link SohStatus} rollup value
   *
   * @return the status rollup for the station
   */
  SohStatus getStatus() {
    return status;
  }

  /**
   * getter for the lag value
   *
   * @return the lag value
   */
  long getLag() {
    return lag;
  }

  /**
   * getter for the missing value
   *
   * @return the missing value
   */
  double getMissing() {
    return missing;
  }

  /**
   * getter for the timeliness value
   *
   * @return the timeliness value
   */
  long getTimeliness() {
    return timeliness;
  }

  /**
   * getter for the environment value
   *
   * @return the environment value
   */
  double getEnvironment() {
    return environment;
  }

  /**
   * Constructor generates a MockStationSoh object MockStationSOH stations
   * randomly walk a random set of monitor values. The more variable the station,
   * the more the values will change on update.
   * <p>
   * //@param fileName the base station file to use //@param stationName the
   * station name to use (overwrites the name in the file)
   *
   * @param variability a double between 0 and 1 that determines how dramatically
   * the station changes on update
   * @param lag the initial lag value to start with
   * @param missing the initial missing value to start with
   * @param environment the initial environment value to start with
   */
  MockStationSoh(Station station, double variability, long lag, double missing, long timeliness,
    double environment) {
    this.station = station;
    this.variability = variability;
    this.lag = lag;
    this.missing = missing;
    this.timeliness = timeliness;
    this.environment = environment;

    this.baseLag = (long) (this.lag * this.variability);
    this.baseMissing = this.missing * this.variability;
    this.baseTimeliness = (long) (this.timeliness * this.variability);
    this.baseEnvironment = this.environment * this.variability;
    final double contributingChance = 1 - (this.random.nextFloat() * this.variability * .5);
    this.contributingMonitorTypes = new ArrayList<>();
    if (this.random.nextFloat() < contributingChance) {
      this.contributingMonitorTypes.add(SohMonitorType.LAG);
    }
    if (this.random.nextFloat() < contributingChance) {
      this.contributingMonitorTypes.add(SohMonitorType.TIMELINESS);
    }
    if (this.random.nextFloat() < contributingChance) {
      this.contributingMonitorTypes.add(SohMonitorType.MISSING);
    }
    if (this.random.nextFloat() < contributingChance) {
      this.contributingMonitorTypes.add(SohMonitorType.TIMELINESS);
    }
    if (this.random.nextFloat() < contributingChance || this.contributingMonitorTypes.isEmpty()) {
      Arrays.stream(SohMonitorType.values())
        .filter(sohMonitorType -> sohMonitorType.isEnvironmentIssue()
          && (sohMonitorType.getSohValueType() != SohMonitorType.SohValueType.INVALID))
        .forEach(monitor -> this.contributingMonitorTypes.add(monitor));
    }
    update();
  }

  /**
   * Returns a {@link SohStatus} for the provided monitor type and value.
   *
   * @param sohMonitorType the {@link SohMonitorType} corresponding to the value
   * @param value the value of the monitor
   * @return the status for this value and monitor type
   */
  static SohStatus getStatusForSohMonitorValue(SohMonitorType sohMonitorType, Number value) {

    Validate.notNull(sohMonitorType, "null monitor type");

    final List<SohMonitorType> monitorTypes = Arrays.asList(SohMonitorType.values());
    Validate.isTrue(monitorTypes.contains(sohMonitorType));

    if (sohMonitorType == SohMonitorType.LAG || sohMonitorType == SohMonitorType.TIMELINESS) {
      if (value.longValue() == -1) {
        return SohStatus.MARGINAL;
      }
      if (value.longValue() > 7000) {
        return SohStatus.BAD;
      }
      if (value.longValue() > 3500) {
        return SohStatus.MARGINAL;
      }
      return SohStatus.GOOD;
    } else {
      if (value.doubleValue() > 90.0) {
        return SohStatus.BAD;
      }
      if (value.doubleValue() > 70.0) {
        return SohStatus.MARGINAL;
      }
      return SohStatus.GOOD;
    }
  }

  /**
   * Generates a number between 1 and 2, or -1 and -2, with a higher probability
   * of numbers farther from 0 depending on the variability. This multiplier is
   * used to randomly walk the values of the monitor types
   *
   * @return the double multiplier value
   */
  private double getMultiplier() {
    double multiplier = this.random.nextFloat() * this.variability + 1;
    if (this.random.nextFloat() > 0.5) {
      multiplier *= -1;
    }
    return multiplier;
  }

  /**
   * Updates this station, with each monitor type having a chance of updating. The
   * more variable the station, the more likely that each monitor type will
   * change, and the more drastically it will change.
   * <p>
   * For each monitor type that is changed, the underlying data undergoes a random
   * walk, moving some amount up or down, with more variable stations having a
   * higher range they can move.
   * <p>
   * Logs the values of the station's monitor types, and the status.
   */
  void update() {
    calculateNextLag();
    calculateNextMissing();
    calculateNextTimeliness();
    calculateNextEnv();

    this.status = SohStatus.GOOD;
    calculateStatus();

    // When lag is not available the status is always Marginal
    if (lag == -1) {
      this.status = SohStatus.MARGINAL;
    }
  }

  /**
   * This helper method calls the "checkForStatus" method with the parameters for each status.
   * This eliminates code duplication and complexity issues.
   */
  private void calculateStatus() {

    if (!checkForStatus(7000, 7000, 90.0, 90.0, SohStatus.BAD)) {
      checkForStatus(3500, 3500, 70.0, 70.0, SohStatus.MARGINAL);
    }
  }

  /**
   * This method applies the values and checks if the status should be changed and if so, does it and returns true.
   *
   * @param lagThresh - the threshold to make a status change if met or exceeded.
   * @param timelinessThresh - the threshold to make a status change if met or exceeded for timeliness.
   * @param missingThresh - the threshold to make a status change if met or exceeded for missing.
   * @param envThresh - the threshold to make a status change if met or exceeded for environments.
   * @param stat - the status to change to, if criteria are met.
   * @return Whether or not the status was changed.
   */
  private boolean checkForStatus(int lagThresh, int timelinessThresh, double missingThresh, double envThresh, SohStatus stat) {
    final boolean lagEnabled = this.contributingMonitorTypes.contains(SohMonitorType.LAG);
    final boolean missingEnabled = this.contributingMonitorTypes.contains(SohMonitorType.MISSING);
    final boolean timelinessEnabled = this.contributingMonitorTypes.contains(SohMonitorType.TIMELINESS);
    final boolean environmentEnabled = this.contributingMonitorTypes
      .contains(SohMonitorType.ENV_BACKUP_POWER_UNSTABLE);

    if (lagEnabled && this.lag > lagThresh || missingEnabled && this.missing > missingThresh
      || timelinessEnabled && this.timeliness > timelinessThresh
      || environmentEnabled && this.environment > envThresh) {
      this.status = stat;
      return true;
    }
    return false;
  }

  private void calculateNextLag() {
    if (this.random.nextFloat() > this.variability) {
      lag = Math.min(Math.max(0, this.lag + (long) (this.baseLag * getMultiplier())), 10000);

      // Set every 200th to unknown (value -1)
      if (this.random.nextFloat() > 0.95) {
        lag = -1;
      }
    }
  }

  private void calculateNextEnv() {
    if (this.random.nextFloat() > this.variability) {
      this.environment = Math.min(Math.max(0, this.environment + (this.baseEnvironment * getMultiplier())), 100);
    }
  }

  private void calculateNextTimeliness() {
    if (this.random.nextFloat() > this.variability) {
      timeliness = Math.min(Math.max(0, this.timeliness + (long) (this.baseTimeliness * getMultiplier())), 10000);

      // Set every 200th to unknown (value -1)
      if (this.random.nextFloat() > 0.95) {
        lag = -1;
      }
    }
  }

  private void calculateNextMissing() {
    if (this.random.nextFloat() > this.variability) {
      this.missing = Math.min(Math.max(0, this.missing + (this.baseMissing * getMultiplier())), 100);
    }
  }

  public static String generateMockStationName() {
    return "MOCK" + ++mockNumber;
  }
}
