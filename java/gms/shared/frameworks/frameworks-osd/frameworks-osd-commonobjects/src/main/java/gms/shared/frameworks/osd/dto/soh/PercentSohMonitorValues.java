package gms.shared.frameworks.osd.dto.soh;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType.SohValueType;

/**
 * SOH monitor values that represent a percentage
 */
@AutoValue
public abstract class PercentSohMonitorValues implements SohMonitorValues {

  @Override
  public SohValueType getType() {
    return SohValueType.PERCENT;
  }

  @Override
  public int size() {
    return getValues().length;
  }

  /**
   * Percentage values ordered by time
   *
   * @return A double array of percentage values ordered by time
   */
  public abstract double[] getValues();

  @JsonCreator
  public static PercentSohMonitorValues create(
    @JsonProperty("values") double[] percents) {
    return new AutoValue_PercentSohMonitorValues(percents);
  }
}
