import type { SohTypes } from '@gms/common-model';

import type { Command } from '~components/common-ui/components/command-palette/types';
import { CommandType } from '~components/common-ui/components/command-palette/types';

/**
 * Creates selection commands, one per station.
 */
export const createSelectionCommands = (
  stationSohList: SohTypes.UiStationSoh[],
  selectedStationIds: string[],
  setSelectedStationIds: (stations: string[]) => void
): Command[] => {
  // Get all stations
  let stationList: string[];
  stationList = stationSohList.map(sSoh => sSoh.stationName);
  // Filter out currently selected station
  if (selectedStationIds && selectedStationIds.length) {
    stationList = stationList.filter(stationName =>
      selectedStationIds.find(selectedStationName => String(selectedStationName) !== stationName)
    );
  }
  if (!stationList) {
    return [];
  }
  return stationList.map(stationName => ({
    commandType: CommandType.SELECT_STATION,
    searchTags: [
      'select',
      stationName,
      `select ${String(stationName)}`,
      'sel',
      `sel ${String(stationName)}`
    ],
    displayText: `${CommandType.SELECT_STATION} ${String(stationName)}`,
    priority: 10,
    action: () => setSelectedStationIds([stationName])
  }));
};
