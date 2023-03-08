import { recordLength } from '../../src/ts/common-util';

describe('record utils', () => {
  test('return correct number of entries in record', () => {
    expect(recordLength(undefined)).toEqual(0);
    expect(recordLength(null)).toEqual(0);
    const someRecord: Record<string, number> = {};
    expect(recordLength(someRecord)).toEqual(0);
    someRecord.foo = 34;
    expect(recordLength(someRecord)).toEqual(1);
  });
});
