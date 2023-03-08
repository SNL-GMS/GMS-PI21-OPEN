import type * as CommonTypes from '../../common/types';
import type { EntityReference, Faceted } from '../../faceted';
import type { Station } from '../station-definitions/station-definitions';
import type * as ResponseTypes from './response-definitions';

/**
 * ChannelGroupType enum represents the different groupings of channels
 */
export enum ChannelGroupType {
  PROCESSING_GROUP = 'PROCESSING_GROUP',
  SITE_GROUP = 'SITE_GROUP',
  PHYSICAL_SITE = 'PHYSICAL_SITE'
}

/**
 * Represents a physical installation (e.g., building, underground vault, borehole)
 * containing a collection of instruments that produce raw channel waveform data.
 *
 * You should think of a channel group as immutable; a change to an installation in the
 * real-world merits creation of a new channel group. For example, if a channel is added
 * at the installation, the old channel group should no longer be used and a new one created
 * to represent the new state of the installation.
 */
export interface ChannelGroup extends Faceted {
  /**
   * Name of the channel group, e.g., "ABC.XY01".
   */
  readonly name: string;
  /**
   * Description of the channel group.
   */
  readonly description: string;
  /**
   * Time that the channel group came online. ISO-8601 time string, down to ms, e.g., "2021-02-26T19:10:41.283Z".
   */
  readonly effectiveAt: number;
  /**
   * Time that the channel group went offline. ISO-8601 time string, down to ms, e.g., "2021-02-26T19:10:41.283Z".
   */
  readonly effectiveUntil: number;
  /**
   * The location of the channel group.
   */
  readonly location: Location;
  /**
   * The type of the group.
   */
  readonly type: ChannelGroupType;
  /**
   * The channels comprising the channel group.
   */
  readonly channels: Channel[];
}

/**
 * Represents a source for unprocessed (raw) or processed (derived) time series data
 * from a seismic, hydroacoustic, or infrasonic sensor.
 */
export interface Channel extends Faceted {
  /**
   * Name of the channel, e.g., "ABC.XY01.BHZ".
   */
  readonly name: string;
  /**
   * Typically the name of the channel for a raw channel and a name that includes the derivation method for non-raw/derived channels.
   */
  readonly canonicalName: string;
  /**
   * Description of the channel.
   */
  readonly description: string;
  /**
   * Time that the channel came online in epoch seconds
   */
  readonly effectiveAt: number;
  /**
   * Time that the channel went offline. ISO-8601 time string, down to ms, e.g., "2021-02-26T19:10:41.283Z".
   */
  readonly effectiveUntil: number;
  /**
   * Station that the channel belongs to, e.g., "ABC.XY01".
   */
  readonly station: Station | EntityReference<Station>;
  /**
   * The type of data recorded by this channel.
   */
  readonly channelDataType: ChannelDataType;
  /**
   * The bandwidth recorded by this channel.
   */
  readonly channelBandType: ChannelBandType;
  /**
   * The type of instrument that records data for this channel.
   */
  readonly channelInstrumentType: ChannelInstrumentType;
  /**
   * The possible directions of travel of the instrument for this channel.
   */
  readonly channelOrientationType: ChannelOrientationType;
  /**
   * A character corresponding to the orientation type.
   */
  readonly channelOrientationCode: string; // Duplicate field necessary because the code in the enum gets transpiled out.
  /**
   * The units used when recording data in this channel.
   */
  readonly units: CommonTypes.Units;
  /**
   * The sample rate of the channel.
   */
  readonly nominalSampleRateHz: number;
  /**
   * The location of the channel.
   */
  readonly location: Location;
  /**
   * The orientation angle of the channel.
   */
  readonly orientationAngles: Orientation;
  readonly configuredInputs: Channel[];
  readonly processingDefinition: Map<string, any>;
  readonly processingMetadata: Map<ChannelProcessingMetadataType, any>;
  response?: ResponseTypes.Response;
}

/**
 * Represents the orientation angles used in processing channels
 */
export interface Orientation {
  horizontalAngleDeg: number;
  verticalAngleDeg: number;
}

/**
 * Enumeration representing the different types of processing channels.
 */
export enum ChannelDataType {
  SEISMIC = 'SEISMIC',
  HYDROACOUSTIC = 'HYDROACOUSTIC',
  INFRASOUND = 'INFRASOUND',
  WEATHER = 'WEATHER',
  DIAGNOSTIC_SOH = 'DIAGNOSTIC_SOH',
  DIAGNOSTIC_WEATHER = 'DIAGNOSTIC_WEATHER'
}

/**
 * Represents the type of processing metadata values that can appear as keys in the
 */
export enum ChannelProcessingMetadataType {
  // General properties
  CHANNEL_GROUP = 'CHANNEL_GROUP',

  // Filtering properties
  FILTER_CAUSALITY = 'FILTER_CAUSALITY',
  FILTER_GROUP_DELAY = 'FILTER_GROUP_DELAY',
  FILTER_HIGH_FREQUENCY_HZ = 'FILTER_HIGH_FREQUENCY_HZ',
  FILTER_LOW_FREQUENCY_HZ = 'FILTER_LOW_FREQUENCY_HZ',
  FILTER_PASS_BAND_TYPE = 'FILTER_PASS_BAND_TYPE',
  FILTER_TYPE = 'FILTER_TYPE',

  // Channel steering properties (used in beaming, rotation)
  STEERING_AZIMUTH = 'STEERING_AZIMUTH',
  STEERING_SLOWNESS = 'STEERING_SLOWNESS',

  // Beaming properties
  BEAM_COHERENT = 'BEAM_COHERENT'
}

/**
 * Represents the SEED / FDSN standard Channel Instruments.  Each instrument has a corresponding
 * single letter code.
 */
export enum ChannelInstrumentType {
  UNKNOWN = '-',
  HIGH_GAIN_SEISMOMETER = 'H',
  LOW_GAIN_SEISMOMETER = 'L',
  GRAVIMETER = 'G',
  MASS_POSITION_SEISMOMETER = 'M',
  ACCELEROMETER = 'N', // Historic channels might use L or G for accelerometers
  ROTATIONAL_SENSOR = 'J',
  TILT_METER = 'A',
  CREEP_METER = 'B',
  CALIBRATION_INPUT = 'C',
  PRESSURE = 'D',
  ELECTRONIC_TEST_POINT = 'E',
  MAGNETOMETER = 'F',
  HUMIDITY = 'I',
  TEMPERATURE = 'K',
  WATER_CURRENT = 'O',
  GEOPHONE = 'P',
  ELECTRIC_POTENTIAL = 'Q',
  RAINFALL = 'R',
  LINEAR_STRAIN = 'S',
  TIDE = 'T',
  BOLOMETER = 'U',
  VOLUMETRIC_STRAIN = 'V',
  WIND = 'W',
  NON_SPECIFIC_INSTRUMENT = 'Y',
  DERVIVED_BEAM = 'X',
  SYNTHESIZED_BEAM = 'Z'
}

/**
 * Represents the SEED / FDSN standard Channel Bands.  Each band has a corresponding single letter
 * code.
 */
export enum ChannelBandType {
  UNKNOWN = '-',

  // Long Period Bands
  /**
   * 1Hz - 10Hz
   */
  MID_PERIOD = 'M',
  /**
   * ~1Hz
   */
  LONG_PERIOD = 'L',
  /**
   * ~0.1Hz
   */
  VERY_LONG_PERIOD = 'V',
  /**
   * ~0.01Hz
   */
  ULTRA_LONG_PERIOD = 'U',
  /**
   * 0.0001Hz - 0.001Hz
   */
  EXTREMELY_LONG_PERIOD = 'R',
  /**
   * 0.00001Hz - 0.0001Hz (new)
   */
  PARTICULARLY_LONG_PERIOD = 'P',
  /**
   * 0.000001Hz - 0.00001Hz (new)
   */
  TREMENDOUSLY_LONG_PERIOD = 'T',
  /**
   * < 0.000001Hz (new)
   */
  IMMENSELY_LONG_PERIOD = 'Q',

  // Short Period Bands
  /**
   * 1000Hz - 5000Hz (new)
   */
  TREMENDOUSLY_SHORT_PERIOD = 'G',
  /**
   * 250Hz - 10000Hz (new)
   */
  PARTICULARLY_SHORT_PERIOD = 'D',
  /**
   * 80Hz - 250Hz
   */
  EXTREMELY_SHORT_PERIOD = 'E',
  /**
   * 10Hz - 80Hz
   */
  SHORT_PERIOD = 'S',

  // Broadband (Corner Periods > 10 sec)
  /**
   * 1000Hz - 5000Hz (new)
   */
  ULTRA_HIGH_BROADBAND = 'F',
  /**
   * 250Hz - 1000Hz (new)
   */
  VERY_HIGH_BROADBAND = 'C',
  /**
   * 80Hz - 250Hz
   */
  HIGH_BROADBAND = 'H',
  /**
   * 10Hz - 80Hz
   */
  BROADBAND = 'B',

  ADMINISTRATIVE = 'A',
  OPAQUE = 'O'
}

/**
 * Seismometer, Rotational Sensor, or Derived/Generated Orientations.
 * These correspond to instrument codes H, L, G, M, N, J, and X.
 */
export enum ChannelOrientationType {
  UNKNOWN = '-',
  VERTICAL = 'Z',
  NORTH_SOUTH = 'N',
  EAST_WEST = 'E',
  TRIAXIAL_A = 'A',
  TRIAXIAL_B = 'B',
  TRIAXIAL_C = 'C',
  TRANSVERSE = 'T',
  RADIAL = 'R',
  ORTHOGONAL_1 = '1',
  ORTHOGONAL_2 = '2',
  ORTHOGONAL_3 = '3',
  OPTIONAL_U = 'U',
  OPTIONAL_V = 'V',
  OPTIONAL_W = 'W'
}

/**
 * Location information
 *
 * @JsonProperty("latitudeDegrees") double latitudeDegrees,
 * @JsonProperty("longitudeDegrees") double longitudeDegrees,
 * @JsonProperty("depthKm") double depthKm,
 * @JsonProperty("elevationKm") double elevationKm)
 */
export interface Location {
  latitudeDegrees: number;
  longitudeDegrees: number;
  elevationKm: number;
  depthKm: number;
}

/**
 * Relative position information
 *
 * @JsonProperty("northDisplacementKm") double northDisplacementKm,
 * @JsonProperty("eastDisplacementKm") double eastDisplacementKm,
 * @JsonProperty("verticalDisplacementKm") double verticalDisplacementKm)
 */
export interface RelativePosition {
  northDisplacementKm: number;
  eastDisplacementKm: number;
  verticalDisplacementKm: number;
}
