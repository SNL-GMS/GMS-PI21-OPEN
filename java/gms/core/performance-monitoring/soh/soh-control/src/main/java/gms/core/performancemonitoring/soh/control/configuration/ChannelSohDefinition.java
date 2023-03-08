package gms.core.performancemonitoring.soh.control.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import org.apache.commons.lang3.Validate;

import java.util.Map;
import java.util.Set;

/**
 * Defines SOH thresholds for ChannelSOH.
 */
@AutoValue
public abstract class ChannelSohDefinition {

  /**
   * @return the name of the channel whose threshold we are configuring.
   */
  public abstract String getChannelName();

  /**
   * @return Set of monitor types that we want in the rollup
   */
  public abstract Set<SohMonitorType> getSohMonitorTypesForRollup();

  /**
   * @return Map of monitor type to the threshold definition for that monitor type.
   */
  // SonarQube: it is not happy with this wild card (S1452) or with out it (S3740)
  public abstract Map<SohMonitorType, SohMonitorStatusThresholdDefinition<?>> getSohMonitorStatusThresholdDefinitionsBySohMonitorType();

  /**
   * @return The particular channels NominalSampleRateHz
   */
  public abstract double getNominalSampleRateHz();


  /**
   * Create a new ChannelSohDefinition
   *
   * @param channelName the channel name
   * @param sohMonitorTypesForRollup the Set of {@link SohMonitorType}s
   * @param sohMonitorStatusThresholdDefinitionsBySohMonitorType monitor type/threshold map
   * @return new ChannelSohDefinition object
   */
  @JsonCreator
  public static ChannelSohDefinition create(
    @JsonProperty("channelName") String channelName,
    @JsonProperty("sohMonitorTypesForRollup") Set<SohMonitorType> sohMonitorTypesForRollup,
    @JsonProperty("sohMonitorStatusThresholdDefinitionsBySohMonitorType") Map<SohMonitorType, SohMonitorStatusThresholdDefinition<?>> sohMonitorStatusThresholdDefinitionsBySohMonitorType,
    @JsonProperty("nominalSampleRateHz") double nominalSampleRateHz
  ) {

    validateParameters(
      channelName,
      sohMonitorTypesForRollup,
      sohMonitorStatusThresholdDefinitionsBySohMonitorType
    );

    return new AutoValue_ChannelSohDefinition(
      channelName,
      sohMonitorTypesForRollup,
      sohMonitorStatusThresholdDefinitionsBySohMonitorType,
      nominalSampleRateHz
    );
  }

  private static void validateParameters(
    String channelName,
    Set<SohMonitorType> sohMonitorTypesForRollup,
    Map<SohMonitorType, SohMonitorStatusThresholdDefinition<?>> sohMonitorStatusThresholdDefinitionsBySohMonitorType
  ) {
    Validate.notBlank(channelName, "Channel name can't be blank");

    Validate.isTrue(
      sohMonitorStatusThresholdDefinitionsBySohMonitorType.keySet()
        .containsAll(sohMonitorTypesForRollup),
      "Monitor types for rollup need to be associated with an SohMonitorStatusThresholdDefinition"
    );
  }
}
