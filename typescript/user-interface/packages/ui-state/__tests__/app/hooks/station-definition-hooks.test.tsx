/* eslint-disable @typescript-eslint/no-magic-numbers */
import type { ConfigurationTypes } from '@gms/common-model';
import { StationTypes } from '@gms/common-model';
import React from 'react';
import { Provider } from 'react-redux';
import { create } from 'react-test-renderer';

import {
  useGetAllStationsQuery,
  useGetCurrentStationsQuery,
  useGetStationsByStationGroupNameQuery
} from '../../../src/ts/app/hooks/station-definition-hooks';
import { analystActions } from '../../../src/ts/app/state/analyst/analyst-slice';
import { workflowActions } from '../../../src/ts/app/state/workflow/workflow-slice';
import { getStore } from '../../../src/ts/app/store';

const defaultMockStationGroup: StationTypes.StationGroup[] = [
  {
    description: 'test group',
    effectiveAt: 123,
    effectiveUntil: 456,
    name: 'test group name',
    stations: [
      {
        name: 'station name',
        description: 'station description',
        type: StationTypes.StationType.HYDROACOUSTIC,
        effectiveAt: 123,
        effectiveUntil: 456,
        relativePositionsByChannel: undefined,
        location: undefined,
        allRawChannels: [],
        channelGroups: []
      }
    ]
  }
];

const defaultMockStation: StationTypes.Station[] = [
  {
    name: 'station name',
    description: 'station description',
    type: StationTypes.StationType.HYDROACOUSTIC,
    effectiveAt: 123,
    effectiveUntil: 456,
    relativePositionsByChannel: undefined,
    location: undefined,
    allRawChannels: [],
    channelGroups: []
  }
];

const defaultMockAnalystConfiguration: Partial<ConfigurationTypes.ProcessingAnalystConfiguration> = {
  defaultInteractiveAnalysisStationGroup: 'test'
};

const defaultMockStationGroupNamesConfiguration: Partial<ConfigurationTypes.StationGroupNamesConfiguration> = {
  stationGroupNames: ['test']
};

let mockStationGroup = defaultMockStationGroup;
let mockStation = defaultMockStation;
let mockAnalystConfiguration = defaultMockAnalystConfiguration;
let mockStationGroupNamesConfiguration = defaultMockStationGroupNamesConfiguration;

jest.mock(
  '../../../src/ts/app/api/processing-configuration/processing-configuration-api-slice',
  () => {
    const actual = jest.requireActual(
      '../../../src/ts/app/api/processing-configuration/processing-configuration-api-slice'
    );
    return {
      ...actual,
      useGetProcessingAnalystConfigurationQuery: jest.fn(() => ({
        data: mockAnalystConfiguration
      })),
      useGetProcessingStationGroupNamesConfigurationQuery: jest.fn(() => ({
        data: mockStationGroupNamesConfiguration
      }))
    };
  }
);

jest.mock('../../../src/ts/app/api/station-definition/station-definition-api-slice', () => {
  const actual = jest.requireActual(
    '../../../src/ts/app/api/station-definition/station-definition-api-slice'
  );
  return {
    ...actual,
    useGetStationGroupsByNamesQuery: jest.fn(() => ({
      data: mockStationGroup
    })),
    useGetStationsQuery: jest.fn(() => ({
      data: mockStation
    })),
    useGetStationsWithChannelsQuery: jest.fn(() => ({
      data: mockStation
    }))
  };
});

describe('station definition hooks', () => {
  it('exists', () => {
    expect(useGetAllStationsQuery).toBeDefined();
    expect(useGetCurrentStationsQuery).toBeDefined();
    expect(useGetStationsByStationGroupNameQuery).toBeDefined();
  });

  it('can handle undefined', () => {
    mockStationGroup = undefined;
    mockStation = undefined;
    mockAnalystConfiguration = undefined;
    mockStationGroupNamesConfiguration = undefined;

    const store = getStore();

    function Component() {
      // eslint-disable-next-line @typescript-eslint/no-magic-numbers
      const result1 = useGetStationsByStationGroupNameQuery(123456789, 'test');
      const result2 = useGetAllStationsQuery(123456789);
      return (
        <div>
          <div>{JSON.stringify(result1.data)}</div>
          <div>{JSON.stringify(result2.data)}</div>
        </div>
      );
    }

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();
  });

  it('can use get stations by station group name query', () => {
    mockStationGroup = defaultMockStationGroup;
    mockStation = defaultMockStation;
    mockAnalystConfiguration = defaultMockAnalystConfiguration;
    mockStationGroupNamesConfiguration = defaultMockStationGroupNamesConfiguration;

    const store = getStore();

    function Component() {
      // eslint-disable-next-line @typescript-eslint/no-magic-numbers
      const result1 = useGetStationsByStationGroupNameQuery(123456789);
      const result2 = useGetStationsByStationGroupNameQuery(123456789, 'test');
      return (
        <div>
          <div>{JSON.stringify(result1.data)}</div>
          <div>{JSON.stringify(result2.data)}</div>
        </div>
      );
    }

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();
  });

  it('can use get all stations query', () => {
    mockStationGroup = defaultMockStationGroup;
    mockStation = defaultMockStation;
    mockAnalystConfiguration = defaultMockAnalystConfiguration;
    mockStationGroupNamesConfiguration = defaultMockStationGroupNamesConfiguration;

    const store = getStore();

    function Component() {
      // eslint-disable-next-line @typescript-eslint/no-magic-numbers
      const result = useGetAllStationsQuery(123456789);
      return <div>{JSON.stringify(result.data)}</div>;
    }

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();
  });

  it('can use get current station query', () => {
    mockStationGroup = defaultMockStationGroup;
    mockStation = defaultMockStation;
    mockAnalystConfiguration = defaultMockAnalystConfiguration;
    mockStationGroupNamesConfiguration = defaultMockStationGroupNamesConfiguration;

    const store = getStore();

    function Component() {
      const result = useGetCurrentStationsQuery();
      return <div>{JSON.stringify(result.data)}</div>;
    }

    const firstMount = create(
      <Provider store={store}>
        <Component />
      </Provider>
    );

    expect(firstMount.toJSON()).toMatchSnapshot();

    store.dispatch(analystActions.setEffectiveNowTime(123));
    store.dispatch(workflowActions.setStationGroup(mockStationGroup[0]));

    const secondMount = create(
      <Provider store={store}>
        <Component />
      </Provider>
    );

    expect(secondMount.toJSON()).toMatchSnapshot();
  });
});
