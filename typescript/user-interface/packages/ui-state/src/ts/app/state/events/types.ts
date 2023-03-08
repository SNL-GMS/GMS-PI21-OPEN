/**
 * Enumerated column names for the event table
 */
export enum EventsColumn {
  conflict = 'conflict',
  time = 'time',
  latitudeDegrees = 'latitudeDegrees',
  longitudeDegrees = 'longitudeDegrees',
  depthKm = 'depthKm',
  magnitudeMb = 'magnitudeMb',
  magnitudeMs = 'magnitudeMs',
  magnitudeMl = 'magnitudeMl',
  coverageSemiMajorAxis = 'coverageSemiMajorAxis',
  coverageSemiMinorAxis = 'coverageSemiMinorAxis',
  confidenceSemiMajorAxis = 'confidenceSemiMajorAxis',
  confidenceSemiMinorAxis = 'confidenceSemiMinorAxis',
  region = 'region',
  activeAnalysts = 'activeAnalysts',
  preferred = 'preferred',
  status = 'status',
  rejected = 'rejected'
}

/**
 * Enumerated event filter options for the event table
 */
export enum EventFilters {
  BEFORE = 'Edge events before interval',
  AFTER = 'Edge events after interval'
}

export interface EventsState {
  eventsColumns: Record<EventsColumn, boolean>;
  edgeEvents: Record<EventFilters, boolean>;
  stationsAssociatedWithCurrentOpenEvent?: string[];
}
