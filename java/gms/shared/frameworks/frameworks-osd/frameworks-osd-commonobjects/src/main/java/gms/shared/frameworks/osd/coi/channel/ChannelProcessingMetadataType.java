package gms.shared.frameworks.osd.coi.channel;

/**
 * Represents the type of processing metadata values that can appear as keys in the
 * {@link Channel#getProcessingMetadata()} map.
 */
public enum ChannelProcessingMetadataType {

  // General properties
  CHANNEL_GROUP,

  // Filtering properties
  FILTER_CAUSALITY,
  FILTER_GROUP_DELAY,
  FILTER_HIGH_FREQUENCY_HZ,
  FILTER_LOW_FREQUENCY_HZ,
  FILTER_PASS_BAND_TYPE,
  FILTER_TYPE,

  // Channel steering properties (used in beaming, rotation)
  STEERING_AZIMUTH,
  STEERING_SLOWNESS,

  // Beaming properties
  BEAM_COHERENT
}
