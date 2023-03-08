/* eslint-disable @typescript-eslint/no-magic-numbers */
import type { ConfigurationTypes } from '@gms/common-model';
import React from 'react';
import { Provider } from 'react-redux';
import { create } from 'react-test-renderer';

import {
  useCurrentIntervalWithBuffer,
  useEffectiveTime,
  useOperationalTimePeriodConfiguration,
  useOperationalTimePeriodTimeRange
} from '../../../src/ts/app/hooks/operational-time-period-configuration-hooks';
import { analystActions } from '../../../src/ts/app/state/analyst/analyst-slice';
import { workflowActions } from '../../../src/ts/app/state/workflow/workflow-slice';
import { getStore } from '../../../src/ts/app/store';

Date.now = jest.fn().mockReturnValue(100);

const mockData: ConfigurationTypes.OperationalTimePeriodConfiguration = {
  operationalPeriodStart: 100,
  operationalPeriodEnd: 200
};

let data = {};
const analystProcessingConfigurationData = {
  leadBufferDuration: 10,
  lagBufferDuration: 10
};

jest.mock(
  '../../../src/ts/app/api/processing-configuration/processing-configuration-api-slice',
  () => {
    const actual = jest.requireActual(
      '../../../src/ts/app/api/processing-configuration/processing-configuration-api-slice'
    );
    return {
      ...actual,
      useGetOperationalTimePeriodConfigurationQuery: jest.fn(() => ({
        data
      })),
      useGetProcessingAnalystConfigurationQuery: jest.fn(() => ({
        analystProcessingConfigurationData
      }))
    };
  }
);

describe('operational time period configuration hooks', () => {
  it('exists', () => {
    expect(useEffectiveTime).toBeDefined();
    expect(useOperationalTimePeriodConfiguration).toBeDefined();
    expect(useOperationalTimePeriodTimeRange).toBeDefined();
    expect(useCurrentIntervalWithBuffer).toBeDefined();
  });

  it('can use effective time', () => {
    const store = getStore();

    function Component() {
      const result = useEffectiveTime();
      return <>{JSON.stringify(result)}</>;
    }

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();
  });

  it('can use operational time period configuration', () => {
    const store = getStore();

    function Component() {
      const result = useOperationalTimePeriodConfiguration();
      return <>{JSON.stringify(result)}</>;
    }

    data = {};

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();

    data = mockData;

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();
  });

  it('can use operational time period time range', () => {
    const store = getStore();
    store.dispatch(analystActions.setEffectiveNowTime(250));

    function Component() {
      const result = useOperationalTimePeriodTimeRange(200, 400);
      return <div>{JSON.stringify(result)}</div>;
    }

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();

    function Component2() {
      const result = useOperationalTimePeriodTimeRange(undefined, undefined);
      return <div>{JSON.stringify(result)}</div>;
    }

    expect(
      create(
        <Provider store={store}>
          <Component2 />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();
  });

  it('can use undefined current interval with buffer', () => {
    const store = getStore();
    store.dispatch(workflowActions.setTimeRange(undefined));

    function Component() {
      const result = useCurrentIntervalWithBuffer();
      return <div>{JSON.stringify(result)}</div>;
    }

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();
  });

  it('can use current interval with buffer', () => {
    const store = getStore();
    store.dispatch(workflowActions.setTimeRange({ startTimeSecs: 200, endTimeSecs: 400 }));

    function Component() {
      const result = useCurrentIntervalWithBuffer();
      return <div>{JSON.stringify(result)}</div>;
    }

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();
  });
});
