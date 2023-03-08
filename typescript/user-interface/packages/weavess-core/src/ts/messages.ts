/**
 * Defines Messages that can be issued to the user
 * through toasts.
 * TODO remove business logic words such as signal detection from weavess
 */
export enum Messages {
  maxZoom = 'Max zoom reached',
  measureWindowDisabled = 'Measure window disabled',
  signalDetectionModificationDisabled = 'Signal detection modification disabled for channel',
  predictedPhaseModificationDisabled = 'Predicted phase modification disabled for channel',
  maskModificationDisabled = 'Mask modification disabled for channel',
  signalDetectionInConflict = 'Signal detection modification disabled due to conflict'
}
