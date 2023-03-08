package gms.testtools.simulators.bridgeddatasourceintervalsimulator;

import com.google.auto.value.AutoValue;

import java.time.Duration;
import java.time.Instant;

/**
 * Wrapper for the seed information needed by the interval simulator.
 */
@AutoValue
abstract class SeedMetadata {

  public abstract Instant getAdjustedSeedDataStartTime();

  public abstract Instant getAdjustedSeedDataEndTime();

  public abstract Duration getAdjustedSeedDuration();

  public abstract int getNumberForwardSeeds();

  public abstract int getNumberBackwardSeeds();

  public static SeedMetadata create(Instant adjustedSeedDataStartTime, Instant adjustedSeedDataEndTime,
    Duration adjustedSeedDuration, int numberForwardSeeds, int numberBackwardSeeds) {

    return new AutoValue_SeedMetadata(
      adjustedSeedDataStartTime,
      adjustedSeedDataEndTime,
      adjustedSeedDuration,
      numberForwardSeeds,
      numberBackwardSeeds
    );
  }

}
