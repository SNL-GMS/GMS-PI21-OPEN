import { QueryReturnValue } from '@reduxjs/toolkit/dist/query/baseQueryTypes';
import {
  FetchArgs,
  FetchBaseQueryError,
} from '@reduxjs/toolkit/dist/query/react';
import { MaybePromise } from '@reduxjs/toolkit/dist/query/tsHelpers';
import { MonitorTypesWithThresholds } from './retrieve-thresholds-for-monitors-in-station';

/**
 * Type of data returned from query to get thresholds for channels
 * Type is a string | number since type can be percentage or duration
 */
export interface ChannelWithThresholds extends MonitorTypesWithThresholds {
  channelName: string;
}

/**
 * Function that returns an array of promises where each promise is a query to the config service
 * to get the thresholds for channel for a specific monitor type for a single station
 *
 * @param stationName name of the station
 * @param monitorType monitor type to get info for
 * @param channelNames list of channels names to get thresholds for
 * @param baseQuery base query function from rtk
 * @returns array of promises object of with threshold data for channels
 */
export const retrieveThresholdsForChannelForMonitorInStation = async (
  stationName: string,
  monitorType: string,
  channelNames: string[],
  baseQuery: (
    arg: string | FetchArgs
  ) => MaybePromise<
    QueryReturnValue<
      { goodThreshold: string | number; marginalThreshold: string | number },
      FetchBaseQueryError,
      {}
    >
  >
): Promise<ChannelWithThresholds[]> => {
  try {
    return await Promise.all<ChannelWithThresholds>(
      channelNames.map(async (channelName) => {
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
                criterion: 'StationName',
                value: stationName,
              },
              {
                criterion: 'MonitorType',
                value: monitorType,
              },
              {
                criterion: 'ChannelName',
                value: channelName,
              },
            ],
          },
        });

        if (!result.data) {
          throw new Error(JSON.stringify(result.error));
        }
        return {
          monitorType: monitorType,
          channelName: channelName,
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
