import type { StationTypes } from '@gms/common-model';
import flatMap from 'lodash/flatMap';
import sortBy from 'lodash/sortBy';
import uniq from 'lodash/uniq';
import React from 'react';

import { useGetProcessingStationGroupNamesConfigurationQuery } from '../api/processing-configuration';
import type { StationQuery } from '../api/station-definition';
import {
  useGetStationGroupsByNamesQuery,
  useGetStationsQuery,
  useGetStationsWithChannelsQuery
} from '../api/station-definition';
import { useEffectiveTime } from './operational-time-period-configuration-hooks';
import { useDefaultInteractiveAnalysisStationGroup } from './processing-analyst-configuration-hooks';
import { useOldQueryDataIfReloading } from './query-util-hooks';
import { useAppSelector } from './react-redux-hooks';
import { useGetVisibleStationsFromStationList } from './waveform-hooks';

/**
 * Queries for provided effective time and station group names, query for all stations.
 *
 * Uses the `useGetStationsQuery` query hook.
 *
 * @returns returns the station query result
 */
const useGetStationGroupsAndGetStationQuery = (
  effectiveTime: number,
  stationGroupNames: string[]
): StationQuery => {
  const stationGroupsByNamesQuery = useGetStationGroupsByNamesQuery({
    effectiveTime,
    stationGroupNames
  });
  const stationGroups = stationGroupsByNamesQuery.data;
  const stationNames = sortBy(uniq(flatMap(stationGroups?.map(x => x.stations.map(y => y.name)))));
  return useGetStationsQuery({ effectiveTime, stationNames });
};

/**
 * Queries for provided effective time and station group, query for all stations.
 *
 * Uses the `useGetStationsQuery` query hook.
 *
 * @returns returns the station query result
 */
export const useGetStationsByStationGroupNameQuery = (
  effectiveTime: number,
  stationGroupName?: string | undefined
): StationQuery => {
  const defaultStationGroupName = useDefaultInteractiveAnalysisStationGroup();
  const stationGroupNames = stationGroupName ? [stationGroupName] : [defaultStationGroupName];
  return useGetStationGroupsAndGetStationQuery(effectiveTime, stationGroupNames);
};

/**
 * Queries for provided effective time, query for all stations for the configured station groups.
 *
 * Uses the `useGetStationsQuery` query hook.
 *
 * @returns returns the station query result
 */
export const useGetAllStationsQuery = (effectiveTime: number): StationQuery => {
  const processingStationGroupNamesConfiguration = useGetProcessingStationGroupNamesConfigurationQuery();
  const stationGroupNames = sortBy(
    processingStationGroupNamesConfiguration.data?.stationGroupNames ?? []
  );
  return useGetStationGroupsAndGetStationQuery(effectiveTime, stationGroupNames);
};

/**
 * Queries for provided effective time, query for all stations for the configured station groups.
 *
 * Uses the `useGetStationsQuery` query hook.
 *
 * @returns returns the station query result
 */
export const useGetAllStationsWithChannelsQuery = (effectiveTime: number): StationQuery => {
  const processingStationGroupNamesConfiguration = useGetProcessingStationGroupNamesConfigurationQuery();
  const stationGroupNames = sortBy(
    processingStationGroupNamesConfiguration.data?.stationGroupNames ?? []
  );

  const stationGroupsByNamesQuery = useGetStationGroupsByNamesQuery({
    effectiveTime,
    stationGroupNames
  });
  const stationGroups = stationGroupsByNamesQuery.data;
  const stationNames = sortBy(uniq(flatMap(stationGroups?.map(x => x.stations.map(y => y.name)))));
  return useGetStationsWithChannelsQuery({ effectiveTime, stationNames });
};

/**
 * Queries for the current station for the current effective time and station group.
 *
 * Uses the `useGetStationsQuery` query hook.
 *
 * @returns returns the station query result
 */
export const useGetCurrentStationsQuery = (): StationQuery => {
  const effectiveTime = useEffectiveTime();
  const stationGroupName = useAppSelector(state => state.app.workflow.stationGroup?.name);
  const stationGroupNames = stationGroupName ? [stationGroupName] : [];
  return useGetStationGroupsAndGetStationQuery(effectiveTime, stationGroupNames);
};

/**
 * Uses all stations query and visible stations list hooks to return an array of stations visible in the waveform display
 *
 * @returns return an array of visible stations
 */
export const useVisibleStations = (): StationTypes.Station[] => {
  const allStationsQuery = useGetAllStationsQuery(useEffectiveTime());
  const getVisibleStationsFromStationList = useGetVisibleStationsFromStationList();
  const stationData = useOldQueryDataIfReloading<StationTypes.Station[]>(allStationsQuery);
  return React.useMemo(() => getVisibleStationsFromStationList(stationData), [
    getVisibleStationsFromStationList,
    stationData
  ]);
};
