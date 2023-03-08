/**
 * Defines the type of the message.
 */
export enum SystemMessageType {
  STATION_NEEDS_ATTENTION = 'STATION_NEEDS_ATTENTION',
  STATION_SOH_STATUS_CHANGED = 'STATION_SOH_STATUS_CHANGED',
  STATION_CAPABILITY_STATUS_CHANGED = 'STATION_CAPABILITY_STATUS_CHANGED',
  STATION_GROUP_CAPABILITY_STATUS_CHANGED = 'STATION_GROUP_CAPABILITY_STATUS_CHANGED',
  CHANNEL_MONITOR_TYPE_STATUS_CHANGED = 'CHANNEL_MONITOR_TYPE_STATUS_CHANGED',
  CHANNEL_MONITOR_TYPE_STATUS_CHANGE_ACKNOWLEDGED = 'CHANNEL_MONITOR_TYPE_STATUS_CHANGE_ACKNOWLEDGED',
  CHANNEL_MONITOR_TYPE_QUIETED = 'CHANNEL_MONITOR_TYPE_QUIETED',
  CHANNEL_MONITOR_TYPE_QUIET_PERIOD_CANCELED = 'CHANNEL_MONITOR_TYPE_QUIET_PERIOD_CANCELED',
  CHANNEL_MONITOR_TYPE_QUIET_PERIOD_EXPIRED = 'CHANNEL_MONITOR_TYPE_QUIET_PERIOD_EXPIRED'
}

/**
 * Defines the severity of the message.
 */
export enum SystemMessageSeverity {
  INFO = 'INFO',
  WARNING = 'WARNING',
  CRITICAL = 'CRITICAL'
}

/**
 * Defines the category of the message.
 */
export enum SystemMessageCategory {
  SOH = 'SOH'
}

/**
 * Defines the subcategory of the message.
 */
export enum SystemMessageSubCategory {
  CAPABILITY = 'CAPABILITY',
  STATUS = 'STATUS',
  STATION = 'STATION',
  USER = 'USER'
}

/**
 * Represents a GMS system message.
 */
export interface SystemMessage {
  /** Id of this object. */
  readonly id: string;

  /** Time this system message was generated (EPOCH milliseconds). */
  readonly time: number;

  /** The content of this system message. */
  readonly message: string;

  /** The type of this system message. */
  readonly type: SystemMessageType;

  /** The severity of this system message. */
  readonly severity: SystemMessageSeverity;

  /** The category of this system message. */
  readonly category: SystemMessageCategory;

  /** The subcategory of this system message. */
  readonly subCategory: SystemMessageSubCategory;
}

/**
 * System message definition
 */
export interface SystemMessageDefinition {
  /** Type of the system message */
  readonly systemMessageType: string;

  /** Describes the system message category */
  readonly systemMessageCategory: string;

  /** Describes system message sub category */
  readonly systemMessageSubCategory: string;

  /** Describes system message severity */
  readonly systemMessageSeverity: string;

  /** Template string that has variable inserts available to provide a description */
  readonly template: string;
}
