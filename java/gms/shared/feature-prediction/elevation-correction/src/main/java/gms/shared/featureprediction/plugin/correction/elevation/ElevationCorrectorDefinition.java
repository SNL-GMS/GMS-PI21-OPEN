package gms.shared.featureprediction.plugin.correction.elevation;

import com.google.auto.value.AutoValue;
import java.util.Map;

@AutoValue
public abstract class ElevationCorrectorDefinition {

  public abstract Map<String, String> getMediumVelocityEarthModelPluginNameByModelNameMap();

  public String getPluginNameForEarthModel(String earthModel) {
    return getMediumVelocityEarthModelPluginNameByModelNameMap().get(earthModel);
  }

  public static ElevationCorrectorDefinition from(
    Map<String, String> mediumVelocityEarthModelPluginNameByModelNameMap
  ) {

    return new AutoValue_ElevationCorrectorDefinition(
      mediumVelocityEarthModelPluginNameByModelNameMap
    );
  }
}
