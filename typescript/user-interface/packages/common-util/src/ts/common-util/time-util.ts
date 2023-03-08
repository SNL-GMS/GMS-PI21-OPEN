/* eslint-disable no-nested-ternary */
import moment from 'moment';

export const SECONDS_IN_MINUTES = 60;
export const SECONDS_IN_HOUR = 3600;
export const MINUTES_IN_HOUR = 60;
export const HOURS_IN_DAY = 24;
export const DAYS_IN_WEEK = 7;
export const WEEKS_IN_YEAR = 52;
export const FORTY_FIVE_DAYS = 45;

export const MILLISECONDS_IN_HALF_SECOND = 500;
export const MILLISECONDS_IN_SECOND = 1000;
export const MICROSECONDS_IN_SECOND = 1000000;
export const MILLISECONDS_IN_MINUTE: number = SECONDS_IN_MINUTES * MILLISECONDS_IN_SECOND;
export const MILLISECONDS_IN_HOUR: number = MINUTES_IN_HOUR * MILLISECONDS_IN_MINUTE;
export const MILLISECONDS_IN_HALF_DAY: number = (HOURS_IN_DAY * MILLISECONDS_IN_HOUR) / 2;
export const MILLISECONDS_IN_DAY: number = HOURS_IN_DAY * MILLISECONDS_IN_HOUR;
export const FORTY_FIVE_DAYS_IN_MILLISECONDS = MILLISECONDS_IN_DAY * FORTY_FIVE_DAYS;
export const FORTY_FIVE_DAYS_IN_SECONDS = FORTY_FIVE_DAYS_IN_MILLISECONDS / MILLISECONDS_IN_SECOND;
export const MILLISECONDS_IN_WEEK: number = DAYS_IN_WEEK * MILLISECONDS_IN_DAY;
export const MILLISECONDS_IN_YEAR: number = MILLISECONDS_IN_WEEK * WEEKS_IN_YEAR;

/**
 * The available date formats as a type
 */
export type DateFormat = 'YYYY-MM-DD';

/**
 * The available time formats as a type
 */
export type TimeFormat = 'HH:mm' | 'HH:mm:ss' | 'HH:mm:ss.SSS';

/**
 * The available date and time formats as a type
 */
export type DateTimeFormat =
  | 'YYYY-MM-DD HH:mm'
  | 'YYYY-MM-DD HH:mm:ss'
  | 'YYYY-MM-DD HH:mm:ss.SSS'
  | 'YYYY-MM-DDTHH:mm'
  | 'YYYY-MM-DDTHH:mm:ss'
  | 'YYYY-MM-DDTHH:mm:ss.SSS';

/**
 * All available date and time formats as a type
 */
export type Format = DateFormat | TimeFormat | DateTimeFormat;

/**
 * Date format.
 */
export const DATE_FORMAT: DateFormat = 'YYYY-MM-DD';

/**
 * Time format.
 */
export const TIME_FORMAT: TimeFormat = 'HH:mm';

/**
 * Time format to the second precision
 */
export const TIME_FORMAT_WITH_SECOND_PRECISION: TimeFormat = 'HH:mm:ss';

/**
 * Date and time format to the sub-second precision (three decimals places)
 */
export const TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION: TimeFormat = 'HH:mm:ss.SSS';

/**
 * Date and time format
 */
export const DATE_TIME_FORMAT: DateTimeFormat = 'YYYY-MM-DD HH:mm';

/**
 * Date and time format with second precision
 */
export const DATE_TIME_FORMAT_WITH_SECOND_PRECISION: DateTimeFormat = 'YYYY-MM-DD HH:mm:ss';

/**
 * Date and time format with fractional second precision
 */
export const DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION: DateTimeFormat =
  'YYYY-MM-DD HH:mm:ss.SSS';

/**
 * ISO Date and time format
 */
export const ISO_DATE_TIME_FORMAT: DateTimeFormat = 'YYYY-MM-DDTHH:mm';

/**
 * ISO Date and time format with second precision
 */
export const ISO_DATE_TIME_FORMAT_WITH_SECOND_PRECISION: DateTimeFormat = 'YYYY-MM-DDTHH:mm:ss';

/**
 * ISO Date and time format with fractional second precision
 */
export const ISO_DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION: DateTimeFormat =
  'YYYY-MM-DDTHH:mm:ss.SSS';

/**
 * A regular expression pattern for matching ISO Date Times
 *
 * Matches: `2010-05-10T10:45:30.123Z`
 */
export const REGEX_ISO_DATE_TIME = /^\d\d\d\d-\d\d-\d\dT\d\d:\d\d:\d\d(?:[.]\d+)?Z/;

/**
 * A regular expression pattern for matching the string `UserCurrentTime`
 */
export const REGEX_USER_CURRENT_TIME = /^UserCurrentTime$/;

/**
 * Format seconds to a Moment object.
 *
 * @param seconds the seconds
 */
export const toMoment = (seconds: number): moment.Moment => moment.unix(seconds).utc();

/**
 * Format seconds to a JS Date object.
 *
 * @param seconds the seconds
 */
export const toDate = (seconds: number): Date => moment.unix(seconds).utc().toDate();

/**
 * Calculates the difference between two times.
 *
 * @param a the first time to compare
 * @param b the second time to compare
 * @param unitOfTime the unit of time
 */
export const diff = (
  start: number,
  end: number,
  unitOfTime: moment.unitOfTime.Diff = 'seconds'
): number => moment.unix(end).diff(moment.unix(start), unitOfTime);

/**
 * Format seconds to a readable string.
 *
 * @param seconds the seconds
 * @param format the format string (defaults to 'YYYY-MM-DD HH:mm:ss')
 */
export const secondsToString = (
  seconds: number,
  format: Format = DATE_TIME_FORMAT_WITH_SECOND_PRECISION
): string => toMoment(seconds).format(format);

/**
 * Format a JS date to a readable string.
 *
 * @param date the JS date
 * @param format the format string (defaults to 'YYYY-MM-DD HH:mm:ss')
 */
export const dateToString = (
  date: Date,
  format: Format = DATE_TIME_FORMAT_WITH_SECOND_PRECISION
): string => moment.utc(date).format(format);

/**
 * Convert an string to a date.
 *
 * @param str the date string
 */
export const stringToDate = (str: string): Date =>
  new Date(
    moment(str, [
      DATE_FORMAT,
      TIME_FORMAT,
      TIME_FORMAT_WITH_SECOND_PRECISION,
      TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION,
      DATE_TIME_FORMAT,
      DATE_TIME_FORMAT_WITH_SECOND_PRECISION,
      DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION,
      ISO_DATE_TIME_FORMAT,
      ISO_DATE_TIME_FORMAT_WITH_SECOND_PRECISION,
      ISO_DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION
    ])
      .utc()
      .valueOf()
  );

/**
 * Converts a UTC date to a date
 *
 * @param date the date to convert
 * @returns the converted date
 */
export const convertUTCDateToDate = (date: Date): Date =>
  new Date(
    Date.UTC(
      date.getFullYear(),
      date.getMonth(),
      date.getDate(),
      date.getHours(),
      date.getMinutes(),
      date.getSeconds(),
      date.getMilliseconds()
    )
  );

/**
 * Converts a date to a UTC representation of the date.
 *
 * @param date the date to convert
 * @returns the converted date
 */

export const convertDateToUTCDate = (date: Date): Date =>
  new Date(date.getTime() + date.getTimezoneOffset() * MILLISECONDS_IN_MINUTE);

/**
 * Convert a formatted date string to epoch seconds.
 *
 * @param str date string in ISO format
 * @returns an epoch seconds representation of the input date spring
 */
export function toEpochSeconds(str: string): number {
  if (str === undefined || str === null || str.length === 0) {
    return 0;
  }
  return new Date(str).valueOf() / MILLISECONDS_IN_SECOND;
}

/**
 * Returns the current time in epoch seconds.
 *
 * @returns an epoch seconds for time now
 */
export function epochSecondsNow(): number {
  return Date.now() / MILLISECONDS_IN_SECOND;
}

/**
 * Convert epoch seconds to OSD compatible ISO formatted date string.
 *
 * If value is NaN, returns the start of Unix epoch time.
 *
 * @param epochSeconds seconds since epoch
 * @returns a New Date string in OSD format
 */
export function toOSDTime(epochSeconds: number): string {
  // eslint-disable-next-line no-restricted-globals
  if (isNaN(epochSeconds)) {
    return toOSDTime(0);
  }
  return new Date(epochSeconds * MILLISECONDS_IN_SECOND).toISOString();
}

/**
 * Takes epoch seconds and returns the closest start of the hour in epoch seconds.
 *
 * @param epochSeconds the epoch seconds
 *
 */
export function startOfHour(epochSeconds: number): number {
  return toEpochSeconds(
    moment(epochSeconds * MILLISECONDS_IN_SECOND)
      .utc()
      .startOf('hour')
      .toISOString()
  );
}

/**
 * takes a UTC formatted time string or number in epoch seconds and formats it for human readable display
 *
 * @param time - the time string to be formatted
 * @param invalidString the string to return if given falsy string, or a string with the wrong format. Defaults to 'Unknown'
 * @returns the time in a human readable format: YYYY-MM-DD HH:MM:SS, or the string 'Unknown' if something goes wrong
 */
export function formatTimeForDisplay(time: number | string, invalidString = 'Unknown'): string {
  let tString;
  if (time === undefined || time === null) {
    return invalidString;
  }
  if (typeof time === 'string') {
    tString = time;
  } else {
    tString = toOSDTime(time);
  }
  if (!tString || tString.length === 0) return invalidString;
  // 1970-01-01T00:00:00Z eg
  const splitTime = tString.split('T');
  if (splitTime.length === 2) {
    // pre to add / for the date
    const date = splitTime[0];
    // remove Z
    const seconds = splitTime[1].replace('Z', '');
    return `${date} ${seconds}`;
  }
  return invalidString;
}

export interface TimeUnits {
  days: number;
  hours: number;
  minutes: number;
  seconds: number;
  milliseconds: number;
}

export const splitMillisIntoTimeUnits = (millis: number): TimeUnits => {
  const days = Math.floor(millis / MILLISECONDS_IN_DAY);
  const remainderAfterDays = millis % MILLISECONDS_IN_DAY;
  const hours = Math.floor(remainderAfterDays / MILLISECONDS_IN_HOUR);
  const remainderAfterHours = millis % MILLISECONDS_IN_HOUR;
  const minutes = Math.floor(remainderAfterHours / MILLISECONDS_IN_MINUTE);
  const remainderAfterMinutes = millis % MILLISECONDS_IN_MINUTE;
  const seconds = Math.floor(remainderAfterMinutes / MILLISECONDS_IN_SECOND);
  const remainderAfterSeconds = millis % MILLISECONDS_IN_SECOND;
  return {
    days,
    hours,
    minutes,
    seconds,
    milliseconds: remainderAfterSeconds
  };
};
/** Functions to format time units * */
const getPluralChar = (units: number): string => (units > 1 ? 's' : '');
const getTimeString = (units: number, timeUnit: string): string =>
  ` ${units} ${timeUnit}${getPluralChar(units)}`;
export const timeUnitsToString = (timeUnits: TimeUnits, includeMilliseconds?: boolean): string => {
  const result =
    `${timeUnits.days > 0 ? getTimeString(timeUnits.days, 'day') : ''}` +
    `${timeUnits.hours > 0 ? getTimeString(timeUnits.hours, 'hour') : ''}` +
    `${timeUnits.minutes > 0 ? getTimeString(timeUnits.minutes, 'minute') : ''}` +
    `${timeUnits.seconds > 0 ? getTimeString(timeUnits.seconds, 'second') : ''}` +
    `${
      includeMilliseconds && timeUnits.milliseconds > 0
        ? getTimeString(timeUnits.milliseconds, 'millisecond')
        : ''
    }`;
  // Strip off leading space to the result
  if (result.length > 0) {
    return result.substring(1);
  }
  return '';
};

export const getIndexOfFirstSignificantTimeUnit = (timeUnits: TimeUnits): number => {
  if (timeUnits.days > 0) {
    return 1;
  }
  if (timeUnits.hours > 0) {
    return 2;
  }
  if (timeUnits.minutes > 0) {
    return 3;
  }
  if (timeUnits.seconds > 0) {
    return 4;
  }
  return 5;
};

/**
 * Sets time units so that no more than the maximum time units are non-zero
 *
 * @param timeUnits units to truncate
 * @param maxTimeUnits maximum amount of units to show
 */
export const truncateTimeUnits = (timeUnits: TimeUnits, maxTimeUnits?: number): TimeUnits => {
  const MAX_THRESHOLD = 9999;
  const increaseThresholdBy = getIndexOfFirstSignificantTimeUnit(timeUnits);
  const threshold = maxTimeUnits > 0 ? maxTimeUnits + increaseThresholdBy : MAX_THRESHOLD;
  return {
    days: threshold >= 2 ? timeUnits.days : 0,
    hours: threshold >= 3 ? timeUnits.hours : 0,
    minutes: threshold >= 4 ? timeUnits.minutes : 0,
    seconds: threshold >= 5 ? timeUnits.seconds : 0,
    // eslint-disable-next-line @typescript-eslint/no-magic-numbers
    milliseconds: threshold >= 6 ? timeUnits.milliseconds : 0
  };
};

/**
 * Converts milliseconds into human readable string
 *
 * @param millis milliseconds to split
 * @param maximumPrecision if provided, will reduce outputted time units to a maximum of three
 * @param includeMilliseconds if provided, output may include milliseconds
 */
export const millisToTimeRemaining = (
  millis: number,
  maximumPrecision?: number,
  includeMilliseconds?: boolean
): string => {
  const timeUnits = splitMillisIntoTimeUnits(millis);
  const timeUnitsToShow: TimeUnits = maximumPrecision
    ? truncateTimeUnits(timeUnits, maximumPrecision)
    : timeUnits;
  return timeUnitsToString(timeUnitsToShow, includeMilliseconds);
};

/**
 * Returns weeks, days, etc. optionally to a maximum given precision. Removes leading a trailing units that are zero
 *
 * ie, 3660001 ms, 2 precision => 1 hour 1 minute
 * ie, 6000 ms, 2 precision => 1 minute
 *
 * @param millis Milliseconds to convert
 * @param maximumPrecision maximum number of time units in string
 */
export const millisToStringWithMaxPrecision = (millis: number, maximumPrecision?: number): string =>
  timeUnitsToString(truncateTimeUnits(splitMillisIntoTimeUnits(millis), maximumPrecision));

/**
 * Convert a duration to seconds.
 *
 * @param duration string of duration i.e. 'PT1.60S' returns 1.6 seconds
 * @returns a number
 */
export function convertDurationToSeconds(duration: string): number {
  // Using milliseconds since asSeconds loses precision
  return moment.duration(duration).asMilliseconds() / MILLISECONDS_IN_SECOND;
}

/**
 * Helper function to convert a Moment string and return epoch milliseconds as a number.
 *
 * @param duration string of duration i.e. 'PT1.60S' returns 1600 milliseconds
 * @returns a number
 */
export function convertDurationToMilliseconds(duration: string): number {
  return moment.duration(duration).asMilliseconds();
}

/**
 * Helper function to format a seconds into duration format.
 *
 * @param duration number of 1.6 seconds returns 'PT1.60S'
 * @param seconds
 * @returns a string
 */
export function convertSecondsToDuration(seconds: number): string {
  // Return formatted string
  return `PT${seconds}S`;
}

/**
 * Calculates what percentage of time remains for a timer.
 *
 * @param timeEndMs the timestamp of the end
 * @param durationMs the timestamp of how long it runs
 */
export function calculatePercentTimeRemaining(timeEndMs: number, durationMs: number): number {
  return 1 - ((Date.now() - timeEndMs) * -1) / durationMs;
}

/**
 * Takes in a time to test and determines the delta time to compare against staleTimeMs.
 * returns true if calculated time is greater than staleTimeMs
 *
 * @param staleTimeMs time limit for stale time to test against
 * @param timeSecs number in seconds gets converted to milliseconds
 */
export const isTimeStale = (timeSecs: number, staleTimeMs: number): boolean => {
  // get milliseconds for current time
  const currentTime: number = Date.now();
  // get milliseconds for the time we are testing
  const timeToTestMS: number = timeSecs * MILLISECONDS_IN_SECOND;
  const deltaTime: number = Math.abs(timeToTestMS - currentTime);
  // send back true if we are past the configured time
  return deltaTime > staleTimeMs;
};
