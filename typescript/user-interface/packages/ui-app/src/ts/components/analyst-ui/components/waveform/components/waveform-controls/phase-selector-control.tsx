import type { CommonTypes } from '@gms/common-model';
import type { ToolbarTypes } from '@gms/ui-core-components';
import { PopoverButtonToolbarItem } from '@gms/ui-core-components';
import * as React from 'react';

import { PhaseSelectionMenu } from '~analyst-ui/common/dialogs';
import { analystUiConfig } from '~analyst-ui/config';

const buildPhaseSelectionDropdown = (
  defaultSignalDetectionPhase: CommonTypes.PhaseType,
  setDefaultSignalDetectionPhase: (phase: CommonTypes.PhaseType) => void,
  widthPx: number,
  key: string | number
): ToolbarTypes.ToolbarItemElement => {
  const defaultSdPhasesList = analystUiConfig.systemConfig.defaultSdPhases;
  const prioritySdPhasesList = analystUiConfig.systemConfig.prioritySdPhases;

  const phaseSelectionDropDown = (
    <PhaseSelectionMenu
      phase={defaultSignalDetectionPhase}
      sdPhases={defaultSdPhasesList}
      prioritySdPhases={prioritySdPhasesList}
      // eslint-disable-next-line @typescript-eslint/no-empty-function
      onBlur={() => {
        // typescript:S1186: empty object is intentional.
      }}
      onEnterForPhases={phase => {
        setDefaultSignalDetectionPhase(phase);
      }}
      onPhaseClicked={phase => {
        setDefaultSignalDetectionPhase(phase);
      }}
    />
  );
  return (
    <PopoverButtonToolbarItem
      key={key}
      disabled
      label={defaultSignalDetectionPhase}
      menuLabel="Default Phase"
      tooltip="Set default phase of new signal detections"
      popoverContent={phaseSelectionDropDown}
      widthPx={widthPx}
    />
  );
};

/**
 * Creates a phase selection dropdown control for the toolbar, or else returns the previously created
 * one if none of the parameters have changed. Requires referentially stable functions as parameters.
 *
 * @param defaultSignalDetectionPhase
 * @param setDefaultSignalDetectionPhase a setter for the default signal detection phase
 * @param key must be unique
 * @returns the toolbar item that controls the phase
 */
export const usePhaseControl = (
  defaultSignalDetectionPhase: CommonTypes.PhaseType,
  setDefaultSignalDetectionPhase: (phase: CommonTypes.PhaseType) => void,
  key: string | number
): ToolbarTypes.ToolbarItemElement => {
  const widthPx = 88;
  return React.useMemo<ToolbarTypes.ToolbarItemElement>(
    () =>
      buildPhaseSelectionDropdown(
        defaultSignalDetectionPhase,
        setDefaultSignalDetectionPhase,
        widthPx,
        key
      ),
    [defaultSignalDetectionPhase, setDefaultSignalDetectionPhase, key]
  );
};
