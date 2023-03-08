import { isNumber } from '@gms/common-util';

/**
 * Reusable formatter for time uncertainty
 *
 * @param unc the uncertainty value in seconds
 *
 * @returns a string with two trailing digits and 's' to indicate the unit.
 * @example 12s
 */
export function formatUncertainty(unc: number): string {
  if (unc === undefined || !isNumber(unc)) {
    return '';
  }
  return `${unc.toFixed(2)}s`;
}
