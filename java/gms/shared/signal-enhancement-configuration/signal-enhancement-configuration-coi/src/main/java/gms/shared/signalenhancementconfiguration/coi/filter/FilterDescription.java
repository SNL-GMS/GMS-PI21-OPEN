package gms.shared.signalenhancementconfiguration.coi.filter;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gms.shared.signalenhancementconfiguration.coi.types.FilterType;

import java.util.Optional;

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  property = "type",
  visible = true
)
@JsonSubTypes({
  @JsonSubTypes.Type(value = CascadeFilterDescription.class, name = "CascadeFilterDescription"),
  @JsonSubTypes.Type(value = LinearFilterDescription.class, name = "LinearFilterDescription")
})
public interface FilterDescription {
  Optional<String> getComments();

  boolean isCausal();

  FilterType getFilterType();
}
