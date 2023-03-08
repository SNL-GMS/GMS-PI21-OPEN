import type { ChannelTypes } from '@gms/common-model';
import orderBy from 'lodash/orderBy';

// Define the 3C orientations to detect and sort order
// (Z,N,E), (Z,1,2), (U,V,W), (L,Q,T) or (Z,R,T)
// Note this can be moved to a configuration in the future
const threeCOrientations = [
  ['Z', 'N', 'E'],
  ['Z', '1', '2'],
  ['U', 'V', 'W'],
  ['L', 'Q', 'T'],
  ['Z', 'R', 'T']
];

/**
 * Helper to determine if the 3C channel list contains the orientations being looked for.
 *
 * @param channels 3 channel list
 * @param endingLetters 3C ending chars to check against
 * @returns boolean are all in the list of channels
 */
function is3CGroup(channels: ChannelTypes.Channel[], endingLetters: string[]): boolean {
  // Does the list length and channel length match?
  if (channels.length !== endingLetters.length) {
    return false;
  }

  // using a for loop since want to return out of the loop immediately
  // eslint-disable-next-line no-restricted-syntax
  for (const orientationChar of endingLetters) {
    if (channels.find(channel => channel.name.endsWith(orientationChar)) === undefined) {
      return false;
    }
  }
  return true;
}

/**
 * Walks thru 3C orientations and find one that match. If found sort on the orientation order
 * otherwise return sorted in ascending order
 *
 * @param channels to sort
 * @returns sorted channels
 */
function sortChannels(channels: ChannelTypes.Channel[]): ChannelTypes.Channel[] {
  // Figure out which orientation list to use
  // using a for loop since want to return out of the loop immediately
  // eslint-disable-next-line no-restricted-syntax
  for (const orientations of threeCOrientations) {
    if (is3CGroup(channels, orientations)) {
      return orientations.map(o => channels.find(chan => chan.name.endsWith(o)));
    }
  }
  return orderBy(channels, [gc => gc.name], ['asc']);
}

/**
 * Build a map of each channel group (station.site.XY) with a list of associated channels
 *
 * @param channels
 * @returns map<string, ChannelTypes.Channel[]>
 */
function buildChannelMap(channels: ChannelTypes.Channel[]): Map<string, ChannelTypes.Channel[]> {
  const map = new Map<string, ChannelTypes.Channel[]>();
  channels.forEach(chan => {
    const nameComps = chan.name.split('.');
    const chanGroupName = `${nameComps[0]}.${nameComps[1]}.${nameComps[2][0]}${nameComps[2][1]}`;
    if (!map.has(chanGroupName)) {
      map.set(chanGroupName, []);
    }
    map.get(chanGroupName).push(chan);
  });
  return map;
}

/**
 * Sort Channels according to 3C orientation rules.
 * The channels will be sorted based on the grouping station.site.XY(?)
 * i.e. ANMO.LH(?) where ? is in one of the following orientation groups:
 * (Z,N,E), (Z,1,2), (U,V,W), (L,Q,T) or (Z,R,T)
 * with a default of ascending order based on channel name if channels don't
 * match one of the orientation rules
 *
 * @param channels channels to be sorted
 * @returns sorted channels
 */
export function sortStationDefinitionChannels(
  channels: ChannelTypes.Channel[]
): ChannelTypes.Channel[] {
  // sort the channels in ascending order to be safe that each channel group is
  // in alphabetical order
  // eslint-disable-next-line no-param-reassign
  channels = orderBy(channels, [gc => gc.name], ['asc']);

  // builds a map with unique keys for each channel group
  const map = buildChannelMap(channels);

  let sortedChannels: ChannelTypes.Channel[] = [];
  map.forEach((cgChannels: ChannelTypes.Channel[]) => {
    sortedChannels = sortedChannels.concat(sortChannels(cgChannels));
  });
  return sortedChannels;
}
