package gms.shared.frameworks.osd.coi.channel;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import gms.shared.frameworks.osd.coi.Units;
import gms.shared.frameworks.osd.coi.provenance.InformationSource;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceAlias;
import gms.shared.frameworks.osd.coi.stationreference.RelativePosition;
import org.apache.commons.lang3.Validate;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;


/**
 * Define a class to represent an instrument channel. A Channel is an identifier for a data stream
 * from a sensor measuring a particular aspect of some physical phenomenon (e.g., ground motion or
 * air pressure). A Channel has metadata, such as a name (e.g., "BHZ" is broadband ground motion in
 * the vertical direction), on time and off time, and a channel type that encodes the type of data
 * recorded by that sensor. There are different conventions for Channel naming, so a Channel can
 * have Aliases. The Channel class also includes information about how the sensor was placed and
 * oriented: depth (relative to the elevation of the associated Site), horizontal angle, and
 * vertical angle.
 */
@AutoValue
@JsonSerialize(as = ReferenceChannel.class)
@JsonDeserialize(builder = AutoValue_ReferenceChannel.Builder.class)
public abstract class ReferenceChannel {

  public abstract String getName();

  public abstract ChannelDataType getDataType();

  public abstract ChannelBandType getBandType();

  public abstract ChannelInstrumentType getInstrumentType();

  public abstract ChannelOrientationType getOrientationType();

  public abstract char getOrientationCode();

  public abstract String getLocationCode();

  public abstract double getLatitude();

  public abstract double getLongitude();

  public abstract double getElevation();

  public abstract double getDepth();

  public abstract double getVerticalAngle();

  public abstract double getHorizontalAngle();

  public abstract Units getUnits();

  public abstract double getNominalSampleRate();

  public abstract Instant getActualTime();

  public abstract Instant getSystemTime();

  public abstract boolean isActive();

  public abstract InformationSource getInformationSource();

  public abstract String getComment();

  public abstract RelativePosition getPosition();

  public abstract List<ReferenceAlias> getAliases();

  /**
   * Sets defaults, for non required fields. Properties not listed here are required and if not
   * provided build() will throw an IllegalStateException
   *
   * @return ReferenceChannel
   */
  public static Builder builder() {
    return new AutoValue_ReferenceChannel.Builder()
      .setComment("")
      .setSystemTime(Instant.EPOCH)
      .setPosition(RelativePosition.from(0, 0, 0))
      .setAliases(List.of());
  }

  public abstract Builder toBuilder();

  @Memoized
  public UUID getEntityId() {
    return UUID.nameUUIDFromBytes((this.getName())
      .getBytes(StandardCharsets.UTF_16LE));
  }

  @Memoized
  public UUID getVersionId() {
    return UUID.nameUUIDFromBytes(
      (this.getName() + this.getDataType() + this.getBandType() + this.getInstrumentType()
        + this.getOrientationType() + this.getOrientationCode() + this.getLocationCode()
        + this.getLatitude() + this.getLongitude() + this.getElevation() + this.getDepth()
        + this.getVerticalAngle() + this.getHorizontalAngle() + this.getUnits()
        + this.getNominalSampleRate() + this.getActualTime())
        .getBytes(StandardCharsets.UTF_16LE));
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Builder setName(String name);

    abstract String getName();

    public abstract Builder setDataType(ChannelDataType dataType);

    public abstract Builder setBandType(ChannelBandType bandType);

    public abstract Builder setInstrumentType(ChannelInstrumentType instrumentType);

    public abstract Builder setOrientationType(ChannelOrientationType orientationType);

    public abstract Builder setOrientationCode(char orientationCode);

    public abstract Builder setLocationCode(String locationCode);

    public abstract Builder setLatitude(double latitude);

    public abstract Builder setLongitude(double longitude);

    public abstract Builder setElevation(double elevation);

    public abstract Builder setDepth(double depth);

    public abstract Builder setVerticalAngle(double verticalAngle);

    public abstract Builder setHorizontalAngle(double horizontalAngle);

    public abstract Builder setUnits(Units units);

    public abstract Builder setNominalSampleRate(double nominalSampleRate);

    public abstract Builder setActualTime(Instant actualTime);

    public abstract Builder setSystemTime(Instant systemTime);

    public abstract Builder setActive(boolean active);

    public abstract Builder setInformationSource(InformationSource source);

    public abstract Builder setComment(String comment);

    public abstract Builder setPosition(RelativePosition position);

    public abstract Builder setAliases(List<ReferenceAlias> aliases);

    abstract ReferenceChannel autoBuild();

    public ReferenceChannel build() {
      setName(getName().trim());
      final ReferenceChannel referenceChannel = autoBuild();

      Validate.notEmpty(referenceChannel.getName(), "name should not be an empty field");

      Validate.isTrue(!Character.isWhitespace(referenceChannel.getOrientationCode()),
        "orientationCode cannot be whitespace");

      Validate.isTrue(orientationCodesMatch(referenceChannel.getOrientationType(),
          referenceChannel.getOrientationCode()),
        "orientationType.code must match orientationCode when orientationType is not 'UNKNOWN'");

      return referenceChannel;
    }

    /**
     * Determines whether the orientationType's {@link ChannelOrientationType#getCode()} matches the
     * provided orientationCode. A match occurs if the orientationType is {@link
     * ChannelOrientationType#UNKNOWN} or if orientationType is not {@link
     * ChannelOrientationType#UNKNOWN} and the orientationType's {@link
     * ChannelOrientationType#getCode()} is equal to orientationCode.
     *
     * @param orientationType a {@link ChannelOrientationType}, not null
     * @param orientationCode a character, not whitespace
     * @return true of the {@link ChannelOrientationType#getCode()} matches the orientationCode and
     * false otherwise.
     */
    private static boolean orientationCodesMatch(ChannelOrientationType orientationType,
      char orientationCode) {

      if (ChannelOrientationType.UNKNOWN != orientationType) {
        return orientationType.getCode() == orientationCode;
      }

      return true;
    }
  }
}