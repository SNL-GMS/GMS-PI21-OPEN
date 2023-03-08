package gms.tools.stationrefbuilder;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.stream.Collectors;

@AutoValue
@JsonSerialize(as = StationGroupBuilderConfiguration.class)
@JsonDeserialize(builder = AutoValue_StationGroupBuilderConfiguration.Builder.class)
public abstract class StationGroupBuilderConfiguration {

  public abstract ImmutableSet<StationGroupMetaData> getGroups();

  public StationGroupMetaData getGroup(String name) {
    StationGroupMetaData tmp;

    for (Iterator<StationGroupMetaData> it = getGroups().iterator(); it.hasNext(); ) {
      tmp = it.next();
      if (tmp.getGroup().equals(name)) {
        return tmp;
      }
    }
    return null;
  }

  /**
   * @return List of group names as Collection
   */

  public Collection<String> getGroupNameList() {

    return getGroups().stream()
      .sorted(Comparator.comparingInt(StationGroupMetaData::getUiPosition))
      .map(StationGroupMetaData::getGroup)
      .collect(Collectors.toList());
  }

  public abstract Builder toBuilder();

  public static AutoValue_StationGroupBuilderConfiguration.Builder builder() {
    return new AutoValue_StationGroupBuilderConfiguration.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Builder setGroups(ImmutableSet<StationGroupMetaData> metadata);

    public abstract ImmutableSet.Builder<StationGroupMetaData> groupsBuilder();

    public Builder addStationGroupMetaDatum(StationGroupMetaData metadata) {
      groupsBuilder().add(metadata);
      return this;
    }

    public Builder setGroups(Collection<StationGroupMetaData> metadata) {
      return setGroups(ImmutableSet.copyOf(metadata));
    }

    protected abstract StationGroupBuilderConfiguration autoBuild();

    public StationGroupBuilderConfiguration build() {
      return autoBuild();
    }

  }

}

