import type { ChannelTypes, StationTypes } from '@gms/common-model';

import type { StationVisibilityChangesDictionary } from '../../../../src/ts/app/state/waveform/types';
import { defaultStationVisibility } from '../../../../src/ts/app/state/waveform/types';
import {
  getAllDisplayedChannels,
  getAllDisplayedChannelsForStation,
  getChangesForHiddenChannel,
  getChangesForVisibleChannel,
  getChannelName,
  getNamesOfAllDisplayedChannels,
  getStationName,
  getVisibleChannels,
  getVisibleStations,
  isChannelVisible,
  isStationExpanded,
  isStationVisible,
  isStationVisibleInChangesDict,
  newStationVisibilityChangesObject
} from '../../../../src/ts/app/state/waveform/util';

describe('Waveform state utils', () => {
  const visibleStation = newStationVisibilityChangesObject('visible', true);
  const hiddenStation = newStationVisibilityChangesObject('hidden', false);
  it('can tell if a station is visible', () => {
    expect(isStationVisible(visibleStation)).toBe(true);
  });

  it('can tell if a station is not visible', () => {
    expect(isStationVisible(hiddenStation)).toBe(false);
  });

  it('can tell if a channel is visible', () => {
    expect(isChannelVisible('visibleChannel', visibleStation)).toBe(true);
  });

  it('can handle station undefined', () => {
    expect(isStationVisible()).toBe(false);
    const undefVisibility = {
      ...visibleStation,
      visibility: undefined
    };
    expect(isStationVisible(undefVisibility)).toBe(false);
  });
  it('can tell if a channel is hidden', () => {
    expect(
      isChannelVisible(
        'hiddenChannel',
        newStationVisibilityChangesObject('withHiddenChannel', true, false, ['withHiddenChannel'])
      )
    ).toBe(true);
  });

  it('can handle channel undefined', () => {
    expect(isChannelVisible('visibleChannel')).toBe(true);
    const undefVisibility = {
      ...visibleStation,
      hiddenChannels: undefined
    };
    // If not in the list then visible
    expect(isChannelVisible('visibleChannel', undefVisibility)).toBe(true);
  });
  it('can tell if a station is expanded', () => {
    expect(
      isStationExpanded(newStationVisibilityChangesObject('expandedStation', true, true))
    ).toBe(true);
  });

  it('can tell if a station is collapsed', () => {
    expect(
      isStationExpanded(newStationVisibilityChangesObject('expandedStation', true, false))
    ).toBe(false);
  });

  it('can handle station expanded undefined', () => {
    expect(isStationExpanded()).toBe(false);
    const undefStationExpanded = {
      ...visibleStation,
      isStationExpanded: undefined
    };
    expect(isStationExpanded(undefStationExpanded)).toBe(false);
  });
  it('correctly categorizes a default station', () => {
    expect(isStationVisible(defaultStationVisibility)).toBe(false);
    expect(isStationExpanded(defaultStationVisibility)).toBe(false);
    expect(isChannelVisible('anything', defaultStationVisibility)).toBe(true);
  });

  describe('getChangesForHiddenChannel', () => {
    test('it can take a channel name as a string', () => {
      const vis = newStationVisibilityChangesObject('stationName');
      const changes = getChangesForHiddenChannel(vis, 'channelName');
      expect(changes).toMatchSnapshot();
    });
    test('it can take a channel as a string', () => {
      const vis = newStationVisibilityChangesObject('stationName');
      const channel: ChannelTypes.Channel = {
        name: 'channelName'
      } as ChannelTypes.Channel;
      const changes = getChangesForHiddenChannel(vis, channel);
      expect(changes).toMatchSnapshot();
    });
    test('it can hide a second channel', () => {
      const vis = newStationVisibilityChangesObject('stationName');
      const changes1 = getChangesForHiddenChannel(vis, 'channelName1');
      const changes2 = getChangesForHiddenChannel(changes1, 'channelName2');
      expect(changes2).toMatchSnapshot();
    });
    test('it returns the original changes object if it is already hidden', () => {
      const vis = newStationVisibilityChangesObject('stationName');
      const changes1 = getChangesForHiddenChannel(vis, 'channel');
      const changes2 = getChangesForHiddenChannel(changes1, 'channel');
      expect(changes2).toBe(changes1);
    });
  });

  test('get station name for station or stationName', () => {
    const stationName = 'AAK';
    expect(getStationName(stationName)).toEqual(stationName);
    const station: StationTypes.Station = {
      name: stationName,
      effectiveAt: 100
    } as StationTypes.Station;
    expect(getStationName(station)).toEqual(stationName);
  });

  test('get channel name for channel or channelName', () => {
    const channelName = 'AAK.AK01.SHZ';
    expect(getChannelName(channelName)).toEqual(channelName);
    const channel: ChannelTypes.Channel = {
      name: channelName,
      effectiveAt: 100
    } as ChannelTypes.Channel;
    expect(getChannelName(channel)).toEqual(channelName);
  });

  describe('StationVisibilityChangesDictionary', () => {
    const stationName = 'AAK';
    const channelName = 'AAK.AK01.SHZ';
    const channel = {
      name: channelName,
      effectiveAt: 100
    } as ChannelTypes.Channel;
    const channelName1 = 'AAK.AK01.SHN';
    const channel1 = {
      name: channelName1,
      effectiveAt: 100
    } as ChannelTypes.Channel;
    const station: StationTypes.Station = {
      name: stationName,
      effectiveAt: 100,
      allRawChannels: [channel, channel1]
    } as StationTypes.Station;
    const visStation = newStationVisibilityChangesObject(stationName, true, true, [channelName1]);
    const sDict: StationVisibilityChangesDictionary = {
      AAK: visStation
    };
    it('can find if station is visibile in dictionary', () => {
      expect(isStationVisibleInChangesDict(sDict, 'AAK')).toBe(true);
    });
    it('can find visibile stations list in dictionary', () => {
      expect(getVisibleStations(sDict, [station])).toMatchSnapshot();
      expect(getVisibleStations(sDict, undefined)).toBeUndefined();
    });
    it('can find changes for visible channels list in dictionary', () => {
      expect(getChangesForVisibleChannel(visStation, channelName)).toMatchSnapshot();
      expect(getChangesForVisibleChannel(visStation, channel1)).toMatchSnapshot();
      const visUndefinedHiddenChannel = newStationVisibilityChangesObject(
        stationName,
        true,
        true,
        undefined
      );
      expect(getChangesForVisibleChannel(visUndefinedHiddenChannel, channel1)).toMatchSnapshot();
    });

    it('can find visible channels from list in dictionary', () => {
      expect(getVisibleChannels(sDict, station)).toMatchSnapshot();
      const localStation: StationTypes.Station = {
        name: stationName,
        effectiveAt: 100,
        allRawChannels: undefined
      } as StationTypes.Station;
      localStation.allRawChannels = undefined;
      expect(getVisibleChannels(sDict, localStation)).toMatchSnapshot();
      expect(getVisibleChannels(sDict, undefined)).toMatchSnapshot();
    });
    it('can find all displayed channels for station from dictionary', () => {
      expect(getAllDisplayedChannelsForStation(sDict, station)).toMatchSnapshot();
    });
    it('can find all displayed channels for stations from dictionary', () => {
      expect(getAllDisplayedChannels(sDict, [station])).toMatchSnapshot();
    });
    it('can find all displayed channel names for stations from dictionary', () => {
      expect(getNamesOfAllDisplayedChannels(sDict, [station])).toMatchSnapshot();
    });
  });
});
