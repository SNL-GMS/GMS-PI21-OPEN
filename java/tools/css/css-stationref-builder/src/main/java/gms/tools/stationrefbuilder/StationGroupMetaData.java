package gms.tools.stationrefbuilder;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@AutoValue
@JsonSerialize(as = StationGroupMetaData.class)
@JsonDeserialize(builder = AutoValue_StationGroupMetaData.Builder.class)
public abstract class StationGroupMetaData {

  public abstract String getGroup();

  public abstract String getDescription();

  public abstract Optional<String> getLogic();

  public abstract ImmutableSet<Rule> getRules();

  public abstract int getUiPosition();

  public Rule getRule(String name) {
    Rule tmp;

    for (Iterator<Rule> it = getRules().iterator(); it.hasNext(); ) {
      tmp = it.next();
      if (tmp.getName().equals(name)) {
        return tmp;
      }
    }
    return null;
  }

  /**
   * @return List of rule names
   */
  public Collection<String> getRuleNameList() {

    return getRules()
      .stream()
      .map(Rule::getName)
      .collect(Collectors.toList());
  }

  /**
   * @return Map of name and type of rules
   */
  public Map<String, String> getRuleMap() {
    return getRules()
      .stream()
      .collect(Collectors.toMap(Rule::getName, Rule::getType));
  }


  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_StationGroupMetaData.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    protected abstract StationGroupMetaData autoBuild();

    public abstract Builder setRules(ImmutableSet<Rule> rules);

    abstract ImmutableSet.Builder<Rule> rulesBuilder();

    public Builder addRule(Rule rule) {
      rulesBuilder().add(rule);
      return this;
    }

    public Builder setRules(
      Collection<Rule> rule) {
      return setRules(ImmutableSet.copyOf(rule));
    }

    public abstract Builder setGroup(String group);

    public abstract Builder setDescription(String description);

    public abstract Builder setUiPosition(int uiPosition);

    public abstract Builder setLogic(Optional<String> logic);

    public StationGroupMetaData build() {
      return autoBuild();
    }
  }

}
