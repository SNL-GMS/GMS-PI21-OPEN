import type { ChannelTypes, FacetedTypes, StationTypes } from '@gms/common-model';
import { axiosBaseQuery } from '@gms/ui-workers';
import { createApi } from '@reduxjs/toolkit/query/react';
import sortBy from 'lodash/sortBy';

import type { UseQueryStateResult } from '../../query';
import { useProduceAndHandleSkip } from '../../query/util';
import { config } from './endpoint-configuration';

export interface StationGroupsByNamesProps {
  stationGroupNames: string[];
  effectiveTime: number | undefined;
}

interface StationFacetingDefinition {
  populated: boolean;
  classType: 'Station';
  facetingDefinitions: {
    channels: {
      classType: 'Channel';
      populated: boolean;
      facetingDefinitions: Record<string, unknown>;
    };
    channelGroups: {
      classType: 'ChannelGroup';
      populated: boolean;
      facetingDefinitions: {
        channels: {
          classType: 'Channel';
          populated: boolean;
          facetingDefinitions: Record<string, unknown>;
        };
      };
    };
  };
}

const stationWithChannelsFacetingDefinition: StationFacetingDefinition = {
  populated: true,
  classType: 'Station',
  facetingDefinitions: {
    channels: {
      classType: 'Channel',
      populated: false,
      facetingDefinitions: {}
    },
    channelGroups: {
      classType: 'ChannelGroup',
      populated: true,
      facetingDefinitions: {
        channels: {
          classType: 'Channel',
          populated: true,
          facetingDefinitions: {}
        }
      }
    }
  }
};

export interface StationsProps {
  stationNames: string[];
  effectiveTime: number;
  facetingDefinition?: StationFacetingDefinition;
}

/**
 * Represents an installation of monitoring sensors for the purposes of processing.
 * Multiple sensors can be installed at the same station.
 */
interface OSDStation extends Omit<StationTypes.Station, 'relativePositionsByChannel'> {
  relativePositionChannelPairs: {
    relativePosition: ChannelTypes.RelativePosition;
    channel: FacetedTypes.EntityReference<ChannelTypes.Channel>;
  }[];
}

export interface StationsEffectiveAtTimesProps {
  stationName: string;
  startTime: number;
  endTime: number;
}

export interface ChannelsByNamesProps {
  channelNames: string[];
  effectiveTime: number;
}

/**
 * Convert the relativePositions from the OSD version to the UI version, which converts the
 * relativePositionChannelPairs object into a dictionary for the UI.
 *
 * @param response the original response from the query
 * @returns the converted response
 */
export function convertRelativePositions(response: OSDStation[]): StationTypes.Station[] {
  const uiStations: StationTypes.Station[] = [];
  response.forEach(station => {
    const relativePositionsByChannel: Record<string, ChannelTypes.RelativePosition> = {};
    station.relativePositionChannelPairs.forEach(pair => {
      relativePositionsByChannel[pair.channel.name] = pair.relativePosition;
    });
    const updatedStation = { ...station, relativePositionsByChannel };
    // get rid of the OSD parameter that was set in the spread.
    delete updatedStation.relativePositionChannelPairs;
    uiStations.push(updatedStation);
  });
  return uiStations;
}

/**
 * The station definition api reducer slice.
 */
export const stationDefinitionSlice = createApi({
  reducerPath: 'stationDefinitionApi',
  baseQuery: axiosBaseQuery({
    baseUrl: config.stationDefinition.baseUrl
  }),
  endpoints(build) {
    return {
      /**
       * defines the query for station groups by names
       */
      getStationGroupsByNames: build.query<StationTypes.StationGroup[], StationGroupsByNamesProps>({
        query: (data: StationGroupsByNamesProps) => ({
          requestConfig: {
            ...config.stationDefinition.services.getStationGroupsByNames.requestConfig,
            data: {
              effectiveTime: data.effectiveTime,
              stationGroupNames: sortBy(data.stationGroupNames) // always use a sorted list
            }
          }
        })
      }),

      /**
       * defines the query for stations
       */
      getStations: build.query<StationTypes.Station[], StationsProps>({
        query: (data: StationsProps) => ({
          requestConfig: {
            ...config.stationDefinition.services.getStations.requestConfig,
            data: {
              effectiveTime: data.effectiveTime,
              stationNames: sortBy(data.stationNames), // always use a sorted list
              facetingDefinition: data.facetingDefinition
            }
          }
        }),
        transformResponse: (responseData: OSDStation[]) => {
          return convertRelativePositions(responseData);
        }
      }),

      /**
       * defines the query for stations at effective times (change-times)
       */
      getStationsEffectiveAtTimes: build.query<string[], StationsEffectiveAtTimesProps>({
        query: (data: StationsEffectiveAtTimesProps) => ({
          requestConfig: {
            ...config.stationDefinition.services.getStationsEffectiveAtTimes.requestConfig,
            data: {
              station: {
                name: data.stationName
              },
              startTime: data.startTime,
              endTime: data.endTime
            }
          }
        })
      }),

      /**
       * defines the query for channels by names
       */
      getChannelsByNames: build.query<ChannelTypes.Channel[], ChannelsByNamesProps>({
        query: (data: ChannelsByNamesProps) => ({
          requestConfig: {
            ...config.stationDefinition.services.getChannelsByNames.requestConfig,
            data: {
              channelNames: data.channelNames,
              effectiveTime: data.effectiveTime
            }
          }
        })
      })
    };
  }
});

export type StationGroupsByNamesQuery = UseQueryStateResult<StationTypes.StationGroup[]>;

export interface StationGroupsByNamesQueryProps {
  stationsGroupsByNamesQuery: StationGroupsByNamesQuery;
}

export type StationQuery = UseQueryStateResult<StationTypes.Station[]>;

export interface StationQueryProps {
  stationsQuery: StationQuery;
}

export type StationsEffectiveAtTimesQuery = UseQueryStateResult<string[]>;

export interface StationsEffectiveAtTimesQueryProps {
  stationsEffectiveAtTimes: StationsEffectiveAtTimesQuery;
}

export type ChannelsByNamesQuery = UseQueryStateResult<ChannelTypes.Channel[]>;

export interface ChannelsByNamesQueryProps {
  channelsByNamesQuery: ChannelsByNamesQuery;
}

/**
 * The useGetStationGroupsByNamesQuery hook. Returns the station groups by names.
 *
 * Wraps the original hook from the api slice to allow for reuse of
 * configuration, i.e. specifying when to skip the query.
 *
 * ! this query will be skipped (not executed) if the station group names list is not provided
 *
 * @param data the station groups by names query props (station group names and effective time)
 * @returns the results from the query
 */
export const useGetStationGroupsByNamesQuery = (
  data: StationGroupsByNamesProps
): StationGroupsByNamesQuery => {
  const skip =
    data.stationGroupNames === undefined ||
    data.stationGroupNames === null ||
    data.stationGroupNames.length === 0;
  return useProduceAndHandleSkip<StationTypes.StationGroup[]>(
    stationDefinitionSlice.useGetStationGroupsByNamesQuery(data, { skip }),
    skip
  );
};

/**
 * Checks if Station query props are valid for the Stations query. If not then
 * the query will be skipped; otherwise it will run.
 *
 * @param data the Stations query props
 * @returns true if the query should be skipped; false otherwise
 */
const shouldSkipStationsQuery = (data: Omit<StationsProps, 'facetingDefinition'>): boolean =>
  data.effectiveTime === undefined ||
  data.effectiveTime === null ||
  data.stationNames === undefined ||
  data.stationNames === null ||
  data.stationNames.length === 0;

/**
 * The useGetStationsQuery hook. Returns the stations.
 *
 * Wraps the original hook from the api slice to allow for reuse of
 * configuration, i.e. specifying when to skip the query.
 *
 * ! this query will be skipped (not executed) if the following is not provided
 * !   - a valid effective time
 * !   - a valid non-empty station names list
 *
 * @param data the stations query props (station names and effective time)
 * @returns the results from the query
 */
export const useGetStationsQuery = (
  data: Omit<StationsProps, 'facetingDefinition'>
): StationQuery => {
  const skip = shouldSkipStationsQuery(data);
  return useProduceAndHandleSkip<StationTypes.Station[]>(
    stationDefinitionSlice.useGetStationsQuery(data, { skip }),
    skip
  );
};

/**
 * The useGetStationsWithChannelsQuery hook. Returns the stations with channels.
 *
 * Wraps the original hook from the api slice to allow for reuse of
 * configuration, i.e. specifying when to skip the query.
 *
 * ! this query will be skipped (not executed) if the following is not provided
 * !   - a valid effective time
 * !   - a valid non-empty station names list
 *
 * @param data the stations query props (station names and effective time)
 * @returns the results from the query
 */
export const useGetStationsWithChannelsQuery = (
  data: Omit<StationsProps, 'facetingDefinition'>
): StationQuery => {
  return stationDefinitionSlice.useGetStationsQuery(
    { ...data, facetingDefinition: stationWithChannelsFacetingDefinition },
    {
      skip: shouldSkipStationsQuery(data)
    }
  );
};

/**
 * The useGetStationsEffectiveAtTimesQuery hook. Returns the effective times.
 *
 * Wraps the original hook from the api slice to allow for reuse of
 * configuration, i.e. specifying when to skip the query.
 *
 * ! this query will be skipped (not executed) if the following is not provided
 * !   - a valid station name
 * !   - a valid start time
 * !   - a valid end time
 *
 * @param data the stations effective at times query props (station name, start time, and end time)
 * @returns the results from the query. If skipped, the returned data will be set to `null`.
 */
export const useGetStationsEffectiveAtTimesQuery = (
  data: StationsEffectiveAtTimesProps
): StationsEffectiveAtTimesQuery => {
  const skip =
    data.stationName === undefined ||
    data.stationName === null ||
    data.startTime === undefined ||
    data.startTime === null ||
    data.endTime === undefined ||
    data.endTime === null;

  return useProduceAndHandleSkip<string[]>(
    stationDefinitionSlice.useGetStationsEffectiveAtTimesQuery(data, { skip }),
    skip
  );
};

/**
 * The useGetChannelsByNamesQuery hook. Returns the stations with channels.
 *
 * Wraps the original hook from the api slice to allow for reuse of
 * configuration, i.e. specifying when to skip the query.
 *
 * ! this query will be skipped (not executed) if the following is not provided
 * !   - an array of channel names
 * !   - a valid effective time
 *
 * @param data the channel query props (channel names and effective time)
 * @returns the results from the query
 */
export const useGetChannelsByNamesQuery = (data: ChannelsByNamesProps): ChannelsByNamesQuery => {
  const skip = data.channelNames.length === 0 || data.effectiveTime === null;

  return useProduceAndHandleSkip<ChannelTypes.Channel[]>(
    stationDefinitionSlice.useGetChannelsByNamesQuery(data, { skip }),
    skip
  );
};
