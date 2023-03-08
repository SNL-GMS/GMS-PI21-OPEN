import * as StringUtils from '../../src/ts/common-util/string-util';

describe('String util', () => {
  it('can generate a unique number from a string seed', () => {
    const seedStr = 'This string is a seed';
    const firstResult = StringUtils.uniqueNumberFromString(seedStr);
    expect(firstResult).toBeDefined();
    const secondResult = StringUtils.uniqueNumberFromString(seedStr);
    expect(secondResult).toEqual(firstResult);
    const differentStr = 'This is a different seed string';
    const numberFromDifferentSeed = StringUtils.uniqueNumberFromString(differentStr);
    expect(numberFromDifferentSeed === firstResult).toBeFalsy();
  });
  test('string lists are the same', () => {
    const list1 = ['foo', 'bar', 'other'];
    const list2 = ['other', 'bar', 'foo'];
    expect(StringUtils.areListsSame(list1, list2)).toBeTruthy();

    // Add one more entry
    list2.push('oneMore');
    expect(StringUtils.areListsSame(list1, list2)).toBeFalsy();
  });

  test('Convert enum to human readable', () => {
    expect(StringUtils.humanReadable('EVENT_REVIEW')).toEqual('Event Review');
  });

  describe('isNumeric', () => {
    test('properly determines strings containing only digits are numeric', () => {
      expect(StringUtils.isNumeric('345')).toEqual(true);
      expect(StringUtils.isNumeric('.23345')).toEqual(true);
      expect(StringUtils.isNumeric('2435.23345')).toEqual(true);
    });
    test('properly determines strings containing negative numbers are numeric', () => {
      expect(StringUtils.isNumeric('-2435.23345')).toEqual(true);
      expect(StringUtils.isNumeric('-34')).toEqual(true);
    });
    test('properly identifies text strings as non-numeric', () => {
      expect(StringUtils.isNumeric('aounsh')).toEqual(false);
      expect(StringUtils.isNumeric('aounsh463456')).toEqual(false);
    });
    test('properly identifies dates and times as non-numeric', () => {
      expect(StringUtils.isNumeric('2013-12-11 23:45:89')).toEqual(false);
      expect(StringUtils.isNumeric('2013-12-11')).toEqual(false);
      expect(StringUtils.isNumeric('23:45:89')).toEqual(false);
    });
  });

  describe('isDate', () => {
    it('properly determines if a string may be parsed into a date', () => {
      expect(StringUtils.isDate('2022-08-24 16:00:00')).toEqual(true);
      expect(StringUtils.isDate('2019-06-11T00:00')).toEqual(true);
    });
    it('properly determines if a string may not be parsed into a date', () => {
      expect(StringUtils.isDate('Friday')).toEqual(false);
    });
    it('returns false if given undefined', () => {
      expect(StringUtils.isDate(undefined)).toEqual(false);
    });
  });

  describe('defaultTo', () => {
    it('returns default if value is null', () => {
      const providedDefault = 'something else';
      const resultWithNoSpecifiedDefault = StringUtils.defaultTo(null);
      const resultWithProvidedDefault = StringUtils.defaultTo(null, providedDefault);
      expect(resultWithNoSpecifiedDefault).toEqual('Unknown');
      expect(resultWithProvidedDefault).toEqual(providedDefault);
    });
    it('returns default if value is empty', () => {
      const providedDefault = 'something else';
      const resultWithNoSpecifiedDefault = StringUtils.defaultTo('');
      const resultWithProvidedDefault = StringUtils.defaultTo('', providedDefault);
      expect(resultWithNoSpecifiedDefault).toEqual('Unknown');
      expect(resultWithProvidedDefault).toEqual(providedDefault);
    });
    it('returns default if value is undefined', () => {
      const providedDefault = 'something else';
      const resultWithNoSpecifiedDefault = StringUtils.defaultTo(undefined);
      const resultWithProvidedDefault = StringUtils.defaultTo(undefined, providedDefault);
      expect(resultWithNoSpecifiedDefault).toEqual('Unknown');
      expect(resultWithProvidedDefault).toEqual(providedDefault);
    });
    it('returns value if value is well defined', () => {
      const value = 'oehtnshtnoeua';
      const providedDefault = 'something else';
      const resultWithNoSpecifiedDefault = StringUtils.defaultTo(value);
      const resultWithProvidedDefault = StringUtils.defaultTo(value, providedDefault);
      expect(resultWithNoSpecifiedDefault).toEqual(value);
      expect(resultWithProvidedDefault).toEqual(value);
    });
    it('returns value if value is white space', () => {
      const value = '   ';
      const providedDefault = 'something else';
      const resultWithNoSpecifiedDefault = StringUtils.defaultTo(value);
      const resultWithProvidedDefault = StringUtils.defaultTo(value, providedDefault);
      expect(resultWithNoSpecifiedDefault).toEqual(value);
      expect(resultWithProvidedDefault).toEqual(value);
    });
  });
});
