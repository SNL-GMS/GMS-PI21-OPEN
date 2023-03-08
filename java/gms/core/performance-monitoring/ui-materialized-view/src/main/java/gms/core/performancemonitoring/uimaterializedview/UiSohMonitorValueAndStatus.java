package gms.core.performancemonitoring.uimaterializedview;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import gms.shared.frameworks.osd.coi.soh.DurationSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.PercentSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType.SohValueType;
import gms.shared.frameworks.osd.coi.soh.SohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.SohStatus;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Represents a generic monitored value and status.
 */

@AutoValue
public abstract class UiSohMonitorValueAndStatus {

  private static final Map<SohValueType, Function<SohMonitorValueAndStatus<?>, Double>> VALUE_EXTRACTORS =
    Map.of(SohValueType.PERCENT,
      smvs -> ((PercentSohMonitorValueAndStatus) smvs).getValue().orElse(-1.0),
      SohValueType.DURATION,
      smvs -> ((DurationSohMonitorValueAndStatus) smvs).getValue().map(duration -> duration.toMillis() / 1000.0).orElse(-1.0));

  /**
   * @return Monitored value
   */
  public abstract Double getValue();

  /**
   * is value present (was the value set in the SohMonitorValueAndStatus)
   */
  public abstract boolean getValuePresent();

  /**
   * @return monitored status
   */
  public abstract SohStatus getStatus();

  /**
   * @return monitor type
   */
  public abstract SohMonitorType getMonitorType();

  /**
   * @return bool has unacknowledged changes
   */
  public abstract boolean getHasUnacknowledgedChanges();

  /**
   * @return threshold for marginal values in percentage
   */
  public abstract double getThresholdMarginal();

  /**
   * @return threshold for bad values in percentage
   */
  public abstract double getThresholdBad();

  /**
   * @return quiet until in millisecond
   */
  public abstract long getQuietUntilMs();

  /**
   * @return quiet length in millisecond
   */
  public abstract long getQuietDurationMs();

  /**
   * @return is this channel contributing to station rollup
   */
  public abstract boolean isContributing();

  /**
   * Creates an UiSohMonitorValueAndStatus
   *
   * @param value
   * @param valuePresent
   * @param status
   * @param monitorType
   * @param hasUnacknowledgedChanges
   * @param thresholdMarginal
   * @param thresholdBad
   * @param quietUntilMs
   * @param quietDurationMs
   * @param contributing
   * @return
   */
  @JsonCreator
  public static UiSohMonitorValueAndStatus create(
    @JsonProperty("value") Double value,
    @JsonProperty("valuePresent") boolean valuePresent,
    @JsonProperty("status") SohStatus status,
    @JsonProperty("monitorType") SohMonitorType monitorType,
    @JsonProperty("hasUnacknowledgedChanges") boolean hasUnacknowledgedChanges,
    @JsonProperty("thresholdMarginal") double thresholdMarginal,
    @JsonProperty("thresholdBad") double thresholdBad,
    @JsonProperty("quietUntilMs") long quietUntilMs,
    @JsonProperty("quietDurationMs") long quietDurationMs,
    @JsonProperty("contributing") boolean contributing
  ) {

    return new AutoValue_UiSohMonitorValueAndStatus(
      value,
      valuePresent,
      status,
      monitorType,
      hasUnacknowledgedChanges,
      thresholdMarginal,
      thresholdBad,
      quietUntilMs,
      quietDurationMs,
      contributing
    );
  }

  public static UiSohMonitorValueAndStatus from(
    SohMonitorValueAndStatus<?> smvs,
    Optional<QuietedSohStatusChangeUpdate> quietedChange,
    boolean hasUnacknowledgedChanges,
    double thresholdMarginal,
    double thresholdBad,
    boolean contributing) {

    Objects.requireNonNull(smvs);
    quietedChange.ifPresent(change ->
      Preconditions.checkState(change.getSohMonitorType() == smvs.getMonitorType()));

    Double value = UiMaterializedViewUtility.setDecimalPrecisionAsNumber(
      VALUE_EXTRACTORS.get(smvs.getMonitorType().getSohValueType()).apply(smvs), 2);

    return new AutoValue_UiSohMonitorValueAndStatus(
      value,
      value != -1.0,
      smvs.getStatus(),
      smvs.getMonitorType(),
      hasUnacknowledgedChanges,
      thresholdMarginal,
      thresholdBad,
      quietedChange.map(change -> change.getQuietUntil().toEpochMilli()).orElse((long) -1),
      quietedChange.map(change -> change.getQuietDuration().toMillis()).orElse((long) 0),
      contributing);
  }
}
