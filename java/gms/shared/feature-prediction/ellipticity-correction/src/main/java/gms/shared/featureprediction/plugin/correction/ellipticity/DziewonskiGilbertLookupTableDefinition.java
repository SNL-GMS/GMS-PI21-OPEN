package gms.shared.featureprediction.plugin.correction.ellipticity;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.auto.value.AutoValue;
import java.util.Map;

/**
 * Contains information about where to find Dziewonsky-Gilbert tables for
 * ellipticity correction using various earth models.
 */
@AutoValue
public abstract class DziewonskiGilbertLookupTableDefinition {

  /**
   * Returns a map from earth model to "data descriptor," which describes where
   * to find Dziewonsky-Gilbert tables for ellipticity correction using various
   * earth models.
   *
   * @return Map from earth model to "data descriptor," which describes where to
   * find Dziewonsky-Gilbert tables for ellipticity correction using various
   * earth models.
   */
  @JsonAnyGetter
  public abstract Map<String, String> getEarthModelToDataDescriptor();

  /**
   * Creates a returns a new DziewonskiGilbertLookupTableDefinition instance.
   *
   * @param earthModelToDataDescriptor Map from earth model to "data
   * descriptor," which describes where to find Dziewonsky-Gilbert tables for
   * ellipticity correction using various earth models.
   *
   * @return New DziewonskiGilbertLookupTableDefinition instance.
   */
  @JsonCreator
  public static DziewonskiGilbertLookupTableDefinition create(Map<String, String> earthModelToDataDescriptor) {
    return new AutoValue_DziewonskiGilbertLookupTableDefinition(earthModelToDataDescriptor);
  }
}
