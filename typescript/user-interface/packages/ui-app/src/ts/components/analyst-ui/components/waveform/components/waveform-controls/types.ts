import type { CommonTypes, WaveformTypes } from '@gms/common-model';
import type { AnalystWorkspaceTypes } from '@gms/ui-state';
import type { AlignWaveformsOn } from '@gms/ui-state/lib/app/state/analyst/types';
import type React from 'react';

import type { QcMaskDisplayFilters } from '~analyst-ui/config';
import type { MaskDisplayFilter } from '~analyst-ui/config/user-preferences';

import type { AmplitudeScalingOptions, FixedScaleValue } from './scaling-options';

/**
 * Waveform Display Controls Props
 */
export interface WaveformControlsProps {
  defaultSignalDetectionPhase: CommonTypes.PhaseType;
  currentSortType: AnalystWorkspaceTypes.WaveformSortType;
  currentTimeInterval: CommonTypes.TimeRange;
  viewableTimeInterval: CommonTypes.TimeRange;
  currentOpenEventId: string;
  analystNumberOfWaveforms: number;
  showPredictedPhases: boolean;
  maskDisplayFilters: QcMaskDisplayFilters;
  alignWaveformsOn: AlignWaveformsOn;
  phaseToAlignOn: CommonTypes.PhaseType | undefined;
  defaultPhaseAlignment: CommonTypes.PhaseType;
  alignablePhases: CommonTypes.PhaseType[] | undefined;
  isMeasureWindowVisible: boolean;
  measurementMode: AnalystWorkspaceTypes.MeasurementMode;
  featurePredictionQueryDataUnavailable: boolean;
  setMode(mode: AnalystWorkspaceTypes.WaveformDisplayMode): void;
  setDefaultSignalDetectionPhase(phase: CommonTypes.PhaseType): void;
  setSelectedSortType(sortType: AnalystWorkspaceTypes.WaveformSortType): void;
  setAnalystNumberOfWaveforms(value: number, valueAsString?: string): void;
  setMaskDisplayFilters(key: string, maskDisplayFilter: MaskDisplayFilter): void;
  setWaveformAlignment(
    alignWaveformsOn: AlignWaveformsOn,
    phaseToAlignOn: CommonTypes.PhaseType,
    showPredictedPhases: boolean
  ): void;
  toggleMeasureWindow(): void;
  pan(panDirection: WaveformTypes.PanType, shouldLoadAdditionalData: boolean): void;
  zoomAlignSort(): void;
  onKeyPress(
    e: React.KeyboardEvent<HTMLDivElement>,
    clientX?: number,
    clientY?: number,
    channelId?: string,
    timeSecs?: number
  ): void;
  amplitudeScaleOption: AmplitudeScalingOptions;
  fixedScaleVal: FixedScaleValue;
  setAmplitudeScaleOption: (option: AmplitudeScalingOptions) => void;
  setFixedScaleVal: (val: FixedScaleValue) => void;
}

export interface WaveformDisplayControlsState {
  hasMounted: boolean;
}
