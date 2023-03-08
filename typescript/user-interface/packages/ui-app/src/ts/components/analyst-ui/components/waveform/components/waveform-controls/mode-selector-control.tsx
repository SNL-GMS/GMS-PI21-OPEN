import type { ToolbarTypes } from '@gms/ui-core-components';
import { DropdownToolbarItem } from '@gms/ui-core-components';
import { AnalystWorkspaceTypes } from '@gms/ui-state';
import * as React from 'react';

const buildModeSelector = (
  measurementMode: AnalystWorkspaceTypes.MeasurementMode,
  setMode: (mode: AnalystWorkspaceTypes.WaveformDisplayMode) => void,
  widthPx: number,
  key: string | number
): ToolbarTypes.ToolbarItemElement => (
  <DropdownToolbarItem
    key={key}
    label="Mode"
    tooltip="Set the display mode"
    value={measurementMode.mode}
    disabled
    onChange={value => setMode(value)}
    dropdownOptions={AnalystWorkspaceTypes.WaveformDisplayMode}
    widthPx={widthPx}
  />
);

/**
 * Creates a mode dropdown item for a toolbar
 *
 * @param measurementMode The current mode
 * @param setMode must be referentially stable (not created every render)
 * @param widthPx width of the toolbar item
 * @param key must be unique
 * @returns a memoized mode dropdown configuration object, which is referentially stable
 * if the provided parameters don't change.
 */
export const useModeControl = (
  measurementMode: AnalystWorkspaceTypes.MeasurementMode,
  setMode: (mode: AnalystWorkspaceTypes.WaveformDisplayMode) => void,
  key: string | number
): ToolbarTypes.ToolbarItemElement => {
  const widthPx = 130;
  return React.useMemo<ToolbarTypes.ToolbarItemElement>(
    () => buildModeSelector(measurementMode, setMode, widthPx, key),
    [measurementMode, setMode, key]
  );
};
