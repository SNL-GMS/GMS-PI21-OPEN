import { StationTypes } from '@gms/common-model';
import { toEpochSeconds } from '@gms/common-util';
import { render } from '@testing-library/react';
import * as React from 'react';

import { SiteConfigurationTable } from '../../../../../src/ts/components/analyst-ui/components/station-properties/site-configuration-table';
import { siteColumnsToDisplay } from '../../../../../src/ts/components/analyst-ui/components/station-properties/station-properties-utils';

window.ResizeObserver = jest.fn(() => {
  return { observe: jest.fn(), disconnect: jest.fn(), unobserve: jest.fn() };
});
const globalAny: any = global;
globalAny.ResizeObserver = window.ResizeObserver;
globalAny.DOMRect = jest.fn(() => ({}));

describe('Site Configuration Table', () => {
  const station: StationTypes.Station = {
    name: 'STA',
    effectiveAt: toEpochSeconds('2021-04-20T16:11:31.118870Z'),
    effectiveUntil: toEpochSeconds('2021-04-20T16:11:31.118870Z'),
    relativePositionsByChannel: {
      'Real Channel Name One': {
        northDisplacementKm: 50,
        eastDisplacementKm: 5,
        verticalDisplacementKm: 10
      },
      'Real Channel Name Two': {
        northDisplacementKm: 50,
        eastDisplacementKm: 5,
        verticalDisplacementKm: 10
      }
    },
    channelGroups: [{} as any],
    allRawChannels: [{} as any],
    description: 'This is a test station',
    location: {
      latitudeDegrees: 35.647,
      longitudeDegrees: 100.0,
      depthKm: 50.0,
      elevationKm: 10.0
    },
    type: StationTypes.StationType.SEISMIC_1_COMPONENT
  };
  const { container } = render(
    <SiteConfigurationTable
      selectedChannelGroup="TEST"
      onRowSelection={jest.fn()}
      station={station}
      columnsToDisplay={siteColumnsToDisplay}
    />
  );
  test('can mount', () => {
    expect(container).toBeDefined();
  });
  test('matches snapshot', () => {
    expect(container).toMatchSnapshot();
  });
  test('table useEffect', () => {
    jest.useFakeTimers();
    const setTimeoutSpy = jest.spyOn(global, 'setTimeout');
    const clearTimeoutSpy = jest.spyOn(global, 'clearTimeout');
    const tableWrapper = render(
      <SiteConfigurationTable
        selectedChannelGroup="TEST"
        onRowSelection={jest.fn()}
        station={station}
        columnsToDisplay={siteColumnsToDisplay}
      />
    );
    jest.runOnlyPendingTimers();
    expect(tableWrapper).toBeDefined();
    expect(setTimeoutSpy).toHaveBeenCalled();
    tableWrapper.unmount();
    expect(clearTimeoutSpy).toHaveBeenCalled();
    jest.useRealTimers();
  });
});
