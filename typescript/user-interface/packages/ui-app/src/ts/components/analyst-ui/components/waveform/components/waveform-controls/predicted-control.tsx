import type { ToolbarTypes } from '@gms/ui-core-components';
import { SwitchToolbarItem } from '@gms/ui-core-components';
import { useShouldShowPredictedPhases } from '@gms/ui-state';
import * as React from 'react';

/**
 * Exported for testing purposes
 *
 * @param shouldShowPredictedPhases
 * @param setShouldShowPredictedPhases
 * @param currentOpenEventId
 * @param key
 * @returns
 */
export const buildPredictedDropdown = (
  shouldShowPredictedPhases: boolean,
  setShouldShowPredictedPhases: (showPredicted: boolean) => void,
  currentOpenEventId: string,
  key: string | number
): ToolbarTypes.ToolbarItemElement => (
  <SwitchToolbarItem
    key={key}
    label="Predicted Phases"
    disabled={!currentOpenEventId}
    tooltip="Show/Hide predicted phases"
    onChange={() => setShouldShowPredictedPhases(!shouldShowPredictedPhases)}
    switchValue={shouldShowPredictedPhases}
    menuLabel={shouldShowPredictedPhases ? 'Hide predicted phases' : 'Show predicted phases'}
    cyData="Predicted Phases"
  />
);

/**
 * Creates a toolbar control item for the predicted phases, or returns the previously created one if none of the
 * parameters have changed. Expects referentially stable functions.
 *
 * @param currentOpenEventId
 * @param key must be unique
 * @returns a toolbar control for the predicted phases
 */
export const usePredictedControl = (
  currentOpenEventId: string,
  key: string | number
): ToolbarTypes.ToolbarItemElement => {
  const [shouldShowPredictedPhases, setShouldShowPredictedPhases] = useShouldShowPredictedPhases();
  return React.useMemo<ToolbarTypes.ToolbarItemElement>(
    () =>
      buildPredictedDropdown(
        shouldShowPredictedPhases,
        setShouldShowPredictedPhases,
        currentOpenEventId,
        key
      ),
    [shouldShowPredictedPhases, setShouldShowPredictedPhases, currentOpenEventId, key]
  );
};
