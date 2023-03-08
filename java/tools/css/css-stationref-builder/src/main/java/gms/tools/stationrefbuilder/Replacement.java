package gms.tools.stationrefbuilder;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;

import java.util.Optional;

@AutoValue
@JsonSerialize(as = Replacement.class)
@JsonDeserialize(builder = AutoValue_Replacement.Builder.class)
public abstract class Replacement {

  public abstract String getName();

  public abstract String getFileName();

  public abstract Optional<String> getStation();

  public abstract String getOriginalEntry();

  public abstract String getReplacementEntry();

  public abstract String getOperator();

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_Replacement.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    protected abstract Replacement autoBuild();

    public abstract Builder setName(String name);

    public abstract Builder setFileName(String fileName);

    public abstract Builder setStation(Optional<String> station);

    public abstract Builder setOriginalEntry(String originalEntry);

    public abstract Builder setReplacementEntry(String replacementEntry);

    public abstract Builder setOperator(String operator);

    public Replacement build() {
      return autoBuild();
    }
  }

}
