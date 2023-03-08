package gms.dataacquisition.stationreceiver.cd11.common.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;

@AutoValue
@JsonSerialize(as = Cd11ConnectionConfig.class)
@JsonDeserialize(builder = AutoValue_Cd11ConnectionConfig.Builder.class)
public abstract class Cd11ConnectionConfig {

  public abstract short getProtocolMajorVersion();

  public abstract short getProtocolMinorVersion();

  public abstract String getServiceType();

  public static Builder builder() {
    return new AutoValue_Cd11ConnectionConfig.Builder();
  }

  public abstract Builder toBuilder();


  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Cd11ConnectionConfig.Builder setProtocolMajorVersion(short majorVersion);

    public abstract Cd11ConnectionConfig.Builder setProtocolMinorVersion(short minorVersion);

    public abstract Cd11ConnectionConfig.Builder setServiceType(String serviceType);

    public abstract Cd11ConnectionConfig build();
  }
}



