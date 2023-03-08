import { QueryReturnValue } from '@reduxjs/toolkit/dist/query/baseQueryTypes';
import {
  FetchArgs,
  FetchBaseQueryError,
} from '@reduxjs/toolkit/dist/query/react';
import { MaybePromise } from '@reduxjs/toolkit/dist/query/tsHelpers';

/**
 * Type of data returned from query to get thresholds
 * Type is a string | number since type can be percentage or duration
 */
export interface MonitorTypesWithThresholds {
  monitorType: string;
  goodThreshold: string | number;
  marginalThreshold: string | number;
}

/**
 * Function that returns an array of promises where each promise is a query to the config service
 * to get the thresholds for a specific monitor type for a single station
 *
 * @param stationName name of the station
 * @param monitorTypes monitor types to get info for
 * @param baseQuery base query function from rtk
 * @returns array of promises object of with threshold data for monitor types
 */
export const retrieveThresholdsForMonitorsInStation = async (
  stationName: string,
  monitorTypes: string[],
  baseQuery: (
    arg: string | FetchArgs
  ) => MaybePromise<
    QueryReturnValue<
      { goodThreshold: string | number; marginalThreshold: string | number },
      FetchBaseQueryError,
      {}
    >
  >
): Promise<MonitorTypesWithThresholds[]> => {
  try {
    return await Promise.all<MonitorTypesWithThresholds>(
      monitorTypes.map(async (monitorType) => {
        const result = await baseQuery({
          method: 'post',
          url: `/ui-processing-configuration-service/resolve`,
          headers: {
            accept: 'application/json',
            'Content-Type': 'application/json',
          },
          body: {
            configName: 'soh-control.soh-monitor-thresholds',
            selectors: [
              {
                criterion: 'MonitorType',
                value: monitorType,
              },
              {
                criterion: 'StationName',
                value: stationName,
              },
            ],
          },
        });

        if (!result.data) {
          throw new Error(JSON.stringify(result.error));
        }
        return {
          monitorType: monitorType,
          goodThreshold: result.data.goodThreshold,
          marginalThreshold: result.data.marginalThreshold,
        };
      })
    );
  } catch (e) {
    console.error(e);
    throw e;
  }
};
