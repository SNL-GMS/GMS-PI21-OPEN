/**
 * Station SOH statuses.
 */
export enum SohStatusSummary {
  NONE = 'NONE',
  BAD = 'BAD',
  MARGINAL = 'MARGINAL',
  GOOD = 'GOOD'
}

/*
 * Monitor types used by the system
 * CD 1.1 types only. This is a subset of all ACEI types, plus Lag and Missing.
 */
export enum SohMonitorType {
  LAG = 'LAG',
  MISSING = 'MISSING',
  TIMELINESS = 'TIMELINESS',
  ENV_AUTHENTICATION_SEAL_BROKEN = 'ENV_AUTHENTICATION_SEAL_BROKEN',
  ENV_BACKUP_POWER_UNSTABLE = 'ENV_BACKUP_POWER_UNSTABLE',
  ENV_CALIBRATION_UNDERWAY = 'ENV_CALIBRATION_UNDERWAY',
  ENV_CLIPPED = 'ENV_CLIPPED',
  ENV_CLOCK_DIFFERENTIAL_IN_MICROSECONDS = 'ENV_CLOCK_DIFFERENTIAL_IN_MICROSECONDS',
  ENV_CLOCK_DIFFERENTIAL_TOO_LARGE = 'ENV_CLOCK_DIFFERENTIAL_TOO_LARGE',
  ENV_LAST_GPS_SYNC_TIME = 'ENV_LAST_GPS_SYNC_TIME',
  ENV_DEAD_SENSOR_CHANNEL = 'ENV_DEAD_SENSOR_CHANNEL',
  ENV_DIGITIZER_ANALOG_INPUT_SHORTED = 'ENV_DIGITIZER_ANALOG_INPUT_SHORTED',
  ENV_DIGITIZER_CALIBRATION_LOOP_BACK = 'ENV_DIGITIZER_CALIBRATION_LOOP_BACK',
  ENV_DIGITIZING_EQUIPMENT_OPEN = 'ENV_DIGITIZING_EQUIPMENT_OPEN',
  ENV_EQUIPMENT_HOUSING_OPEN = 'ENV_EQUIPMENT_HOUSING_OPEN',
  ENV_EQUIPMENT_MOVED = 'ENV_EQUIPMENT_MOVED',
  ENV_GPS_RECEIVER_OFF = 'ENV_GPS_RECEIVER_OFF',
  ENV_GPS_RECEIVER_UNLOCKED = 'ENV_GPS_RECEIVER_UNLOCKED',
  ENV_MAIN_POWER_FAILURE = 'ENV_MAIN_POWER_FAILURE',
  ENV_STATION_POWER_VOLTAGE = 'ENV_STATION_POWER_VOLTAGE',
  ENV_VAULT_DOOR_OPENED = 'ENV_VAULT_DOOR_OPENED',
  ENV_ZEROED_DATA = 'ENV_ZEROED_DATA'
}

/**
 * Station AggregateTypes enumeration
 */
export enum StationAggregateType {
  LAG = 'LAG',
  MISSING = 'MISSING',
  TIMELINESS = 'TIMELINESS',
  ENVIRONMENTAL_ISSUES = 'ENVIRONMENTAL_ISSUES'
}

/**
 * ! ENV_LAST_GPS_SYNC_TIME needs to become LAST_GPS_SYNC_TIME
 * Remove code out of formatting-util function
 * that is looking for this special case of ENV_LAST_GPS_SYNC_TIME
 * ! AVAILABLITY is a known misspelling
 * Acquired Channel Environment Issue enumerations
 * (defined in section 2.4.5 of Data Model v2.1)
 */
export enum AceiType {
  AMPLIFIER_SATURATION_DETECTED = 'AMPLIFIER_SATURATION_DETECTED', // MiniSeed only
  AUTHENTICATION_SEAL_BROKEN = 'AUTHENTICATION_SEAL_BROKEN',
  BACKUP_POWER_UNSTABLE = 'BACKUP_POWER_UNSTABLE',
  BEGINNING_DATE_OUTAGE = 'BEGINNING_DATE_OUTAGE', // IMS 2.0: Analog
  BEGINNING_TIME_OUTAGE = 'BEGINNING_TIME_OUTAGE', // IMS 2.0: Analog
  CALIBRATION_UNDERWAY = 'CALIBRATION_UNDERWAY', // aka. CALIBRATION
  CLIPPED = 'CLIPPED',
  CLOCK_DIFFERENTIAL_IN_MICROSECONDS = 'CLOCK_DIFFERENTIAL_IN_MICROSECONDS',
  CLOCK_DIFFERENTIAL_TOO_LARGE = 'CLOCK_DIFFERENTIAL_TOO_LARGE',
  CLOCK_LOCKED = 'CLOCK_LOCKED', // MiniSeed Addition
  DATA_AVAILABILITY_MINIMUM_CHANNELS = 'DATA_AVAILABILITY_MINIMUM_CHANNELS', // IMS 2.0: Analog
  DATA_AVAILABLITY_GEOPHYSICAL_CHANNELS = 'DATA_AVAILABLITY_GEOPHYSICAL_CHANNELS', // IMS 2.0: Analog
  DATA_AVAILABLITY_GEOPHYSICAL_CHANNELS_UNAUTHENTICATED = 'DATA_AVAILABLITY_GEOPHYSICAL_CHANNELS_UNAUTHENTICATED',
  ENV_LAST_GPS_SYNC_TIME = 'ENV_LAST_GPS_SYNC_TIME',
  DEAD_SENSOR_CHANNEL = 'DEAD_SENSOR_CHANNEL',
  DIGITAL_FILTER_MAY_BE_CHARGING = 'DIGITAL_FILTER_MAY_BE_CHARGING', // MiniSeed Addition
  DIGITIZER_ANALOG_INPUT_SHORTED = 'DIGITIZER_ANALOG_INPUT_SHORTED',
  DIGITIZER_CALIBRATION_LOOP_BACK = 'DIGITIZER_CALIBRATION_LOOP_BACK',
  DIGITIZING_EQUIPMENT_OPEN = 'DIGITIZING_EQUIPMENT_OPEN',
  DURATION_OUTAGE = 'DURATION_OUTAGE', // IMS 2.0: Analog
  ENDING_DATE_OUTAGE = 'ENDING_DATE_OUTAGE', // IMS 2.0: Analog
  ENDING_TIME_OUTAGE = 'ENDING_TIME_OUTAGE', // IMS 2.0: Analog
  END_TIME_SERIES_BLOCKETTE = 'END_TIME_SERIES_BLOCKETTE', // MiniSeed Addition
  EQUIPMENT_HOUSING_OPEN = 'EQUIPMENT_HOUSING_OPEN', // aka. EQUIPMENT_OPEN
  EQUIPMENT_MOVED = 'EQUIPMENT_MOVED',
  EVENT_IN_PROGRESS = 'EVENT_IN_PROGRESS', // MiniSeed Addition
  GAP = 'GAP',
  GLITCHES_DETECTED = 'GLITCHES_DETECTED', // MiniSeed Addition
  GPS_RECEIVER_OFF = 'GPS_RECEIVER_OFF', // aka. GPS_OFF
  GPS_RECEIVER_UNLOCKED = 'GPS_RECEIVER_UNLOCKED',
  LONG_DATA_RECORD = 'LONG_DATA_RECORD', // MiniSeed Addition
  MAIN_POWER_FAILURE = 'MAIN_POWER_FAILURE',
  MAXIMUM_DATA_TIME = 'MAXIMUM_DATA_TIME', // IMS 2.0: Analog
  MEAN_AMPLITUDE = 'MEAN_AMPLITUDE', // IMS 2.0: Analog
  MISSION_CAPABILITY_STATISTIC = 'MISSION_CAPABILITY_STATISTIC', // IMS 2.0: Analog
  NEGATIVE_LEAP_SECOND_DETECTED = 'NEGATIVE_LEAP_SECOND_DETECTED', // MiniSeed Addition
  NUMBER_OF_CONSTANT_VALUES = 'NUMBER_OF_CONSTANT_VALUES',
  NUMBER_OF_DATA_GAPS = 'NUMBER_OF_DATA_GAPS', // IMS 2.0: Analog
  NUMBER_OF_SAMPLE = 'NUMBER_OF_SAMPLES', // IMS 2.0: Analog
  OUTAGE_COMMENT = 'OUTAGE_COMMENT', // IMS 2.0: Analog
  PERCENT_AUTHENTICATED_DATA_AVAILABLE = 'PERCENT_AUTHENTICATED_DATA_AVAILABLE', // IMS 2.0: Analog
  PERCENT_DATA_RECEIVED = 'PERCENT_DATA_RECEIVED', // IMS 2.0: Analog
  PERCENT_UNAUTHENTICATED_DATA_AVAILABLE = 'PERCENT_UNAUTHENTICATED_DATA_AVAILABLE', // IMS 2.0: Analog
  PERCENTAGE_GEOPHYSICAL_CHANNEL_RECEIVED = 'PERCENTAGE_GEOPHYSICAL_CHANNEL_RECEIVED', // IMS 2.0: Analog
  POSITIVE_LEAP_SECOND_DETECTED = 'POSITIVE_LEAP_SECOND_DETECTED', // MiniSeed Addition
  QUESTIONABLE_TIME_TAG = 'QUESTIONABLE_TIME_TAG', // MiniSeed Addition
  ROOT_MEAN_SQUARE_AMPLITUDE = 'ROOT_MEAN_SQUARE_AMPLITUDE',
  SHORT_DATA_RECORD = 'SHORT_DATA_RECORD', // MiniSeed Addition
  SPIKE_DETECTED = 'SPIKE_DETECTED', // MiniSeed Addition
  START_TIME_SERIES_BLOCKETTE = 'START_TIME_SERIES_BLOCKETTE', // MiniSeed Addition
  STATION_EVENT_DETRIGGER = 'STATION_EVENT_DETRIGGER', // MiniSeed Addition
  STATION_EVENT_TRIGGER = 'STATION_EVENT_TRIGGER', // MiniSeed Addition
  STATION_POWER_VOLTAGE = 'STATION_POWER_VOLTAGE', // analog soh value
  STATION_VOLUME_PARITY_ERROR_POSSIBLY_PRESENT = 'STATION_VOLUME_PARITY_ERROR_POSSIBLY_PRESENT',
  TELEMETRY_SYNCHRONIZATION_ERROR = 'TELEMETRY_SYNCHRONIZATION_ERROR',
  TIMELY_DATA_AVAILABILITY = 'TIMELY_DATA_AVAILABILITY',
  TIMING_CORRECTION_APPLIED = 'TIMING_CORRECTION_APPLIED',
  VAULT_DOOR_OPENED = 'VAULT_DOOR_OPENED',
  ZEROED_DATA = 'ZEROED_DATA'
}

export enum AnalogAceiType {
  CLOCK_DIFFERENTIAL_IN_MICROSECONDS = 'CLOCK_DIFFERENTIAL_IN_MICROSECONDS',
  ENV_LAST_GPS_SYNC_TIME = 'ENV_LAST_GPS_SYNC_TIME',
  PERCENT_DATA_RECEIVED = 'PERCENT_DATA_RECEIVED', // IMS 2.0: Analog
  PERCENT_UNAUTHENTICATED_DATA_AVAILABLE = 'PERCENT_UNAUTHENTICATED_DATA_AVAILABLE', // IMS 2.0: Analog
  NUMBER_OF_DATA_GAPS = 'NUMBER_OF_DATA_GAPS', // IMS 2.0: Analog
  NUMBER_OF_SAMPLE = 'NUMBER_OF_SAMPLES', // IMS 2.0: Analog
  NUMBER_OF_CONSTANT_VALUES = 'NUMBER_OF_CONSTANT_VALUES',
  MEAN_AMPLITUDE = 'MEAN_AMPLITUDE', // IMS 2.0: Analog
  ROOT_MEAN_SQUARE_AMPLITUDE = 'ROOT_MEAN_SQUARE_AMPLITUDE',
  BEGINNING_DATE_OUTAGE = 'BEGINNING_DATE_OUTAGE', // IMS 2.0: Analog
  BEGINNING_TIME_OUTAGE = 'BEGINNING_TIME_OUTAGE', // IMS 2.0: Analog
  ENDING_DATE_OUTAGE = 'ENDING_DATE_OUTAGE', // IMS 2.0: Analog
  ENDING_TIME_OUTAGE = 'ENDING_TIME_OUTAGE', // IMS 2.0: Analog
  DURATION_OUTAGE = 'DURATION_OUTAGE', // IMS 2.0: Analog
  OUTAGE_COMMENT = 'OUTAGE_COMMENT', // IMS 2.0: Analog
  MAXIMUM_DATA_TIME = 'MAXIMUM_DATA_TIME', // IMS 2.0: Analog
  DATA_AVAILABILITY_MINIMUM_CHANNELS = 'DATA_AVAILABILITY_MINIMUM_CHANNELS', // IMS 2.0: Analog
  TIMELY_DATA_AVAILABILITY = 'TIMELY_DATA_AVAILABILITY',
  MISSION_CAPABILITY_STATISTIC = 'MISSION_CAPABILITY_STATISTIC', // IMS 2.0: Analog
  PERCENTAGE_GEOPHYSICAL_CHANNEL_RECEIVED = 'PERCENTAGE_GEOPHYSICAL_CHANNEL_RECEIVED', // IMS 2.0: Analog
  DATA_AVAILABLITY_GEOPHYSICAL_CHANNELS = 'DATA_AVAILABLITY_GEOPHYSICAL_CHANNELS', // IMS 2.0: Analog,
  STATION_POWER_VOLTAGE = 'STATION_POWER_VOLTAGE' // analog soh value
}

/**
 * SOH Value types
 */
export enum SohValueType {
  PERCENT = 'PERCENT',
  DURATION = 'DURATION'
}

/**
 * Channel SOH statuses.
 */
export interface ChannelSohForStation {
  stationName: string;
  uuid: string;
  channelSohs: ChannelSoh[];
}

export interface StationSohIssue {
  requiresAcknowledgement: boolean;
  acknowledgedAt: string;
}

export interface SohStatus {
  stationAcquisitionSohStatus: StationAcquisitionSohStatus;
  environmentSohStatus: EnvironmentSohStatus;
}

export interface StationAcquisitionSohStatus {
  completeness: number;
  completenessSummary: SohStatusSummary;
  lag: string;
  lagSummary: SohStatusSummary;
}

export interface EnvironmentSohStatus {
  countBySohType: CountBySoh;
  summaryBySohType: SummaryBySoh;
}

/**
 * Type used by EnvironmentSohStatus
 */
export interface CountBySoh {
  CLOCK_LOCKED: number;
  POSITIVE_LEAP_SECOND_DETECTED: number;
  QUESTIONABLE_TIME_TAG: number;
  START_TIME_SERIES_BLOCKETTE: number;
  EVENT_IN_PROGRESS: number;
  STATION_EVENT_DETRIGGER: number;
  DIGITAL_FILTER_MAY_BE_CHARGING: number;
  SPIKE_DETECTED: number;
  GLITCHES_DETECTED: number;
  STATION_EVENT_TRIGGER: number;
  END_TIME_SERIES_BLOCKETTE: number;
  SHORT_DATA_RECORD: number;
  NEGATIVE_LEAP_SECOND_DETECTED: number;
  LONG_DATA_RECORD: number;
}

/**
 * Type used by EnvironmentSohStatus
 */
export interface SummaryBySoh {
  CLOCK_LOCKED: SohStatusSummary;
  POSITIVE_LEAP_SECOND_DETECTED: SohStatusSummary;
  QUESTIONABLE_TIME_TAG: SohStatusSummary;
  START_TIME_SERIES_BLOCKETTE: SohStatusSummary;
  EVENT_IN_PROGRESS: SohStatusSummary;
  STATION_EVENT_DETRIGGER: SohStatusSummary;
  DIGITAL_FILTER_MAY_BE_CHARGING: SohStatusSummary;
  SPIKE_DETECTED: SohStatusSummary;
  GLITCHES_DETECTED: SohStatusSummary;
  STATION_EVENT_TRIGGER: SohStatusSummary;
  END_TIME_SERIES_BLOCKETTE: SohStatusSummary;
  SHORT_DATA_RECORD: SohStatusSummary;
  NEGATIVE_LEAP_SECOND_DETECTED: SohStatusSummary;
  LONG_DATA_RECORD: SohStatusSummary;
}

export interface ChannelSohStatus {
  channelName: string;
  sohStatus: SohStatus;
}

export interface SohSummary {
  modified: boolean;
}

export interface SohContributor {
  value: number;
  valuePresent: boolean;
  statusSummary: SohStatusSummary;
  contributing: boolean;
  type: SohMonitorType;
}

/**
 * Base Monitor value and status
 */
export interface SohMonitorValueAndStatus {
  readonly status: SohStatusSummary;
  readonly value: number;
  readonly valuePresent: boolean;
  readonly monitorType: SohMonitorType;
  readonly hasUnacknowledgedChanges: boolean;
  readonly contributing: boolean;
  readonly thresholdMarginal?: number;
  readonly thresholdBad?: number;
  readonly quietUntilMs?: number;
  readonly quietDurationMs?: number;
}

/**
 * Channel SOH object
 */
export interface ChannelSoh {
  readonly channelName: string;
  readonly channelSohStatus: SohStatusSummary;
  readonly allSohMonitorValueAndStatuses: SohMonitorValueAndStatus[];
}

/**
 * Station SOH Capability specific to a station group's station
 */
export interface StationSohCapabilityStatus {
  groupName: string;
  stationName: string;
  sohStationCapability: SohStatusSummary;
}

/**
 * StationAggregate list for StationAggregates (Missing, Lag, Timeliness and Environmental_Issue)
 */
export interface StationAggregate {
  value: number;
  valuePresent: boolean;
  aggregateType: StationAggregateType;
}
/**
 * UiStationSoh created by the SSAM in the OSD and sent on Kafka topic
 */
export interface UiStationSoh {
  id: string;
  uuid: string;
  stationName: string;
  sohStatusSummary: SohStatusSummary;
  needsAcknowledgement: boolean;
  needsAttention: boolean;
  statusContributors: SohContributor[];
  stationGroups: StationSohCapabilityStatus[];
  channelSohs: ChannelSoh[];
  time: number;
  allStationAggregates: StationAggregate[];
}

/**
 * Monitor Value that contains average data that doesn't reflect decimated points
 * Back end calculates the average based on all the points.
 * ! Once Backend updates response to return values as an array and type at
 * ! level higher this interface will need to be updated
 */
export interface MonitorValue {
  readonly channelName: string;
  readonly values: Values;
  readonly average: number;
}

/**
 * The input for the query to retrieve decimated historical station SOH.
 */
export interface RetrieveDecimatedHistoricalStationSohInput {
  readonly stationName: string;
  readonly startTime: number;
  readonly endTime: number;
  readonly sohMonitorType: SohMonitorType;
  readonly samplesPerChannel: number;
}

/**
 * ! Temp interface that is being used due to backend not easily able
 * ! to provide the correct structure in response. Once they update it, this can be removed
 * ! And the type will be brought back up to the MonitorValue level
 */
export interface Values {
  readonly values: number[];
  readonly type: SohValueType;
}

export interface UiHistoricalSoh {
  readonly stationName: string;
  readonly calculationTimes: number[];
  readonly monitorValues: MonitorValue[];
  readonly percentageSent: number;
}

/**
 * Input used to query against station trend line (issues) for a specific issue topic
 */
export interface UiHistoricalAceiInput {
  stationName: string;
  startTime: number;
  endTime: number;
  type: AceiType;
}

/**
 * Historical AcquiredChannelEnvironmentalIssues (Acei)
 *
 * The environmental issues trend line. The inner most collection represents a data point with
 * x value start time and y value status, either int or double. The exception to this is the last
 * data point; it is comprised of end time and status matching the status of the preceding
 * data point. In the below example, the trend line is comprised of three line segments. There
 * is a gap in data between each line segment.
 *
 *  serialized example:
 *  [
 *    [ [ 1482456217000, 0 ], [ 1482456293000, 1 ], [ 1482456369000, 0 ], [ 1482456445000, 0 ] ],
 *    [ [ 1482457781000, 1 ], [ 1482457857000, 0 ], [ 1482457933000, 1 ], [ 1482458009000, 1 ] ],
 *    [ [ 1482459345000, 0 ], [ 1482459421000, 1 ], [ 1482459497000, 0 ], [ 1482459573000, 0 ] ]
 *
 *  ]
 *
 */
export interface UiHistoricalAcei {
  channelName: string;
  monitorType: AceiType;
  issues: number[][][];
}

/**
 * end of Historical Acei
 */

export interface StationGroupSohStatus {
  stationGroupName: string;
  time: number;
  groupCapabilityStatus: SohStatusSummary;
  id: string;
  priority: number;
}

export interface StationAndStationGroupSoh {
  stationGroups: StationGroupSohStatus[];
  stationSoh: UiStationSoh[];
  isUpdateResponse: boolean;
}

export interface ChannelMonitorPair {
  channelName: string;
  monitorType: SohMonitorType;
}

export interface ChannelMonitorInput {
  stationName: string;
  channelMonitorPairs: ChannelMonitorPair[];
  userName: string;
  quietDurationMs?: number;
  comment?: string;
}

/**
 * A Quieted Soh status change, that is used to keep track of when a channel
 * monitor was quieted, and how much longer it should be quite.
 */
export interface QuietedSohStatusChange {
  readonly stationName: string;
  readonly sohMonitorType: SohMonitorType;
  readonly channelName: string;
  readonly quietUntil: string;
  readonly quietedBy: string;
  readonly quietDuration: string;
  readonly comment?: string;
}

export interface AcknowledgeSohStatus {
  stationNames: string[];
  userName: string;
  comment?: string;
}

export interface AcknowledgedSohStatusChange {
  id: string;
  acknowledgedBy: string;
  acknowledgedAt: string;
  comment?: string;
  acknowledgedChanges: SohStatusChange[];
  acknowledgedStation: string;
}

export interface SohStatusChange {
  firstChangeTime: number;
  sohMonitorType: SohMonitorType;
  changedChannel: string;
}

export const isEnvironmentalIssue = (sohMonitorType: SohMonitorType): boolean =>
  sohMonitorType !== SohMonitorType.LAG &&
  sohMonitorType !== SohMonitorType.MISSING &&
  sohMonitorType !== SohMonitorType.TIMELINESS;
