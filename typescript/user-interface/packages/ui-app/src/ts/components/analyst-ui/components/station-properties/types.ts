import type { ChannelTypes, StationTypes } from '@gms/common-model';
import type GoldenLayout from '@gms/golden-layout';
import type { DeprecatedToolbarTypes, Row, RowClickedEvent } from '@gms/ui-core-components';
import Immutable from 'immutable';

export interface StationPropertiesComponentProps {
  // passed in from golden-layout
  glContainer?: GoldenLayout.Container;
}
export interface StationPropertiesPanelProps {
  selectedStation: string;
  effectiveAtTimes: string[];
}
export interface StationPropertiesToolbarProps {
  selectedEffectiveAt: string;
  selectedStation: StationTypes.Station;
  stationName: string;
  onEffectiveTimeChange: (args: string) => void;
  effectiveAtTimes: string[];
  widthPx?: number;
  siteColumnsToDisplay: Immutable.Map<SiteColumn, boolean>;
  channelColumnsToDisplay: Immutable.Map<ChannelColumn, boolean>;
  setSelectedSiteColumnsToDisplay: (args: Immutable.Map<SiteColumn, boolean>) => void;
  setSelectedChannelColumnsToDisplay: (args: Immutable.Map<ChannelColumn, boolean>) => void;
}

export interface SiteConfigurationTableProps {
  selectedChannelGroup: string;
  station: StationTypes.Station;
  onRowSelection(event: SiteConfigurationRowClickedEvent);
  columnsToDisplay: Immutable.Map<SiteColumn, boolean>;
}

export interface ChannelConfigurationTableProps {
  channels: ChannelTypes.Channel[];
  stationData: StationTypes.Station;
  columnsToDisplay: Immutable.Map<ChannelColumn, boolean>;
}

export interface ChannelConfigurationRow extends Row {
  name: string;
  channelBandType;
  channelInstrumentType;
  channelOrientationType;
  channelOrientationCode;
  channelDataType;
  nominalSampleRateHz;
  description: string;
  calibrationFactor: string;
  calibrationStandardDeviation: string;
  calibrationPeriod: string;
  calibrationTimeShift: string;
  orientationHorizontalDegrees: string;
  orientationVerticalDegrees: string;
  latitudeDegrees: string;
  longitudeDegrees: string;
  depthKm: string;
  elevationKm: string;
  units: string;
  effectiveAt: string;
  effectiveUntil: string;
  calibrationEffectiveAt: string;
  fapResponseId: string;
  calibrationResponseId: string;
  northDisplacementKm: string;
  eastDisplacementKm: string;
  verticalDisplacementKm: string;
}

/**
 * Table row clicked event
 */
export type SiteConfigurationRowClickedEvent = RowClickedEvent<
  { id: string },
  any,
  number | string
>;

export interface SiteConfigurationRow extends Row {
  name: string;
  description: string;
  effectiveAt: string;
  effectiveUntil: string;
  latitudeDegrees: string;
  longitudeDegrees: string;
  elevationKm: string;
  depthKm: string;
  type: string;
}
export interface PropertiesToolbarItemDefs {
  rightToolbarItemDefs: DeprecatedToolbarTypes.ToolbarItem[];
  leftToolbarItemDefs: DeprecatedToolbarTypes.ToolbarItem[];
}

/**
 * used to populate the values of the site column picker dropdown, and match the values to the table column ids
 */
export enum SiteColumn {
  name = 'name',
  effectiveAt = 'effectiveAt',
  effectiveUntil = 'effectiveUntil',
  latitudeDegrees = 'latitudeDegrees',
  longitudeDegrees = 'longitudeDegrees',
  depthKm = 'depthKm',
  elevationKm = 'elevationKm',
  type = 'type',
  description = 'description'
}

/**
 * used to populate the values of the channel column picker dropdown, and match the values to the table column ids
 */
export enum ChannelColumn {
  name = 'name',
  effectiveAt = 'effectiveAt',
  effectiveUntil = 'effectiveUntil',
  latitudeDegrees = 'latitudeDegrees',
  longitudeDegrees = 'longitudeDegrees',
  depthKm = 'depthKm',
  elevationKm = 'elevationKm',
  nominalSampleRateHz = 'nominalSampleRateHz',
  units = 'units',
  orientationHorizontalDegrees = 'orientationHorizontalDegrees',
  orientationVerticalDegrees = 'orientationVerticalDegrees',
  calibrationFactor = 'calibrationFactor',
  calibrationPeriod = 'calibrationPeriod',
  calibrationEffectiveAt = 'calibrationEffectiveAt',
  calibrationTimeShift = 'calibrationTimeShift',
  calibrationStandardDeviation = 'calibrationStandardDeviation',
  northDisplacementKm = 'northDisplacementKm',
  eastDisplacementKm = 'eastDisplacementKm',
  verticalDisplacementKm = 'verticalDisplacementKm',
  description = 'description',
  channelDataType = 'channelDataType',
  channelBandType = 'channelBandType',
  channelInstrumentType = 'channelInstrumentType',
  channelOrientationCode = 'channelOrientationCode',
  channelOrientationType = 'channelOrientationType',
  calibrationResponseId = 'calibrationResponseId',
  fapResponseId = 'fapResponseId'
}

/**
 * used to match the display strings to values in the site table column picker dropdown
 */
export const siteColumnDisplayStrings: Immutable.Map<SiteColumn, string> = Immutable.Map<
  SiteColumn,
  string
>([
  [SiteColumn.name, 'Name'],
  [SiteColumn.effectiveAt, 'Effective at'],
  [SiteColumn.effectiveUntil, 'Effective until'],
  [SiteColumn.latitudeDegrees, 'Lat (°)'],
  [SiteColumn.longitudeDegrees, 'Lon (°)'],
  [SiteColumn.depthKm, 'Depth (km)'],
  [SiteColumn.elevationKm, 'Elev (km)'],
  [SiteColumn.description, 'Description'],
  [SiteColumn.type, 'Type']
]);

/**
 * used to match the display strings to values in the channel table column picker dropdown
 */
export const channelColumnDisplayStrings: Immutable.Map<ChannelColumn, string> = Immutable.Map<
  ChannelColumn,
  string
>([
  [ChannelColumn.name, 'Name'],
  [ChannelColumn.effectiveAt, 'Effective at'],
  [ChannelColumn.effectiveUntil, 'Effective until'],
  [ChannelColumn.latitudeDegrees, 'Lat (°)'],
  [ChannelColumn.longitudeDegrees, 'Lon (°)'],
  [ChannelColumn.depthKm, 'Depth (km)'],
  [ChannelColumn.elevationKm, 'Elev (km)'],
  [ChannelColumn.nominalSampleRateHz, 'Sample rate (Hz)'],
  [ChannelColumn.units, 'Units'],
  [ChannelColumn.orientationHorizontalDegrees, 'Horiz angle (°)'],
  [ChannelColumn.orientationVerticalDegrees, 'Vert angle (°)'],
  [ChannelColumn.calibrationFactor, 'Calib factor (s)'],
  [ChannelColumn.calibrationPeriod, 'Calib period (s)'],
  [ChannelColumn.calibrationEffectiveAt, 'Calib effective at'],
  [ChannelColumn.calibrationTimeShift, 'Calib time shift (s)'],
  [ChannelColumn.calibrationStandardDeviation, 'Calib std dev'],
  [ChannelColumn.northDisplacementKm, 'North displacement (km)'],
  [ChannelColumn.eastDisplacementKm, 'East displacement (km)'],
  [ChannelColumn.verticalDisplacementKm, 'Vert displacement (km)'],
  [ChannelColumn.description, 'Description'],
  [ChannelColumn.channelDataType, 'Data type'],
  [ChannelColumn.channelBandType, 'Band type'],
  [ChannelColumn.channelInstrumentType, 'Instrument type'],
  [ChannelColumn.channelOrientationCode, 'Orientation code'],
  [ChannelColumn.channelOrientationType, 'Orientation type'],
  [ChannelColumn.calibrationResponseId, 'Calib ID'],
  [ChannelColumn.fapResponseId, 'FAP resp ID']
]);
