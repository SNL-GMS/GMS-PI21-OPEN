import { StationTypes } from '@gms/common-model';
import Immutable from 'immutable';

import { getToolbarItemDefs } from '../../../../../src/ts/components/analyst-ui/components/station-properties/toolbar-item-defs';
import {
  ChannelColumn,
  SiteColumn
} from '../../../../../src/ts/components/analyst-ui/components/station-properties/types';

describe('toolbar item defs', () => {
  const siteColumnsToDisplay = Immutable.Map(
    Object.values(SiteColumn)
      // all columns are visible by default
      .map(v => [v, true])
  );

  const channelColumnsToDisplay = Immutable.Map(
    Object.values(ChannelColumn)
      // all columns are visible by default
      .map(v => [v, true])
  );
  test('has a getToolbarItemDefs function defined', () => {
    expect(getToolbarItemDefs).toBeDefined();
  });
  test('has a getToolbarItemDefs function that matches snapshots', () => {
    const effectiveAtTime: any = ['selectedEffectiveAt'];
    const selectedStation: any = {
      name: 'name',
      type: StationTypes.StationType.WEATHER,
      location: {
        latitudeDegrees: 10,
        longitudeDegrees: 10,
        depthKm: 100,
        description: 'description ',
        elevationKm: 100
      }
    };
    const selectedEffectiveAt = 'selectedEffectiveAt';
    expect(
      getToolbarItemDefs(
        effectiveAtTime,
        selectedStation,
        'stations name',
        selectedEffectiveAt,
        siteColumnsToDisplay,
        channelColumnsToDisplay,
        jest.fn(),
        jest.fn(),
        jest.fn()
      )
    ).toMatchSnapshot();
  });
  test('has a getToolbarItemDefs function that matches snapshots with empty station', () => {
    const effectiveAtTime: any = ['selectedEffectiveAt'];
    // empty station
    const selectedStation: any = {
      name: 'name'
    };
    const selectedEffectiveAt = 'selectedEffectiveAt';
    expect(
      getToolbarItemDefs(
        effectiveAtTime,
        selectedStation,
        'stations name',
        selectedEffectiveAt,
        siteColumnsToDisplay,
        channelColumnsToDisplay,
        jest.fn(),
        jest.fn(),
        jest.fn()
      )
    ).toMatchSnapshot();
  });
  test('has a getToolbarItemDefs function that matches snapshots with null station', () => {
    const effectiveAtTime: any = ['selectedEffectiveAt'];
    expect(
      getToolbarItemDefs(
        effectiveAtTime,
        null,
        null,
        null,
        null,
        null,
        jest.fn(),
        jest.fn(),
        jest.fn()
      )
    ).toMatchSnapshot();
  });
});
