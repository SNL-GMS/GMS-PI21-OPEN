package gms.testtools.simulators.bridgeddatasourcesimulator.api.coi;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import gms.shared.stationdefinition.dao.css.enums.StaType;

import java.time.Instant;

@AutoValue
@JsonSerialize(as = Site.class)
@JsonDeserialize(builder = AutoValue_Site.Builder.class)
public abstract class Site {

  public abstract String getStationCode();

  public abstract Instant getOnDate();

  public abstract Instant getOffDate();

  public abstract double getLatitude();

  public abstract double getLongitude();

  public abstract double getElevation();

  public abstract String getStationName();

  public abstract StaType getStationType();

  public abstract double getDegreesNorth();

  public abstract double getDegreesEast();

  public abstract String getReferenceStation();

  public abstract Instant getLoadDate();

  public static Builder builder() {
    return new AutoValue_Site.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Builder setStationCode(String stationCode);

    public abstract Builder setOnDate(Instant onDate);

    public abstract Builder setOffDate(Instant offDate);

    public abstract Builder setLatitude(double latitude);

    public abstract Builder setLongitude(double longitude);

    public abstract Builder setElevation(double elevation);

    public abstract Builder setStationName(String stationName);

    public abstract Builder setStationType(StaType stationType);

    public abstract Builder setDegreesNorth(double degreesNorth);

    public abstract Builder setDegreesEast(double degreesEast);

    public abstract Builder setReferenceStation(String referenceStation);

    public abstract Builder setLoadDate(Instant loadDate);

    abstract Site autoBuild();

    public Site build() {
      return autoBuild();
    }
  }
}
