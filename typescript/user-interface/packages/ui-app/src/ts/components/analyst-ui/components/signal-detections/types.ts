import type GoldenLayout from '@gms/golden-layout';
import type { Row } from '@gms/ui-core-components';
import type { SignalDetectionFetchResult } from '@gms/ui-state';
import { DisplayedSignalDetectionConfigurationEnum, SignalDetectionColumn } from '@gms/ui-state';
import Immutable from 'immutable';

import { messageConfig } from '~analyst-ui/config/message-config';

export interface SignalDetectionsComponentProps {
  glContainer?: GoldenLayout.Container;
}

export interface SignalDetectionsPanelProps {
  signalDetectionsQuery: SignalDetectionFetchResult;
}

export interface SignalDetectionsToolbarProps {
  setSelectedSDColumnsToDisplay: (args: Immutable.Map<SignalDetectionColumn, boolean>) => void;
  selectedSDColumnsToDisplay: Immutable.Map<SignalDetectionColumn, boolean>;
}

export interface SignalDetectionsTableProps {
  isSynced: boolean;
  signalDetectionsQuery: SignalDetectionFetchResult;
  data: SignalDetectionRow[];
  columnsToDisplay: Immutable.Map<SignalDetectionColumn, boolean>;
}

export interface SignalDetectionRow extends Row {
  unsavedChanges: string;
  assocStatus: string;
  conflict: string;
  station: string;
  channel: string;
  phase: string;
  phaseConfidence: string;
  time: string;
  timeStandardDeviation: string;
  azimuth: string;
  azimuthStandardDeviation: string;
  slowness: string;
  slownessStandardDeviation: string;
  amplitude: string;
  period: string;
  sNR: string;
  rectilinearity: string;
  emergenceAngle: string;
  shortPeriodFirstMotion: string;
  longPeriodFirstMotion: string;
  rejected: string;
  edgeType: string;
}

/**
 * Used to match the display strings to values in the SD sync dropdown.
 */
export const signalDetectionSyncDisplayStrings: Immutable.Map<
  DisplayedSignalDetectionConfigurationEnum,
  string
> = Immutable.Map<DisplayedSignalDetectionConfigurationEnum, string>([
  [
    DisplayedSignalDetectionConfigurationEnum.syncWaveform,
    messageConfig.labels.syncToWaveformDisplayVisibleTimeRange
  ],
  [
    DisplayedSignalDetectionConfigurationEnum.signalDetectionBeforeInterval,
    'Edge detections before interval'
  ],
  [
    DisplayedSignalDetectionConfigurationEnum.signalDetectionAfterInterval,
    'Edge detections after interval'
  ],
  [
    DisplayedSignalDetectionConfigurationEnum.signalDetectionAssociatedToOpenEvent,
    'Associated to open event'
  ],
  [
    DisplayedSignalDetectionConfigurationEnum.signalDetectionAssociatedToCompletedEvent,
    'Associated to completed event'
  ],
  [
    DisplayedSignalDetectionConfigurationEnum.signalDetectionAssociatedToOtherEvent,
    'Associated to other event'
  ],
  [DisplayedSignalDetectionConfigurationEnum.signalDetectionUnassociated, 'Unassociated']
]);

export const signalDetectionSyncLabelStrings: Immutable.Map<
  DisplayedSignalDetectionConfigurationEnum,
  string
> = Immutable.Map<DisplayedSignalDetectionConfigurationEnum, string>([
  [DisplayedSignalDetectionConfigurationEnum.signalDetectionBeforeInterval, 'Edge Detections'],
  [
    DisplayedSignalDetectionConfigurationEnum.signalDetectionAssociatedToOpenEvent,
    'Association Status'
  ]
]);

export const signalDetectionSyncRenderDividers: Immutable.Map<
  DisplayedSignalDetectionConfigurationEnum,
  boolean
> = Immutable.Map<DisplayedSignalDetectionConfigurationEnum, boolean>([
  [DisplayedSignalDetectionConfigurationEnum.syncWaveform, true],
  [DisplayedSignalDetectionConfigurationEnum.signalDetectionAfterInterval, true]
]);

/** TODO add units, have names and units reviewed
 * used to match the display strings to values in the SD table column picker dropdown
 */
export const signalDetectionColumnDisplayStrings: Immutable.Map<
  SignalDetectionColumn,
  string
> = Immutable.Map<SignalDetectionColumn, string>([
  [SignalDetectionColumn.unsavedChanges, 'Unsaved changes'],
  [SignalDetectionColumn.assocStatus, 'Assoc status'],
  [SignalDetectionColumn.conflict, 'Conflict'],
  [SignalDetectionColumn.station, 'Station'],
  [SignalDetectionColumn.channel, 'Channel'],
  [SignalDetectionColumn.phase, 'Phase'],
  [SignalDetectionColumn.phaseConfidence, 'Phase confidence'],
  [SignalDetectionColumn.time, 'Time'],
  [SignalDetectionColumn.timeStandardDeviation, 'Time std dev (s)'],
  [SignalDetectionColumn.azimuth, 'Azimuth (°)'],
  [SignalDetectionColumn.azimuthStandardDeviation, 'Azimuth std dev (°)'],
  [SignalDetectionColumn.slowness, 'Slowness (s/°)'],
  [SignalDetectionColumn.slownessStandardDeviation, 'Slowness std dev (s/°)'],
  [SignalDetectionColumn.amplitude, 'Amplitude'],
  [SignalDetectionColumn.period, 'Period (s)'],
  [SignalDetectionColumn.sNR, 'SNR'],
  [SignalDetectionColumn.rectilinearity, 'Rectilinearity'],
  [SignalDetectionColumn.emergenceAngle, 'Emergence angle (°)'],
  [SignalDetectionColumn.shortPeriodFirstMotion, 'Short period first motion'],
  [SignalDetectionColumn.longPeriodFirstMotion, 'Long period first motion'],
  [SignalDetectionColumn.rejected, 'Rejected']
]);
