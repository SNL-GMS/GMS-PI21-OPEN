package gms.shared.signalenhancementconfiguration.coi.filter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;

import java.util.Optional;

@AutoValue
@JsonSerialize(as = FilterDefinition.class)
public abstract class FilterDefinition {
  public abstract String getName();

  public abstract Optional<String> getComments();

  public abstract FilterDescription getFilterDescription();

  @JsonCreator
  public static FilterDefinition from(
    @JsonProperty("name") String name,
    @JsonProperty("comments") Optional<String> comments,
    @JsonProperty("filterDescription") FilterDescription filterDescription) {


    return new AutoValue_FilterDefinition(name, comments, filterDescription);
  }
}
