import { uniqBy } from 'lodash';
import { ProcessingStation, ProcessingStationGroups } from './processing-types';

/**
 * @param processingStationGroupData loaded from processing-station-groups.json
 * @returns a list of stations, unique by the station name
 */
export const getStations = (
  processingStationGroupData: ProcessingStationGroups
): ProcessingStation[] => {
  return uniqBy(
    processingStationGroupData?.flatMap((group) => group?.stations),
    (s) => s.name
  );
};

export const getStationNames = (
  processingStationGroupData: ProcessingStationGroups
): string[] => {
  return getStations(processingStationGroupData)?.map((sta) => sta?.name);
};
