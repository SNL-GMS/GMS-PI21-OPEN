import { ConfigurationOption } from './configuration-types';

export const allMonitorTypeNames = [
  'LAG',
  'MISSING',
  'TIMELINESS',
  'ENV_AMPLIFIER_SATURATION_DETECTED',
  'ENV_AUTHENTICATION_SEAL_BROKEN',
  'ENV_BACKUP_POWER_UNSTABLE',
  'ENV_BEGINNING_DATE_OUTAGE',
  'ENV_BEGINNING_TIME_OUTAGE',
  'ENV_CALIBRATION_UNDERWAY',
  'ENV_CLIPPED',
  'ENV_CLOCK_DIFFERENTIAL_IN_MICROSECONDS',
  'ENV_CLOCK_DIFFERENTIAL_TOO_LARGE',
  'ENV_CLOCK_LOCKED',
  'ENV_DATA_AVAILABILITY_MINIMUM_CHANNELS',
  'ENV_DATA_AVAILABILITY_GEOPHYSICAL_CHANNELS',
  'ENV_DATA_AVAILABILITY_GEOPHYSICAL_CHANNELS_UNAUTHENTICATED',
  'ENV_LAST_GPS_SYNC_TIME',
  'ENV_DEAD_SENSOR_CHANNEL',
  'ENV_DIGITAL_FILTER_MAY_BE_CHARGING',
  'ENV_DIGITIZER_ANALOG_INPUT_SHORTED',
  'ENV_DIGITIZER_CALIBRATION_LOOP_BACK',
  'ENV_DIGITIZING_EQUIPMENT_OPEN',
  'ENV_DURATION_OUTAGE',
  'ENV_ENDING_DATE_OUTAGE',
  'ENV_ENDING_TIME_OUTAGE',
  'ENV_END_TIME_SERIES_BLOCKETTE',
  'ENV_EQUIPMENT_HOUSING_OPEN',
  'ENV_EQUIPMENT_MOVED',
  'ENV_EVENT_IN_PROGRESS',
  'ENV_GAP',
  'ENV_GLITCHES_DETECTED',
  'ENV_GPS_RECEIVER_OFF',
  'ENV_GPS_RECEIVER_UNLOCKED',
  'ENV_LONG_DATA_RECORD',
  'ENV_MAIN_POWER_FAILURE',
  'ENV_MAXIMUM_DATA_TIME',
  'ENV_MEAN_AMPLITUDE',
  'ENV_MISSION_CAPABILITY_STATISTIC',
  'ENV_NEGATIVE_LEAP_SECOND_DETECTED',
  'ENV_NUMBER_OF_CONSTANT_VALUES',
  'ENV_NUMBER_OF_DATA_GAPS',
  'ENV_NUMBER_OF_SAMPLES',
  'ENV_OUTAGE_COMMENT',
  'ENV_PERCENT_AUTHENTICATED_DATA_AVAILABLE',
  'ENV_PERCENT_DATA_RECEIVED',
  'ENV_PERCENT_UNAUTHENTICATED_DATA_AVAILABLE',
  'ENV_PERCENTAGE_GEOPHYSICAL_CHANNEL_RECEIVED',
  'ENV_POSITIVE_LEAP_SECOND_DETECTED',
  'ENV_QUESTIONABLE_TIME_TAG',
  'ENV_ROOT_MEAN_SQUARE_AMPLITUDE',
  'ENV_SHORT_DATA_RECORD',
  'ENV_SPIKE_DETECTED',
  'ENV_START_TIME_SERIES_BLOCKETTE',
  'ENV_STATION_EVENT_DETRIGGER',
  'ENV_STATION_EVENT_TRIGGER',
  'ENV_STATION_POWER_VOLTAGE',
  'ENV_STATION_VOLUME_PARITY_ERROR_POSSIBLY_PRESENT',
  'ENV_TELEMETRY_SYNCHRONIZATION_ERROR',
  'ENV_TIMELY_DATA_AVAILABILITY',
  'ENV_TIMING_CORRECTION_APPLIED',
  'ENV_VAULT_DOOR_OPENED',
  'ENV_ZEROED_DATA',
];

const MonitorTypeSet = new Set(allMonitorTypeNames);

type SetKeys<T extends Set<string>> = T extends Set<infer I> ? I : never;

/**
 * A type containing all monitor type names
 */
export type MonitorType = SetKeys<typeof MonitorTypeSet>;

export type MonitorTypesForRollupStationConfig = ConfigurationOption<{
  sohMonitorTypesForRollup: MonitorType[];
}>;

export interface ChannelOverrides {
  name: string;
  isIncluded: boolean;
  goodThreshold?: number | string;
  marginalThreshold?: number | string;
}
export interface MonitorTypeConfig {
  isIncluded?: boolean;
  name: string;
  goodThreshold?: number | string;
  marginalThreshold?: number | string;
  channelOverrides?: ChannelOverrides[];
}

export type ThresholdsMap = Record<
  string,
  {
    goodThreshold: string | number;
    marginalThreshold: string | number;
  }
>;
