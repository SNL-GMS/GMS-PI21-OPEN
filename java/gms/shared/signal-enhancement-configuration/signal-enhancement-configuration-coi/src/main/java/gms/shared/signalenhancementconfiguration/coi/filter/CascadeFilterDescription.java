package gms.shared.signalenhancementconfiguration.coi.filter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import gms.shared.signalenhancementconfiguration.coi.types.FilterType;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

@AutoValue
@JsonTypeName("CascadeFilterDescription")
public abstract class CascadeFilterDescription implements FilterDescription {
  public abstract ImmutableList<FilterDescription> getFilterDescriptions();

  public abstract Optional<CascadeFilterParameters> getParameters();

  @Override
  public FilterType getFilterType() {
    return FilterType.CASCADE;
  }

  @JsonCreator
  public static CascadeFilterDescription from(
    @JsonProperty("comments") Optional<String> comments,
    @JsonProperty("filterDescriptions") ImmutableList<FilterDescription> filterDescriptions,
    @JsonProperty("parameters") Optional<CascadeFilterParameters> parameters) {

    checkArgument(filterDescriptions.size() > 1,
      "List of filter descriptions must be greater than one");

    boolean isCausal = isGroupCausal(filterDescriptions);

    return new AutoValue_CascadeFilterDescription(comments, isCausal, filterDescriptions, parameters);
  }

  private static boolean isGroupCausal(List<FilterDescription> filterDescriptions) {
    return filterDescriptions.stream().allMatch(FilterDescription::isCausal);
  }
}
