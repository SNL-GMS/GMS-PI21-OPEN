package gms.shared.frameworks.osd.coi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class RejectedInput<T> {

  public abstract T getInput();

  public abstract String getRationale();

  @JsonCreator
  public static <T> RejectedInput<T> create(@JsonProperty("input") T input,
    @JsonProperty("rationale") String rationale) {
    return new AutoValue_RejectedInput(input, rationale);
  }

}
