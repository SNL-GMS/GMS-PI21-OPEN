import type { Faceted } from '../../faceted';
import type {
  Channel,
  ChannelGroup,
  Location,
  RelativePosition
} from '../channel-definitions/channel-definitions';

/**
 * Enumeration representing the different types of stations in the monitoring network.
 */
export enum StationType {
  SEISMIC_3_COMPONENT = 'SEISMIC_3_COMPONENT',
  SEISMIC_1_COMPONENT = 'SEISMIC_1_COMPONENT',
  SEISMIC_ARRAY = 'SEISMIC_ARRAY',
  HYDROACOUSTIC = 'HYDROACOUSTIC',
  HYDROACOUSTIC_ARRAY = 'HYDROACOUSTIC_ARRAY',
  INFRASOUND = 'INFRASOUND',
  INFRASOUND_ARRAY = 'INFRASOUND_ARRAY',
  WEATHER = 'WEATHER',
  UNKNOWN = 'UNKNOWN'
}

/**
 * Represents a group of stations used for processing.
 */
export interface StationGroup extends Faceted {
  name: string;
  description: string;
  effectiveAt: number;
  effectiveUntil: number;
  stations: Station[];
}

/**
 * Represents an installation of monitoring sensors for the purposes of processing.
 * Multiple sensors can be installed at the same station.
 */
export interface Station extends Faceted {
  name: string;
  type: StationType;
  description: string;
  effectiveAt: number;
  effectiveUntil: number;
  relativePositionsByChannel: Record<string, RelativePosition>;
  location: Location;
  channelGroups: ChannelGroup[];
  allRawChannels: Channel[];
}
