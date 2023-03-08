package gms.tools.stationrefbuilder;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;

import java.util.Optional;

@AutoValue
@JsonSerialize(as = Rule.class)
@JsonDeserialize(builder = AutoValue_Rule.Builder.class)
public abstract class Rule {

  public abstract String getName();

  public abstract String getType();

  public abstract Optional<String> getLookuptable();

  public abstract Optional<String> getTable();

  public abstract Optional<String> getField();

  public abstract Optional<String> getOperator();

  public abstract Optional<String> getValue();

  public abstract Optional<String> getIndex();

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_Rule.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    protected abstract Rule autoBuild();

    public abstract Builder setName(String name);

    public abstract Builder setType(String name);

    public abstract Builder setLookuptable(Optional<String> lookuptable);

    public abstract Builder setTable(Optional<String> table);

    public abstract Builder setField(Optional<String> field);

    public abstract Builder setOperator(Optional<String> operator);

    public abstract Builder setValue(Optional<String> value);

    public abstract Builder setIndex(Optional<String> index);

    public Rule build() {
      return autoBuild();
    }
  }


}
