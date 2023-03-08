import Immutable from 'immutable';

import { GMS_PERFORMANCE_MONITORING_ENABLED } from './environment-util';
import { Logger } from './logger';

const logger = Logger.create(
  'GMS_LOG_PERFORMANCE_MONITORING',
  process.env.GMS_LOG_PERFORMANCE_MONITORING
);

export const ONE_FRAME_MS = 16;

/**
 * A map of keys to start times, for comparison against end times.
 */
let startTimeMap = Immutable.Map<string, number>();

/**
 * Checks to see if the start time map already has the key set.
 *
 * @param key the key to check
 */
const checkForDuplicateKey = (key: string) => {
  if (startTimeMap.has(key)) {
    logger.warn(
      'PERFORMANCE MONITORING KEY DUPLICATION',
      `key ${key} has already been set. This may cause unreliable behavior for the timing points.`
    );
  }
};

/**
 * Checks to see if the start time map already has the key set.
 *
 * @param key the key to check
 */
const checkThatKeyIsSet = (key: string) => {
  if (!startTimeMap.has(key)) {
    logger.warn(
      'PERFORMANCE MONITORING KEY NOT SET',
      `Timer.end(${key}) has already been set. Cannot check time.`
    );
    return false;
  }
  return true;
};

/**
 * Starts a timer, if the GMS_PERFORMANCE_MONITORING_ENABLED
 * environment variable is set.
 *
 * @param key a descriptive key representing this timer.
 * Keys should be unique per timer (only one timer running
 * at a time for a given key.)
 */
export const start = (key: string): void => {
  if (GMS_PERFORMANCE_MONITORING_ENABLED) {
    checkForDuplicateKey(key);
    if (GMS_PERFORMANCE_MONITORING_ENABLED === 'verbose') {
      logger.time(key);
    }
    startTimeMap = startTimeMap.set(key, Date.now());
  }
};

/**
 * Stops the timer corresponding to a key. If the GMS_PERFORMANCE_MONITORING_ENABLED
 * environment variable is set to 'verbose', it will log times. If it is set to
 * any other string, it will simply log warnings if an operation takes above the
 * acceptableDurationMs, or the 16ms (one frame) default if none is set.
 *
 * @param key the string key which was used to start the timer.
 * @param acceptableDurationMs optional max duration. Timers over this
 * limit will log a warning.
 */
export const end = (key: string, acceptableDurationMs: number = ONE_FRAME_MS): void => {
  if (GMS_PERFORMANCE_MONITORING_ENABLED) {
    if (!checkThatKeyIsSet(key)) {
      return;
    }
    if (GMS_PERFORMANCE_MONITORING_ENABLED === 'verbose') {
      logger.timeEnd(key);
    }
    const startTime = startTimeMap.get(key);
    // clean up after ourselves.
    startTimeMap = startTimeMap.remove(key);
    const duration = Date.now() - startTime;
    if (duration > acceptableDurationMs) {
      logger.warn(
        'TIMING POINT TOO SLOW',
        `${key} timing point took ${duration}ms, which is above the ${acceptableDurationMs}ms threshold.`
      );
    }
  }
};
