import { ChannelTypes } from '@gms/common-model';
import Immutable from 'immutable';

import { ChannelColumn, SiteColumn } from '~analyst-ui/components/station-properties/types';
import { messageConfig } from '~analyst-ui/config/message-config';

/**
 * ChannelGroupType represents the different groupings of channels and maps them to their human readable display values
 */
const channelGroupTypeForDisplay = Immutable.Map([
  [ChannelTypes.ChannelGroupType.PROCESSING_GROUP, 'Processing Group'],
  [ChannelTypes.ChannelGroupType.SITE_GROUP, 'Site Group'],
  [ChannelTypes.ChannelGroupType.PHYSICAL_SITE, 'Physical Site']
]);

/**
 *  Maps ChannelDataType strings to human readable display values
 */
const channelDataTypeForDisplay = Immutable.Map([
  [ChannelTypes.ChannelDataType.SEISMIC, 'Seismic'],
  [ChannelTypes.ChannelDataType.HYDROACOUSTIC, 'Hydroacoustic'],
  [ChannelTypes.ChannelDataType.INFRASOUND, 'Infrasound'],
  [ChannelTypes.ChannelDataType.WEATHER, 'Weather'],
  [ChannelTypes.ChannelDataType.DIAGNOSTIC_SOH, 'Diagnostic SOH'],
  [ChannelTypes.ChannelDataType.DIAGNOSTIC_WEATHER, 'Diagnostic Weather']
]);

/**
 * Takes a string and returns the first contiguous set of digits in that string
 * as a new string, removing all other characters
 * If the there were no digits in the original string, returns 'Unknown'
 *
 * EX input: 'P2TS'
 * EX output: '2'
 *
 * EX input: 'P23teu234noe'
 * EX output '23'
 *
 * EX input: 'oaeu'
 * EX output: 'Unknown'
 *
 * @param str - the string to be formatted
 */
export function formatTimeShift(str: string): string {
  if (!str || str.length === 0) return messageConfig.invalidCellText;
  const regexMatch = str.match('\\d+');
  const numString: string = regexMatch ? regexMatch.toString() : messageConfig.invalidCellText;
  return numString.length === 0 ? messageConfig.invalidCellText : numString;
}

/**
 * Formats channel group type for display in the station properties table
 * Returns 'Unknown' if the input is invalid
 *
 * @param type
 */
export function getChannelGroupTypeForDisplay(type): string {
  if (!type || type.length === 0) return messageConfig.invalidCellText;

  return channelGroupTypeForDisplay.get(type) ?? messageConfig.invalidCellText;
}

/**
 * Formats channel data type for display in the station properties table
 * Returns 'Unknown' if the input is invalid
 *
 * @param type
 */
export function getChannelDataTypeForDisplay(type): string {
  if (!type || type.length === 0) return messageConfig.invalidCellText;

  return channelDataTypeForDisplay.get(type) ?? messageConfig.invalidCellText;
}

/**
 * This is the set of default columns to be displayed in the site (channel group) table
 * This object gets updated when columns are selected/deselected in the column picker so that state
 * doesn't get lost when switching between channels, or when the column picker is closed
 */

export const siteColumnsToDisplay: Immutable.Map<SiteColumn, boolean> = Immutable.Map([
  ...Object.values(SiteColumn).map<[SiteColumn, boolean]>(v => [v, true])
]);

/**
 * This is the set of default columns to be displayed in the channel table
 * This object gets updated when columns are selected/deselected in the column picker so that state
 * doesn't get lost when switching between channels, or when the column picker is closed
 */
export const channelColumnsToDisplay: Immutable.Map<ChannelColumn, boolean> = Immutable.Map([
  ...Object.values(ChannelColumn).map<[ChannelColumn, boolean]>(v => [v, true]),
  [ChannelColumn.channelDataType, false],
  [ChannelColumn.channelBandType, false],
  [ChannelColumn.channelInstrumentType, false],
  [ChannelColumn.channelOrientationCode, false],
  [ChannelColumn.channelOrientationType, false]
]);
