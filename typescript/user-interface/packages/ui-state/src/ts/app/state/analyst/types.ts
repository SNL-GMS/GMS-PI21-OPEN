import type { CommonTypes, WaveformTypes } from '@gms/common-model';
import { createEnumTypeGuard } from '@gms/common-util';
import Immutable from 'immutable';

export enum AnalystKeyAction {
  ESCAPE = 'Escape',
  UNDO_GLOBAL = 'History: Undo Global',
  REDO_GLOBAL = 'History: Redo Global',
  TOGGLE_FILTERS_UP = 'Toggle Channel Filter Up',
  TOGGLE_FILTERS_DOWN = 'Toggle Channel Filter Down',
  SAVE_OPEN_EVENT = 'Save Open Event',
  SAVE_ALL_EVENTS = 'Save All Events in Interval'
}

export const AnalystKeyActions: Immutable.Map<string, AnalystKeyAction> = Immutable.Map([
  ['Control+KeyZ', AnalystKeyAction.UNDO_GLOBAL],
  ['Control+Shift+KeyZ', AnalystKeyAction.REDO_GLOBAL],
  ['Control+ArrowUp', AnalystKeyAction.TOGGLE_FILTERS_UP],
  ['Control+ArrowDown', AnalystKeyAction.TOGGLE_FILTERS_DOWN],
  ['Control+KeyS', AnalystKeyAction.SAVE_OPEN_EVENT],
  ['Control+Shift+KeyS', AnalystKeyAction.SAVE_ALL_EVENTS],
  ['Escape', AnalystKeyAction.ESCAPE]
]);

/**
 * The display mode options for the waveform display.
 */
export enum WaveformDisplayMode {
  DEFAULT = 'Default',
  MEASUREMENT = 'Measurement'
}
/**
 * Available waveform align types.
 */
export enum AlignWaveformsOn {
  TIME = 'Time',
  PREDICTED_PHASE = 'Predicted',
  OBSERVED_PHASE = 'Observed'
}

/**
 * Available waveform sort types.
 */
export enum WaveformSortType {
  distance = 'Distance',
  stationNameAZ = 'Station: A-Z',
  stationNameZA = 'Station: Z-A'
}

export type DisplayedMagnitudeTypes = Record<string, boolean>;

export const isAnalystKeyAction = createEnumTypeGuard(AnalystKeyAction);

/**
 * Measurement mode state.
 */
export interface MeasurementMode {
  /** The display mode */
  mode: WaveformDisplayMode;

  /**
   * Measurement entries that are manually added or hidden by the user.
   * The key is the signal detection id
   */
  entries: Record<string, boolean>;
}

/**
 * A list such that at each index, it indicates whether the filter at the
 * same index is within the hotkey cycle.
 */
export type HotkeyCycleList = boolean[];

/**
 * The location solution state.
 * Includes:
 *   * The selected location solution set and solution
 *   * The selected preferred location solution set and solution
 */
export interface LocationSolutionState {
  selectedLocationSolutionId: string;
  selectedLocationSolutionSetId: string;
  selectedPreferredLocationSolutionId: string;
  selectedPreferredLocationSolutionSetId: string;
}

export interface AnalystState {
  channelFilters: Record<string, WaveformTypes.WaveformFilter>;
  defaultSignalDetectionPhase: CommonTypes.PhaseType;
  effectiveNowTime: number;
  historyActionInProgress: number;
  hotkeyCycleOverrides: Record<string, Record<number, boolean>>;
  location: LocationSolutionState;
  measurementMode: MeasurementMode;
  openEventId: string;
  openLayoutName: string;
  sdIdsToShowFk: string[];
  selectedEventIds: string[];
  selectedFilterIndex: number | null;
  selectedFilterList: string;

  selectedSdIds: string[];
  selectedSortType: WaveformSortType;

  eventListOpenEventTriggered: boolean;
  mapOpenEventTriggered: boolean;
  alignWaveformsOn: AlignWaveformsOn;
  phaseToAlignOn: CommonTypes.PhaseType;
}
