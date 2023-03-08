import type { SohStatus } from '@gms/ui-state';

import type { Command } from '~components/common-ui/components/command-palette/types';
import { CommandType } from '~components/common-ui/components/command-palette/types';

/**
 * Creates the acknowledgement commands, one for each station that needs attention,
 * and one for the currently selected stations, if it needs attention
 */
export const createAcknowledgementCommands = (
  sohStatus: SohStatus,
  selectedStationIds: string[],
  showAcknowledgeOverlayForStations: (stations: string[], comment?: string) => void
): Command[] => {
  const stationsThatNeedAttention = sohStatus?.stationAndStationGroupSoh?.stationSoh?.filter(
    stations => stations?.needsAttention
  );
  if (!stationsThatNeedAttention) {
    return [];
  }

  const doSelectedStationsNeedAttention = selectedStationIds.reduce<boolean>(
    (doAllNeedAttention: boolean, stationId: string) =>
      doAllNeedAttention &&
      stationsThatNeedAttention.find(
        stationThatNeedsAttention => stationThatNeedsAttention.id === stationId
      ) !== undefined,
    true
  );

  const ackCommands: Command[] = [
    ...stationsThatNeedAttention.map(station => ({
      commandType: CommandType.ACKNOWLEDGE,
      searchTags: ['ack', 'acknowledge', `ack ${station.stationName}`, station.stationName],
      displayText: `${CommandType.ACKNOWLEDGE} ${station.stationName}`,
      priority: 1,
      action: () => showAcknowledgeOverlayForStations([station.stationName])
    }))
  ];
  if (selectedStationIds.length > 0 && doSelectedStationsNeedAttention) {
    ackCommands.push({
      commandType: CommandType.ACKNOWLEDGE,
      searchTags: ['ack', 'acknowledge', 'all', 'selected', 'stations', 'ack all'],
      displayText: `${CommandType.ACKNOWLEDGE} All Selected Stations (${selectedStationIds.length})`,
      priority: 2,
      action: () => showAcknowledgeOverlayForStations(selectedStationIds)
    });
  }
  return ackCommands;
};
