package gms.shared.signalenhancementconfiguration.coi.filter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.util.Collection;

@AutoValue
@JsonSerialize(as = FilterConfiguration.class)
public abstract class FilterConfiguration {

  public abstract ImmutableList<FilterDefinition> getFilterDefinitions();

  @JsonCreator
  public static FilterConfiguration from(
    @JsonProperty("filterDefinitions") Collection<FilterDefinition> filterDefinitions) {

    return new AutoValue_FilterConfiguration(ImmutableList.copyOf(filterDefinitions));
  }
}
