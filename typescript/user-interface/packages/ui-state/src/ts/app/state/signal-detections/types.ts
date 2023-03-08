/**
 * Displayed signal detection configuration options
 */
export enum DisplayedSignalDetectionConfigurationEnum {
  syncWaveform = 'syncWaveform',
  signalDetectionBeforeInterval = 'signalDetectionBeforeInterval',
  signalDetectionAfterInterval = 'signalDetectionAfterInterval',
  signalDetectionAssociatedToOpenEvent = 'signalDetectionAssociatedToOpenEvent',
  signalDetectionAssociatedToCompletedEvent = 'signalDetectionAssociatedToCompletedEvent',
  signalDetectionAssociatedToOtherEvent = 'signalDetectionAssociatedToOtherEvent',
  signalDetectionUnassociated = 'signalDetectionUnassociated'
}

/**
 * used to populate the values of the SD column picker dropdown, and match
 * the values to the table column ids
 */
export enum SignalDetectionColumn {
  unsavedChanges = 'unsavedChanges',
  assocStatus = 'assocStatus',
  conflict = 'conflict',
  station = 'station',
  channel = 'channel',
  phase = 'phase',
  phaseConfidence = 'phaseConfidence',
  time = 'time',
  timeStandardDeviation = 'timeStandardDeviation',
  azimuth = 'azimuth',
  azimuthStandardDeviation = 'azimuthStandardDeviation',
  slowness = 'slowness',
  slownessStandardDeviation = 'slownessStandardDeviation',
  amplitude = 'amplitude',
  period = 'period',
  sNR = 'sNR',
  rectilinearity = 'rectilinearity',
  emergenceAngle = 'emergenceAngle',
  shortPeriodFirstMotion = 'shortPeriodFirstMotion',
  longPeriodFirstMotion = 'longPeriodFirstMotion',
  rejected = 'rejected'
}

/**
 * Signal detections Redux State consists of Signal Detection display state
 */
export interface SignalDetectionsState {
  displayedSignalDetectionConfiguration: Record<DisplayedSignalDetectionConfigurationEnum, boolean>;
  signalDetectionsColumns: Record<SignalDetectionColumn, boolean>;
}
