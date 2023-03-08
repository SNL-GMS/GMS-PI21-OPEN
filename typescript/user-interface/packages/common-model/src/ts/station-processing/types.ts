import type { Location, StationType } from '../common/types';

// ***************************************
// Model
// ***************************************

/**
 * Processing Station Types
 */

/**
 * Represents a group of stations used for monitoring.
 * This is the processing equivalent of the ReferenceNetwork.
 */
export interface ProcessingStationGroup {
  name: string;
  description: string;
  stations: ProcessingStation[];
}
/**
 * Represents an installation of monitoring sensors for the purposes of processing.
 * Multiple sensors can be installed at the same station.
 */
export interface ProcessingStation {
  name: string;
  type: StationType;
  description: string;
  location: Location;
  channelGroups: ProcessingChannelGroup[];
  channels: ProcessingChannel[];
}

/**
 * ChannelGroupType enum represents the different groupings of channels
 */
export enum ChannelGroupType {
  PROCESSING_GROUP = 'PROCESSING_GROUP',
  SITE_GROUP = 'SITE_GROUP'
}
/** Represents a physical installation (e.g., building, underground vault, borehole)
 * containing a collection of Instruments that produce Raw Channel waveform data.
 */
export interface ProcessingChannelGroup {
  name: string;
  description: string;
  location: Location;
  type: ChannelGroupType;
  channels: ProcessingChannel[];
}

/**
 * Represents a source for unprocessed (raw) or processed (derived) time series data
 * from a seismic, hydroacoustic, or infrasonic sensor.
 */
export interface ProcessingChannel {
  name: string;
  displayName: string;
  canonicalName: string;
  description: string;
  station: string;
  channelDataType: ChannelDataType;
  nominalSampleRateHz: number;
  location: Location;
  orientationAngles: Orientation;
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
