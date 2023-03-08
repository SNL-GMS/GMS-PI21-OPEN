package gms.shared.featureprediction.configuration;

import com.google.auto.value.AutoValue;
import gms.shared.event.coi.featureprediction.type.FeaturePredictionType;
import java.util.Map;

@AutoValue
public abstract class FeaturePredictorDefinition {

  public abstract TypeSafePluginByTypeMap getPluginByPredictionTypeMap();

  public static FeaturePredictorDefinition from(
    Map<FeaturePredictionType<?>, String> pluginByPredictionTypeMap
  ) {

    return new AutoValue_FeaturePredictorDefinition(
      new TypeSafePluginByTypeMap(pluginByPredictionTypeMap)
    );
  }

  public String getPluginNameByType(FeaturePredictionType<?> featureMeasurementType) {
    return getPluginByPredictionTypeMap().getPluginNameForFeatureMeasurement(
      featureMeasurementType);
  }
}
