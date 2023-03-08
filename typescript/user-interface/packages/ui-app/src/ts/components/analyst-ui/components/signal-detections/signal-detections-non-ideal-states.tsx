import { nonIdealStateWithNoSpinner, nonIdealStateWithSpinner } from '@gms/ui-core-components';

export const nonIdealStateLoadingSignalDetections = nonIdealStateWithSpinner(
  'Loading Signal Detections',
  'Please wait'
);

export const nonIdealStateSelectAnInterval = nonIdealStateWithNoSpinner(
  'No Interval Selected',
  'Select an interval in the Workflow Display to view signal detections',
  'select'
);

export const nonIdealStateNoSignalDetections = nonIdealStateWithNoSpinner(
  'No Signal Detection Data',
  'There is no signal detection data available for this interval',
  'exclude-row'
);

export const nonIdealStateNoSignalDetectionsSyncedTimeRange = nonIdealStateWithNoSpinner(
  'No signal detections found in the synced time range',
  'View a different time range in the Waveform display to see signal detections'
);

export const nonIdealStateNoSignalDetectionsErrorState = nonIdealStateWithNoSpinner(
  'Error',
  'Problem Loading Signal Detections'
);
