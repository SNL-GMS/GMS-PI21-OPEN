package gms.tools.stationrefbuilder;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@AutoValue
@JsonSerialize(as = StationReferenceBuilderConfiguration.class)
@JsonDeserialize(builder = AutoValue_StationReferenceBuilderConfiguration.Builder.class)
public abstract class StationReferenceBuilderConfiguration {

  public abstract String getDetailedStationGroupListFilename();

  public abstract String getStationGroupListFilename();

  public abstract String getDefaultJsonFilename();

  public abstract String getCd11JsonFilename();

  public abstract String getUiStationGroupFilename();

  public abstract String getMonitoringOrgFilename();

  public abstract String getCurrentImplementor();

  public abstract String getMonitoringOrganization();

  public abstract boolean getWriteJson();

  public abstract boolean getReplaceZeroes();

  public abstract boolean getWriteAffiliations();

  public abstract boolean getWriteCsv();

  public abstract boolean getCheckForDuplicates();

  public abstract boolean getCheckForWhitespace();

  public abstract ImmutableMap<String, Object> getStationsByProtocol();

  public abstract ImmutableMap<String, Object> getStationsByPriority();

  public abstract ImmutableSet<Replacement> getSpecialReplacements();

  public abstract ImmutableSet<String> getPriorities();

  public abstract ImmutableSet<String> getProtocols();

  public abstract ImmutableMap<String, List<String>> getCountriesByContinent();

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_StationReferenceBuilderConfiguration.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Builder setDetailedStationGroupListFilename(
      String detailedStationGroupListFilename);

    public abstract Builder setStationGroupListFilename(String stationGroupListFilename);

    public abstract Builder setDefaultJsonFilename(String defaultJsonFilename);

    public abstract Builder setCd11JsonFilename(String cd11JsonFilename);

    public abstract Builder setMonitoringOrgFilename(String monitoringOrgFilename);

    public abstract Builder setUiStationGroupFilename(String uiStationGroupJson);

    public abstract Builder setCurrentImplementor(String implementor);

    public abstract Builder setMonitoringOrganization(String monitoringOrganization);

    public abstract Builder setWriteJson(boolean writeJson);

    public abstract Builder setReplaceZeroes(boolean replaceZeroes);

    public abstract Builder setWriteAffiliations(boolean writeAffiliations);

    public abstract Builder setWriteCsv(boolean writeCsv);

    public abstract Builder setCheckForDuplicates(boolean checkForDuplicates);

    public abstract Builder setCheckForWhitespace(boolean checkForWhitespace);

    abstract Builder setStationsByProtocol(ImmutableMap<String, Object> stationsByProtocol);

    public Builder setCountriesByContinent(Map<String, List<String>> countriesByContinent) {
      return setCountriesByContinent(ImmutableMap.copyOf(countriesByContinent));
    }

    abstract Builder setCountriesByContinent(ImmutableMap<String, List<String>> countriesByContinent);

    public Builder setPriorities(Set<String> priorities) {
      return setPriorities(ImmutableSet.copyOf(priorities));
    }

    abstract Builder setPriorities(ImmutableSet<String> priorities);

    public Builder setProtocols(Set<String> protocols) {
      return setProtocols(ImmutableSet.copyOf(protocols));
    }

    abstract Builder setProtocols(ImmutableSet<String> protocols);

    public Builder setStationsByProtocol(Map<String, Object> stationsByProtocol) {
      return setStationsByProtocol(ImmutableMap.copyOf(stationsByProtocol));
    }

    abstract ImmutableMap.Builder<String, Object> stationsByProtocolBuilder();

    public Builder putStationsByProtocol(String protocol, Object stations) {
      stationsByProtocolBuilder().put(protocol, stations);
      return this;
    }

    abstract Builder setStationsByPriority(ImmutableMap<String, Object> stationsByPriority);

    public Builder setStationsByPriority(Map<String, Object> stationsByPriority) {
      return setStationsByPriority(ImmutableMap.copyOf(stationsByPriority));
    }

    abstract ImmutableMap.Builder<String, Object> stationsByPriorityBuilder();

    public Builder putStationsByPriority(String priority, Object stations) {
      stationsByPriorityBuilder().put(priority, stations);
      return this;
    }

    abstract Builder setSpecialReplacements(ImmutableSet<Replacement> specialReplacements);

    public Builder setSpecialReplacements(Collection<Replacement> specialReplacements) {
      return setSpecialReplacements(ImmutableSet.copyOf(specialReplacements));
    }

    abstract ImmutableSet.Builder<Replacement> specialReplacementsBuilder();

    public Builder putSpecialReplacement(Replacement replacement) {
      specialReplacementsBuilder().add(replacement);
      return this;
    }

    protected abstract StationReferenceBuilderConfiguration autoBuild();

    public StationReferenceBuilderConfiguration build() {
      return autoBuild();
    }
  }

}
