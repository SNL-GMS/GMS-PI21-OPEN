package gms.shared.frameworks.osd.coi.channel;

import gms.shared.frameworks.osd.coi.FieldMapUtilities;
import gms.shared.frameworks.osd.coi.signaldetection.BeamDefinition;
import gms.shared.frameworks.osd.coi.signaldetection.FilterDefinition;
import gms.shared.frameworks.osd.coi.signaldetection.FkSpectraDefinition;
import gms.shared.frameworks.osd.coi.signaldetection.Location;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides factory operations to construct raw and derived {@link Channel}s.
 */
public class ChannelFactory {

  private static final Logger logger = LoggerFactory.getLogger(ChannelFactory.class);

  /**
   * Group name for all beamed Channel's (i.e. beam Channels have names beginning with
   * [STATION].beam.[CHANNEL_BAND}[CHANNEL_INSTRUMENT][CHANNEL_ORIENTATION])
   */
  private static final String BEAM_CHANNEL_GROUP_NAME = "beam";

  /**
   * Group name for all FK'ed Channels (i.e. fk Channels have names beginning with
   * [STATION].fk.[CHANNEL_BAND][CHANNEL_INSTRUMENT][CHANNEL_ORIENTATION])
   */
  private static final String FK_CHANNEL_GROUP_NAME = "fk";

  /**
   * Private constructor to explicitly prevent ChannelFactory instantiation.
   */
  private ChannelFactory() {
  }

  /**
   * Obtain a new raw {@link Channel} corresponding to a {@link ReferenceChannel}. The new Channel's
   * name uses the provided station and channelGroup names. The Channel has the same {@link
   * Channel#getName()} and {@link Channel#getCanonicalName()} (i.e. the Channel is not aliased to a
   * different Channel).
   *
   * @param referenceChannel {@link ReferenceChannel} used as a template for the new {@link
   * Channel}, not null
   * @param stationName station component of the new {@link Channel}'s name, not null
   * @param channelGroupName channel group component of the new {@link Channel}'s name, not null
   * @return the {@link Channel} created from the {@link ReferenceChannel}
   * @throws NullPointerException if referenceChannel, stationName, or channelGroupName are null
   */
  public static Channel rawFromReferenceChannel(ReferenceChannel referenceChannel,
    String stationName, String channelGroupName) {

    Objects.requireNonNull(referenceChannel, "referenceChannel can't be null");
    Objects.requireNonNull(stationName, "stationName can't be null");
    Objects.requireNonNull(channelGroupName, "channelGroupName can't be null");

    return Channel.builder()
      .setDescription(
        String.format("Raw Channel created from ReferenceChannel %s with version %s",
          referenceChannel.getEntityId(), referenceChannel.getVersionId()))

      .setStation(stationName)

      .setChannelDataType(referenceChannel.getDataType())

      .setChannelBandType(referenceChannel.getBandType())
      .setChannelInstrumentType(referenceChannel.getInstrumentType())
      .setChannelOrientationType(referenceChannel.getOrientationType())
      .setChannelOrientationCode(referenceChannel.getOrientationCode())

      .setUnits(referenceChannel.getUnits())
      .setNominalSampleRateHz(referenceChannel.getNominalSampleRate())

      .setLocation(Location.from(
        referenceChannel.getLatitude(),
        referenceChannel.getLongitude(),
        referenceChannel.getDepth(),
        referenceChannel.getElevation()))
      .setOrientationAngles(Orientation.from(
        referenceChannel.getHorizontalAngle(),
        referenceChannel.getVerticalAngle()))

      .setProcessingMetadata(
        Map.of(ChannelProcessingMetadataType.CHANNEL_GROUP, channelGroupName))
      .buildRaw();
  }

  /**
   * Obtain a new derived {@link Channel} for applying the provided {@link FilterDefinition} to the
   * provided input {@link Channel}.
   *
   * @param input input {@link Channel} to the filtered Channel, not null
   * @param filter {@link FilterDefinition} applied to the input Channel, not null
   * @return derived {@link Channel} describing a filtered Channel, not null
   * @throws NullPointerException if input or filter are null
   */
  public static Channel filtered(Channel input, FilterDefinition filter) {

    Objects.requireNonNull(input, "input can't be null");
    Objects.requireNonNull(filter, "filterDefinition can't be null");

    final Map<ChannelProcessingMetadataType, Object> metadata =
      new EnumMap<>(ChannelProcessingMetadataType.class);

    metadata.putAll(input.getProcessingMetadata());
    metadata.put(ChannelProcessingMetadataType.FILTER_TYPE, filter.getFilterType());
    metadata
      .put(ChannelProcessingMetadataType.FILTER_PASS_BAND_TYPE, filter.getFilterPassBandType());
    metadata.put(ChannelProcessingMetadataType.FILTER_LOW_FREQUENCY_HZ, filter.getLowFrequencyHz());
    metadata
      .put(ChannelProcessingMetadataType.FILTER_HIGH_FREQUENCY_HZ, filter.getHighFrequencyHz());
    metadata.put(ChannelProcessingMetadataType.FILTER_CAUSALITY, filter.getFilterCausality());
    metadata.put(ChannelProcessingMetadataType.FILTER_GROUP_DELAY, filter.getGroupDelaySecs());

    final String description = "Filtered channel created by applying " + filter.getDescription()
      + " to " + input.getName();

    return input.toBuilder()
      .setConfiguredInputs(List.of(input.getName()))
      .setProcessingDefinition(FieldMapUtilities.toFieldMap(filter))
      .setProcessingMetadata(metadata)
      .setDescription(description)
      .buildDerived();
  }

  /**
   * Obtain a new derived {@link Channel} for applying the provided {@link BeamDefinition} to the
   * provided input {@link Channel}s. The output beam Channel has {@link Channel#getLocation()}
   * equal to the {@link Station#getLocation} for the Station grouping all of the input Channels.
   * The output beam Channel has {@link Channel#getNominalSampleRateHz()} equal to the {@link
   * BeamDefinition#getNominalWaveformSampleRate()}.  The output beam Channel has {@link
   * Channel#getOrientationAngles()} assigned based on the {@link Channel#getChannelOrientationType()}
   * of the input Channels, see {@link ChannelFactory#beamOrientationAnglesFromType(ChannelOrientationType)}.
   * <p>
   * Precondition: Assumes all of the input Channels have the same: {@link Channel#getStation()},
   * {@link Channel#getChannelDataType()}, {@link Channel#getChannelBandType()}, {@link
   * Channel#getChannelInstrumentType()}, {@link Channel#getChannelOrientationType()}, {@link
   * Channel#getChannelOrientationCode()}, and {@link Channel#getUnits()}
   *
   * @param station The {@link Station} grouping the channels  that were beamed
   * @param inputs input {@link Channel}s to the beam Channel, meet the precondition specified
   * above, not null
   * @param beam {@link BeamDefinition} applied to the input Channels, not null
   * @return derived {@link Channel} describing a beam Channel, not null
   * @throws NullPointerException if inputs or beam are null
   */
  public static Channel beamed(Station station, List<Channel> inputs, BeamDefinition beam) {

    Objects.requireNonNull(inputs, "inputs can't be null");
    Objects.requireNonNull(beam, "beamDefinition can't be null");

    // Precondition guarantees all of the input Channels can be templates for the derived Channel.
    final Channel canonical = inputs.get(0);

    // Add processing metadata for beaming and channel steering
    final EnumMap<ChannelProcessingMetadataType, Object> metadata = new EnumMap<>(
      ChannelProcessingMetadataType.class);
    metadata.putAll(canonical.getProcessingMetadata());

    // Warn if metadata already contains STEERING entries
    final Set<ChannelProcessingMetadataType> steeringKeys = Set.of(
      ChannelProcessingMetadataType.STEERING_AZIMUTH,
      ChannelProcessingMetadataType.STEERING_SLOWNESS);

    if (steeringKeys.stream().anyMatch(metadata::containsKey)) {
      logger.warn(
        "Creating a beam derived Channel but the input Channels have already been steered to an (azimuth, slowness). The canonical input channel is: {}",
        canonical);
    }

    metadata.put(ChannelProcessingMetadataType.CHANNEL_GROUP, BEAM_CHANNEL_GROUP_NAME);
    metadata.put(ChannelProcessingMetadataType.STEERING_AZIMUTH, beam.getAzimuth());
    metadata.put(ChannelProcessingMetadataType.STEERING_SLOWNESS, beam.getSlowness());
    metadata.put(ChannelProcessingMetadataType.BEAM_COHERENT, beam.isCoherent());

    final List<String> configuredInputs = inputs.stream()
      .map(Channel::getName)
      .collect(Collectors.toList());

    return canonical.toBuilder()
      .setNominalSampleRateHz(beam.getNominalWaveformSampleRate())
      .setLocation(station.getLocation())
      .setOrientationAngles(beamOrientationAnglesFromType(canonical.getChannelOrientationType()))
      .setConfiguredInputs(configuredInputs)
      .setProcessingDefinition(FieldMapUtilities.toFieldMap(beam))
      .setProcessingMetadata(metadata)
      .setDescription("Beam created for " + station.getName() + " with location " + station.getLocation())
      .buildDerived();
  }

  /**
   * Obtain the {@link Orientation} angles for a channel beamed from channels with {@link
   * ChannelOrientationType} orientation.
   *
   * @param orientationType {@link ChannelOrientationType}, not null
   * @return {@link Orientation} angles, not null
   */
  private static Orientation beamOrientationAnglesFromType(ChannelOrientationType orientationType) {

    // TODO: handle other beam orientations (radial, etc.)

    // TODO: could also extract horizontal orientationAngles from an input channel if we can assume
    //       all of the Channels have been rotated prior to beaming.  This would allow e.g. beam
    //       channels oriented to SOUTH instead of NORTH or WEST instead of EAST.

    if (ChannelOrientationType.VERTICAL == orientationType) {
      return Orientation.from(Double.NaN, 0.0);
    } else if (ChannelOrientationType.EAST_WEST == orientationType) {
      return Orientation.from(90.0, 90.0);
    } else if (ChannelOrientationType.NORTH_SOUTH == orientationType) {
      return Orientation.from(0.0, 90.0);
    }

    throw new IllegalStateException("Can only create vertical beam channels");
  }

  /**
   * Obtain a new derived {@link Channel} for applying the provided {@link FkSpectraDefinition} to the
   * provided input {@link Channel}.
   *
   * @param station The {@link Station} providing the channels contributing to the FK
   * @param inputs input {@link Channel} to the filtered Channel, not null
   * @param fk {@link FkSpectraDefinition} applied to the input Channel, not null
   * @return derived {@link Channel} describing a filtered Channel, not null
   * @throws NullPointerException if input or filter are null
   */
  public static Channel fk(Station station, List<Channel> inputs, FkSpectraDefinition fk) {

    Objects.requireNonNull(inputs, "inputs can't be null");
    Objects.requireNonNull(fk, "FkSpectraDefinition can't be null");

    // Precondition guarantees all of the input Channels can be templates for the derived Channel.
    final Channel canonical = inputs.get(0);

    final Map<ChannelProcessingMetadataType, Object> metadata =
      new EnumMap<>(ChannelProcessingMetadataType.class);

    metadata.putAll(canonical.getProcessingMetadata());

    //create the data to be used to create the unique name of this derived channel
    metadata.put(ChannelProcessingMetadataType.CHANNEL_GROUP, FK_CHANNEL_GROUP_NAME);

    final List<String> configuredInputs = inputs.stream()
      .map(Channel::getName)
      .collect(Collectors.toList());

    final String description = String
      .format("FK channel created by applying fk to %s", configuredInputs);

    return canonical.toBuilder()
      .setNominalSampleRateHz(fk.getWaveformSampleRateHz())
      .setConfiguredInputs(configuredInputs)
      .setLocation(station.getLocation())
      .setProcessingDefinition(FieldMapUtilities.toFieldMap(fk))
      .setProcessingMetadata(metadata)
      .setDescription(description)
      .buildDerived();
  }
}
