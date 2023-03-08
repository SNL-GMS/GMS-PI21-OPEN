import type { Channel } from '../../src/ts/types';
import { findChannelInStation, findChannelInStations } from '../../src/ts/util';
import { stations } from './__data__/weavess-stations';

describe('test station util', () => {
  test('can find parent and child channels', () => {
    // Test ability to find Weavess Channel based on channel name
    const station = stations[0];
    const channel = station.nonDefaultChannels[0];
    expect(findChannelInStation(station, channel.id)).toMatchSnapshot();
    expect(findChannelInStation(station, station.id)).toMatchSnapshot();
  });

  it('should have function findWeavessChannel', () => {
    // Test finding a station
    let channel: Channel = findChannelInStations(stations, 'PDAR');
    expect(channel.id).toEqual('PDAR');

    // Test finding a channel
    channel = findChannelInStations(stations, 'PDAR.PD10.SHZ');
    expect(channel.id).toEqual('PDAR.PD10.SHZ');

    // Test finding a bogus channel
    channel = findChannelInStations(stations, 'FOO');
    expect(channel).toBeUndefined();
  });
});
