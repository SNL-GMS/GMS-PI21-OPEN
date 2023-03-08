/* eslint-disable @typescript-eslint/no-magic-numbers */
import type { TimeUnits } from '../../src/ts/common-util/time-util';
import {
  calculatePercentTimeRemaining,
  convertDurationToMilliseconds,
  convertDurationToSeconds,
  convertSecondsToDuration,
  DATE_FORMAT,
  DATE_TIME_FORMAT,
  DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION,
  DATE_TIME_FORMAT_WITH_SECOND_PRECISION,
  dateToString,
  DAYS_IN_WEEK,
  epochSecondsNow,
  formatTimeForDisplay,
  getIndexOfFirstSignificantTimeUnit,
  HOURS_IN_DAY,
  ISO_DATE_TIME_FORMAT,
  ISO_DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION,
  ISO_DATE_TIME_FORMAT_WITH_SECOND_PRECISION,
  MILLISECONDS_IN_DAY,
  MILLISECONDS_IN_HALF_DAY,
  MILLISECONDS_IN_HALF_SECOND,
  MILLISECONDS_IN_HOUR,
  MILLISECONDS_IN_MINUTE,
  MILLISECONDS_IN_SECOND,
  MILLISECONDS_IN_WEEK,
  MILLISECONDS_IN_YEAR,
  millisToStringWithMaxPrecision,
  millisToTimeRemaining,
  MINUTES_IN_HOUR,
  REGEX_ISO_DATE_TIME,
  SECONDS_IN_MINUTES,
  secondsToString,
  splitMillisIntoTimeUnits,
  startOfHour,
  stringToDate,
  TIME_FORMAT,
  TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION,
  TIME_FORMAT_WITH_SECOND_PRECISION,
  timeUnitsToString,
  toDate,
  toEpochSeconds,
  toMoment,
  toOSDTime,
  truncateTimeUnits,
  WEEKS_IN_YEAR
} from '../../src/ts/common-util/time-util';

const MOCK_TIME = 1606818240000;
Date.now = jest.fn(() => MOCK_TIME);
Date.constructor = jest.fn(() => new Date(MOCK_TIME));

const realDate = Date;

const millis =
  MILLISECONDS_IN_WEEK +
  MILLISECONDS_IN_DAY +
  MILLISECONDS_IN_MINUTE +
  // eslint-disable-next-line @typescript-eslint/no-magic-numbers
  MILLISECONDS_IN_SECOND * 30;

let spy;

beforeAll(() => {
  const mockDate = new Date(MOCK_TIME);
  // eslint-disable-next-line @typescript-eslint/ban-ts-comment
  // @ts-ignore
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  spy = jest.spyOn(global, 'Date').mockImplementation(() => mockDate);
});

afterAll(() => {
  spy.mockReset();
});

describe('Time Utils', () => {
  test(`Is exported`, () => {
    expect(SECONDS_IN_MINUTES).toBeDefined();
    expect(MINUTES_IN_HOUR).toBeDefined();
    expect(HOURS_IN_DAY).toBeDefined();
    expect(DAYS_IN_WEEK).toBeDefined();
    expect(WEEKS_IN_YEAR).toBeDefined();
    expect(MILLISECONDS_IN_HALF_SECOND).toBeDefined();
    expect(MILLISECONDS_IN_SECOND).toBeDefined();
    expect(MILLISECONDS_IN_MINUTE).toBeDefined();
    expect(MILLISECONDS_IN_HOUR).toBeDefined();
    expect(MILLISECONDS_IN_HALF_DAY).toBeDefined();
    expect(MILLISECONDS_IN_DAY).toBeDefined();
    expect(MILLISECONDS_IN_WEEK).toBeDefined();
    expect(MILLISECONDS_IN_YEAR).toBeDefined();
    expect(DATE_FORMAT).toBeDefined();
    expect(TIME_FORMAT).toBeDefined();
    expect(TIME_FORMAT_WITH_SECOND_PRECISION).toBeDefined();
    expect(TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION).toBeDefined();
    expect(DATE_TIME_FORMAT).toBeDefined();
    expect(DATE_TIME_FORMAT_WITH_SECOND_PRECISION).toBeDefined();
    expect(DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION).toBeDefined();
    expect(ISO_DATE_TIME_FORMAT).toBeDefined();
    expect(ISO_DATE_TIME_FORMAT_WITH_SECOND_PRECISION).toBeDefined();
    expect(ISO_DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION).toBeDefined();
    expect(toMoment).toBeDefined();
    expect(toDate).toBeDefined();
    expect(secondsToString).toBeDefined();
    expect(dateToString).toBeDefined();
    expect(stringToDate).toBeDefined();
    expect(toEpochSeconds).toBeDefined();
    expect(epochSecondsNow).toBeDefined();
    expect(toOSDTime).toBeDefined();
    expect(startOfHour).toBeDefined();
    expect(splitMillisIntoTimeUnits).toBeDefined();
    expect(timeUnitsToString).toBeDefined();
    expect(getIndexOfFirstSignificantTimeUnit).toBeDefined();
    expect(truncateTimeUnits).toBeDefined();
    expect(millisToTimeRemaining).toBeDefined();
    expect(millisToStringWithMaxPrecision).toBeDefined();
    expect(convertDurationToSeconds).toBeDefined();
    expect(convertDurationToMilliseconds).toBeDefined();
    expect(convertSecondsToDuration).toBeDefined();
    expect(calculatePercentTimeRemaining).toBeDefined();
    expect(REGEX_ISO_DATE_TIME).toBeDefined();
  });

  test(`getIndexOfFirstSignificantTimeUnit and timeUnitsToString`, () => {
    const testUnits: TimeUnits = {
      days: 0,
      hours: 0,
      minutes: 0,
      seconds: 0,
      milliseconds: 0
    };
    expect(timeUnitsToString(testUnits, false)).toEqual('');
    testUnits.days = 1;
    expect(getIndexOfFirstSignificantTimeUnit(testUnits)).toEqual(1);
    expect(timeUnitsToString(testUnits, false)).toMatchSnapshot();
    testUnits.days = 0;
    testUnits.hours = 1;
    expect(getIndexOfFirstSignificantTimeUnit(testUnits)).toEqual(2);
    expect(timeUnitsToString(testUnits, false)).toMatchSnapshot();
    testUnits.hours = 0;
    testUnits.minutes = 1;
    expect(getIndexOfFirstSignificantTimeUnit(testUnits)).toEqual(3);
    expect(timeUnitsToString(testUnits, false)).toMatchSnapshot();
    testUnits.minutes = 0;
    testUnits.seconds = 1;
    expect(getIndexOfFirstSignificantTimeUnit(testUnits)).toEqual(4);
    expect(timeUnitsToString(testUnits, false)).toMatchSnapshot();
    testUnits.seconds = 0;
    testUnits.milliseconds = 1;
    expect(getIndexOfFirstSignificantTimeUnit(testUnits)).toEqual(5);
    expect(timeUnitsToString(testUnits, true)).toMatchSnapshot();
  });

  test('toMoment', () => {
    const m = toMoment(0);
    expect(m.valueOf()).toEqual(MOCK_TIME);
  });

  test('toDate', () => {
    const date = toDate(0);
    expect(date.valueOf()).toEqual(MOCK_TIME);
  });

  test('secondsToString', () => {
    expect(secondsToString(0, DATE_FORMAT)).toMatchInlineSnapshot(`"2020-12-01"`);
    expect(secondsToString(0, TIME_FORMAT)).toMatchInlineSnapshot(`"10:24"`);
    expect(secondsToString(0, TIME_FORMAT_WITH_SECOND_PRECISION)).toMatchInlineSnapshot(
      `"10:24:00"`
    );
    expect(secondsToString(0, TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION)).toMatchInlineSnapshot(
      `"10:24:00.000"`
    );
    expect(secondsToString(0, DATE_TIME_FORMAT)).toMatchInlineSnapshot(`"2020-12-01 10:24"`);
    expect(secondsToString(0, DATE_TIME_FORMAT_WITH_SECOND_PRECISION)).toMatchInlineSnapshot(
      `"2020-12-01 10:24:00"`
    );
    expect(
      secondsToString(0, DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION)
    ).toMatchInlineSnapshot(`"2020-12-01 10:24:00.000"`);
    expect(secondsToString(0, ISO_DATE_TIME_FORMAT)).toMatchInlineSnapshot(`"2020-12-01T10:24"`);
    expect(secondsToString(0, ISO_DATE_TIME_FORMAT_WITH_SECOND_PRECISION)).toMatchInlineSnapshot(
      `"2020-12-01T10:24:00"`
    );
    expect(
      secondsToString(0, ISO_DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION)
    ).toMatchInlineSnapshot(`"2020-12-01T10:24:00.000"`);
  });

  test('dateToString', () => {
    expect(dateToString(new Date(0), DATE_FORMAT)).toMatchInlineSnapshot(`"2020-12-01"`);
    expect(dateToString(new Date(0), TIME_FORMAT)).toMatchInlineSnapshot(`"10:24"`);
    expect(dateToString(new Date(0), TIME_FORMAT_WITH_SECOND_PRECISION)).toMatchInlineSnapshot(
      `"10:24:00"`
    );
    expect(
      dateToString(new Date(0), TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION)
    ).toMatchInlineSnapshot(`"10:24:00.000"`);
    expect(dateToString(new Date(0), DATE_TIME_FORMAT)).toMatchInlineSnapshot(`"2020-12-01 10:24"`);
    expect(dateToString(new Date(0), DATE_TIME_FORMAT_WITH_SECOND_PRECISION)).toMatchInlineSnapshot(
      `"2020-12-01 10:24:00"`
    );
    expect(
      dateToString(new Date(0), DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION)
    ).toMatchInlineSnapshot(`"2020-12-01 10:24:00.000"`);
    expect(dateToString(new Date(0), ISO_DATE_TIME_FORMAT)).toMatchInlineSnapshot(
      `"2020-12-01T10:24"`
    );
    expect(
      dateToString(new Date(0), ISO_DATE_TIME_FORMAT_WITH_SECOND_PRECISION)
    ).toMatchInlineSnapshot(`"2020-12-01T10:24:00"`);
    expect(
      dateToString(new Date(0), ISO_DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION)
    ).toMatchInlineSnapshot(`"2020-12-01T10:24:00.000"`);
  });

  test('stringToDate', () => {
    expect(dateToString(stringToDate('2020-12-01'))).toMatchInlineSnapshot(`"2020-12-01 10:24:00"`);
    expect(dateToString(stringToDate('10:24'))).toMatchInlineSnapshot(`"2020-12-01 10:24:00"`);
    expect(dateToString(stringToDate('10:24:00'))).toMatchInlineSnapshot(`"2020-12-01 10:24:00"`);
    expect(dateToString(stringToDate('10:24:00.000'))).toMatchInlineSnapshot(
      `"2020-12-01 10:24:00"`
    );
    expect(dateToString(stringToDate('2020-12-01 10:24'))).toMatchInlineSnapshot(
      `"2020-12-01 10:24:00"`
    );
    expect(dateToString(stringToDate('2020-12-01T10:24:00'))).toMatchInlineSnapshot(
      `"2020-12-01 10:24:00"`
    );
    expect(dateToString(stringToDate('22020-12-01 10:24:00.000'))).toMatchInlineSnapshot(
      `"2020-12-01 10:24:00"`
    );
    expect(dateToString(stringToDate('2020-12-01T10:24:00'))).toMatchInlineSnapshot(
      `"2020-12-01 10:24:00"`
    );
    expect(dateToString(stringToDate('2020-12-01T10:24:00.000'))).toMatchInlineSnapshot(
      `"2020-12-01 10:24:00"`
    );
  });

  test('toEpochSeconds', () => {
    expect(toEpochSeconds(undefined)).toEqual(0);
    expect(toEpochSeconds(null)).toEqual(0);
    expect(toEpochSeconds('')).toEqual(0);

    const secs = toEpochSeconds('1970/01/01 00:00:00.010000');
    expect(secs).toEqual(1606818240);
  });

  test('epochSecondsNow', () => {
    spy.mockReset();
    // eslint-disable-next-line no-global-assign
    Date = realDate;
    expect(epochSecondsNow()).toEqual(1606818240);
  });

  describe('toOSDTime', () => {
    const startOfTimeOSD = '1970-01-01T00:00:00.000Z';
    test('should format a date correctly', () => {
      const secondsInMinute = 60;
      expect(toOSDTime(secondsInMinute)).toBe('1970-01-01T00:01:00.000Z');

      spy.mockReset();
      // eslint-disable-next-line no-global-assign
      Date = realDate;
      expect(toOSDTime(epochSecondsNow())).toEqual('2020-12-01T10:24:00.000Z');
    });
    test('should handle NaN', () => {
      expect(toOSDTime('abc' as any)).toBe(startOfTimeOSD);
    });
  });

  describe('startOfHour', () => {
    const epochSeconds = MOCK_TIME / MILLISECONDS_IN_SECOND;
    test('should be at the start of the hour', () => {
      expect(startOfHour(epochSeconds)).toMatchInlineSnapshot(`1606816800`);
      expect(secondsToString(startOfHour(epochSeconds))).toMatchInlineSnapshot(
        `"2020-12-01 10:00:00"`
      );
    });
  });

  describe('formatTimeForDisplay', () => {
    const defaultInvalidString = 'Unknown';
    const customInvalidString = 'Custom';
    const beginningOfTimeSec = 1;

    it('should return the invalidString for an undefined or null time', () => {
      expect(formatTimeForDisplay(undefined)).toBe(defaultInvalidString);
      expect(formatTimeForDisplay(undefined, customInvalidString)).toBe(customInvalidString);
      expect(formatTimeForDisplay(null, customInvalidString)).toBe(customInvalidString);
    });
    it('should convert numbers into strings of the expected format', () => {
      expect(formatTimeForDisplay(beginningOfTimeSec)).toBe('1970-01-01 00:00:01.000');
    });
    it('should handle the time of 0 correctly', () => {
      expect(formatTimeForDisplay(0)).toBe('1970-01-01 00:00:00.000');
    });
    it('should handle UTC strings', () => {
      expect(formatTimeForDisplay('2020-12-01T10:24:00.000Z')).toBe('2020-12-01 10:24:00.000');
    });
    it('should return the invalidString for an empty string input', () => {
      expect(formatTimeForDisplay('')).toBe(defaultInvalidString);
    });
    it('should return the invalidString for malformed time strings', () => {
      expect(formatTimeForDisplay('1970-01-01T00T:00:00Z')).toBe(defaultInvalidString);
    });
  });

  test('getDurationTime', () => {
    expect(convertDurationToSeconds('PT6S')).toEqual(6);
  });

  test('getDurationMilliTime', () => {
    expect(convertDurationToMilliseconds('PT6S')).toEqual(6000);
  });

  test('setDurationTime', () => {
    expect(convertSecondsToDuration(25000)).toEqual('PT25000S');
  });

  test('Can split seconds', () => {
    const split = splitMillisIntoTimeUnits(millis);
    expect(split).toMatchSnapshot();
  });

  test('Can put time units into a high precision string', () => {
    const split = splitMillisIntoTimeUnits(millis);
    const asString = timeUnitsToString(split);
    expect(asString).toMatchSnapshot();
  });
  test('Can put time units into a max precision string', () => {
    expect(millisToStringWithMaxPrecision(MILLISECONDS_IN_MINUTE, 2)).toMatchSnapshot();
  });

  test('calculatePercentTimeRemaining', () => {
    spy.mockReset();
    // eslint-disable-next-line no-global-assign
    Date = realDate;
    expect(calculatePercentTimeRemaining(1000, 200)).toEqual(8034091196);
  });
});
