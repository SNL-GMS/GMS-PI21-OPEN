import { millisToStringWithMaxPrecision } from '@gms/common-util';

/**
 * Returns time in ms between then and now
 */
export const calculateTimeFromThenUntilNow = (then: number): number => then - Date.now();

/**
 * Shows time until quiet expiration in a readable format
 */
export const createTooltipMessage = (quietUntilMs: number): string => {
  const timeRemaining = calculateTimeFromThenUntilNow(quietUntilMs);
  const formattedTimeStr = millisToStringWithMaxPrecision(timeRemaining, 2);
  if (timeRemaining > 0 && formattedTimeStr.trim().length > 0) {
    return `Quieted for ${formattedTimeStr}`;
  }
  return 'Quieting is expired';
};
