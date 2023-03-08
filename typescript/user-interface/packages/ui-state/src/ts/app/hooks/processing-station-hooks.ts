import type { ProcessingStationTypes } from '@gms/common-model';
import flatMap from 'lodash/flatMap';
import uniqBy from 'lodash/uniqBy';

import { useGetProcessingSohControlStationGroupNamesConfigurationQuery } from '../api/processing-configuration/processing-configuration-api-slice';
import type { ProcessingStationGroupQuery } from '../api/processing-station/processing-station-api-slice';
import { useGetProcessingStationGroupsQuery } from '../api/processing-station/processing-station-api-slice';

/**
 * Query for processing station groups
 *
 * Uses the `useGetProcessingStationGroupsQuery` query hook.
 *
 * @returns returns the processing station group query result
 */
export const useProcessingStationGroupsQuery = (): ProcessingStationGroupQuery => {
  const stationGroupNamesConfiguration = useGetProcessingSohControlStationGroupNamesConfigurationQuery();
  const stationGroupNames = stationGroupNamesConfiguration.data?.stationGroupNames;
  return useGetProcessingStationGroupsQuery(stationGroupNames);
};

/**
 * Query for processing stations
 *
 * @returns returns the processing stations from the useProcessingStationGroupsQuery result
 */
export const useProcessingStations = (): ProcessingStationTypes.ProcessingStation[] => {
  const stationGroupsQuery = useProcessingStationGroupsQuery();
  if (!stationGroupsQuery.data || stationGroupsQuery.data.length === 0) {
    return [];
  }

  // Returns the unique list of Processing stations
  return uniqBy(flatMap(stationGroupsQuery.data.map(sg => sg.stations)), s => s.name);
};
