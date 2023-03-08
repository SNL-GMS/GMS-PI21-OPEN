package gms.dataacquisition.stationreceiver.cd11.common.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11Validator;

import static com.google.common.base.Preconditions.checkState;

@AutoValue
@JsonSerialize(as = Cd11SocketConfig.class)
@JsonDeserialize(builder = AutoValue_Cd11SocketConfig.Builder.class)
public abstract class Cd11SocketConfig {

  public static final String DEFAULT_STATION_OR_RESPONDER_NAME = "H04N";
  public static final String DEFAULT_STATION_OR_RESPONDER_TYPE = "IDC";
  public static final String DEFAULT_SERVICE_TYPE = "TCP";
  public static final String DEFAULT_FRAME_CREATOR = "TEST";
  public static final String DEFAULT_FRAME_DESTINATION = "0";
  public static final short DEFAULT_PROTOCOL_MAJOR_VERSION = 1;
  public static final short DEFAULT_PROTOCOL_MINOR_VERSION = 1;
  public static final int DEFAULT_AUTHENTICATION_KEY_IDENTIFIER = 0;

  public abstract String getStationOrResponderName();

  public abstract String getStationOrResponderType();

  public abstract String getServiceType();

  public abstract String getFrameCreator();

  public abstract String getFrameDestination();

  public abstract int getAuthenticationKeyIdentifier();

  public abstract short getProtocolMajorVersion();

  public abstract short getProtocolMinorVersion();

  public static Cd11SocketConfig.Builder builder() {
    return new AutoValue_Cd11SocketConfig.Builder();
  }

  public static Cd11SocketConfig.Builder builderWithDefaults() {
    return builder()
      .setStationOrResponderName(DEFAULT_STATION_OR_RESPONDER_NAME)
      .setStationOrResponderType(DEFAULT_STATION_OR_RESPONDER_TYPE)
      .setServiceType(DEFAULT_SERVICE_TYPE)
      .setFrameCreator(DEFAULT_FRAME_CREATOR)
      .setFrameDestination(DEFAULT_FRAME_DESTINATION)
      .setProtocolMajorVersion(DEFAULT_PROTOCOL_MAJOR_VERSION)
      .setProtocolMinorVersion(DEFAULT_PROTOCOL_MINOR_VERSION)
      .setAuthenticationKeyIdentifier(DEFAULT_AUTHENTICATION_KEY_IDENTIFIER);
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {
    /**
     * The name of the station or responder as specified in the CD 1.1 Protocol.
     *
     * @param value Station or responder name.
     * @return Configuration built from this {@link Builder}, not null
     */
    public abstract Builder setStationOrResponderName(String value);

    /**
     * Station or responder type as specified in the CD 1.1 Protocol: IMS, IDC, etc.
     *
     * @param value Responder type.
     * @return Configuration built from this {@link Builder}, not null
     */
    public abstract Builder setStationOrResponderType(String value);

    /**
     * TCP or UDP (default: TCP).
     *
     * @param value TCP or UDP.
     * @return Configuration built from this {@link Builder}, not null
     */
    public abstract Builder setServiceType(String value);

    /**
     * Name of the frame creator as specified in the CD 1.1 Protocol.
     *
     * @param value Frame creator.
     * @return Configuration built from this {@link Builder}, not null
     */
    public abstract Builder setFrameCreator(String value);

    /**
     * IMS, IDC, 0, etc (default: 0).
     *
     * @param value Frame destination.
     * @return Configuration built from this {@link Builder}, not null
     */
    public abstract Builder setFrameDestination(String value);

    /**
     * Auth key identifier for the CD 1.1 frame trailers.
     *
     * @param value Authentication key identifier.
     * @return Configuration built from this {@link Builder}, not null
     */
    public abstract Builder setAuthenticationKeyIdentifier(int value);

    /**
     * The major version number of the CD protocol (default: 1).
     *
     * @param value Major version number.
     * @return Configuration built from this {@link Builder}, not null
     */
    public abstract Builder setProtocolMajorVersion(short value);

    /**
     * The minor version number of the CD protocol (default: 1).
     *
     * @param value Minor version number.
     * @return Configuration built from this {@link Builder}, not null
     */
    public abstract Builder setProtocolMinorVersion(short value);

    public abstract Cd11SocketConfig autoBuild();

    public Cd11SocketConfig build() {
      var cd11SocketConfig = autoBuild();

      Cd11Validator.validStationOrResponderName(cd11SocketConfig.getStationOrResponderName());
      Cd11Validator.validStationOrResponderType(cd11SocketConfig.getStationOrResponderType());
      Cd11Validator.validServiceType(cd11SocketConfig.getServiceType());
      Cd11Validator.validFrameCreator(cd11SocketConfig.getFrameCreator());
      Cd11Validator.validFrameDestination(cd11SocketConfig.getFrameDestination());
      checkState(cd11SocketConfig.getProtocolMajorVersion() >= 0, "Invalid major version number.");
      checkState(cd11SocketConfig.getProtocolMinorVersion() >= 0, "Invalid minor version number.");

      return cd11SocketConfig;
    }
  }
}
