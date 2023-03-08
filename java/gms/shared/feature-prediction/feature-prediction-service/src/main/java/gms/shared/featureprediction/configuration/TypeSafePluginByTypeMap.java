package gms.shared.featureprediction.configuration;

import gms.shared.event.coi.featureprediction.type.FeaturePredictionType;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * This is meant to be a heterogeneous container, a-la Effective Java Generics item 33:
 * https://www.informit.com/articles/article.aspx?p=2861454&seqNum=8
 * <p>
 * The idea is to effectively hide the wildcard in the map, so that we are not returning types
 * with wildcards, which is a code smell that will be flagged by SonarQube.
 */
public class TypeSafePluginByTypeMap {

  private final Map<FeaturePredictionType<?>, String> map;

  public TypeSafePluginByTypeMap(
    Map<FeaturePredictionType<?>, String> map) {
    this.map = map;
  }

  public String getPluginNameForFeatureMeasurement(
    FeaturePredictionType<?> featureMeasurementType) {
    return map.get(featureMeasurementType);
  }

  public Collection<String> getPluginNames() {
    return map.values();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TypeSafePluginByTypeMap)) {
      return false;
    }
    TypeSafePluginByTypeMap that = (TypeSafePluginByTypeMap) o;
    return Objects.equals(map, that.map);
  }

  @Override
  public int hashCode() {
    return Objects.hash(map);
  }
}
