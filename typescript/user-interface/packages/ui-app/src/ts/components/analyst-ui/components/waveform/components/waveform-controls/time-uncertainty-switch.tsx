import type { ToolbarTypes } from '@gms/ui-core-components';
import { SwitchToolbarItem } from '@gms/ui-core-components';
import { useShouldShowTimeUncertainty } from '@gms/ui-state';
import * as React from 'react';

const buildTimeUncertaintySwitch = (
  shouldShowTimeUncertainty: boolean,
  setShouldShowTimeUncertainty: (newValue: boolean) => void,
  key: string | number
): ToolbarTypes.ToolbarItemElement => (
  <SwitchToolbarItem
    label="Time Uncertainty"
    menuLabel={
      shouldShowTimeUncertainty ? 'Hide time uncertainty bars' : 'Show time uncertainty bars'
    }
    tooltip="Show/hide time uncertainty bars on signal detections (Ctrl + Shift + U)"
    key={key}
    switchValue={shouldShowTimeUncertainty}
    onChange={() => setShouldShowTimeUncertainty(!shouldShowTimeUncertainty)}
  />
);

/**
 * Switch that toggles time uncertainty on and off.
 *
 * @param key must be unique
 * @returns a SwitchItem for the toolbar
 */
export const useTimeUncertaintySwitch = (key: string | number): ToolbarTypes.ToolbarItemElement => {
  const [shouldShowTimeUncertainty, setShouldShowTimeUncertainty] = useShouldShowTimeUncertainty();
  return React.useMemo<ToolbarTypes.ToolbarItemElement>(
    () => buildTimeUncertaintySwitch(shouldShowTimeUncertainty, setShouldShowTimeUncertainty, key),
    [key, shouldShowTimeUncertainty, setShouldShowTimeUncertainty]
  );
};
