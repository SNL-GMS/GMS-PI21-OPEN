import type {
  CommonTypes,
  FkTypes,
  LegacyEventTypes,
  ProcessingStationTypes,
  SignalDetectionTypes,
  WaveformTypes,
  WorkflowTypes
} from '@gms/common-model';
import type GoldenLayout from '@gms/golden-layout';
import type {
  AnalystWorkspaceTypes,
  ProcessingAnalystConfigurationQueryProps
} from '@gms/ui-state';
import type Immutable from 'immutable';

import type {
  FilterType,
  FkThumbnailSize
} from './components/fk-thumbnail-list/fk-thumbnails-controls';
import type { LeadLagPairs } from './constants';

export interface FkParams {
  windowParams: FkTypes.WindowParameters;
  frequencyPair: FkTypes.FrequencyBand;
}

export interface LeadLagPairAndString {
  leadLagPairs: LeadLagPairs;
  windowParams: FkTypes.WindowParameters;
}

export enum FkUnits {
  FSTAT = 'FSTAT',
  POWER = 'POWER'
}

/**
 * Used to return a super set of the fk configuration from the fk config popover
 */
export interface FkConfigurationWithUnits extends FkTypes.FkConfiguration {
  fkUnitToDisplay: FkUnits;
}

/**
 * Azimuth Slowness Redux Props
 */
export interface AzimuthSlownessReduxProps {
  // passed in from golden-layout
  glContainer?: GoldenLayout.Container;
  currentTimeInterval: CommonTypes.TimeRange;
  selectedSdIds: string[];
  openEventId: string;
  unassociatedSDColor: string;
  sdIdsToShowFk: string[];
  analysisMode: WorkflowTypes.AnalysisMode;
  location: AnalystWorkspaceTypes.LocationSolutionState;
  defaultSignalDetectionPhase?: CommonTypes.PhaseType;
  channelFilters: Record<string, WaveformTypes.WaveformFilter>;
  selectedSortType: AnalystWorkspaceTypes.WaveformSortType;
  setSelectedSdIds(ids: string[]): void;
  setChannelFilters(filters: Record<string, WaveformTypes.WaveformFilter>): void;
  setSdIdsToShowFk(signalDetectionIds: string[]): void;
  setMeasurementModeEntries(entries: Record<string, boolean>): void;
}

export interface SubscriptionAction {
  (
    list: SignalDetectionTypes.SignalDetection[],
    index: number,
    prev: SignalDetectionTypes.SignalDetection[],
    currentIteree: SignalDetectionTypes.SignalDetection
  ): void;
}

/**
 * Azimuth Slowness State
 */
export interface AzimuthSlownessState {
  fkThumbnailSizePx: FkThumbnailSize;
  fkThumbnailColumnSizePx: number;
  filterType: FilterType;
  userInputFkWindowParameters: FkTypes.WindowParameters;
  userInputFkFrequency: FkTypes.FrequencyBand;
  numberOfOutstandingComputeFkMutations: number;
  fkUnitsForEachSdId: Immutable.Map<string, FkUnits>;
  fkInnerContainerWidthPx: number;
  fkFrequencyThumbnails: Immutable.Map<string, FkTypes.FkFrequencyThumbnail[]>;
}

/**
 * Mutations used by the Az Slow display
 */
export interface AzimuthSlownessMutations {
  computeFks: (args: any) => Promise<void>;
  computeFkFrequencyThumbnails: (args: any) => Promise<void>;
  setWindowLead: (args: any) => Promise<void>;
  markFksReviewed: (args: any) => Promise<void>;
}

/**
 * Consolidated props for Azimuth Slowness
 */
export type AzimuthSlownessProps = AzimuthSlownessReduxProps &
  AzimuthSlownessMutations &
  ProcessingAnalystConfigurationQueryProps;

/**
 * State of the az slow panel
 */
export interface AzimuthSlownessPanelState {
  currentMovieSpectrumIndex: number;
}

export interface AzimuthSlownessPanelProps {
  // Data
  defaultStations: ProcessingStationTypes.ProcessingStation[];
  eventsInTimeRange: LegacyEventTypes.Event[];
  displayedSignalDetection: SignalDetectionTypes.SignalDetection | undefined;
  openEvent: LegacyEventTypes.Event | undefined;
  unassociatedSignalDetectionByColor: string;
  associatedSignalDetections: SignalDetectionTypes.SignalDetection[];
  signalDetectionsToDraw: SignalDetectionTypes.SignalDetection[];
  signalDetectionsIdToFeaturePredictions: Immutable.Map<
    string,
    LegacyEventTypes.FeaturePrediction[]
  >;
  signalDetectionsByStation: SignalDetectionTypes.SignalDetection[];
  featurePredictionsForDisplayedSignalDetection: LegacyEventTypes.FeaturePrediction[];
  distances: LegacyEventTypes.LocationToStationDistance[];
  // Redux state as props
  selectedSdIds: string[];
  defaultWaveformFilters: WaveformTypes.WaveformFilter[];
  sdIdsToShowFk: string[];
  location: AnalystWorkspaceTypes.LocationSolutionState;
  fkFrequencyThumbnails: FkTypes.FkFrequencyThumbnail[];
  // Azimuth display state as props
  fkThumbnailColumnSizePx: number;
  fkDisplayWidthPx: number;
  fkDisplayHeightPx: number;
  filterType: FilterType;
  fkThumbnailSizePx: FkThumbnailSize;
  fkUnitsForEachSdId: Immutable.Map<string, FkUnits>;
  numberOfOutstandingComputeFkMutations: number;
  userInputFkFrequency: FkTypes.FrequencyBand;
  fkUnitForDisplayedSignalDetection: FkUnits;
  userInputFkWindowParameters: FkTypes.WindowParameters;
  fkInnerContainerWidthPx: number;
  channelFilters: Record<string, WaveformTypes.WaveformFilter>;
  selectedSortType: AnalystWorkspaceTypes.WaveformSortType;
  defaultSignalDetectionPhase?: CommonTypes.PhaseType;
  // Prop functions
  adjustFkInnerContainerWidth(
    fkThumbnailsContainer: HTMLDivElement,
    fkThumbnailsInnerContainer: HTMLDivElement
  ): void;
  markFksForSdIdsAsReviewed(sdIds: string[]): void;
  updateFkThumbnailSize(size: FkThumbnailSize): void;
  updateFkFilter(filterType: FilterType): void;
  setFkThumbnailColumnSizePx(newSizePx: number): void;
  computeFkAndUpdateState(fkInput: FkTypes.FkInput): void;
  changeUserInputFks(
    windowParams: FkTypes.WindowParameters,
    frequencyBand: FkTypes.FrequencyBand
  ): void;
  setFkUnitForSdId(sdId: string, fkUnit: FkUnits): void;

  // Redux functions
  setSelectedSdIds(sdIds: string[]): void;
  setSdIdsToShowFk(sdIds: string[]): void;
  setChannelFilters(filters: Record<string, WaveformTypes.WaveformFilter>): void;
  setMeasurementModeEntries(entries: Record<string, boolean>): void;
}
