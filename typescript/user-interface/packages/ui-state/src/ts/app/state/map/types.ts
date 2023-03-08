export enum MapLayers {
  stations = 'stations',
  sites = 'sites',
  signalDetections = 'signalDetections',
  events = 'events',
  preferredLocationSolution = 'preferredLocationSolution',
  edgeEventsBeforeInterval = 'edgeEventsBeforeInterval',
  edgeEventsAfterInterval = 'edgeEventsAfterInterval',
  nonPreferredLocationSolution = 'nonPreferredLocationSolution',
  confidenceEllipse = 'confidenceEllipse',
  coverageEllipse = 'coverageEllipse',

  edgeDetectionBefore = 'edgeDetectionBefore',
  edgeDetectionAfter = 'edgeDetectionAfter',

  unassociatedDetection = 'unassociatedDetection',
  associatedOpenDetection = 'associatedOpenDetection',
  associatedOtherDetection = 'associatedOtherDetection',
  associatedCompleteDetection = 'associatedCompleteDetection'
}

/**
 * Map Redux State consists of Signal Detection sync state with the
 * Waveform Client's zoom position
 */
export interface MapState {
  isSyncedWithWaveformZoom: boolean;
  layerVisibility: Record<MapLayers, boolean>;
}
