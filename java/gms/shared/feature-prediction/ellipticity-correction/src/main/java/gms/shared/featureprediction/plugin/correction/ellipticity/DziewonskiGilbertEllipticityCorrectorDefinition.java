package gms.shared.featureprediction.plugin.correction.ellipticity;

import com.google.auto.value.AutoValue;
import java.util.Map;

@AutoValue
public abstract class DziewonskiGilbertEllipticityCorrectorDefinition {

  public abstract Map<String, String> getCorrectionModelPluginNameByModelNameMap();

  public static DziewonskiGilbertEllipticityCorrectorDefinition from(
    Map<String, String> correctionModelPluginNameByModelNameMap
  ) {

    return new AutoValue_DziewonskiGilbertEllipticityCorrectorDefinition(
      correctionModelPluginNameByModelNameMap
    );
  }
}
