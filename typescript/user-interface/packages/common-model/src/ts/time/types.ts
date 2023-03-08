/**
 * JSON fields of type Instance.
 * ! Defines the fields to search that may contain Instance type values for serializing and deserializing
 */
export const JSON_INSTANT_NAMES = [
  'time',
  'startTime',
  'endTime',
  'effectiveAt',
  'effectiveTime',
  'effectiveUntil',
  'creationTime',
  'currentIntervalEndTime',
  'modificationTime',
  'processingStartTime',
  'processingEndTime',
  'referenceTime',
  'travelTime'
] as const;

/**
 * JSON fields of type Duration.
 * ! Defines the fields to search that may contain Duration type values for serializing and deserializing
 */
export const JSON_DURATION_NAMES = [
  'duration',
  'currentIntervalDuration',
  'maximumOpenAnythingDuration',
  'minimumRequestDuration',
  'operationalPeriodStart',
  'operationalPeriodEnd',
  'panSingleArrow',
  'panDoubleArrow',
  'period',
  'leadBufferDuration',
  'lagBufferDuration',
  'waveformPanningBoundaryDuration',
  'groupDelaySec',
  'zasZoomInterval'
] as const;

const InstanceKeySet = new Set(JSON_INSTANT_NAMES);
const DurationKeySet = new Set(JSON_DURATION_NAMES);

type SetKeys<T extends Set<string>> = T extends Set<infer I> ? I : never;

/**
 * A type containing all key strings which represent instance values in OSD-sent JSON objects.
 * ! Represents the json fields that may contain Instance type values for serializing and deserializing
 */
export type InstanceKeys = SetKeys<typeof InstanceKeySet>;

/**
 * A type containing all key strings which represent duration values in OSD-sent JSON objects.
 * ! Represents the json fields that may contain Duration type values for serializing and deserializing
 */
export type DurationKeys = SetKeys<typeof DurationKeySet>;

/**
 * A type containing all key strings which represent instance and duration values in OSD-sent JSON objects.
 * ! Represents the union of all json fields that may contain Instance or Duration type values for serializing and deserializing
 */
export type AllTimeKeys = InstanceKeys | DurationKeys;
