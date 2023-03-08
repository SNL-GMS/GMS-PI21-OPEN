/* eslint-disable react/jsx-no-constructed-context-values */
import { getStore, stationPropertiesConfigurationSlice } from '@gms/ui-state';
import { render } from '@testing-library/react';
import * as React from 'react';
import { Provider } from 'react-redux';
import type * as Redux from 'redux';

import { convertMapToObject } from '~common-ui/common/table-utils';

import { StationPropertiesPanel } from '../../../../../src/ts/components/analyst-ui/components/station-properties';
import { getOnEffectiveTimeChange } from '../../../../../src/ts/components/analyst-ui/components/station-properties/station-properties-panel';
import { channelColumnsToDisplay } from '../../../../../src/ts/components/analyst-ui/components/station-properties/station-properties-utils';
import { BaseDisplayContext } from '../../../../../src/ts/components/common-ui/components/base-display/base-display-context';

window.ResizeObserver = jest.fn(() => {
  return { observe: jest.fn(), disconnect: jest.fn(), unobserve: jest.fn() };
});
const globalAny: any = global;
globalAny.ResizeObserver = window.ResizeObserver;
globalAny.DOMRect = jest.fn(() => ({}));

let isSuccess = true;
let hasData = true;

jest.mock('@gms/ui-state', () => {
  const actual = jest.requireActual('@gms/ui-state');
  return {
    ...actual,
    useGetStationsWithChannelsQuery: jest.fn(() => {
      if (isSuccess) {
        return {
          isSuccess,
          data: hasData
            ? [
                {
                  station: 'station',
                  name: 'test',
                  channelGroups: [{ name: 'channelGroupName' }]
                }
              ]
            : []
        };
      }
      return {
        isSuccess,
        data: []
      };
    })
  };
});

describe('station-properties-panel', () => {
  test('can mount', () => {
    expect(StationPropertiesPanel).toBeDefined();
    expect(getOnEffectiveTimeChange).toBeDefined();
  });
  test('OnEffectiveTimeChange calls functions with correct data', () => {
    const dispatch = jest.fn();
    const effectiveTimes: string[] = ['1234'];
    const newEffectiveAt = '1234';
    const action: Redux.AnyAction = {
      type: stationPropertiesConfigurationSlice.actions.setSelectedEffectiveAt.type,
      payload: 0
    };
    getOnEffectiveTimeChange(dispatch, newEffectiveAt, effectiveTimes);
    expect(dispatch).toHaveBeenCalledWith(action);
  });
  test('matches snapshot', () => {
    isSuccess = true;
    const { container } = render(
      <Provider store={getStore()}>
        <BaseDisplayContext.Provider
          value={{
            glContainer: {} as any,
            widthPx: 100,
            heightPx: 100
          }}
        >
          <StationPropertiesPanel selectedStation="test" effectiveAtTimes={['some time']} />
        </BaseDisplayContext.Provider>
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });
  test('matches snapshot with [] station data', () => {
    isSuccess = true;
    hasData = false;
    const { container } = render(
      <Provider store={getStore()}>
        <BaseDisplayContext.Provider
          value={{
            glContainer: {} as any,
            widthPx: 100,
            heightPx: 100
          }}
        >
          <StationPropertiesPanel selectedStation="test" effectiveAtTimes={['some time']} />
        </BaseDisplayContext.Provider>
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });
  test('map to object conversion', () => {
    const expected = {
      name: true,
      effectiveAt: true,
      effectiveUntil: true,
      latitudeDegrees: true,
      longitudeDegrees: true,
      depthKm: true,
      elevationKm: true,
      nominalSampleRateHz: true,
      units: true,
      orientationHorizontalDegrees: true,
      orientationVerticalDegrees: true,
      calibrationFactor: true,
      calibrationPeriod: true,
      calibrationEffectiveAt: true,
      calibrationTimeShift: true,
      calibrationStandardDeviation: true,
      northDisplacementKm: true,
      eastDisplacementKm: true,
      verticalDisplacementKm: true,
      description: true,
      channelDataType: false,
      channelBandType: false,
      channelInstrumentType: false,
      channelOrientationCode: false,
      channelOrientationType: false,
      calibrationResponseId: true,
      fapResponseId: true
    };
    const actual = convertMapToObject(channelColumnsToDisplay);
    expect(expected).toEqual(actual);
  });
});
