package gms.shared.frameworks.osd.coi.channel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.Units;
import gms.shared.frameworks.osd.coi.channel.ChannelNameUtilities.NameHash;
import gms.shared.frameworks.osd.coi.signaldetection.Location;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Represents both raw and derived channels created and used in GMS processing operations. Construct
 * new Channels using the operations in {@link ChannelFactory}.
 */
@AutoValue
public abstract class Channel {

  static final String NAME_SEPARATOR = ".";
  static final String COMPONENT_SEPARATOR = "/";
  static final String ATTRIBUTE_SEPARATOR = ",";

  public abstract String getName();

  public abstract String getCanonicalName();

  public abstract String getDescription();

  public abstract String getStation();

  public abstract ChannelDataType getChannelDataType();

  public abstract ChannelBandType getChannelBandType();

  public abstract ChannelInstrumentType getChannelInstrumentType();

  public abstract ChannelOrientationType getChannelOrientationType();

  public abstract char getChannelOrientationCode();

  public abstract Units getUnits();

  public abstract double getNominalSampleRateHz();

  public abstract Location getLocation();

  public abstract Orientation getOrientationAngles();

  public abstract List<String> getConfiguredInputs();

  public abstract Map<String, Object> getProcessingDefinition();

  public abstract Map<ChannelProcessingMetadataType, Object> getProcessingMetadata();

  @JsonCreator
  public static Channel from(
    @JsonProperty("name") String name,
    @JsonProperty("canonicalName") String canonicalName,
    @JsonProperty("description") String description,
    @JsonProperty("station") String station,
    @JsonProperty("channelDataType") ChannelDataType channelDataType,
    @JsonProperty("channelBandType") ChannelBandType channelBandType,
    @JsonProperty("channelInstrumentType") ChannelInstrumentType channelInstrumentType,
    @JsonProperty("channelOrientationType") ChannelOrientationType channelOrientationType,
    @JsonProperty("channelOrientationCode") char channelOrientationCode,
    @JsonProperty("units") Units units,
    @JsonProperty("nominalSampleRateHz") double nominalSampleRateHz,
    @JsonProperty("location") Location location,
    @JsonProperty("orientationAngles") Orientation orientationAngles,
    @JsonProperty("configuredInputs") Collection<String> configuredInputs,
    @JsonProperty("processingDefinition") Map<String, Object> processingDefinition,
    @JsonProperty("processingMetadata") Map<ChannelProcessingMetadataType, Object> processingMetadata) {

    // Copy processingMetadata entries into a new EnumMap. Constructed this way to avoid
    // IllegalArgumentException when the provided processingMetadata is empty.
    final EnumMap<ChannelProcessingMetadataType, Object> processingMetadataEnumMap = new EnumMap<>(
      ChannelProcessingMetadataType.class);
    processingMetadataEnumMap.putAll(processingMetadata);

    return builder()
      .setName(name)
      .setCanonicalName(canonicalName)
      .setDescription(description)
      .setStation(station)
      .setChannelDataType(channelDataType)
      .setChannelBandType(channelBandType)
      .setChannelInstrumentType(channelInstrumentType)
      .setChannelOrientationType(channelOrientationType)
      .setChannelOrientationCode(channelOrientationCode)
      .setUnits(units)
      .setNominalSampleRateHz(nominalSampleRateHz)
      .setLocation(location)
      .setOrientationAngles(orientationAngles)
      .setConfiguredInputs(Collections.unmodifiableList(new ArrayList<>(configuredInputs)))
      .setProcessingDefinition(Collections.unmodifiableMap(new HashMap<>(processingDefinition)))
      .setProcessingMetadata(Collections.unmodifiableMap(processingMetadataEnumMap))
      .build();
  }

  /**
   * Obtain a new {@link Channel.Builder} with all properties left unset.  Package private since
   * {@link Channel} should only be instantiated via {@link ChannelFactory} or via deserialization
   * with {@link Channel#from(String, String, String, String, ChannelDataType, ChannelBandType,
   * ChannelInstrumentType, ChannelOrientationType, char, Units, double, Location, Orientation,
   * Collection, Map, Map)}.
   *
   * @return {@link Channel.Builder}, not null
   */
  static Builder builder() {
    return new AutoValue_Channel.Builder();
  }

  /**
   * Obtain a new {@link Channel.Builder} with all properties initialized to match this {@link
   * Channel}.Package private since {@link Channel} should only be instantiated via {@link
   * ChannelFactory} or via deserialization with {@link Channel#from(String, String, String, String,
   * ChannelDataType, ChannelBandType, ChannelInstrumentType, ChannelOrientationType, char, Units,
   * double, Location, Orientation, Collection, Map, Map)}.
   *
   * @return {@link Channel.Builder}, not null
   */
  abstract Builder toBuilder();

  @AutoValue.Builder
  interface Builder {

    Builder setName(String name);

    Builder setCanonicalName(String canonicalName);

    Builder setDescription(String description);

    Builder setStation(String station);

    Builder setChannelDataType(ChannelDataType channelDataType);

    Builder setChannelBandType(ChannelBandType channelBandType);

    Builder setChannelInstrumentType(ChannelInstrumentType channelInstrumentType);

    Builder setChannelOrientationType(ChannelOrientationType channelOrientationType);

    Builder setChannelOrientationCode(char channelOrientationCode);

    Builder setUnits(Units units);

    Builder setNominalSampleRateHz(double nominalSampleRateHz);

    Builder setLocation(Location location);

    Builder setOrientationAngles(Orientation orientationAngles);

    Builder setConfiguredInputs(List<String> configuredInputs);

    List<String> getConfiguredInputs();

    Builder setProcessingDefinition(Map<String, Object> processingDefinition);

    Builder setProcessingMetadata(
      Map<ChannelProcessingMetadataType, Object> processingMetadata);

    Map<ChannelProcessingMetadataType, Object> getProcessingMetadata();

    Channel autoBuild();

    /**
     * Obtain a {@link Channel} constructed using exactly the properties set in this {@link
     * Channel.Builder}.  Validates some Channel constraints:
     * <p>
     * 1. {@link Channel#getName()} and {@link Channel#getCanonicalName()} cannot be empty.
     * <p>
     * 2. {@link Channel#getProcessingMetadata()} must have a value for {@link
     * ChannelProcessingMetadataType#CHANNEL_GROUP}
     * <p>
     * 3. {@link Channel#getChannelOrientationType()} and {@link Channel#getChannelOrientationCode()}
     * must be consistent: code must match the type if type is not {@link
     * ChannelOrientationType#UNKNOWN}; code can never be whitespace.
     *
     * @return {@link Channel}, not null
     * @throws IllegalArgumentException if any of the Channel constraints listed above are
     * unsatisfied.
     */
    default Channel build() {
      setProcessingMetadata(Collections.unmodifiableMap(getProcessingMetadata()));

      List<String> sortedInputs = getConfiguredInputs().stream().sorted().collect(toList());
      setConfiguredInputs(Collections.unmodifiableList(sortedInputs));

      final Channel channel = autoBuild();

      Validate.notEmpty(channel.getName(), "name should not be an empty field");
      Validate.notEmpty(channel.getCanonicalName(), "canonicalName should not be an empty field");
      channel.getConfiguredInputs().forEach(
        ci -> Validate.notBlank(ci, "none of the configured inputs should be an empty field"));

      // Validate necessary processingMetadata entries
      Validate.isTrue(
        channel.getProcessingMetadata().containsKey(ChannelProcessingMetadataType.CHANNEL_GROUP),
        "processingMetadata must include an entry for CHANNEL_GROUP");

      // Validate consistent channelOrientationType and channelOrientationCode parameters
      Validate.isTrue(!Character.isWhitespace(channel.getChannelOrientationCode()),
        "channelOrientationCode cannot be whitespace");

      Validate.isTrue(orientationCodesMatch(channel.getChannelOrientationType(),
          channel.getChannelOrientationCode()),
        "channelOrientationType.code must match orientationCode when orientationType is not 'UNKNOWN'");

      return channel;
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

    /**
     * Create a raw {@link Channel} from the {@link Builder} after assigning the Channel a name and
     * canonicalName based on the other Channel fields.
     *
     * @return {@link Channel}, not null
     * @throws IllegalArgumentException if any of the Channel constraints listed in {@link
     * Builder#build()} are unsatisfied.
     * @implNote first builds a temporary Channel, uses {@link ChannelNameUtilities#createShortName(Channel)}
     * to create the raw Channel name, sets the name in the Builder, and then builds the final
     * Channel. Could also implement the name operation using Channel.Builder instead of using a
     * temporary Channel but that would require implementing getters for all of the builder fields
     * used in the Channel name.
     */
    default Channel buildRaw() {

      // This instance of Channel.Builder has all of the fields necessary to create the Channel's
      // name.  Create a temporary "unnamed" Channel from the builder, use that Channel's
      // properties to create the Channel name, and then reuse the Builder to create and return
      // the actual raw Channel.
      final String placeholderName = "[placeholder]";
      final Channel unnamed = setConfiguredInputs(List.of())
        .setProcessingDefinition(Map.of())
        .setName(placeholderName)
        .setCanonicalName(placeholderName)
        .build();

      // Create the new Channel's name name and canonicalName of: [STA].[GROUP].[FDSN_CHAN_NAME]
      final String rawChannelName = ChannelNameUtilities.createShortName(unnamed.getStation(),
        ChannelNameUtilities.lookupChannelGroup(unnamed.getProcessingMetadata()),
        ChannelNameUtilities.getFdsnChannelName(unnamed));

      // Build and return the Channel using the short name
      return buildWithName(rawChannelName);
    }

    /**
     * Create a derived {@link Channel} from the {@link Builder} after assigning the Channel a name
     * and canonicalName based on the other Channel fields.
     *
     * @return {@link Channel}, not null
     * @throws IllegalArgumentException if any of the Channel constraints listed in {@link
     * Builder#build()} are unsatisfied.
     * @implNote first builds a temporary Channel, uses {@link ChannelNameUtilities#createName(Channel)}
     * and {@link NameHash} to create the derived Channel name, sets the name in the Builder, and
     * then builds the final Channel.  Could also implement the name and hash operations using
     * Channel.Builder instead of using a temporary Channel but that would require implementing
     * getters for all of the builder fields used in the Channel name and hash.
     */
    default Channel buildDerived() {
      return buildWithName(ChannelNameUtilities.createName(build()));
    }

    /**
     * Assigns the provided name to {@link Builder#setName(String)} and {@link
     * Builder#setCanonicalName(String)} and then builds the Channel.
     *
     * @param name Channel name and canonicalName, not empty, not null
     * @return {@link Channel}, not null
     */
    private Channel buildWithName(String name) {
      return setName(name)
        .setCanonicalName(name)
        .build();
    }
  }
}
