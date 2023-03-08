/* eslint-disable @typescript-eslint/no-magic-numbers */
import type { MinAndMax } from '../../src/ts/common-util';
import { findMinAndMax, findXYMinAndMax } from '../../src/ts/common-util';

describe('min and max utils', () => {
  test('function exist', () => {
    expect(findMinAndMax).toBeDefined();
    expect(findXYMinAndMax).toBeDefined();
  });

  test('can min and max', () => {
    expect(findMinAndMax(undefined)).toMatchInlineSnapshot(`
      Object {
        "max": undefined,
        "min": undefined,
      }
    `);
    expect(findMinAndMax([])).toMatchInlineSnapshot(`
      Object {
        "max": undefined,
        "min": undefined,
      }
    `);
    expect(findMinAndMax([1, 2, 3, 4, 5])).toMatchInlineSnapshot(`
      Object {
        "max": 5,
        "min": 1,
      }
    `);
    expect(findMinAndMax([0.010231231, 0.9299922, 0.123223, 0.222314, 0.00000005]))
      .toMatchInlineSnapshot(`
      Object {
        "max": 0.9299922,
        "min": 5e-8,
      }
    `);
  });

  it('is finding min and max correctly, no empty', () => {
    const minMax: MinAndMax = findXYMinAndMax([], []);
    expect(minMax.xMin).toEqual(undefined);
    expect(minMax.xMax).toEqual(undefined);
    expect(minMax.yMin).toEqual(undefined);
    expect(minMax.yMax).toEqual(undefined);
  });

  it('is finding min and max correctly, ordered data', () => {
    const minMax: MinAndMax = findXYMinAndMax([0, 1, 2, 3, 4, 5, 6, 7], [0, 1, 2, 3, 4, 5, 6, 7]);

    expect(minMax.xMin).toEqual(0);
    expect(minMax.xMax).toEqual(7);
    expect(minMax.yMin).toEqual(0);
    expect(minMax.yMax).toEqual(7);
  });

  it('is finding min and max correctly, out of order data', () => {
    const minMax: MinAndMax = findXYMinAndMax([0.1, 1.9, 2, 3.6], [11, 200, 300, 40, 2, 7, 9, 32]);

    expect(minMax.xMin).toEqual(0.1);
    expect(minMax.xMax).toEqual(3.6);
    expect(minMax.yMin).toEqual(2);
    expect(minMax.yMax).toEqual(300);
  });
});
