/* eslint-disable @typescript-eslint/no-magic-numbers */
import {
  capitalizeFirstLetters,
  determinePrecisionByType,
  prettifyAllCapsEnumType,
  setDecimalPrecision,
  setDecimalPrecisionAsNumber,
  stripOutFirstOccurrence
} from '../../src/ts/common-util/formatting-util';
import { ValueType } from '../../src/ts/types/value-type';

describe('Format utils', () => {
  test('Set Decimal Precision', () => {
    const numberToEdit = 4.89756;
    const one = setDecimalPrecision(numberToEdit, 1);
    const oneNumber = setDecimalPrecisionAsNumber(numberToEdit, 1);
    expect(one).toEqual('4.9');
    expect(oneNumber).toEqual(4.9);

    const two = setDecimalPrecision(numberToEdit, 2);
    const twoNumber = setDecimalPrecisionAsNumber(numberToEdit, 2);
    expect(two).toEqual('4.90');
    expect(twoNumber).toEqual(4.9);

    const three = setDecimalPrecision(numberToEdit, 3);
    const threeNumber = setDecimalPrecisionAsNumber(numberToEdit, 3);
    expect(three).toEqual('4.898');
    expect(threeNumber).toEqual(4.898);

    const four = setDecimalPrecision(numberToEdit, 4);
    const fourNumber = setDecimalPrecisionAsNumber(numberToEdit, 4);
    expect(four).toEqual('4.8976');
    expect(fourNumber).toEqual(4.8976);
  });

  test('Prettify All Caps Enum', () => {
    const singleWordEnum = 'MISSING';
    expect(prettifyAllCapsEnumType(singleWordEnum)).toEqual('Missing');

    const multiWordEnum = 'ENV_TYPE_TO_TEST';
    expect(prettifyAllCapsEnumType(multiWordEnum, true)).toEqual('Type To Test');
  });

  test('Strip station from channel name', () => {
    const channel = 'STATION.SITE.CHANNEL';
    expect(stripOutFirstOccurrence(channel)).toEqual('SITE.CHANNEL');

    // Handle where pattern doesn't match channel
    expect(stripOutFirstOccurrence(channel, '*')).toEqual(channel);
  });

  test('capital first letters', () => {
    expect(capitalizeFirstLetters('foo was here! * ')).toEqual('Foo Was Here! *');
  });

  test('precision by type', () => {
    // Strings
    expect(determinePrecisionByType(99.919, ValueType.PERCENTAGE, true)).toEqual('99.92');
    expect(determinePrecisionByType(99.919, ValueType.INTEGER, true)).toEqual('100');
    expect(determinePrecisionByType(99.919, ValueType.FLOAT, true)).toEqual('99.92');
    expect(determinePrecisionByType(99.919, undefined, true)).toEqual('99.92');

    // Numbers
    expect(determinePrecisionByType(99.919, ValueType.PERCENTAGE, false)).toEqual(99.92);
    expect(determinePrecisionByType(99.919, ValueType.INTEGER, false)).toEqual(100);
    expect(determinePrecisionByType(99.919, ValueType.FLOAT, false)).toEqual(99.92);
    expect(determinePrecisionByType(99.919, undefined, false)).toEqual(99.92);

    // Handle undefined value and pattern
    expect(determinePrecisionByType(undefined, undefined, false)).toEqual(undefined);
  });
});
