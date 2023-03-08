package gms.shared.featureprediction.plugin.prediction;

import com.google.auto.value.AutoValue;
import gms.shared.event.coi.featureprediction.EllipticityCorrectionType;

import java.util.Map;

/**
 * Defines processing configuration for BicubicSplineFeaturePredictor
 */
@AutoValue
public abstract class BicubicSplineFeaturePredictorDefinition {

  public static Builder builder() {
    return new AutoValue_BicubicSplineFeaturePredictorDefinition.Builder();
  }

  /**
   * Gets the flag signifying if extrapolation will be done on mossing values
   *
   * @return A boolean indicating if extrapolation should occur
   */
  public abstract boolean getExtrapolate();

  public abstract Map<String, String> getTravelTimeDepthDistanceLookupTablePluginNameByEarthModel();

  public abstract Map<EllipticityCorrectionType, String> getEllipticityCorrectorPluginNameByEllipticityCorrectionPluginType();

  /**
   * Builder for constructing BicubicSplineFeaturePredictorDefinition instances
   */
  @AutoValue.Builder
  public abstract static class Builder {


    public abstract Builder setExtrapolate(boolean extrapolate);

    public abstract Builder setTravelTimeDepthDistanceLookupTablePluginNameByEarthModel(
      Map<String, String> plugins);

    public abstract Builder setEllipticityCorrectorPluginNameByEllipticityCorrectionPluginType(
      Map<EllipticityCorrectionType, String> corrections);

    /**
     * Creates a new BicubicSplineFeaturePredictorDefinition instance with attributes initialized by
     * the Builder
     *
     * @return A new BicubicSplineFeaturePredictorDefinition instance with attributes initialized by
     * the Builder
     */
    public abstract BicubicSplineFeaturePredictorDefinition build();

  }
}
