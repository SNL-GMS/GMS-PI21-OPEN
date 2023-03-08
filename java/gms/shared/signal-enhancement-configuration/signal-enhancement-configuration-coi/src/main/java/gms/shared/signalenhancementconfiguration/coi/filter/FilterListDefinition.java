package gms.shared.signalenhancementconfiguration.coi.filter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.util.Collection;

@AutoValue
public abstract class FilterListDefinition {

  public abstract ImmutableList<WorkflowDefinitionIdStringPair> getPreferredFilterListByActivity();

  public abstract ImmutableList<FilterList> getFilterLists();

  @JsonCreator
  public static FilterListDefinition from(
    @JsonProperty("preferredFilterListByActivity") Collection<WorkflowDefinitionIdStringPair> preferredFilterListByActivity,
    @JsonProperty("filterLists") Collection<FilterList> filterLists) {

    return new AutoValue_FilterListDefinition(ImmutableList.copyOf(preferredFilterListByActivity),
      ImmutableList.copyOf(filterLists));
  }
}
