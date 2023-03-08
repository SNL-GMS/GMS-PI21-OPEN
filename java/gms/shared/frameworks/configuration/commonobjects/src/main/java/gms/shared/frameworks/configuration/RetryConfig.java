package gms.shared.frameworks.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import net.jodah.failsafe.RetryPolicy;

import java.time.temporal.ChronoUnit;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Configuration class for applying basic retry configuration to policies such as {@link RetryPolicy} and Project Reactor's Retry
 */
@AutoValue
public abstract class RetryConfig {
  public abstract long getInitialDelay();

  public abstract long getMaxDelay();

  public abstract ChronoUnit getDelayUnits();

  public abstract int getMaxAttempts();

  /**
   * {@link RetryConfig} Factory method
   *
   * @param initialDelay Initial delay before retrying
   * @param maxDelay Maximum delay duration for an individual retry
   * @param delayUnits {@link ChronoUnit} to scale the delays into a {@link java.time.Duration}
   * @param maxAttempts Maximum attempts to allow before failure
   * @return A {@link RetryConfig} to be used to constrain a retry policy
   */
  @JsonCreator
  public static RetryConfig create(
    @JsonProperty("initialDelay") long initialDelay,
    @JsonProperty("maxDelay") long maxDelay,
    @JsonProperty("delayUnits") ChronoUnit delayUnits,
    @JsonProperty("maxAttempts") int maxAttempts) {
    checkArgument(initialDelay > 0, "Initial delay must be greater than 0");
    checkArgument(maxDelay > initialDelay, "Initial delay must be less than max delay");
    checkArgument(maxAttempts != 0 && maxAttempts >= -1, "Max attempts must be greater than zero, or exactly -1 for no limit");
    return new AutoValue_RetryConfig(initialDelay, maxDelay, delayUnits, maxAttempts);
  }

  /**
   * Convenience method for producing a basic {@link RetryPolicy<T>}
   *
   * @param <T> result type for the returned {@link RetryPolicy}
   * @return A {@link RetryPolicy<T>} containing basic configuration provided by this {@link RetryConfig}
   */
  public <T> RetryPolicy<T> toBaseRetryPolicy() {
    return new RetryPolicy<T>()
      .withBackoff(getInitialDelay(), getMaxDelay(), getDelayUnits())
      .withMaxAttempts(getMaxAttempts());
  }
}
