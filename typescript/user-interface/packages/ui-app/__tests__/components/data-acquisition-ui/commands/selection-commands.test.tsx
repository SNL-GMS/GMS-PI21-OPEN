import type { SohTypes } from '@gms/common-model';

import { createSelectionCommands } from '../../../../src/ts/components/data-acquisition-ui/commands/selection-commands';

describe('Selection commands', () => {
  const stationSohList: Partial<SohTypes.UiStationSoh>[] = [
    { stationName: 'ABC' },
    { stationName: 'TEST' }
  ];
  const selectedStationIds = ['ABC', 'TEST'];
  const setSelectedStationIds = jest.fn();
  const selectionCommands = createSelectionCommands(
    stationSohList as SohTypes.UiStationSoh[],
    selectedStationIds,
    setSelectedStationIds
  );
  it('match snapshot', () => {
    expect(selectionCommands).toMatchSnapshot();
  });

  it('creates a selection command for the provided stations', () => {
    selectedStationIds.forEach(station => {
      const commandForStation = selectionCommands.find(c =>
        c.searchTags.find(tag => tag === station)
      );
      expect(commandForStation).toBeDefined();
    });
  });

  it('sets the selected ids with the correct station', () => {
    selectedStationIds.forEach((station, index) => {
      const commandForStation = selectionCommands.find(c =>
        c.searchTags.find(tag => tag === station)
      );
      commandForStation.action();
      expect(setSelectedStationIds).toHaveBeenCalledWith([selectedStationIds[index]]);
    });
  });
});
