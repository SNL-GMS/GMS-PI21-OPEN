import type { CommonTypes } from '@gms/common-model';
import type { ToolbarTypes } from '@gms/ui-core-components';
import { PopoverButtonToolbarItem } from '@gms/ui-core-components';
import { useAppDispatch, waveformActions } from '@gms/ui-state';
import { AlignWaveformsOn } from '@gms/ui-state/lib/app/state/analyst/types';
import sortBy from 'lodash/sortBy';
import * as React from 'react';

import { AlignmentMenu } from '~analyst-ui/common/dialogs';
import { analystUiConfig } from '~analyst-ui/config';

/**
 * @returns a referentially-stable {@link AlignmentMenu} component.
 */
const useAlignmentDropdown = (
  alignWaveformsOn: AlignWaveformsOn,
  _alignablePhases: CommonTypes.PhaseType[],
  phaseToAlignOn: CommonTypes.PhaseType,
  defaultPhaseAlignment: CommonTypes.PhaseType,
  showPredictedPhases: boolean,
  setWaveformAlignment: (
    alignWaveformsOn: AlignWaveformsOn,
    phaseToAlignOn: CommonTypes.PhaseType,
    showPredictedPhases: boolean
  ) => void
) => {
  const defaultSdPhasesList = analystUiConfig.systemConfig.defaultSdPhases;
  const prioritySdPhasesList = analystUiConfig.systemConfig.prioritySdPhases;
  const dispatch = useAppDispatch();

  const onSubmit = React.useCallback(
    (alignedOn: AlignWaveformsOn, sdPhase?: CommonTypes.PhaseType) => {
      setWaveformAlignment(
        alignedOn,
        sdPhase,
        alignedOn !== AlignWaveformsOn.TIME ? true : showPredictedPhases
      );
      if (alignedOn === AlignWaveformsOn.PREDICTED_PHASE) {
        dispatch(waveformActions.setShouldShowPredictedPhases(true));
      }
    },
    [dispatch, setWaveformAlignment, showPredictedPhases]
  );

  return React.useMemo(
    () => (
      <AlignmentMenu
        alignedOn={alignWaveformsOn}
        prioritySdPhases={sortBy(prioritySdPhasesList)}
        sdPhases={sortBy(defaultSdPhasesList)}
        phaseAlignedOn={phaseToAlignOn}
        defaultPhaseAlignment={defaultPhaseAlignment}
        onSubmit={onSubmit}
      />
    ),
    [
      alignWaveformsOn,
      defaultSdPhasesList,
      onSubmit,
      phaseToAlignOn,
      prioritySdPhasesList,
      defaultPhaseAlignment
    ]
  );
};

const buildAlignmentDropdown = (
  alignWaveformsOn: AlignWaveformsOn,
  alignmentDropdown: JSX.Element,
  phaseToAlignOn: CommonTypes.PhaseType,
  currentOpenEventId: string,
  key: string | number
): ToolbarTypes.ToolbarItemElement => {
  const alignmentType = alignWaveformsOn === AlignWaveformsOn.TIME ? 'Time' : phaseToAlignOn;
  const widthPx = 154;

  return (
    <PopoverButtonToolbarItem
      key={key}
      label={`Align: ${alignmentType}`}
      tooltip={
        alignWaveformsOn === AlignWaveformsOn.TIME
          ? 'Time'
          : `${alignWaveformsOn} ${phaseToAlignOn}`
      }
      menuLabel="Alignment"
      disabled={
        currentOpenEventId === null || currentOpenEventId === undefined || currentOpenEventId === ''
      }
      popoverContent={alignmentDropdown}
      widthPx={widthPx}
    />
  );
};

/**
 * Creates an alignment control if any of the props have changed
 * Expects all parameters passed in to be referentially stable.
 *
 * @param alignWaveformsOn the phase on which to align the waveforms
 * @param alignablePhases the phases which can be aligned
 * @param phaseToAlignOn the new phase to align on
 * @param defaultPhaseAlignment ZAS default phase alignment from processing analyst configuration
 * @param hideToolbarPopover a function to hide the toolbar popover. Must be referentially stable.
 * @param showPredictedPhases whether to show the predicted phases
 * @param setWaveformAlignment a function that sets the waveform alignment. Must be referentially stable.
 * @param currentOpenEventId the id of the currently open event
 * @param key must be unique
 * @returns a toolbar control for the alignment dropdown
 */
export const useAlignmentControl = (
  alignWaveformsOn: AlignWaveformsOn,
  alignablePhases: CommonTypes.PhaseType[],
  phaseToAlignOn: CommonTypes.PhaseType,
  defaultPhaseAlignment: CommonTypes.PhaseType,
  showPredictedPhases: boolean,
  setWaveformAlignment: (
    alignWaveformsOn: AlignWaveformsOn,
    phaseToAlignOn: CommonTypes.PhaseType,
    showPredictedPhases: boolean
  ) => void,
  currentOpenEventId: string,
  key: string | number
): ToolbarTypes.ToolbarItemElement => {
  const alignmentDropdown = useAlignmentDropdown(
    alignWaveformsOn,
    alignablePhases,
    phaseToAlignOn,
    defaultPhaseAlignment,
    showPredictedPhases,
    setWaveformAlignment
  );

  return React.useMemo(
    () =>
      buildAlignmentDropdown(
        alignWaveformsOn,
        alignmentDropdown,
        phaseToAlignOn,
        currentOpenEventId,
        key
      ),
    [alignWaveformsOn, alignmentDropdown, phaseToAlignOn, currentOpenEventId, key]
  );
};
