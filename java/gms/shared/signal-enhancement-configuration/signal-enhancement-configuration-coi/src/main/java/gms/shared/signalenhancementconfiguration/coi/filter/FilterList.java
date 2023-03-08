package gms.shared.signalenhancementconfiguration.coi.filter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkArgument;

@AutoValue
public abstract class FilterList {
  public abstract String getName();

  public abstract int getDefaultFilterIndex();

  public abstract ImmutableList<FilterListEntry> getFilters();


  @JsonCreator
  public static FilterList from(
    @JsonProperty("name") String name,
    @JsonProperty("defaultFilterIndex") int defaultFilterIndex,
    @JsonProperty("filters") ImmutableList<FilterListEntry> filters) {

    checkArgument(!filters.isEmpty(), "The filter list must contain at list one entry");

    return new AutoValue_FilterList(name, defaultFilterIndex, ImmutableList.copyOf(filters));
  }
}
