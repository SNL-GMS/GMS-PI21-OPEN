import type Immutable from 'immutable';

import type { Channel, Station } from '../types';

/**
 * Searches thru list of Weavess Channel looking for matching channel
 * uses the Weavess channel id to match
 *
 * @param station Weavess Station
 * @param channelName string channel name i.e. 'AAK.AK01.SHZ'
 * @returns channel Weavess Channel or undefined
 */
export const findChannelInStation = (
  station: Station,
  channelName: string | JSX.Element
): Channel | undefined => {
  if (station.id === channelName) {
    return station.defaultChannel;
  }
  return station.nonDefaultChannels?.find(newChannel => {
    return newChannel.id === channelName;
  });
};

/**
 * Search thru the weavess stations if the channel name matches the parent channel name
 * return the defaultChannel else search child channels for a match
 *
 * @param weavessStations list
 * @param channelName
 * @returns
 */
export const findChannelInStations = (
  weavessStations: Immutable.List<Station> | Station[],
  channelName: string
): Channel | undefined => {
  /** Find the WeavessChannel to check if a waveform is loaded */
  // eslint-disable-next-line no-restricted-syntax
  for (const ws of weavessStations) {
    const channel = findChannelInStation(ws, channelName);
    if (channel) {
      return channel;
    }
  }
  return undefined;
};
