import type { ToolbarTypes } from '@gms/ui-core-components';
import { DropdownToolbarItem } from '@gms/ui-core-components';
import { AnalystWorkspaceTypes } from '@gms/ui-state';
import type { AlignWaveformsOn } from '@gms/ui-state/lib/app/state/analyst/types';
import * as React from 'react';

const buildStationSort = (
  currentSortType: AnalystWorkspaceTypes.WaveformSortType,
  alignWaveformsOn: AlignWaveformsOn,
  currentOpenEventId: string,
  setSelectedSortType: (sortType: AnalystWorkspaceTypes.WaveformSortType) => void,
  widthPx: number,
  key: string | number
): ToolbarTypes.ToolbarItemElement => {
  const disabledDropdownOptions = [];
  if (!currentOpenEventId) {
    disabledDropdownOptions.push(AnalystWorkspaceTypes.WaveformSortType.distance);
  }
  return (
    <DropdownToolbarItem
      key={key}
      tooltip="Set the sort order of stations"
      label="Sort"
      displayLabel
      value={currentSortType}
      onChange={value => {
        setSelectedSortType(value);
      }}
      dropdownOptions={AnalystWorkspaceTypes.WaveformSortType}
      disabledDropdownOptions={disabledDropdownOptions}
      widthPx={widthPx}
    />
  );
};

/**
 * Creates a sort control for the toolbar, or returns the previously created control if none of the
 * parameters have changed.
 *
 * @param currentSortType on what should we sort
 * @param alignWaveformsOn on what are waveforms aligned
 * @param currentOpenEventId the id of the currently open event
 * @param setSelectedSortType a function to set hte state of the sort. Must be referentially stable
 * @param key must be unique
 * @returns a sort control for the toolbar
 */
export const useStationSortControl = (
  currentSortType: AnalystWorkspaceTypes.WaveformSortType,
  alignWaveformsOn: AlignWaveformsOn,
  currentOpenEventId: string,
  setSelectedSortType: (sortType: AnalystWorkspaceTypes.WaveformSortType) => void,
  key: string | number
): ToolbarTypes.ToolbarItemElement => {
  const widthPx = 130;
  return React.useMemo<ToolbarTypes.ToolbarItemElement>(
    () =>
      buildStationSort(
        currentSortType,
        alignWaveformsOn,
        currentOpenEventId,
        setSelectedSortType,
        widthPx,
        key
      ),
    [currentSortType, alignWaveformsOn, currentOpenEventId, key, setSelectedSortType]
  );
};
