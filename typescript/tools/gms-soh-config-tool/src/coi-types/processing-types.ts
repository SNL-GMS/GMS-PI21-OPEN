export interface Channel {
  name: string;
  canonicalName: string;
  description: string;
  station: string;
  channelDataType: string;
  channelBandType: string;
  channelInstrumentType: string;
  channelOrientationType: string;
  channelOrientationCode: string;
  units: string;
  nominalSampleRateHz: number;
  location: any;
  orientationAngles: any;
  configuredInputs: any[];
  processingDefinition: any;
  processingMetadata: any;
}

export interface ChannelGroup {
  name: string;
  description: string;
  type: string;
  location: any;
  channels: Channel[];
}

export interface ProcessingStation {
  name: string;
  type: string;
  description: string;
  relativePositionsByChannel: any;
  location: any;
  channelGroups: ChannelGroup[];
  channels: Channel[];
}

export interface ProcessingStationGroup {
  name: string;
  description: string;
  stations: ProcessingStation[];
}

export type ProcessingStationGroups = ProcessingStationGroup[];

const isString = (s: unknown): s is string =>
  s != null && typeof s === 'string';

const isNumber = (n: unknown): n is number =>
  n != null && typeof n === 'number';

const isChannel = (ch: any) =>
  ch != null &&
  isString(ch.name) &&
  isString(ch.canonicalName) &&
  isString(ch.description) &&
  isString(ch.station) &&
  isString(ch.channelDataType) &&
  isString(ch.channelBandType) &&
  isString(ch.channelInstrumentType) &&
  isString(ch.channelOrientationType) &&
  isString(ch.channelOrientationCode) &&
  isString(ch.units) &&
  isNumber(ch.nominalSampleRateHz) &&
  ch.location != null &&
  ch.orientationAngles != null &&
  ch.configuredInputs != null &&
  Array.isArray(ch.configuredInputs) &&
  ch.processingDefinition != null &&
  ch.processingMetadata != null;

const isChannelGroup = (chg: any): chg is ChannelGroup =>
  chg != null &&
  isString(chg.name) &&
  isString(chg.description) &&
  isString(chg.type) &&
  chg.location != null &&
  chg.channels.every(isChannel);

export const isStation = (sta: any): sta is ProcessingStation =>
  sta != null &&
  isString(sta.name) &&
  isString(sta.type) &&
  isString(sta.description) &&
  sta.relativePositionsByChannel != null &&
  !sta.location != null &&
  !sta.channelGroups != null &&
  Array.isArray(sta.channelGroups) &&
  sta.channelGroups.every(isChannelGroup) &&
  sta.channels != null &&
  Array.isArray(sta.channels) &&
  sta.channels.every(isChannel);

export const isProcessingStationGroup = (
  psg: any
): psg is ProcessingStationGroup =>
  isString(psg.name) &&
  isString(psg.description) &&
  psg.stations.every(isStation);

export const isProcessingStationGroups = (
  psgs: any
): psgs is ProcessingStationGroup[] =>
  psgs != null && Array.isArray(psgs) && psgs.every(isProcessingStationGroup);
