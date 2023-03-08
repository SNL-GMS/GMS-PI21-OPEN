import { SohTypes } from '@gms/common-model';

import type { CellData } from '../../../../../src/ts/components/data-acquisition-ui/shared/table/types';
import {
  compareCellValues,
  DataReceivedStatus,
  formatSohValue,
  getDataReceivedStatus,
  getDataReceivedStatusRollup,
  getWorseStatus,
  isAcknowledgeEnabled
} from '../../../../../src/ts/components/data-acquisition-ui/shared/table/utils';

/**
 * Checks
 */
describe('Table Utils', () => {
  test('functions should be defined', () => {
    expect(getWorseStatus).toBeDefined();
    expect(compareCellValues).toBeDefined();
    expect(formatSohValue).toBeDefined();
  });

  it('compareCellValues function should properly order numbers mixed with undefined values', () => {
    expect(compareCellValues(undefined, undefined)).toEqual(0);
    expect(compareCellValues(1, undefined)).toBeGreaterThan(0);
    expect(compareCellValues(undefined, 1)).toBeLessThan(0);
    expect(compareCellValues(1, 1)).toEqual(0);
    expect(compareCellValues(1, 2)).toBeLessThan(0);
    expect(compareCellValues(2, 1)).toBeGreaterThan(0);
  });

  it('getWorseStatus function should get the worst status from all permutations', () => {
    const statuses = [
      SohTypes.SohStatusSummary.GOOD,
      SohTypes.SohStatusSummary.BAD,
      SohTypes.SohStatusSummary.MARGINAL,
      SohTypes.SohStatusSummary.NONE
    ];
    let output: SohTypes.SohStatusSummary;
    statuses.forEach((statusA: SohTypes.SohStatusSummary) => {
      statuses.forEach((statusB: SohTypes.SohStatusSummary) => {
        output =
          // eslint-disable-next-line no-nested-ternary
          statusA === SohTypes.SohStatusSummary.BAD || statusB === SohTypes.SohStatusSummary.BAD
            ? SohTypes.SohStatusSummary.BAD
            : // eslint-disable-next-line no-nested-ternary
            statusA === SohTypes.SohStatusSummary.MARGINAL ||
              statusB === SohTypes.SohStatusSummary.MARGINAL
            ? SohTypes.SohStatusSummary.MARGINAL
            : statusA === SohTypes.SohStatusSummary.GOOD ||
              statusB === SohTypes.SohStatusSummary.GOOD
            ? SohTypes.SohStatusSummary.GOOD
            : SohTypes.SohStatusSummary.NONE;
        expect(getWorseStatus(statusA, statusB)).toEqual(output);
      });
    });
  });
  it('formatSohValue returns Unknown when value is NaN', () => {
    const result = formatSohValue(undefined);
    expect(result).toEqual('Unknown');
  });

  it('formatSohValue returns value as string with precision', () => {
    const value = 2.1234;
    const expectedResult = '2.12';
    const result = formatSohValue(value);
    expect(result).toEqual(expectedResult);
  });
  it('getDataReceivedStatusRollup can handle NOT_ENOUGH_DATA', () => {
    const cell: CellData = {
      value: undefined,
      status: SohTypes.SohStatusSummary.GOOD,
      isContributing: true
    };
    const expectedResult = DataReceivedStatus.NOT_ENOUGH_DATA;
    const result = getDataReceivedStatusRollup([cell]);
    expect(result).toEqual(expectedResult);
  });

  it('getDataReceivedStatusRollup can handle NOT_RECEIVED', () => {
    const cell: CellData = {
      value: 5,
      status: undefined,
      isContributing: true
    };
    const expectedResult = DataReceivedStatus.NOT_RECEIVED;
    const result = getDataReceivedStatusRollup([cell]);
    expect(result).toEqual(expectedResult);
  });

  it('getDataReceivedStatus can handle number and RECEIVED', () => {
    const value = 5;
    const expectedResult = DataReceivedStatus.RECEIVED;
    const result = getDataReceivedStatus(value);
    expect(result).toEqual(expectedResult);
  });

  it('getDataReceivedStatus can handle undefined number and NOT_RECEIVED', () => {
    const value = undefined;
    const expectedResult = DataReceivedStatus.NOT_RECEIVED;
    const result = getDataReceivedStatus(value);
    expect(result).toEqual(expectedResult);
  });

  it('isAcknowledgeEnabled can return disable', () => {
    const uiStationSoh: SohTypes.UiStationSoh = {
      id: '1',
      uuid: '1',
      needsAcknowledgement: true,
      needsAttention: true,
      sohStatusSummary: undefined,
      stationGroups: [],
      statusContributors: [],
      time: undefined,
      stationName: '1',
      allStationAggregates: [],
      channelSohs: undefined
    };
    const result = isAcknowledgeEnabled(['Test'], [uiStationSoh], undefined);
    expect(result).toBeFalsy();
  });
});
