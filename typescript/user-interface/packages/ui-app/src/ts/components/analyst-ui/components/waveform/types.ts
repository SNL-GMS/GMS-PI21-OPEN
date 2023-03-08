import type {
  ChannelTypes,
  CommonTypes,
  ConfigurationTypes,
  EventTypes,
  SignalDetectionTypes,
  StationTypes,
  WaveformTypes,
  WorkflowTypes
} from '@gms/common-model';
import type GoldenLayout from '@gms/golden-layout';
import type {
  AnalystWaveformTypes,
  AnalystWorkspaceTypes,
  EventStatus,
  PredictFeaturesForEventLocationQueryProps,
  ProcessingAnalystConfigurationQueryProps,
  StationQueryProps,
  UiChannelSegment
} from '@gms/ui-state';
import type { AlignWaveformsOn } from '@gms/ui-state/lib/app/state/analyst/types';
import type { WeavessTypes } from '@gms/weavess-core';

import type { QcMaskDisplayFilters } from '~components/analyst-ui/config';

import type {
  AmplitudeScalingOptions,
  FixedScaleValue
} from './components/waveform-controls/scaling-options';

export enum KeyDirection {
  UP = 'Up',
  DOWN = 'Down',
  LEFT = 'Left',
  RIGHT = 'Right'
}

/**
 * Waveform Display display state.
 * keep track of selected channels & signal detections
 */
export interface WaveformDisplayState {
  weavessStations: WeavessTypes.Station[];
  currentTimeInterval: CommonTypes.TimeRange;
  loadingWaveforms: boolean;
  loadingWaveformsPercentComplete: number;
  maskDisplayFilters: QcMaskDisplayFilters;
  analystNumberOfWaveforms: number;
  currentOpenEventId: string;
  isMeasureWindowVisible: boolean;
  amplitudeScaleOption: AmplitudeScalingOptions;
  fixedScaleVal: FixedScaleValue;
  scaleAmplitudeChannelName: string;
  scaledAmplitudeChannelMinValue: number;
  scaledAmplitudeChannelMaxValue: number;
}

/**
 * Props mapped in from Redux state
 */
export interface WaveformDisplayReduxProps {
  // passed in from golden-layout
  glContainer?: GoldenLayout.Container;
  currentTimeInterval: CommonTypes.TimeRange;
  currentStageName: string;
  defaultSignalDetectionPhase: CommonTypes.PhaseType;
  currentOpenEventId: string;
  selectedSdIds: string[];
  stationsVisibility: AnalystWaveformTypes.StationVisibilityChangesDictionary;
  selectedStationIds: string[];
  selectedSortType: AnalystWorkspaceTypes.WaveformSortType;
  analysisMode: WorkflowTypes.AnalysisMode;
  measurementMode: AnalystWorkspaceTypes.MeasurementMode;
  sdIdsToShowFk: string[];
  location: AnalystWorkspaceTypes.LocationSolutionState;
  channelFilters: Record<string, WaveformTypes.WaveformFilter>;
  openEventId: string;
  keyPressActionQueue: Record<string, number>;
  // because the user may load more waveform
  // data than the currently opened time interval
  viewableInterval: WeavessTypes.TimeRange;
  minimumOffset: number;
  maximumOffset: number;
  baseStationTime: number;
  shouldShowTimeUncertainty: boolean;
  shouldShowPredictedPhases: boolean;
  qcMaskQuery: { data: any };
  alignablePhases: CommonTypes.PhaseType[];
  stationsAssociatedWithCurrentOpenEvent: string[];
  phaseToAlignOn: CommonTypes.PhaseType | undefined;
  alignWaveformsOn: AlignWaveformsOn;

  // callbacks
  isStationVisible(station: StationTypes.Station | string): boolean;
  isStationExpanded(station: StationTypes.Station | string): boolean;
  getVisibleStationsFromStationList(stations: StationTypes.Station[]): StationTypes.Station[];
  pan(
    panDirection: WaveformTypes.PanType,
    options?: { shouldLoadAdditionalData?: boolean; onPanningBoundaryReached?: () => void }
  ): WeavessTypes.TimeRange;
  setDefaultSignalDetectionPhase(phase: CommonTypes.PhaseType): void;
  setMode(mode: AnalystWorkspaceTypes.WaveformDisplayMode): void;
  setOpenEventId(eventId: string): void;
  setSelectedSdIds(idx: string[]): void;
  setSelectedStationIds(ids: string[]);
  setSdIdsToShowFk(signalDetections: string[]): void;
  setSelectedSortType(selectedSortType: AnalystWorkspaceTypes.WaveformSortType): void;
  setChannelFilters(filters: Record<string, WaveformTypes.WaveformFilter>);
  setMeasurementModeEntries(entries: Record<string, boolean>): void;
  setKeyPressActionQueue(actions: Record<string, number>): void;
  setStationsVisibility(
    stationsVisibility: AnalystWaveformTypes.StationVisibilityChangesDictionary
  );
  setStationVisibility(station: StationTypes.Station | string, isVisible: boolean): void;
  setStationExpanded(station: StationTypes.Station | string, isExpanded?: boolean): void;
  setChannelVisibility(
    station: StationTypes.Station | string,
    channel: ChannelTypes.Channel | string,
    isVisible: boolean
  ): void;
  setViewableInterval(viewableInterval: CommonTypes.TimeRange): void;
  setMinimumOffset(minimumOffset: number): void;
  setMaximumOffset(maximumOffset: number): void;
  setBaseStationTime(baseStationTime: number): void;
  setStationsAssociatedWithCurrentOpenEvent(stationsAssociatedWithCurrentOpenEvent: string[]): void;
  setZoomInterval(zoomInterval: CommonTypes.TimeRange): void;
  showAllChannels(station: StationTypes.Station | string): void;
  setShouldShowTimeUncertainty(newValue: boolean): void;
  setShouldShowPredictedPhases(newValue: boolean): void;
  markAmplitudeMeasurementReviewed(args: any): Promise<void>;
  onWeavessMount?(weavessInstance: WeavessTypes.WeavessInstance): void;
  setAlignWaveformsOn(alignWaveformsOn: AlignWaveformsOn): void;
  setPhaseToAlignOn(phaseToAlignOn: CommonTypes.PhaseType): void;
}

/**
 * Consolidated props type for waveform display.
 */
export type WaveformDisplayProps = WaveformDisplayReduxProps &
  ProcessingAnalystConfigurationQueryProps &
  PredictFeaturesForEventLocationQueryProps &
  StationQueryProps & {
    events: EventTypes.Event[];
    signalDetections: SignalDetectionTypes.SignalDetection[];
    channelSegments: Record<string, Record<string, UiChannelSegment[]>>;
    uiTheme: ConfigurationTypes.UITheme;
    eventStatuses: Record<string, EventStatus>;
    distances: EventTypes.LocationDistance[];
  };

/**
 * The props for the {@link WaveformComponent}.
 * We omit the signalDetectionResults and channelSegmentResults and replace them with
 * the modified fetch results type, because the non-ideal state component consumes
 * and removes the metadata (such as isLoading and isError). This is a performance
 * optimization, since it reduces the number of times the {@link WaveformPanel} renders
 */
export type WaveformComponentProps = Omit<
  WaveformDisplayProps,
  'channelSegments' | 'events' | 'signalDetections'
>;
