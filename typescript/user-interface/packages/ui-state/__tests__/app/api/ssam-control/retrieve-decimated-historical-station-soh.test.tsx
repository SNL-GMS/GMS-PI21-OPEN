/* eslint-disable react/function-component-definition */
/* eslint-disable jest/expect-expect */
/* eslint-disable @typescript-eslint/no-magic-numbers */
import { SohTypes } from '@gms/common-model';
import type { AxiosRequestConfig } from 'axios';
import React from 'react';
import { Provider } from 'react-redux';
import { create } from 'react-test-renderer';

import { config } from '../../../../src/ts/app/api/ssam-control/endpoint-configuration';
import type {
  RetrieveDecimatedHistoricalStationSohQueryProps,
  UiHistoricalSohAsTypedArray
} from '../../../../src/ts/app/api/ssam-control/retrieve-decimated-historical-station-soh';
import {
  convertToTypedArray,
  retrieveDecimatedHistoricalStationSoh,
  transformHistoricalSohData
} from '../../../../src/ts/app/api/ssam-control/retrieve-decimated-historical-station-soh';
import {
  ssamControlApiSlice,
  useRetrieveDecimatedHistoricalStationSohQuery
} from '../../../../src/ts/app/api/ssam-control/ssam-control-api-slice';
import { getStore } from '../../../../src/ts/app/store';
import { sohConfiguration } from '../../../__data__/soh-configuration-query-data';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();

describe('Historical retrieve decimated historical station soh', () => {
  it('is functions exported', () => {
    expect(convertToTypedArray).toBeDefined();
    expect(retrieveDecimatedHistoricalStationSoh).toBeDefined();
    expect(transformHistoricalSohData).toBeDefined();
  });

  const uiHistoricalSohEmpty: SohTypes.UiHistoricalSoh = {
    stationName: 'test',
    calculationTimes: [0, 1, 2, 3],
    monitorValues: [],
    percentageSent: 100
  };

  it('convert to TypedArray of empty/undefined', async () => {
    expect(await convertToTypedArray(0, 3, uiHistoricalSohEmpty)).toEqual([]);
  });

  it('convert to TypedArray of bad data', async () => {
    const uiHistoricalSohBad: SohTypes.UiHistoricalSoh = {
      stationName: 'test',
      calculationTimes: [0, 1, 2],
      monitorValues: [
        {
          channelName: 'testChan0',
          values: {
            values: [10, 20, 30, 40],
            type: SohTypes.SohValueType.DURATION
          },
          average: 70
        },
        {
          channelName: 'testChan1',
          values: {
            values: [2, 7, 9, 32],
            type: SohTypes.SohValueType.DURATION
          },
          average: 26
        }
      ],
      percentageSent: 100
    };

    await expect(convertToTypedArray(0, 2, uiHistoricalSohBad)).rejects.toThrow(
      'Typed array conversion failed; requires equal length values 3 4'
    );
  });

  it('convert to TypedArray of defined with duration', async () => {
    const uiHistoricalSohDuration: SohTypes.UiHistoricalSoh = {
      stationName: 'test',
      calculationTimes: [0, 1, 2, 3],
      monitorValues: [
        {
          channelName: 'testChan0',
          values: {
            values: [10, 20, 30, 40],
            type: SohTypes.SohValueType.DURATION
          },
          average: 70.4
        },
        {
          channelName: 'testChan1',
          values: {
            values: [2, 7, 9, 32],
            type: SohTypes.SohValueType.DURATION
          },
          average: 26.4
        }
      ],
      percentageSent: 100
    };

    const result = [
      {
        average: 70.4,
        channelName: 'testChan0',
        type: SohTypes.SohValueType.DURATION,
        values: new Float32Array([0, 10, 33.33333206176758, 20, 66.66666412353516, 30, 100, 40])
      },
      {
        average: 26.4,
        channelName: 'testChan1',
        type: SohTypes.SohValueType.DURATION,
        values: new Float32Array([0, 2, 33.33333206176758, 7, 66.66666412353516, 9, 100, 32])
      }
    ];

    expect(await convertToTypedArray(0, 3, uiHistoricalSohDuration)).toEqual(result);
  });

  it('convert to TypedArray of defined with percent', async () => {
    const uiHistoricalSohPercent: SohTypes.UiHistoricalSoh = {
      stationName: 'test',
      calculationTimes: [0, 1, 2, 3],
      monitorValues: [
        {
          channelName: 'testChan0',
          values: {
            values: [10, 20, 30, 40],
            type: SohTypes.SohValueType.PERCENT
          },
          average: 70.4
        },
        {
          channelName: 'testChan1',
          values: {
            values: [2, 7, 9, 32],
            type: SohTypes.SohValueType.PERCENT
          },
          average: 26.4
        }
      ],
      percentageSent: 100
    };

    const result = [
      {
        average: 70.4,
        channelName: 'testChan0',
        type: SohTypes.SohValueType.PERCENT,
        values: new Float32Array([0, 10, 33.33333206176758, 20, 66.66666412353516, 30, 100, 40])
      },
      {
        average: 26.4,
        channelName: 'testChan1',
        type: SohTypes.SohValueType.PERCENT,
        values: new Float32Array([0, 2, 33.33333206176758, 7, 66.66666412353516, 9, 100, 32])
      }
    ];

    expect(await convertToTypedArray(0, 3, uiHistoricalSohPercent)).toEqual(result);
  });

  it('handle historical SOH query data - no data', async () => {
    const data: SohTypes.UiHistoricalSoh = {
      calculationTimes: [],
      stationName: `station name`,
      monitorValues: [],
      percentageSent: 100
    };

    const result = await transformHistoricalSohData(0, 2)(data);
    expect(result.stationName).toEqual('station name');
    expect(result.calculationTimes).toEqual([]);
    expect(result.minAndMax.xMin).toEqual(-1);
    expect(result.minAndMax.xMax).toEqual(-1);
    expect(result.minAndMax.yMin).toEqual(-1);
    expect(result.minAndMax.yMax).toEqual(-1);
    expect(result.monitorValues).toHaveLength(0);
  });

  it('handle historical SOH query data - duration', async () => {
    const data: SohTypes.UiHistoricalSoh = {
      calculationTimes: [0, 1, 2],
      stationName: `station name`,
      monitorValues: [
        {
          channelName: `channel name`,
          average: 6000.7,
          values: {
            values: [5000, 6000, 9000],
            type: SohTypes.SohValueType.DURATION
          }
        }
      ],
      percentageSent: 100
    };

    const result = await transformHistoricalSohData(0, 2)(data);
    expect(result.stationName).toEqual('station name');
    expect(result.calculationTimes).toEqual([0, 0.001, 0.002]);
    expect(result.minAndMax.xMin).toEqual(0);
    expect(result.minAndMax.xMax).toEqual(0.002);
    expect(result.minAndMax.yMin).toEqual(5);
    expect(result.minAndMax.yMax).toEqual(9);
    expect(result.monitorValues).toHaveLength(1);
    expect(result.monitorValues[0].channelName).toEqual('channel name');
    expect(result.monitorValues[0].type).toEqual(SohTypes.SohValueType.DURATION);
    expect(result.monitorValues[0].average).toEqual(6);
    expect(result.monitorValues[0].values).toMatchInlineSnapshot(`
      Float32Array [
        0,
        5,
        0.05000000074505806,
        6,
        0.10000000149011612,
        9,
      ]
    `);
  });

  it('handle historical SOH query data - percent', async () => {
    const data: SohTypes.UiHistoricalSoh = {
      calculationTimes: [0, 1, 2],
      stationName: `station name`,
      monitorValues: [
        {
          channelName: `channel name 1`,
          average: 7,
          values: {
            values: [5, 6, 9],
            type: SohTypes.SohValueType.PERCENT
          }
        },
        {
          channelName: `channel name 2`,
          average: 2,
          values: {
            values: [2, 2, 2],
            type: SohTypes.SohValueType.PERCENT
          }
        }
      ],
      percentageSent: 100
    };

    const result = await transformHistoricalSohData(0, 2)(data);
    expect(result.stationName).toEqual('station name');
    expect(result.calculationTimes).toEqual([0, 0.001, 0.002]);
    expect(result.minAndMax.xMin).toEqual(0);
    expect(result.minAndMax.xMax).toEqual(0.002);
    expect(result.minAndMax.yMin).toEqual(2);
    expect(result.minAndMax.yMax).toEqual(9);
    expect(result.monitorValues).toHaveLength(2);
    expect(result.monitorValues[0].channelName).toEqual('channel name 1');
    expect(result.monitorValues[0].type).toEqual(SohTypes.SohValueType.PERCENT);
    expect(result.monitorValues[0].average).toEqual(7);
    expect(result.monitorValues[0].values).toMatchInlineSnapshot(`
      Float32Array [
        0,
        5,
        0.05000000074505806,
        6,
        0.10000000149011612,
        9,
      ]
    `);
    expect(result.monitorValues[1].channelName).toEqual('channel name 2');
    expect(result.monitorValues[1].type).toEqual(SohTypes.SohValueType.PERCENT);
    expect(result.monitorValues[1].average).toEqual(2);
    expect(result.monitorValues[1].values).toMatchInlineSnapshot(`
      Float32Array [
        0,
        2,
        0.05000000074505806,
        2,
        0.10000000149011612,
        2,
      ]
    `);
  });

  it('hook queries for decimated historical station soh', () => {
    Object.assign(ssamControlApiSlice, {
      ...ssamControlApiSlice,
      useGetSohConfigurationQuery: jest.fn(() => ({
        data: sohConfiguration,
        isLoading: false
      }))
    });
    const params: SohTypes.RetrieveDecimatedHistoricalStationSohInput = {
      startTime: 200,
      endTime: 500,
      samplesPerChannel: 50,
      sohMonitorType: SohTypes.SohMonitorType.ENV_AUTHENTICATION_SEAL_BROKEN,
      stationName: 'test station name'
    };

    const TestComponent: React.FC = () => {
      const query = useRetrieveDecimatedHistoricalStationSohQuery(params);
      return <div>{JSON.stringify(query.data)}</div>;
    };

    const TestComponentWithProvider: React.FC = () => (
      <Provider store={getStore()}>
        <TestComponent />
      </Provider>
    );

    const wrapper = create(<TestComponentWithProvider />);
    expect(wrapper.toJSON()).toMatchSnapshot();
  });

  it('can retrieve and process decimated Historical Station Soh', async () => {
    const requestConfig: AxiosRequestConfig<RetrieveDecimatedHistoricalStationSohQueryProps> = {
      ...config.ssamControl.services.retrieveDecimatedHistoricalStationSoh.requestConfig,
      data: {
        data: {
          endTime: 100,
          startTime: 0,
          stationName: undefined,
          samplesPerChannel: 10000,
          sohMonitorType: SohTypes.SohMonitorType.MISSING
        },
        maxQueryIntervalSize: 200
      }
    };

    const response: UiHistoricalSohAsTypedArray = {
      stationName: 'test',
      calculationTimes: [1, 2, 3, 4],
      monitorValues: [
        {
          average: 3,
          channelName: 'test',
          type: SohTypes.SohValueType.DURATION,
          values: new Float32Array([1, 2, 3, 4, 1, 2, 3, 4])
        }
      ],
      percentageSent: 0,
      minAndMax: { xMax: 0, yMax: 0, xMin: 0, yMin: 0 }
    };

    const baseQuery: any = jest.fn(() => ({
      data: response
    }));

    const results = await retrieveDecimatedHistoricalStationSoh(requestConfig, baseQuery);
    expect(results).toMatchSnapshot();
  });
});
