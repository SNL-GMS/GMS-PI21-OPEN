import { SohTypes } from '@gms/common-model';
import type { MinAndMax } from '@gms/common-util';
import {
  chunkRanges,
  findXYMinAndMax,
  MILLISECONDS_IN_SECOND,
  setDecimalPrecisionAsNumber,
  Timer,
  uuid
} from '@gms/common-util';
import { UILogger } from '@gms/ui-util';
import type { AxiosBaseQueryFn, AxiosBaseQueryResult } from '@gms/ui-workers';
import { defaultResponseTransformers } from '@gms/ui-workers';
import type { AxiosRequestConfig, AxiosTransformer } from 'axios';
import * as d3 from 'd3';
import flatMap from 'lodash/flatMap';
import uniq from 'lodash/uniq';

const logger = UILogger.create('GMS_LOG_SSAM_CONTROL', process.env.GMS_LOG_SSAM_CONTROL);

// Map to cancel any outstanding queries when a new query is executed (i.e. usually when selected station changes)
// The map is keyed by the different SohMonitorTypes 'Timeliness', 'Lag' and 'Missing'
const monitorTypeAbortControllerMap: Map<SohTypes.SohMonitorType, AbortController> = new Map();

/**
 * The query props for the query to retrieve decimated historical station SOH
 */
export interface RetrieveDecimatedHistoricalStationSohQueryProps {
  readonly data: SohTypes.RetrieveDecimatedHistoricalStationSohInput;
  readonly maxQueryIntervalSize: number;
}

/**
 * UiHistoricalSoh data that has been converted to
 * use TypedArrays for improved memory management.
 *
 * @see UiHistoricalSoh
 */
export interface UiHistoricalSohAsTypedArray {
  readonly stationName: string;
  readonly calculationTimes: number[];
  readonly monitorValues: MonitorValueAsTypedArray[];
  readonly percentageSent: number;
  readonly minAndMax: MinAndMax;
}

/**
 * MonitorValue data that has been converted to
 * use TypedArrays for improved memory management.
 *
 * @see MonitorValue
 */
export interface MonitorValueAsTypedArray {
  readonly channelName: string;
  readonly values: Float32Array;
  readonly average: number;
  readonly type: SohTypes.SohValueType;
}

/**
 * Converts two arrays (the x-axis values and the y-axis values) into
 * a single typed array (Float32Array). The Float32Array contains both x and y values
 * in the following format: `x, y, x, y, x, y`.
 *
 * NOTE: The conversion to typed arrays provides better memory management. It also
 * allows Weavess to have better performance because it no longer needs to perform
 * this pre-processing, i.e. it requires the Float32Array for WebGL.
 *
 * @param startTime the start time
 * @param endTime the end time
 * @param xValues the x-axis values (in most case time)
 * @param yValues the y-axis values
 */
const convertToFloat32Array = async (
  startTime: number,
  endTime: number,
  xValues: number[],
  yValues: number[]
): Promise<Float32Array> => {
  if (xValues.length !== yValues.length) {
    throw new Error(
      `Typed array conversion failed; requires equal length values ${xValues.length} ${yValues.length}`
    );
  }

  return new Promise<Float32Array>(resolve => {
    const id = uuid.asString();
    Timer.start(`[historical soh util]: convert to float 32 array ${id}`);

    // Filter out all 'Unknown' (-1) yValues along with corresponding xValues
    const filterXValues = [];
    const filteredYValues = yValues.filter((y, index) => {
      if (y !== -1 && y !== null) {
        filterXValues.push(xValues[index]);
      }
      return y !== -1 && y !== null;
    });

    const domain = [startTime, endTime];
    const rangeLow = 0;
    const rangeHigh = 100;
    const timeToGlScale = d3.scaleLinear().domain(domain).range([rangeLow, rangeHigh]);

    const values: Float32Array = new Float32Array(filterXValues.length * 2);

    let n = 0;
    filterXValues.forEach((xValue, idx) => {
      // eslint-disable-next-line no-plusplus
      values[n++] = timeToGlScale(xValue);
      // eslint-disable-next-line no-plusplus
      values[n++] = filteredYValues[idx];
    });
    Timer.end(`[historical soh util]: convert to float 32 array ${id}`);

    resolve(values);
  });
};

/**
 * Converts the provided channel date to a typed array.
 *
 * @param startTime the start time
 * @param endTime the end time
 * @param calculationTimes the calculation times (x-axis values)
 * @param monitorValue the monitor value
 */
const convertChannelToTypedArray = async (
  startTime: number,
  endTime: number,
  calculationTimes: number[],
  monitorValue: SohTypes.MonitorValue
): Promise<MonitorValueAsTypedArray> => {
  const id = uuid.asString();
  Timer.start(`[historical soh util]: convert to typed array ${monitorValue.channelName} ${id}`);
  const data: MonitorValueAsTypedArray = {
    channelName: monitorValue.channelName,
    // get values for monitor type requested
    values: await convertToFloat32Array(
      startTime,
      endTime,
      calculationTimes,
      monitorValue.values.values
    ),
    average:
      monitorValue.average === -1 || monitorValue.average === null
        ? null
        : setDecimalPrecisionAsNumber(monitorValue.average, 2),
    type: monitorValue.values.type
  };
  Timer.end(`[historical soh util]: convert to typed array ${monitorValue.channelName} ${id}`);
  return data;
};

/**
 * Converts the provided data to typed arrays.
 *
 * The conversion to typed arrays provides better memory management.
 *
 * @param startTime the start time
 * @param endTime the end time
 * @param uiHistoricalSoh the SOh historical data
 */
export const convertToTypedArray = async (
  startTime: number,
  endTime: number,
  uiHistoricalSoh: SohTypes.UiHistoricalSoh
): Promise<MonitorValueAsTypedArray[]> => {
  const id = uuid.asString();
  Timer.start(`[historical soh util]: convert to typed array ${id}`);
  // loop through each channel and convert to typed array
  const monitorValues = Promise.all(
    uiHistoricalSoh.monitorValues.map(async monitorValue =>
      convertChannelToTypedArray(startTime, endTime, uiHistoricalSoh.calculationTimes, monitorValue)
    )
  );
  Timer.end(`[historical soh util]: convert to typed array ${id}`);
  return monitorValues;
};

/**
 * Converts the UiHistoricalSoh average, calculation times and values from Milliseconds to Seconds
 *
 * @param response UiHistoricalSoh
 * @param calculationTimes used in populating
 * @returns new UiHistoricalSoh in seconds
 */
const convertUiHistoricalSohInSeconds = (
  response: SohTypes.UiHistoricalSoh
): SohTypes.UiHistoricalSoh => {
  // convert the times to seconds from milliseconds
  const calculationTimes: number[] = response.calculationTimes.map(
    time => time / MILLISECONDS_IN_SECOND
  );

  return {
    ...response,
    calculationTimes,
    monitorValues: response.monitorValues.map<SohTypes.MonitorValue>(mv => {
      // If a duration then convert to seconds from milliseconds if percent leave alone (1)
      const conversionFactor = mv.values.type === SohTypes.SohValueType.DURATION ? 1000 : 1;
      return {
        ...mv,
        average: mv.average !== -1 && mv.average !== null ? mv.average / conversionFactor : null,
        values: {
          ...mv.values,
          values: mv.values.values.map<number>(v => (v !== -1 ? v / conversionFactor : null))
        }
      };
    })
  };
};

/**
 * Customer axios response transformer for converting UiHistoricalSoh to
 * UiHistoricalSohAsTypedArray. This conversion does all of the necessary
 * preprocessing so that the data can be just by WEAVESS.
 *
 * !This conversion is done for performance reasons.
 *
 * @param startTime the start time
 * @param endTime the end time
 *
 * @returns a function that takes in UiHistoricalSoh data and converts it to UiHistoricalSohAsTypedArray
 */
export const transformHistoricalSohData = (
  startTime: number,
  endTime: number
): AxiosTransformer => async (
  response: SohTypes.UiHistoricalSoh
): Promise<UiHistoricalSohAsTypedArray> => {
  const { stationName } = response;

  if (!response || response.calculationTimes?.length === 0) {
    return {
      stationName,
      calculationTimes: [],
      monitorValues: [],
      percentageSent: 0,
      minAndMax: { xMax: -1, yMax: -1, xMin: -1, yMin: -1 }
    };
  }

  Timer.start('[historical soh]: handle historical SOH by station query response');

  const uiHistoricalSohAsSeconds: SohTypes.UiHistoricalSoh = convertUiHistoricalSohInSeconds(
    response
  );

  const monitorValues: MonitorValueAsTypedArray[] = await convertToTypedArray(
    startTime,
    endTime,
    uiHistoricalSohAsSeconds
  );

  // Ensure that monitor values are all sorted by channel name
  monitorValues.sort((a, b) => a.channelName.toString().localeCompare(b.channelName.toString()));

  const filteredValues = flatMap(
    uiHistoricalSohAsSeconds.monitorValues.map(mv =>
      mv.values.values.filter(v => v !== null && v !== -1)
    )
  );
  const minAndMax: MinAndMax =
    filteredValues.length > 0
      ? findXYMinAndMax(uiHistoricalSohAsSeconds.calculationTimes, filteredValues)
      : { xMax: -1, yMax: -1, xMin: -1, yMin: -1 };
  const percentageSent: number = setDecimalPrecisionAsNumber(
    uiHistoricalSohAsSeconds.percentageSent,
    1
  );

  Timer.end('[historical soh]: handle historical SOH by station query response');
  return {
    stationName,
    calculationTimes: uiHistoricalSohAsSeconds.calculationTimes,
    monitorValues,
    percentageSent,
    minAndMax
  };
};

/**
 * The custom historical SOH query using Axios underneath.
 * This will chunk up the interval (if necessary) to all for smaller requests.
 *
 * @param key the unique key for the query
 * @param requestConfig the request configuration for the query request
 */
export const retrieveDecimatedHistoricalStationSoh = async (
  requestConfig: AxiosRequestConfig<RetrieveDecimatedHistoricalStationSohQueryProps>,
  baseQuery: AxiosBaseQueryFn<UiHistoricalSohAsTypedArray>
): Promise<AxiosBaseQueryResult<UiHistoricalSohAsTypedArray>> => {
  try {
    const id = uuid.asString();
    const {
      data: { data, maxQueryIntervalSize }
    } = requestConfig;
    const { samplesPerChannel, sohMonitorType } = data;

    if (monitorTypeAbortControllerMap.has(sohMonitorType)) {
      monitorTypeAbortControllerMap.get(sohMonitorType).abort();
    }
    monitorTypeAbortControllerMap.set(sohMonitorType, new AbortController());

    Timer.start(`[axios]: historicalSohQuery (chunked) ${id}`);
    const ranges = chunkRanges(
      [{ start: data.startTime, end: data.endTime }],
      maxQueryIntervalSize
    );

    const chunks: SohTypes.RetrieveDecimatedHistoricalStationSohInput[] = ranges.map(value => ({
      ...data,
      startTime: value.start,
      endTime: value.end,
      samplesPerChannel: Math.ceil(samplesPerChannel / ranges.length)
    }));

    const transformResponse = [
      ...defaultResponseTransformers,
      transformHistoricalSohData(data.startTime, data.endTime)
    ];

    const responses = (
      await Promise.all<UiHistoricalSohAsTypedArray>(
        chunks.map(async d => {
          const config: AxiosRequestConfig<SohTypes.RetrieveDecimatedHistoricalStationSohInput> = {
            ...requestConfig,
            transformResponse,
            signal: monitorTypeAbortControllerMap.get(sohMonitorType).signal,
            data: d
          };
          return (await baseQuery({ requestConfig: config }, undefined, undefined)).data;
        })
      )
    )
      // remove any responses that returned no results
      .filter(r => r.calculationTimes?.length > 0);

    if (responses.length > 0) {
      // sort the returned results
      responses.sort((a, b) => {
        if (a.calculationTimes[0] < b.calculationTimes[0]) return -1;
        return a.calculationTimes[0] > b.calculationTimes[0] ? 1 : 0;
      });

      const monitorValues = flatMap(responses.map(r => r.monitorValues));
      const channelNames = uniq(flatMap(monitorValues.map(m => m.channelName)));

      const response = {
        data: {
          stationName: data.stationName,
          calculationTimes: flatMap(responses.map(r => r.calculationTimes)),
          monitorValues: channelNames.map(channelName => {
            const channelMonitorValues = monitorValues.filter(mv => mv.channelName === channelName);

            const type: SohTypes.SohValueType = channelMonitorValues
              .map(mv => mv.type)
              .reduce(a => a);

            const values: Float32Array = channelMonitorValues
              .map(mv => mv.values)
              .reduce((a: Float32Array, b: Float32Array) => {
                const v = new Float32Array(a.length + b.length);
                v.set(a);
                v.set(b, a.length);
                return v;
              });

            // Filter out all unknown averages
            const filteredAverages = channelMonitorValues
              .map(mv => mv.average)
              .filter(avg => avg !== null && avg !== -1);

            const average: number =
              filteredAverages.length === 0
                ? null
                : filteredAverages.map(avg => avg).reduce((a: number, b: number) => a + b) /
                  channelMonitorValues.length;

            return {
              channelName,
              values,
              type,
              average
            };
          }),
          percentageSent: setDecimalPrecisionAsNumber(
            responses.map(r => r.percentageSent).reduce((a: number, b: number) => a + b) /
              responses.length,
            1
          ),
          minAndMax: responses
            .map(r => r.minAndMax)
            .reduce((a, b) => ({
              xMin: Math.min(a.xMin, b.xMin),
              xMax: Math.max(a.xMax, b.xMax),
              yMin: Math.min(a.yMin, b.yMin),
              yMax: Math.max(a.yMax, b.yMax)
            }))
        }
      };
      Timer.end(`[axios]: historicalSohQuery (chunked) ${id}`);
      return response;
    }
  } catch (e) {
    if (e.message !== 'canceled') {
      logger.error(`Failed Axios request: ${JSON.stringify(requestConfig)} : ${JSON.stringify(e)}`);
      throw e;
    }
  }
  return {
    data: {
      stationName: undefined,
      calculationTimes: [],
      minAndMax: {
        xMin: -Infinity,
        xMax: Infinity,
        yMin: -Infinity,
        yMax: Infinity
      },
      monitorValues: [],
      percentageSent: 0
    }
  };
};
