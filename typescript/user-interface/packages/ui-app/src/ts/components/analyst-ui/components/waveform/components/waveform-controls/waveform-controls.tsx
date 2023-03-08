/* eslint-disable no-plusplus */
import type { ToolbarTypes } from '@gms/ui-core-components';
import React from 'react';

import { useBaseDisplaySize } from '~common-ui/components/base-display/base-display-hooks';

import { useAlignmentControl } from './alignment-control';
import { useMeasureWindowControl } from './measure-window-control';
import { useModeControl } from './mode-selector-control';
import { useNumWaveformControl } from './num-waveform-control';
import { usePanGroupControl } from './pan-group-controls';
import { usePhaseControl } from './phase-selector-control';
import { usePredictedControl } from './predicted-control';
import { useQcMaskControl } from './qc-mask-control';
import { useScalingOptions } from './scaling-options';
import { useStationSortControl } from './station-sort-control';
import { useStationsDropdownControl } from './stations-control';
import { useTimeUncertaintySwitch } from './time-uncertainty-switch';
import type { WaveformControlsProps } from './types';
import { WaveformToolbar } from './waveform-toolbar';
import { useZASControl } from './zas-control';

// eslint-disable-next-line react/function-component-definition
const InternalWaveformControls: React.FC<WaveformControlsProps> = ({
  measurementMode,
  currentOpenEventId,
  currentTimeInterval,
  viewableTimeInterval,
  setMode,
  defaultSignalDetectionPhase,
  setDefaultSignalDetectionPhase,
  analystNumberOfWaveforms,
  setAnalystNumberOfWaveforms,
  alignWaveformsOn,
  alignablePhases,
  phaseToAlignOn,
  defaultPhaseAlignment,
  showPredictedPhases,
  setWaveformAlignment,
  currentSortType,
  setSelectedSortType,
  maskDisplayFilters,
  setMaskDisplayFilters,
  isMeasureWindowVisible,
  toggleMeasureWindow,
  pan,
  zoomAlignSort,
  amplitudeScaleOption,
  fixedScaleVal,
  setAmplitudeScaleOption,
  setFixedScaleVal,
  featurePredictionQueryDataUnavailable
}: WaveformControlsProps) => {
  const [widthPx] = useBaseDisplaySize();

  const panGroup = usePanGroupControl(pan, currentTimeInterval, viewableTimeInterval, 'wfpangroup');

  const zoomAlignSortGroup = useZASControl(
    zoomAlignSort,
    currentOpenEventId,
    featurePredictionQueryDataUnavailable,
    'wfzasgroup'
  );

  const stationSelector = useStationsDropdownControl('wfstationselect');

  const numWaveformsSelector = useNumWaveformControl(
    analystNumberOfWaveforms,
    setAnalystNumberOfWaveforms,
    'wfnumwaveformselect'
  );

  const { toolbarItem: scalingOptions } = useScalingOptions(
    amplitudeScaleOption,
    fixedScaleVal,
    setAmplitudeScaleOption,
    setFixedScaleVal,
    'wfscaling'
  );

  const timeUncertaintySwitch = useTimeUncertaintySwitch('wftimeuncertainty');

  const modeSelector = useModeControl(measurementMode, setMode, 'wfmodeselect');

  const sdPhaseSelector = usePhaseControl(
    defaultSignalDetectionPhase,
    setDefaultSignalDetectionPhase,
    'wfsdphaseselect'
  );

  const alignmentSelector = useAlignmentControl(
    alignWaveformsOn,
    alignablePhases,
    phaseToAlignOn,
    defaultPhaseAlignment,
    showPredictedPhases,
    setWaveformAlignment,
    currentOpenEventId,
    'wfalignment'
  );

  const stationSort = useStationSortControl(
    currentSortType,
    alignWaveformsOn,
    currentOpenEventId,
    setSelectedSortType,
    'wfstationsort'
  );

  const predictedDropdown = usePredictedControl(currentOpenEventId, 'wfpredicted');

  const qcMaskPicker = useQcMaskControl(maskDisplayFilters, setMaskDisplayFilters, 'wfqcmask');

  const measureWindowSwitch = useMeasureWindowControl(
    isMeasureWindowVisible,
    toggleMeasureWindow,
    'wfmeasurewindow'
  );

  const leftItems: ToolbarTypes.ToolbarItemElement[] = React.useMemo(() => {
    return [panGroup, zoomAlignSortGroup];
  }, [panGroup, zoomAlignSortGroup]);

  const rightItems: ToolbarTypes.ToolbarItemElement[] = React.useMemo(() => {
    return [
      stationSelector,
      modeSelector,
      scalingOptions,
      sdPhaseSelector,
      numWaveformsSelector,
      alignmentSelector,
      stationSort,
      timeUncertaintySwitch,
      predictedDropdown,
      qcMaskPicker,
      measureWindowSwitch
    ];
  }, [
    alignmentSelector,
    measureWindowSwitch,
    modeSelector,
    numWaveformsSelector,
    predictedDropdown,
    qcMaskPicker,
    scalingOptions,
    sdPhaseSelector,
    stationSelector,
    stationSort,
    timeUncertaintySwitch
  ]);

  return (
    <WaveformToolbar
      leftToolbarItems={leftItems}
      rightToolbarItems={rightItems}
      widthPx={widthPx}
    />
  );
};

/**
 * Waveform Display Controls Component
 * Builds and renders the waveform toolbar and loading spinner (absolutely positioned to appear at
 * a different location on the screen).
 */
export const WaveformControls = React.memo(InternalWaveformControls);
