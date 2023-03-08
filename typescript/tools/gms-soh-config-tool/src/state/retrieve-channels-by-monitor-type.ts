import { QueryReturnValue } from '@reduxjs/toolkit/dist/query/baseQueryTypes';
import {
  FetchArgs,
  FetchBaseQueryError,
} from '@reduxjs/toolkit/dist/query/react';
import { MaybePromise } from '@reduxjs/toolkit/dist/query/tsHelpers';

export type ChannelsByMonitorType = Record<
  string,
  { channelsMode: string; channels: string[] }
>;

export interface RetrieveChannelsByMonitorTypeQueryProps {
  stationName: string;
}

export enum ChannelMode {
  USE_ALL = 'USE_ALL',
  USE_LISTED = 'USE_LISTED',
}

/**
 * Query to retrieve included channels per monitor type
 *
 * @param stationName name of the station
 * @param baseQuery default query function
 * @returns record of monitor types with value {channelMode, channels}
 */
export const retrieveChannelsByMonitorType = async (
  stationName: string,
  baseQuery: (
    arg: string | FetchArgs
  ) => MaybePromise<
    QueryReturnValue<ChannelsByMonitorType, FetchBaseQueryError, {}>
  >
): Promise<ChannelsByMonitorType> => {
  try {
    const result = await baseQuery({
      method: 'post',
      url: `/ui-processing-configuration-service/resolve`,
      headers: {
        accept: 'application/json',
        'Content-Type': 'application/json',
      },
      body: {
        configName: 'soh-control.channels-by-monitor-type',
        selectors: [
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
    return result.data;
  } catch (e) {
    console.error(e);
    throw e;
  }
};
