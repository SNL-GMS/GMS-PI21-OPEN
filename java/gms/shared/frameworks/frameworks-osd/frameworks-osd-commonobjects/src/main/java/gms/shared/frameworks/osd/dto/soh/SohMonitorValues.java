package gms.shared.frameworks.osd.dto.soh;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType.SohValueType;

/**
 * Contains the computed SOH monitor values for a single Channel and SOH value type.
 * <p>
 * Implementations must have their value collections ordered by time, as they must correlate with
 * the {@link HistoricalStationSoh}'s calculation time array
 */
@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = As.EXISTING_PROPERTY,
  property = "type")
@JsonSubTypes({
  @Type(value = DurationSohMonitorValues.class, name = "DURATION"),
  @Type(value = PercentSohMonitorValues.class, name = "PERCENT")
})
public interface SohMonitorValues {

  /**
   * The SOH type of the associated values
   */
  SohValueType getType();

  /**
   * The size of the container that values are stored in
   */
  int size();
}