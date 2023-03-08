package gms.shared.frameworks.osd.dto.soh;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType.SohValueType;

/**
 * SOH monitor values that represent a duration of time
 */
@AutoValue
public abstract class DurationSohMonitorValues implements SohMonitorValues {

  @Override
  public SohValueType getType() {
    return SohValueType.DURATION;
  }

  @Override
  public int size() {
    return getValues().length;
  }

  /**
   * Duration values ordered by time, in epoch milliseconds
   *
   * @return A double array of duration values ordered by time
   */
  public abstract long[] getValues();

  @JsonCreator
  public static DurationSohMonitorValues create(
    @JsonProperty("values") long[] durations) {
    return new AutoValue_DurationSohMonitorValues(durations);
  }
}
