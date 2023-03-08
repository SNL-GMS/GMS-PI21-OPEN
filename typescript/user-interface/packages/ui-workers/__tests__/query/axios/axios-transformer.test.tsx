/* eslint-disable jest/expect-expect */
import { TimeTypes } from '@gms/common-model';
import { MINUTES_IN_HOUR, SECONDS_IN_MINUTES, toEpochSeconds } from '@gms/common-util';
import type { AxiosResponse } from 'axios';
import Axios from 'axios';
import msgpack from 'msgpack-lite';

import { axiosBaseQuery } from '../../../src/ts/query/axios/axios-query';
import {
  axiosDefaultRequestTransformers,
  axiosDefaultResponseTransformers,
  defaultRequestTransformers,
  defaultResponseTransformers,
  deserializeTypeTransformer,
  msgPackDecodeTransformer,
  serializeTypeTransformer
} from '../../../src/ts/query/axios/axios-transformers';

Date.now = jest.fn().mockImplementation(() => 1000);

describe('axios response transformers', () => {
  it('is exported', () => {
    expect(axiosDefaultResponseTransformers).toBeDefined();
    expect(axiosDefaultRequestTransformers).toBeDefined();
    expect(defaultResponseTransformers).toBeDefined();
    expect(defaultRequestTransformers).toBeDefined();
    expect(deserializeTypeTransformer).toBeDefined();
    expect(TimeTypes.JSON_DURATION_NAMES).toBeDefined();
    expect(TimeTypes.JSON_INSTANT_NAMES).toBeDefined();
    expect(msgPackDecodeTransformer).toBeDefined();
    expect(serializeTypeTransformer).toBeDefined();
  });

  it('axios query succeeds with default response transformer', async () => {
    const requestConfig: any = {
      url: 'Someurl'
    };

    const response: AxiosResponse<string> = {
      status: 200,
      config: {},
      headers: {},
      statusText: '',
      data: 'mockedQueryResult'
    };

    Axios.request = jest.fn().mockImplementation(async () => Promise.resolve(response));
    const result = await axiosBaseQuery({ baseUrl: '' })({ requestConfig }, undefined, undefined);
    const data = axiosDefaultResponseTransformers()[0](result.data);
    expect(data).toEqual('mockedQueryResult');
  });

  it('axios query succeeds with no data', async () => {
    const requestConfig: any = {
      url: 'Someurl',
      headers: {
        accept: 'application/json',
        'content-type': 'application/json'
      },
      responseType: 'arraybuffer'
    };

    const response: AxiosResponse<unknown> = {
      status: 200,
      config: {},
      statusText: '',
      headers: {
        'content-type': 'application/json'
      },
      data: undefined
    };

    Axios.request = jest.fn().mockImplementation(async () => Promise.resolve(response));
    const result = await axiosBaseQuery({ baseUrl: '' })({ requestConfig }, undefined, undefined);
    const data = deserializeTypeTransformer(result.data);
    expect(data).toBeUndefined();
  });

  it('axios query succeeds with message pack transformer and json data', async () => {
    const requestConfig: any = {
      url: 'Someurl',
      headers: {
        accept: 'application/json',
        'content-type': 'application/json'
      }
    };

    const response: AxiosResponse<unknown> = {
      status: 200,
      config: {},
      statusText: '',
      headers: {
        'content-type': 'application/json'
      },
      data: 'mockedQueryResult'
    };

    Axios.request = jest.fn().mockImplementation(async () => Promise.resolve(response));
    const result = await axiosBaseQuery({ baseUrl: '' })({ requestConfig }, undefined, undefined);
    const data = axiosDefaultResponseTransformers()[0](
      msgPackDecodeTransformer(result.data, {
        'content-type': 'application/json'
      })
    );
    expect(data).toEqual('mockedQueryResult');
  });

  it('axios query succeeds with message pack transformer', async () => {
    const requestConfig: any = {
      url: 'Someurl',
      headers: {
        accept: 'application/msgpack',
        'content-type': 'application/json'
      },
      responseType: 'arraybuffer'
    };

    const response: AxiosResponse<unknown> = {
      status: 200,
      config: {},
      statusText: '',
      headers: {
        'content-type': 'application/msgpack'
      },
      data: msgpack.encode('mockedQueryResult')
    };

    Axios.request = jest.fn().mockImplementation(async () => Promise.resolve(response));
    const result = await axiosBaseQuery({ baseUrl: '' })({ requestConfig }, undefined, undefined);
    const data = axiosDefaultResponseTransformers()[0](
      msgPackDecodeTransformer(result.data, {
        'content-type': 'application/msgpack'
      })
    );
    expect(data).toEqual('mockedQueryResult');
  });

  it('instance json fields', () => {
    expect(TimeTypes.JSON_INSTANT_NAMES).toEqual([
      'time',
      'startTime',
      'endTime',
      'effectiveAt',
      'effectiveTime',
      'effectiveUntil',
      'creationTime',
      'currentIntervalEndTime',
      'modificationTime',
      'processingStartTime',
      'processingEndTime',
      'referenceTime',
      'travelTime'
    ]);
  });

  it('duration json fields', () => {
    expect(TimeTypes.JSON_DURATION_NAMES).toEqual([
      'duration',
      'currentIntervalDuration',
      'maximumOpenAnythingDuration',
      'minimumRequestDuration',
      'operationalPeriodStart',
      'operationalPeriodEnd',
      'panSingleArrow',
      'panDoubleArrow',
      'period',
      'leadBufferDuration',
      'lagBufferDuration',
      'waveformPanningBoundaryDuration',
      'groupDelaySec',
      'zasZoomInterval'
    ]);
  });

  it('axios query succeeds with message pack and instance type transformer', async () => {
    const dataResponse = {
      startTime: '2010-05-21T03:24:33.123Z',
      endTime: '2010-05-21T03:25:33.123Z',
      effectiveTime: '2010-05-21T04:24:33.123Z',
      creationTime: '2010-05-21T04:24:33.123Z',
      modificationTime: '2010-05-21T03:24:33.123Z',
      processingStartTime: '2010-05-21T03:24:33Z',
      processingEndTime: '2010-05-21T03:24:33.123456Z',
      referenceTime: '2010-05-21T03:24:33.123456Z',
      travelTime: '2010-05-21T03:24:33.123Z',
      duration: 'PT5M',
      badFormats: {
        startTime: '2010/05/21T03:24:33.123Z',
        endTime: '2010-5-21T 3:24:33.123Z',
        effectiveTime: 'my time is 2010-05-21T03:24:33.123Z',
        modificationTime: '2010-05-21 03:24:33.123Z',
        processingStartTime: '2010-05-2103:24:33.123Z',
        processingEndTime: '2010-05-21T03:24:33.123',
        duration: '42M'
      },
      invalid: {
        startTime: null,
        endTime: undefined,
        effectiveTime: ' ',
        modificationTime: 1,
        processingStartTime: 'this is not a time',
        processingEndTime: {
          'a time': '2010-05-21T03:24:33.123'
        },
        duration: 'a bad duration'
      },
      moreInvalid: {
        startTime: 'null',
        endTime: 'undefined',
        effectiveTime: '',
        duration: ''
      },
      additionalInvalid: {
        duration: 'PT5thisisnotaduration',
        badUndefined: {
          duration: undefined
        },
        badNull: {
          duration: null
        }
      },
      multiValueTest: {
        arrivalTime: {
          value: '1970-01-01T00:00:15.000Z',
          standardDeviation: 'P0D'
        },

        startTime: {
          value: '1970-01-01T00:00:45.000Z',
          standardDeviation: 'PT1M'
        },

        travelTime: {
          value: 'PT15S',
          standardDeviation: 'PT1S'
        },

        duration: {
          value: 'PT45S',
          standardDeviation: 'PT3S'
        },

        testNonObjects: { duration: 'PT3S', startTime: '1970-01-01T00:00:12.000Z' }
      }
    };

    const requestConfig: any = {
      url: 'Someurl',
      headers: {
        accept: 'application/msgpack',
        'content-type': 'application/json'
      },
      responseType: 'arraybuffer'
    };

    const response: AxiosResponse<unknown> = {
      status: 200,
      config: {},
      statusText: '',
      headers: {
        'content-type': 'application/msgpack'
      },
      data: msgpack.encode(dataResponse)
    };

    Axios.request = jest.fn().mockImplementation(async () => Promise.resolve(response));
    const result = await axiosBaseQuery({ baseUrl: '' })({ requestConfig }, undefined, undefined);
    const data = axiosDefaultResponseTransformers()[0](
      deserializeTypeTransformer(
        msgPackDecodeTransformer(result.data, {
          'content-type': 'application/msgpack'
        })
      )
    );

    expect(data).toMatchSnapshot();
    expect(toEpochSeconds('2010-05-21T03:24:33.123Z')).toEqual(data.startTime);
    expect(toEpochSeconds('2010-05-21T03:25:33.123Z')).toEqual(data.endTime);
    expect(toEpochSeconds('2010-05-21T04:24:33.123Z')).toEqual(data.effectiveTime);
    expect(toEpochSeconds('2010-05-21T03:24:33.123Z')).toEqual(data.modificationTime);
    expect(toEpochSeconds('2010-05-21T03:24:33Z')).toEqual(data.processingStartTime);
    expect(toEpochSeconds('2010-05-21T03:24:33.123456Z')).toEqual(data.processingEndTime);

    expect(data.endTime - data.startTime).toEqual(SECONDS_IN_MINUTES);
    expect(data.effectiveTime - data.startTime).toEqual(SECONDS_IN_MINUTES * MINUTES_IN_HOUR);
    expect(data.modificationTime - data.startTime).toEqual(0);

    expect(data.multiValueTest.testNonObjects).toEqual({ duration: 3, startTime: 12 });
    expect(data.multiValueTest.arrivalTime).toEqual({
      value: 15,
      standardDeviation: 0
    });
    expect(data.multiValueTest.startTime).toEqual({
      value: 45,
      standardDeviation: 60
    });
    expect(data.multiValueTest.travelTime).toEqual({
      value: 15,
      standardDeviation: 1
    });
    expect(data.multiValueTest.duration).toEqual({
      value: 45,
      standardDeviation: 3
    });
  });

  it('serialization of requests', () => {
    expect(
      serializeTypeTransformer({
        startTime: toEpochSeconds('2010-05-21T03:24:33.123Z'),
        endTime: toEpochSeconds('2010-05-21T03:25:33.123Z'),
        effectiveTime: toEpochSeconds('2010-05-21T04:24:33.123Z'),
        modificationTime: toEpochSeconds('2010-05-21T03:24:33.123Z'),
        processingStartTime: toEpochSeconds('2010-05-21T03:24:33Z'),
        processingEndTime: '2010-05-21T03:24:33.123456Z',
        duration: 'PT5M'
      })
    ).toMatchInlineSnapshot(`
      Object {
        "duration": "PT5M",
        "effectiveTime": "2010-05-21T04:24:33.123Z",
        "endTime": "2010-05-21T03:25:33.123Z",
        "modificationTime": "2010-05-21T03:24:33.123Z",
        "processingEndTime": "2010-05-21T03:24:33.123456Z",
        "processingStartTime": "2010-05-21T03:24:33.000Z",
        "startTime": "2010-05-21T03:24:33.123Z",
      }
    `);

    expect(
      serializeTypeTransformer({
        startTime: toEpochSeconds('2010-05-21T03:24:33.123Z'),
        endTime: toEpochSeconds('2010-05-21T03:25:33.123Z'),
        effectiveTime: toEpochSeconds('2010-05-21T04:24:33.123Z'),
        modificationTime: toEpochSeconds('2010-05-21T03:24:33.123Z'),
        processingStartTime: toEpochSeconds('2010-05-21T03:24:33Z'),
        processingEndTime: toEpochSeconds('2010-05-21T03:24:33.123456Z'),
        duration: 300
      })
    ).toMatchInlineSnapshot(`
      Object {
        "duration": "PT5M",
        "effectiveTime": "2010-05-21T04:24:33.123Z",
        "endTime": "2010-05-21T03:25:33.123Z",
        "modificationTime": "2010-05-21T03:24:33.123Z",
        "processingEndTime": "2010-05-21T03:24:33.123Z",
        "processingStartTime": "2010-05-21T03:24:33.000Z",
        "startTime": "2010-05-21T03:24:33.123Z",
      }
    `);
  });

  it('serializes feature prediction values', () => {
    expect(
      serializeTypeTransformer({
        testData: {
          arrivalTime: {
            value: 15,
            standardDeviation: null
          },

          startTime: {
            value: 45,
            standardDeviation: 60
          },

          travelTime: {
            value: 15,
            standardDeviation: 1
          },

          measuredValue: {
            value: 30,
            standardDeviation: 2
          },

          duration: {
            value: 45,
            standardDeviation: 3
          },

          testNonObjects: { duration: 3, startTime: 12 }
        }
      })
    ).toMatchInlineSnapshot(`
      Object {
        "testData": Object {
          "arrivalTime": Object {
            "standardDeviation": "P0D",
            "value": "1970-01-01T00:00:15.000Z",
          },
          "duration": Object {
            "standardDeviation": "PT3S",
            "value": "PT45S",
          },
          "measuredValue": Object {
            "standardDeviation": 2,
            "value": 30,
          },
          "startTime": Object {
            "standardDeviation": "PT1M",
            "value": "1970-01-01T00:00:45.000Z",
          },
          "testNonObjects": Object {
            "duration": "PT3S",
            "startTime": "1970-01-01T00:00:12.000Z",
          },
          "travelTime": Object {
            "standardDeviation": "PT1S",
            "value": "PT15S",
          },
        },
      }
    `);
  });
});
