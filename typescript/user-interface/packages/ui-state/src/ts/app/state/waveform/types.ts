import type { CommonTypes } from '@gms/common-model';

/**
 * A record of changes to station visibility from @default defaultStationVisibility.
 * A station is considered "visible" if it appears in the waveform display, even if it
 * is drawn off the screen. If it is hidden from the waveform display, it is not
 * considered "visible".
 */
export interface StationVisibilityChanges {
  /**
   * Whether this station is visible.
   */
  visibility: boolean;

  /**
   * The name of this station. Used to look up the station from the results received from the server,
   * and assumed to be unique.
   */
  stationName: string;

  /**
   * If not set, default to true.
   */
  hiddenChannels?: string[];

  /**
   * Whether the station is expanded to show all visible channels. Defaults to false.
   */
  isStationExpanded?: boolean;
}

/**
 * The type definition for the default station visibility object, sans @param stationName
 */
export type DefaultStationVisibility = Omit<StationVisibilityChanges, 'stationName'>;

/**
 * This is a default @interface StationVisibilityChanges object.
 * It represents the state of all stations that are not tracked with their
 * own @interface StationVisibilityChanges object.
 */
export const defaultStationVisibility: DefaultStationVisibility = {
  visibility: false,
  hiddenChannels: undefined,
  isStationExpanded: false
};

/**
 * An object with station names as keys and StationVisibilityChanges objects as values.
 * The changes objects represent any changes to the default visibility. This object, then,
 * represents the state of all stations, regardless of whether there is a corresponding
 * StationVisibilityChanges object in the dictionary. If not present, then it is assumed
 * to be in the default state. (see @const defaultStationVisibility).
 */
export type StationVisibilityChangesDictionary = Record<string, StationVisibilityChanges>;

// State of loading waveforms
export interface WaveformLoadingState {
  isLoading: boolean;
  total: number;
  completed: number;
  percent: number;
  description: string;
}

/**
 * Waveform Redux State
 */
export interface WaveformState {
  stationsVisibility: StationVisibilityChangesDictionary;
  loadingState: WaveformLoadingState;
  // whether or not to show the time uncertainty bars in the waveform display.
  shouldShowTimeUncertainty: boolean;
  // whether or not to show predicted phases in the waveform display.
  shouldShowPredictedPhases: boolean;
  // The amount that is currently in on the screen due to zooming.
  zoomInterval: CommonTypes.TimeRange;
  // The amount of time that can be viewed without loading more data.
  // because the user may load more waveform
  // data than the currently opened time interval via panning.
  viewableInterval: CommonTypes.TimeRange;
  // The offsets when not aligned by time
  minimumOffset: number;
  maximumOffset: number;
  baseStationTime: number;
}

/**
 * Properties for calculating the zoom interval to prevent setting it outside the viewable interval
 * Broken out to reduce function complexity
 */
export interface ZoomIntervalProperties {
  prevZoomInterval: CommonTypes.TimeRange;
  viewableInterval: CommonTypes.TimeRange;
  diffStartTimeSecs: number;
  diffEndTimeSecs: number;
  minOffset: number;
  maxOffset: number;
  startTimeDiff: number;
  endTimeDiff: number;
}

export const ZOOM_INTERVAL_TOO_LARGE_ERROR_MESSAGE = 'Cannot set a zoom interval if no viewable interval is set' as const;
