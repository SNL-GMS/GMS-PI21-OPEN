package gms.testtools.simulators.bridgeddatasourcesimulator.api.coi;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import gms.shared.stationdefinition.dao.css.enums.ChannelType;

import java.time.Instant;

@AutoValue
@JsonSerialize(as = SiteChan.class)
@JsonDeserialize(builder = AutoValue_SiteChan.Builder.class)
public abstract class SiteChan {

  public abstract String getStationCode();

  public abstract String getChannelCode();

  public abstract Instant getOnDate();

  public abstract Instant getOffDate();

  public abstract ChannelType getChannelType();

  public abstract double getEmplacementDepth();

  public abstract double getHorizontalAngle();

  public abstract double getVerticalAngle();

  public abstract String getChannelDescription();

  public abstract Instant getLoadDate();

  public static Builder builder() {
    return new AutoValue_SiteChan.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Builder setStationCode(String stationCode);

    public abstract Builder setChannelCode(String channelCode);

    public abstract Builder setOnDate(Instant onDate);

    public abstract Builder setOffDate(Instant offDate);

    public abstract Builder setChannelType(ChannelType channelType);

    public abstract Builder setEmplacementDepth(double emplacementDepth);

    public abstract Builder setHorizontalAngle(double horizontalAngle);

    public abstract Builder setVerticalAngle(double verticalAngle);

    public abstract Builder setChannelDescription(String channelDescription);

    public abstract Builder setLoadDate(Instant loadDate);

    abstract SiteChan autoBuild();

    public SiteChan build() {
      return autoBuild();
    }
  }
}
