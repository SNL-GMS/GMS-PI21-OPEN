package gms.shared.stationdefinition.coi.facets;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

/**
 * Faceting definition class. This is a recursive structure where:
 * <p>
 * 1. Each faceting definition defines a class with faceted attributes
 * and a map of attribute names to {@link FacetingDefinition  }
 * <p>
 * 2. Each {@link } has:
 * <p>
 * a. A boolean indicating whether the attribute should be a "reference-only" instance or a
 * populated instances.
 * <p>
 * b. If the attribute is a populated instance and it is also faceted, a {@link FacetingDefinition}
 * describing how to populate that instance.
 * <p>
 * This supports defining how to populate nested structures of faceted objects where each faceted
 * object has one or more faceted attributes.
 */
@AutoValue
@JsonSerialize(as = FacetingDefinition.class)
@JsonDeserialize(builder = AutoValue_FacetingDefinition.Builder.class)
public abstract class FacetingDefinition {

  public abstract boolean isPopulated();

  public abstract String getClassType();

  public abstract ImmutableMap<String, FacetingDefinition> getFacetingDefinitions();

  public FacetingDefinition getFacetingDefinitionByName(String attributeName) {
    return getFacetingDefinitions() != null ? getFacetingDefinitions().get(attributeName) : null;
  }

  public static Builder builder() {
    return new AutoValue_FacetingDefinition.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {


    Builder setPopulated(boolean isPopulated);

    Builder setClassType(String classType);

    Builder setFacetingDefinitions(Map<String, FacetingDefinition> facetingDefinitions);

    ImmutableMap.Builder<String, FacetingDefinition> facetingDefinitionsBuilder();

    default Builder addFacetingDefinitions(String key, FacetingDefinition facetingDefinition) {
      facetingDefinitionsBuilder().put(key, facetingDefinition);
      return this;
    }

    FacetingDefinition autoBuild();

    default FacetingDefinition build() {
      FacetingDefinition facetingDefinition = autoBuild();

      if (!facetingDefinition.isPopulated()) {
        checkState(facetingDefinition.getFacetingDefinitions().isEmpty());
      }

      return facetingDefinition;
    }
  }
}