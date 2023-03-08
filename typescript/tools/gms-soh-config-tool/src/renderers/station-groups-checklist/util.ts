import { includes } from 'lodash';
import { StationGroupsDefinition } from '../../state/retrieve-station-groups';
import { StationGroup } from '../../state/station-controls-slice';

export const ALL_STATION_GROUP_NAME = 'ALL';

/**
 * Uses station definitions and station groups to determine what groups the
 * Selected station is in
 *
 * @param stationName name of the station
 * @param stationGroups station groups from query
 * @param stationDefinitions station definitions read from processing-station-group-definition.json on disk
 * @returns StationGroups array with included info
 */
export const determineGroupsForStation = (
  stationName: string | null,
  stationGroups: string[] | undefined,
  stationDefinitions: StationGroupsDefinition[]
): StationGroup[] => {
  if (!stationName || !stationGroups || !stationDefinitions) return [];
  let groups: StationGroup[] = [];

  // ALL group is a special case and handles differently
  if (includes(stationGroups, ALL_STATION_GROUP_NAME)) {
    groups.push({ name: ALL_STATION_GROUP_NAME, included: true });
  }
  stationDefinitions.forEach((definition) => {
    if (
      includes(stationGroups, definition.name) &&
      includes(definition.stationNames, stationName)
    ) {
      groups.push({ name: definition.name, included: true });
    } else {
      groups.push({ name: definition.name, included: false });
    }
  });
  return groups;
};
