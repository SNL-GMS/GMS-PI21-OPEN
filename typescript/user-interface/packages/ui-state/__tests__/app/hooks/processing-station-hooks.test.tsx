/* eslint-disable @typescript-eslint/no-magic-numbers */
import type { ConfigurationTypes } from '@gms/common-model';
import React from 'react';
import { Provider } from 'react-redux';
import { create } from 'react-test-renderer';

import {
  useProcessingStationGroupsQuery,
  useProcessingStations
} from '../../../src/ts/app/hooks/processing-station-hooks';
import { getStore } from '../../../src/ts/app/store';
import { processingStationGroups } from '../../__data__/processing-station-data';

const defaultMockStationGroupNamesConfiguration: Partial<ConfigurationTypes.UiSohConfiguration> = {
  displayedStationGroups: ['CD1.1']
};
const mockStationGroupNamesConfiguration = defaultMockStationGroupNamesConfiguration;

jest.mock('../../../src/ts/app/api/ssam-control/ssam-control-api-slice', () => {
  const actual = jest.requireActual('../../../src/ts/app/api/ssam-control/ssam-control-api-slice');
  return {
    ...actual,
    useGetSohConfigurationQuery: jest.fn(() => ({
      data: mockStationGroupNamesConfiguration
    }))
  };
});

jest.mock('../../../src/ts/app/api/processing-station/processing-station-api-slice', () => {
  const actual = jest.requireActual(
    '../../../src/ts/app/api/processing-station/processing-station-api-slice'
  );
  return {
    ...actual,
    useGetProcessingStationGroupsQuery: jest.fn(() => ({
      data: processingStationGroups
    }))
  };
});

describe('processing station hooks', () => {
  it('exists', () => {
    expect(useProcessingStationGroupsQuery).toBeDefined();
    expect(useProcessingStations).toBeDefined();
  });

  it('can use get processing station groups', () => {
    const store = getStore();

    function Component() {
      const result = useProcessingStationGroupsQuery();
      return <div>{JSON.stringify(result.data)}</div>;
    }

    const firstMount = create(
      <Provider store={store}>
        <Component />
      </Provider>
    );

    expect(firstMount.toJSON()).toMatchSnapshot();
  });

  it('can use get processing station', () => {
    const store = getStore();

    function Component() {
      const result = useProcessingStations();
      return <div>{JSON.stringify(result)}</div>;
    }

    const firstMount = create(
      <Provider store={store}>
        <Component />
      </Provider>
    );

    expect(firstMount.toJSON()).toMatchSnapshot();
  });
});
