import type { ToolbarTypes } from '@gms/ui-core-components';
import { SwitchToolbarItem } from '@gms/ui-core-components';
import * as React from 'react';

const buildMeasureWindowSwitch = (
  isMeasureWindowVisible: boolean,
  toggleMeasureWindow: () => void,
  key: string | number
): ToolbarTypes.ToolbarItemElement => (
  <SwitchToolbarItem
    key={key}
    disabled={false}
    label="Measure Window"
    tooltip="Show/Hide Measure Window"
    switchValue={isMeasureWindowVisible}
    onChange={() => toggleMeasureWindow()}
    menuLabel={isMeasureWindowVisible ? 'Hide Measure Window' : 'Show Measure Window'}
  />
);

/**
 * Measure window toolbar control, or returns the measure window control that was previously
 * created if none of the parameters have changed.
 *
 * @param isMeasureWindowVisible whether the measure window is displayed
 * @param toggleMeasureWindow a function to toggle measure window visibility. Must be referentially stable.
 * @param key must be unique
 * @returns a toolbar item for the measure window switch
 */
export const useMeasureWindowControl = (
  isMeasureWindowVisible: boolean,
  toggleMeasureWindow: () => void,
  key: string | number
): ToolbarTypes.ToolbarItemElement =>
  React.useMemo<ToolbarTypes.ToolbarItemElement>(
    () => buildMeasureWindowSwitch(isMeasureWindowVisible, toggleMeasureWindow, key),
    [isMeasureWindowVisible, toggleMeasureWindow, key]
  );
