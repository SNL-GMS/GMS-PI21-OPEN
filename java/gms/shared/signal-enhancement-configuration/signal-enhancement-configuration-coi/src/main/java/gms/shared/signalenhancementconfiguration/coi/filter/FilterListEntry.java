package gms.shared.signalenhancementconfiguration.coi.filter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.signalenhancementconfiguration.coi.types.FilterDefinitionUsage;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

@AutoValue
public abstract class FilterListEntry {
  public abstract boolean getWithinHotKeyCycle();

  public abstract Optional<Boolean> getUnfiltered();

  public abstract Optional<FilterDefinitionUsage> getNamedFilter();

  public abstract Optional<FilterDefinition> getFilterDefinition();

  @JsonCreator
  public static FilterListEntry from(
    @JsonProperty("withinHotKeyCycle") boolean withinHotKeyCycle,
    @JsonProperty("unfiltered") Optional<Boolean> unfiltered,
    @JsonProperty("namedFilter") Optional<FilterDefinitionUsage> namedFilter,
    @JsonProperty("filterDefinition") Optional<FilterDefinition> filterDefinition) {

    checkArgument(unfiltered.isPresent() || namedFilter.isPresent() || filterDefinition.isPresent(),
      "All filter entries are empty. You must populate exactly one filter entry");

    checkArgument(unfiltered.isEmpty() && namedFilter.isEmpty() ||
        unfiltered.isEmpty() && filterDefinition.isEmpty() ||
        namedFilter.isEmpty() && filterDefinition.isEmpty(),
      "Only one filter entry must be populated at all times");

    return new AutoValue_FilterListEntry(withinHotKeyCycle, unfiltered, namedFilter, filterDefinition);
  }
}
