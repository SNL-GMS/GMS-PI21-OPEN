import type {
  CommonTypes,
  ConfigurationTypes,
  EventTypes,
  QcMaskTypes,
  SignalDetectionTypes,
  StationTypes,
  WaveformTypes,
  WorkflowTypes
} from '@gms/common-model';
import type GoldenLayout from '@gms/golden-layout';
import type { AnalystWorkspaceTypes, EventStatus } from '@gms/ui-state';
import type { WaveformDisplayProps as WeavessProps } from '@gms/weavess/lib/components/waveform-display/types';

import type {
  AmplitudeScalingOptions,
  FixedScaleValue
} from '../components/waveform-controls/scaling-options';

export interface WeavessDisplayState {
  qcMaskModifyInterval?: CommonTypes.TimeRange;
  selectedQcMask?: QcMaskTypes.QcMask;
  /**
   * The anchor for the channel selection range: this defines the starting point for a range selection.
   */
  selectionRangeAnchor: string;
}

interface WeavessDisplayReduxProps {
  // passed in from golden-layout
  glContainer?: GoldenLayout.Container;

  // callbacks
  createSignalDetection(args: any): Promise<void>;
  rejectSignalDetection(args: any): Promise<void>;
  updateSignalDetection(args: any): Promise<void>;
  createQcMask(args: any): Promise<void>;
  updateQcMask(args: any): Promise<void>;
  rejectQcMask(args: any): Promise<void>;
  createEvent(args: any): Promise<void>;
  setEventSignalDetectionAssociation(args: any): Promise<void>;
}

export interface WeavessDisplayComponentProps {
  weavessProps: WeavessProps;
  defaultWaveformFilters: WaveformTypes.WaveformFilter[];
  defaultStations: StationTypes.Station[];
  defaultSignalDetectionPhase?: CommonTypes.PhaseType;
  events: EventTypes.Event[];
  qcMasksByChannelName: QcMaskTypes.QcMask[];
  measurementMode: AnalystWorkspaceTypes.MeasurementMode;
  signalDetections: SignalDetectionTypes.SignalDetection[];
  selectedSdIds: string[];
  setSelectedSdIds(id: string[]): void;
  selectedStationIds: string[];
  setSelectedStationIds(ids: string[]);
  setMeasurementModeEntries(entries: Record<string, boolean>): void;
  amplitudeScaleOption?: AmplitudeScalingOptions;
  fixedScaleVal?: FixedScaleValue;
  scaleAmplitudeChannelName?: string;
  scaledAmplitudeChannelMinValue?: number;
  scaledAmplitudeChannelMaxValue?: number;
  currentTimeInterval: CommonTypes.TimeRange;
  currentOpenEventId: string;
  analysisMode: WorkflowTypes.AnalysisMode;
  sdIdsToShowFk: string[];
  setSdIdsToShowFk(signalDetections: string[]): void;
  eventStatuses: Record<string, EventStatus>;
  uiTheme: ConfigurationTypes.UITheme;
  stationsAssociatedToCurrentOpenEvent?: string[];
}

export type WeavessDisplayProps = WeavessDisplayReduxProps & WeavessDisplayComponentProps;
