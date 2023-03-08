/* eslint-disable react/jsx-no-constructed-context-values */
import { StationTypes } from '@gms/common-model';
import { render } from '@testing-library/react';
import Immutable from 'immutable';
import * as React from 'react';

import { StationPropertiesToolbar } from '../../../../../src/ts/components/analyst-ui/components/station-properties/station-properties-toolbar';
import {
  ChannelColumn,
  SiteColumn
} from '../../../../../src/ts/components/analyst-ui/components/station-properties/types';
import { BaseDisplayContext } from '../../../../../src/ts/components/common-ui/components/base-display/base-display-context';

window.ResizeObserver = jest.fn(() => {
  return { observe: jest.fn(), disconnect: jest.fn(), unobserve: jest.fn() };
});
const globalAny: any = global;
globalAny.ResizeObserver = window.ResizeObserver;
globalAny.DOMRect = jest.fn(() => ({}));

describe('station-properties-panel', () => {
  const selectedEffectiveAt = '1970-01-01T00:00:00.000Z';
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
  const onEffectiveTimeChange: (args: any) => void = jest.fn();
  const setSelectedSiteColumnsToDisplay = jest.fn();
  const setSelectedChannelColumnsToDisplay = jest.fn();
  const effectiveAtTimes: string[] = ['1970-01-01T00:00:00.000Z', '1970-01-01T00:00:00.000Z'];
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
  test('can mount', () => {
    expect(StationPropertiesToolbar).toBeDefined();
  });
  test('matches snapshot', () => {
    const { container } = render(
      <BaseDisplayContext.Provider
        value={{
          glContainer: {} as any,
          widthPx: 100,
          heightPx: 100
        }}
      >
        <StationPropertiesToolbar
          stationName="name"
          selectedStation={selectedStation}
          selectedEffectiveAt={selectedEffectiveAt}
          effectiveAtTimes={effectiveAtTimes}
          siteColumnsToDisplay={siteColumnsToDisplay}
          channelColumnsToDisplay={channelColumnsToDisplay}
          onEffectiveTimeChange={onEffectiveTimeChange}
          setSelectedSiteColumnsToDisplay={setSelectedSiteColumnsToDisplay}
          setSelectedChannelColumnsToDisplay={setSelectedChannelColumnsToDisplay}
        />
      </BaseDisplayContext.Provider>
    );
    expect(container).toMatchSnapshot();
  });
});
